package pipefilter.pipeline;

import pipefilter.exception.PipeFilterException;
import pipefilter.filter.FilterFactory;
import pipefilter.pump.PumpFactory;
import pipefilter.sink.SinkFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static pipefilter.config.Registry.*;

public class PipelineFactory {

    @SuppressWarnings("unchecked")
    public static <T, U> Pipeline build(T input, U output, String[] pipeline, String pipelineType) {

        validate(pipeline);

        /*
         * We are hard-coding the creation logic for each pipeline type, and we
         * know the exact types of input and output. So, the casting is safe.
         * i.e. @SuppressWarnings
         */
        if(pipelineType.equalsIgnoreCase("first")) {
            // TODO: check if input & output types are correct before building the pipeline.
            return new TermFrequencyPipeline((String) input, (Map<String, Integer>) output, pipeline);
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

    private static void pipeTypesMatch(String[] pipeline) {
        String out = PumpFactory.inferPumpOutputType(pipeline[0]);
        String in;
        for(int i = 1; i < pipeline.length - 1; i++) {
            in = FilterFactory.inferFilterInputType(pipeline[i]);
            if(!out.equals(in)) {
                throw new PipeFilterException("Pipe mismatch: " + pipeline[i - 1] + " <> " + pipeline[1]);
            }
            out = FilterFactory.inferFilterOutputType(pipeline[i]);
        }
        in = SinkFactory.inferSinkInputType(pipeline[pipeline.length - 1]);
        if(!out.equals(in)) {
            throw new PipeFilterException("Pipe mismatch: " + pipeline[pipeline.length - 2] + " <> " + pipeline[pipeline.length - 1]);
        }
    }
}
