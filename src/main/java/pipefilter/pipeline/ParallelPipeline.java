package pipefilter.pipeline;

import pipefilter.filter.Filter;
import pipefilter.filter.FilterFactory;
import pipefilter.filter.Parallelizer;
import pipefilter.filter.Serializer;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;
import pipefilter.pump.Pump;
import pipefilter.pump.PumpFactory;
import pipefilter.sink.Sink;
import pipefilter.sink.SinkFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static pipefilter.config.Configuration.NUMBER_OF_PARALLEL_INSTANCES;
import static pipefilter.config.Configuration.PIPE_CAPACITY;
import static pipefilter.config.Configuration.parallelizable;

/**
 * @author Nardos Tessema
 *
 * A parallel pipeline.
 *
 * New special filters Parallelizer and Serializer
 * parallelize and serialize the pipeline.
 *
 * The implementation is sort of convoluted but
 * works for demonstration purposes.
 *
 * Known issues:
 *
 * (1) The pipeline gets stuck for low pipe capacities.
 * (2) I am assuming parallelizable filters all use String Pipes.
 *     The dynamic type checking is sort of lost here...
 * (3)
 */
public class ParallelPipeline implements Pipeline {

    private String input;
    private Map<String, Integer> output;
    private final List<Runnable> pipelineComponents;
    private final CountDownLatch doneSignal;

    public ParallelPipeline(String input, Map<String, Integer> output, String[] pipeline) {
        this.input = input;
        this.output = output;
        this.pipelineComponents = new LinkedList<>();

        int countDown = pipeline.length;
        for(String component : pipeline) {
            if(parallelizable.containsKey(component) && parallelizable.get(component)) {
                countDown += NUMBER_OF_PARALLEL_INSTANCES + 1;
            }
        }
        this.doneSignal = new CountDownLatch(countDown);
        compose(input, output, pipeline);
    }

    @Override
    public void run() throws InterruptedException {
        System.out.println("---------------------------------------------------------------------------------------");
        System.out.printf("%1$-26s | %2$10s | %3$11s | %4$9s | %5$8s | %6$8s%n", "Component Class Name", "Blocked on", " Blocked on", " Response", "   Input", "  Output");
        System.out.printf("%1$-26s | %2$10s | %3$11s | %4$9s | %5$5s | %6$6s%n", "[Pump | Filter | Sink]", "Input (ms)", "Output (ms)", "Time (ms)", "   Count", "   Count");
        System.out.println("---------------------------------------------------------------------------------------");
        /*
         * Use ExecutorService instead of creating Threads explicitly.
         * We know exactly how many threads there will be in the pipeline,
         * so we can use a fixed thread pool.
         */
        final int nThreads = (int) doneSignal.getCount();
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        pipelineComponents.forEach(executor::execute);
        /*
         * Wait for all threads to be done before returning to the main thread.
         */
        doneSignal.await();
        /*
         * Shutdown the executor so that the program returns
         */
        executor.shutdown();
        /*
         * At this point, it is guaranteed that all the threads
         * (the pump, the filters, and the sink) have completed
         * their operations (threads have stopped), meaning that
         * the text processing has completed.
         *
         * This is guaranteed by the countdown latch.
         */
    }

