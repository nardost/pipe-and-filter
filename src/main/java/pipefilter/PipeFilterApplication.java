package pipefilter;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipeline.Pipeline;
import pipefilter.pipeline.PipelineFactory;
import pipefilter.pipeline.TermFrequencyPipeline;

import java.util.HashMap;
import java.util.Map;

public class PipeFilterApplication {

    public static void main(String[] args) throws InterruptedException {

        try {
            final String text = parseCommandLineArguments(args);
            final Map<String, Integer> terms = new HashMap<>();
            final String[] assembly = new String[]{
                    "text-streamer", "tokenizer", "frequency-counter"
            };
            String pipelineType = "first";

            final Pipeline pipeline = PipelineFactory.build(text, terms, assembly, pipelineType);

            pipeline.run();

            terms.forEach((k, v) -> System.out.printf("%s: %d%n", k, v));
        } catch (PipeFilterException pfe) {
            System.out.println(pfe.getMessage());
        }
    }

    /**
     * Get the file to be processed
     * @param args command line arguments list
     */
    private static String parseCommandLineArguments(String[] args) {
        if(args.length == 1) {
            return args[0];
        }
        throw new PipeFilterException("Provide file path.");
    }
}
