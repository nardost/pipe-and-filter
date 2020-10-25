package pipefilter.pipeline;

import pipefilter.filter.FilterFactory;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;
import pipefilter.pump.PumpFactory;
import pipefilter.sink.SinkFactory;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Registry.*;

/**
 * The Pipeline class abstracts the entirety of the
 * Pump, Filter, Sink, Pipe arrangement as a list of
 * Threads. The filters, pumps and sinks are active
 * elements and implement the Runnable interface.
 *
 * It is the single point where the pipeline operation
 * could be triggered.
 */
public class TermFrequencyPipeline implements Pipeline {

    private final String input;
    private final Map<String, Integer> output;
    private final List<Thread> pipelineComponents;
    private final CountDownLatch doneSignal;

    public TermFrequencyPipeline(String input, Map<String, Integer> output, String[] pipeline) {

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
         * Create the source and attach to pipeline
         */
        String name = components[0];
        String pipeDataType = inferPumpOutputType(name);

        Pipe<?> out = PipeFactory.build(pipeDataType);
        Pipe<?> in = out;

        pipelineComponents.add(new Thread(PumpFactory.build(name, input, out, doneSignal)));

        /*
         * Create the filters and attach to pipeline
         */
        for(int i = 1; i <= components.length - 2; i++) {
            name = components[i];
            pipeDataType = inferFilterOutputType(name);
            out = PipeFactory.build(pipeDataType);
            pipelineComponents.add(new Thread(FilterFactory.build(name, in, out, doneSignal)));
            in = out;
        }
        /*
         * Create the sink and attach to pipeline
         */
        name = components[components.length - 1];
        pipelineComponents.add(new Thread(SinkFactory.build(name, in, output, doneSignal)));
    }

    /**
     * This method infers the output type of a filter by reflection.
     *
     * Example: Suppose there is a Filter named "some-filter" and
     *          defined as:
     *
     *   public class SomeFilter implements Filter<String, IntStream>
     *
     * inferFilterOutput("some-filter") will return "java.util.stream.IntStream"
     *
     * @param name the name of the filter in the registry
     * @return the output type fo the filter
     */
    private String inferFilterOutputType(String name) {
        ParameterizedType t = (ParameterizedType) registeredFilters.get(name).getGenericInterfaces()[0];
        return t.getActualTypeArguments()[1].getTypeName();
    }
    /**
     * This method infers the output type of a pump by reflection.
     *
     * @param name the name of the pump in the registry
     * @return the output type of the pump
     */
    private String inferPumpOutputType(String name) {
        ParameterizedType t = (ParameterizedType) registeredPumps.get(name).getGenericInterfaces()[0];
        return t.getActualTypeArguments()[1].getTypeName();
    }
}