    /**
     * Composes a parallel pipeline.
     *
     * A filter can be declared to be parallelizable in Configuration
     * by registering it in the parallelComponents map with its degree of parallelism.
     *
     * This pipeline composer checks the parallelComponents map and creates multiple
     * parallel instances of filters that are parallelized with degrees of parallelism > 1,
     *
     * I have introduced Parallelizer and Serializer filters to parallelize the pipeline.
     * Parallelizer spreads incoming stream into parallel pipes and parallel instances of
     * a filter Serializer collects back the parallel streams into a single pipe.
     *
     * Suppose that F0, F1, F2 are sequences of filters in a pipeline and that F2 is
     * parallelized with a degree of parallelism of 3.
     * This composer constructs the parallel pipeline as follows:
     * (1) inserts a Parallelizer after F0.
     * (2) creates 3 input pipes
     * (3) creates 3 instances of F1
     * (4) creates 3 output pipes
     * (5) inserts a Serializer before F2
     *
     * The picture belows shows the resulting assembly.
     *
     *                                 +--(pipe)-->[F1]--(pipe)--+
     *  [F0]--(pipe)-->[Parallelizer]--+--(pipe)-->[F1]--(pipe)--+-->[Serializer]--(pipe)-->[F2]
     *                                 +--(pipe)-->[F1]--(pipe)--+
     *
     * @param input input pipe
     * @param output output pipe
     * @param components array of pipeline components
     */
    private void compose(String input, Map<String, Integer> output, String[] components) {

        /*
         * Create pump and attach to pipeline
         */
        String name = components[0];
        String pipeDataType = PumpFactory.getPumpOutputType(name);
        /*
         * Look ahead and see if the next filter is parallelized.
         * If next filter is parallelized, get a bigger pipe capacity.
         */
        int capacity = getCapacity(0, components);
        Pipe<?> out = PipeFactory.build(pipeDataType, capacity);
        Pipe<?> in = out;
        Pump<?, ?> pump = PumpFactory.build(name, input, out, doneSignal);
        pipelineComponents.add(pump);

        /*
         * Create a chain of filters.
         * Output pipe of pump is input pipe of first filter.
         * Output pipe of first filter is input pipe of second filter, and so on...
         */
        for(int i = 1; i <= components.length - 2; i++) {
            name = components[i];
            pipeDataType = FilterFactory.getFilterOutputType(name);
            /*
             * Look ahead and see if the next filter is parallelized.
             * If next filter is parallelized, make this output pipe
             * bigger so that the parallelizer has more room...
             */
            capacity = getCapacity(i, components);
            out = PipeFactory.build(pipeDataType, capacity);

            /*
             * If filter is not parallelized, fit it into the pipeline in series.
             */
            if(!parallelizable.containsKey(name) || !parallelizable.get(name)) {
                Filter<?, ?> filter = FilterFactory.build(name, in, out, doneSignal);
                pipelineComponents.add(filter);
            } else {
                /*
                 * Get the degree of parallelism from Configuration.
                 */
                final int N = NUMBER_OF_PARALLEL_INSTANCES;//parallelComponents.get(name);
                /*
                 * Parallel instances of filter
                 */
                List<Filter<?,?>> parallelFilters = new ArrayList<>();
                /*
                 * A list of input pipes for each parallel filter instance
                 */
                List<Pipe<String>> inputs = new ArrayList<>();
                /*
                 * A list of output pipes for each parallel filter instance
                 */
                List<Pipe<String>> outputs = new ArrayList<>();
                /*
                 * Parallelizer spreads the stream out into N parallel streams
                 */
                Parallelizer parallelizer = new Parallelizer((Pipe<String>) in, inputs, doneSignal);
                /*
                 * Create N input pipes, N output pipes, Parallelizer, Serializer
                 */
                for(int j = 0; j < N; j++) {
                    inputs.add(j, (Pipe<String>) PipeFactory.build("java.lang.String", PIPE_CAPACITY));
                    outputs.add(j, (Pipe<String>) PipeFactory.build("java.lang.String", PIPE_CAPACITY));
                    parallelFilters.add(FilterFactory.build(name, inputs.get(j), outputs.get(j), doneSignal));
                }
                /*
                 * Serializer collects the N parallel streams into one stream
                 */
                Serializer serializer = new Serializer(outputs, (Pipe<String>) out, doneSignal);
                pipelineComponents.add(parallelizer);
                parallelFilters.forEach(filter -> pipelineComponents.add(filter));
                pipelineComponents.add(serializer);
            }
            /*
             * progress to the next component in the chain
             */
            in = out;
        }

        /*
         * Create sink and attach to pipeline
         */
        name = components[components.length - 1];
        Sink<?, ?> sink = SinkFactory.build(name, in, output, doneSignal);
        pipelineComponents.add(sink);
    }

    /**
     * Looks ahead in the array of components and returns a bigger
     * pipe capacity value if the current of the next filter is parallelized.
     *
     * The idea is that whenever a Parallelizer/Serializer is inserted,
     * its input/output pipe should be bigger than the other pipes in the
     * pipeline as the data volume is likely to be high at those points.
     *
     * @param indexOfCurrentComponent index of current component
     * @param components the array of pipeline components
     * @return pipe capacity
     */
    private int getCapacity(int indexOfCurrentComponent, String[] components) {
        final String thisComponent = components[indexOfCurrentComponent];
        final String nextComponent = indexOfCurrentComponent <= components.length - 2 ? components[indexOfCurrentComponent + 1] : "";
        int capacity = PIPE_CAPACITY;
        if(
                (parallelizable.containsKey(nextComponent) && parallelizable.get(nextComponent)) ||
                (parallelizable.containsKey(thisComponent) && parallelizable.get(thisComponent))
        ) {
            capacity = NUMBER_OF_PARALLEL_INSTANCES > 1 ? 16 * PIPE_CAPACITY : PIPE_CAPACITY;
        }
        return capacity;
    }
}
