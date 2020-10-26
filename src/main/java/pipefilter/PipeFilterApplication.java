package pipefilter;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipeline.Pipeline;
import pipefilter.pipeline.PipelineFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PipeFilterApplication {

    /**
     * The output of the frequency-term-inverter sink.
     * TODO: Choice between HashMap and TreeMap
     */
    private static final Map<Integer, List<String>> frequencies = new TreeMap<>(Collections.reverseOrder());
    /**
     * The output of the frequency-counter sink.
     */
    private static final  Map<String, Integer> terms = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {

        try {
            final String text = parseCommandLineArguments(args);
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

            /*
             * Construct the pipeline. There is only
             * one type of pipeline for now - first.
             */
            final String pipelineType = "first";
            final Pipeline pipeline = PipelineFactory.build(text, frequencies, assembly, pipelineType);
            /*
             * Run the pipeline
             */
            pipeline.run();
            /*
             * At this point, all the threads have finished their jobs.
             *  - guaranteed by the Count Down Latch.
             */

            final int N_MOST_COMMON = 10;
            Map<Integer, List<String>> mostCommon = Utilities.mostCommonTerms(frequencies, N_MOST_COMMON);
            Map<Integer, List<String>> trimmedAndSorted = Utilities.trim(frequencies);
            //System.out.println(Utilities.prettyPrintMap(frequencies));
            System.out.println(Utilities.prettyPrintMap(mostCommon));
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
