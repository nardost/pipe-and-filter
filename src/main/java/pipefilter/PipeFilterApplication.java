package pipefilter;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipeline.Pipeline;
import pipefilter.pipeline.PipelineFactory;
import pipefilter.pipeline.TermFrequencyPipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipeFilterApplication {

    public static void main(String[] args) throws InterruptedException {

        try {
            final String text = parseCommandLineArguments(args);
            final Map<String, Integer> terms = new HashMap<>();
            final Map<Integer, List<String>> frequencies = new HashMap<>();
            final String[] assembly = new String[] {
                    "text-streamer",
                    "tokenizer",
                    "non-word-char-cleaner",
                    "to-lower-case-transformer",
                    "stop-word-remover",
                    "porter-stemmer",
                    "term-frequency-counter",
                    "frequency-term-inverter"

            };
            String pipelineType = "first";

            final Pipeline pipeline = PipelineFactory.build(text, frequencies, assembly, pipelineType);

            pipeline.run();

            //terms.forEach((k, v) -> System.out.printf("%s: %d%n", k, v));

            StringBuilder sb = new StringBuilder();
            frequencies.forEach((k, v) -> {
                sb.append(k).append(" -> ");
                v.forEach(t -> sb.append(t).append(" "));
                sb.append("\n");
            });
            System.out.println(sb.toString());

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
