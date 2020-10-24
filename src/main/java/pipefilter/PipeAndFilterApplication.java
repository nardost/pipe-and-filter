package pipefilter;

import pipefilter.pipeline.Pipeline;
import pipefilter.pipeline.TermFrequencyPipeline;

import java.util.HashMap;
import java.util.Map;

public class PipeAndFilterApplication {

    public static void main(String[] args) throws InterruptedException {

        final String text = parseCommandLineArguments(args);
        final Map<String, Integer> terms = new HashMap<>();

        final Pipeline pipeline = new TermFrequencyPipeline(text, terms, new String[] {
                "tokenizer"
        });

        pipeline.run();

        terms.forEach((k, v) -> System.out.printf("%s: %d%n", k, v));
}

    /**
     * Get the file to be processed
     * @param args command line arguments list
     */
    private static String parseCommandLineArguments(String[] args) {
        if(args.length == 1) {
            return args[0];
        }
        throw new RuntimeException("Provide file path.");
    }
}
