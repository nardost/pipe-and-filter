package pipefilter;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipeline.Pipeline;
import pipefilter.pipeline.PipelineFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Nardos Tessema
 *
 * A Pipe & Filter Architecture
 *
 * The application is a text processing pipeline.
 *
 * The user of this application can compose a pipeline
 * using the available pumps, filters, and sinks.
 *
 * It was designed in such a way that enables the user
 * to choose which components to use in a pipeline.
 */
public class PipeFilterApplication {

    /**
     * The output of the frequency-term-inverter sink.
     * TODO: Choose between HashMap & TreeMap.
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
                    "non-alphanumeric-word-remover",
                    "numeric-only-word-remover",
                    "to-lower-case-transformer",
                    "stop-word-remover",
                    "en-porter-stemmer",
                    "term-frequency-counter",
                    "frequency-term-inverter"
            };

            /*
             * Construct the pipeline. There is only
             * one type of pipeline for now - first.
             */
            final String pipelineType = "serial";
            final Pipeline pipeline = PipelineFactory.build(text, frequencies, assembly, pipelineType);
            /*
             * Run the pipeline
             */
            long start = System.currentTimeMillis();
            pipeline.run();
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.printf("Time taken to process %s: %d ms%n", text, elapsedTime);
            /*
             * At this point, all the threads have finished their jobs.
             *  - guaranteed by the CountDownLatch signal.
             */

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
