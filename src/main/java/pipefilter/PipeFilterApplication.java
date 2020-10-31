package pipefilter;

import pipefilter.config.Configuration;
import pipefilter.exception.PipeFilterException;
import pipefilter.pipeline.Pipeline;
import pipefilter.pipeline.PipelineFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Nardos Tessema
 *
 * A Pipe & Filter Architecture
 *
 * The application is a text processing pipeline.
 *
 * The user of this application can compose a pipeline
 * using the available pumps, filters, and sinks.
 */
public class PipeFilterApplication {

    /**
     * The output of the frequency-term-inverter sink.
     *
     * The choice between HashMap & TreeMap:
     *
     * HashMap: keys are sorted in ascending order
     * TreeMap (with reverse order comparator): keys are sorted in reverse order.
     *
     * Since listing the N most commonly occurring terms is the goal,
     * a TreeMap that sorts keys in reverse order should be our choice.
     */
    private static final Map<Integer, List<String>> frequencies =  new TreeMap<>(Collections.reverseOrder());

    /**
     * The output of the frequency-counter sink (not used).
     */
    private static final  Map<String, Integer> terms = new TreeMap<>();

    private static String inputFile;

    public static void main(String[] args) throws InterruptedException {

        try {
            // Extract program arguments
            parseCommandLineArguments(args);

            // The components that make up the pipeline (in that order)
            final String[] assembly = new String[] {
                    "text-streamer",
                    "tokenizer",
                    "text-preprocessor",
                    // "non-alphanumeric-word-remover",
                    // "numeric-only-word-remover",
                    // "to-lower-case-transformer",
                    "stop-word-remover",
                    "en-porter-stemmer",
                    "term-frequency-counter",
                    "frequency-term-inverter"
            };

            // What type of pipeline? (Only serial available)
            final String pipelineType = "parallel";

            // Construct the pipeline.
            final Pipeline pipeline = PipelineFactory.build(inputFile, frequencies, assembly, pipelineType);

            // Start the pipeline (with timing instrumentation code)
            long start = System.currentTimeMillis();
            pipeline.run();
            long responseTime = System.currentTimeMillis() - start;
            TimeUnit.MILLISECONDS.sleep(2000L);
            final String message = "Response Time for";
            final int N = message.length() + inputFile.length() + 4;
            System.out.printf("%1$" + N + "s%2$s%3$10s%n", "", "┌", "┐");
            System.out.printf("%s <%s>: %d ms%n", message, inputFile, responseTime);
            System.out.printf("%1$" + N + "s%2$s%3$10s%n", "", "└", "┘");
        } catch (PipeFilterException pfe) {
            System.out.println(pfe.getMessage());
        }
    }

    /**
     * Extract command line program arguments.
     *
     * 1st arg: The input file name (absolute path).
     * 2nd arg: The pipe capacity. Optional. Default value used if not supplied.
     *
     * @param args command line arguments list
     */
    private static void parseCommandLineArguments(String[] args) {
        if(args.length == 1) {
            inputFile = args[0];
            return;
        }
        if(args.length > 1) {
            inputFile = args[0];
            try {
                final int pipeCapacity = Integer.parseInt(args[1]);
                if(pipeCapacity > 0) {
                    Configuration.PIPE_CAPACITY = pipeCapacity;
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        throw new PipeFilterException("1st arg: valid absolute path to input file\n2nd arg: positive integer");
    }
}
