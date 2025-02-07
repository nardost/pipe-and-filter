package pipefilter.pipeline;

import pipefilter.filter.Filter;
import pipefilter.filter.FilterFactory;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;
import pipefilter.pump.Pump;
import pipefilter.pump.PumpFactory;
import pipefilter.sink.Sink;
import pipefilter.sink.SinkFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static pipefilter.config.Configuration.PIPE_CAPACITY;

/**
 * @author Nardos Tessema
 *
 * The Pipeline class abstracts the entirety of the
 * Pump, Filter, Sink, Pipe arrangement as a list of
 * Threads. The filters, pumps and sinks are active
 * elements and implement the Runnable interface.
 *
 * It is the single point where the pipeline operation
 * could be triggered.
 */
public class SerialPipeline implements Pipeline {

    private String input;
    private Map<String, Integer> output;
    private final List<Runnable> pipelineComponents;
    private final CountDownLatch doneSignal;

    public SerialPipeline(String input, Map<String, Integer> output, String[] pipeline) {

        this.input = input;
        this.output = output;
        this.pipelineComponents = new LinkedList<>();
        this.doneSignal = new CountDownLatch(pipeline.length);

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
     * Given an array of names of pipeline components, this method assembles
     * a pipeline using the Java Reflection API. Each Pump, Sink, or Filter
     * has a unique name assigned to it in the registry.
     *
     * @see pipefilter.config.Registry
     *
     * The method uses the registry to determine the class type of the component
     * and then uses the Java reflection API to:
     * (1) determine the specific types of pipes that will fit the components.
     * (2) determine which Pump, Filter, Sink to instantiate.
     * It assembles the components as a list of Threads (the Pumps, Filters,
     * Sinks are all ACTIVE and, therefore, Runnable).
     *
     * @param input The input of the pipeline.
     * @param output The output of the pipeline.
     * @param components The pipeline components in order.
     */
    private void compose(String input, Map<String, Integer> output, String[] components) {

        /*
         * Create pump and attach to pipeline
         */
        String name = components[0];
        String pipeDataType = PumpFactory.getPumpOutputType(name);

        Pipe<?> out = PipeFactory.build(pipeDataType, PIPE_CAPACITY);
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
            out = PipeFactory.build(pipeDataType, PIPE_CAPACITY);
            Filter<?, ?> filter = FilterFactory.build(name, in, out, doneSignal);
            pipelineComponents.add(filter);
            in = out;
        }
        /*
         * Create sink and attach to pipeline
         */
        name = components[components.length - 1];
        Sink<?, ?> sink = SinkFactory.build(name, in, output, doneSignal);
        pipelineComponents.add(sink);
    }
}
