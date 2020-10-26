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

/**
 * The Pipeline class abstracts the entirety of the
 * Pump, Filter, Sink, Pipe arrangement as a list of
 * Threads. The filters, pumps and sinks are active
 * elements and implement the Runnable interface.
 *
 * It is the single point where the pipeline operation
 * could be triggered.
 */
public class SerialPipeline implements Pipeline {

    private final String input;
    private final Map<String, Integer> output;
    private final List<Thread> pipelineComponents;
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
        pipelineComponents.forEach(Thread::start);
        /*
         * Wait for all threads to be done before returning to the main thread.
         */
        doneSignal.await();
    }

    /**
     * Given an array of names of pipeline components, this method assembles
     * a pipeline using the Java reflection API. Each Pump, Sink, or Filter
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

        Pipe<?> out = PipeFactory.build(pipeDataType);
        Pipe<?> in = out;
        Pump<?, ?> pump = PumpFactory.build(name, input, out, doneSignal);
        pipelineComponents.add(new Thread(pump));

        /*
         * Create a chain of filters.
         * Output pipe of pump is input pipe of first filter.
         * Output pipe of first filter is input pipe of second filter, and so on...
         */
        for(int i = 1; i <= components.length - 2; i++) {
            name = components[i];
            pipeDataType = FilterFactory.getFilterOutputType(name);
            out = PipeFactory.build(pipeDataType);
            Filter<?, ?> filter = FilterFactory.build(name, in, out, doneSignal);
            pipelineComponents.add(new Thread(filter));
            in = out;
        }
        /*
         * Create sink and attach to pipeline
         */
        name = components[components.length - 1];
        Sink<?, ?> sink = SinkFactory.build(name, in, output, doneSignal);
        pipelineComponents.add(new Thread(sink));
    }
}
