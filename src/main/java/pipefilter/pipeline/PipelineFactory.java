package pipefilter.pipeline;

import pipefilter.exception.PipeFilterException;
import pipefilter.filter.FilterFactory;
import pipefilter.pump.PumpFactory;
import pipefilter.sink.SinkFactory;

import java.util.Map;

import static pipefilter.config.Registry.*;

/**
 * @author Nardos Tessema
 *
 * A factory that creates Pipeline objects.
 *
 * The creation logic of each pipeline type is hard-coded.
 * i.e. this is not a dynamic factory like FilterFactory, PumpFactory, SinkFactory.
 */
public class PipelineFactory {

    /**
     * @param input the input to the pipeline
     * @param output the output of the pipeline
     * @param pipeline the ordered list of components that make up the pipeline
     * @param pipelineType they type of pipeline (serial, parallel, etc.)
     * @param <T> the input type
     * @param <U> the output type
     * @return a Pipeline object
     */
    @SuppressWarnings("unchecked")
    public static <T, U> Pipeline build(T input, U output, String[] pipeline, String pipelineType) {

        /*
         * (1) check if the user supplied compatible sequence of components
         * (2) check if the components are registered in the Registry
         */
        validate(pipeline);

        /*
         * We know the exact types of input and output.
         * So, the casting is safe. i.e. @SuppressWarnings
         */
        if(pipelineType.equalsIgnoreCase("serial")) {
            // TODO: check if input & output types are correct before building the pipeline.
            return new SerialPipeline((String) input, (Map<String, Integer>) output, pipeline);
        }
        throw new PipeFilterException("Unknown pipeline assembly: " + pipelineType);
    }

    /**
     * Check if the given pipeline assembly is valid by comparing
     * the output and the input types of adjacent components.
     *
     * If the output type of the pump is different from the input
     * type of the first filter, the method throws an exception, and so on...
     *
     * @param pipeline the pipeline component lineup
     */
    private static void validate(String[] pipeline) {
        componentsAreRegistered(pipeline);
        pipeTypesMatch(pipeline);
    }

    /**
     * Check if the named components are in the registry.
     *
     * Throws an exception if any of the names is not
     * registered in the registry.
     *
     * @param pipeline the pipeline assembly array
     */
    private static void componentsAreRegistered(String[] pipeline) {

        if(!registeredPumps.containsKey(pipeline[0])) {
            throw new PipeFilterException("Pump not in the registry: " + pipeline[0]);
        }

        for(int i = 1; i < pipeline.length - 1; i++) {
            if(!registeredFilters.containsKey(pipeline[i])) {
                throw new PipeFilterException("Filter not in the registry: " + pipeline[i]);
            }
        }

        if(!registeredSinks.containsKey(pipeline[pipeline.length - 1])) {
            throw new PipeFilterException("Sink not in the registry: " + pipeline[pipeline.length - 1]);
        }
    }

    /**
     * Check if the output pipe type of a component matches with
     * the input pipe type of the next component in the pipeline chain.
     *
     * Throws an exception if output/input pipe types of adjacent
     * pipeline component do not match.
     *
     * @param pipeline the pipeline assembly (array of component ids).
     */
    private static void pipeTypesMatch(String[] pipeline) {
        String out = PumpFactory.getPumpOutputType(pipeline[0]);
        String in;
        for(int i = 1; i < pipeline.length - 1; i++) {
            in = FilterFactory.getFilterInputType(pipeline[i]);
            if(!out.equals(in)) {
                throw new PipeFilterException("Pipe mismatch: " + pipeline[i - 1] + " <> " + pipeline[1]);
            }
            out = FilterFactory.getFilterOutputType(pipeline[i]);
        }
        in = SinkFactory.getSinkInputType(pipeline[pipeline.length - 1]);
        if(!out.equals(in)) {
            throw new PipeFilterException("Pipe mismatch: " + pipeline[pipeline.length - 2] + " <> " + pipeline[pipeline.length - 1]);
        }
    }
}
