package pipefilter;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipeline.Pipeline;
import pipefilter.pipeline.PipelineFactory;

import static pipefilter.config.Configuration.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
     * absolute path of the input file
     */
    private static String inputFile;
    /**
     * serial | parallel
     */
    private static String pipelineType = "serial";

    public static void main(String[] args) throws InterruptedException {

        try {
            // Extract filename and options
            parseProgramOptions(args);

            // The components that make up the pipeline (in that order)
            final String[] assembly = new String[] {
                    "text-streamer",
                    "tokenizer",
                    "text-preprocessor",
                    "stop-word-remover",
                    "en-porter-stemmer",
                    "term-frequency-counter",
                    "frequency-term-inverter"
            };

            // Construct the pipeline.
            final Pipeline pipeline = PipelineFactory.build(inputFile, frequencies, assembly, pipelineType);

            // Start the pipeline (with timing instrumentation code)
            long start = System.currentTimeMillis();
            pipeline.run();
            long responseTime = System.currentTimeMillis() - start;

            /*
             * Artificial time delay so that other threads
             * finish before printing the response time.
             */
            TimeUnit.MILLISECONDS.sleep(2000L);
            final String message = "Response time of processing";
            final int n = message.length() + inputFile.length() + Long.toString(responseTime).length() + 7;
            line(n);
            System.out.printf("%s %s = %d ms%n", message, inputFile, responseTime);
            line(n);
        } catch (PipeFilterException pfe) {
            System.out.println(pfe.getMessage());
        }
    }

    private static void line(int n) {
        StringBuilder sb = new StringBuilder();
        System.out.println(sb.append("=".repeat(Math.max(0, n))));
    }

    /**
     * Parses program arguments and options.
     *
     * 1st arg: file name
     * rest of arguments should be in the form of key=value.
     *
     * The keys recognized are:
     *    capacity: pipe capacity (int)
     *    type: pipe type (serial | parallel)
     *    streams: number of parallel instances for parallelized filters (int)
     *
     * Example:
     *    java -jar executable.jar filename capacity=100 type=serial streams=4
     *
     * @param args program arguments
     */
    private static void parseProgramOptions(String[] args) {
        if(args.length == 0) {
            throw new PipeFilterException("Input file not provided.");
        }
        inputFile = args[0];
        if(args.length == 1) {
            return;
        }
        /*
         * if args.length > 1 then it must be 3 or 5 or 7
         * filename  capacity 100     type   serial     streams     3
         * --------  -------- ---     ----   ------     -------     -
         *    0         1      2        3      4           5        6
         */
        if(args.length != 3 && args.length != 5 & args.length != 7) {
            throw new PipeFilterException("Invalid program options.");
        }

        Map<String, String> options = new HashMap<>();
        for(int i = 1; i < args.length; i += 2) {
            options.put(args[i], args[i + 1]);
        }
        options.keySet().forEach(key -> {
            final String value = options.get(key);
            if(key.equalsIgnoreCase("type")) {
                /*
                 * valid values are serial and parallel
                 */
                if(!value.equalsIgnoreCase("serial") && !value.equalsIgnoreCase("parallel")) {
                    throw new PipeFilterException("Invalid program option: " + key + "=" + value);
                }
                pipelineType = value;
            } else if(
                    key.equalsIgnoreCase("capacity") ||
                    key.equalsIgnoreCase("streams")) {
                /*
                 * valid values are positive integers
                 */
                try {
                   final int number = Integer.parseInt(value);
                   if(number < 1) {
                       throw new IllegalArgumentException();
                   }
                   if(key.equalsIgnoreCase("capacity")) {
                       PIPE_CAPACITY = number;
                   } else {
                       NUMBER_OF_PARALLEL_INSTANCES = number;
                   }
                } catch (IllegalArgumentException iae) {
                    throw new PipeFilterException("Invalid program option: " + key + "=" + value);
                }
            } else {
                /*
                 * valid values are positive integers
                 */
                throw new PipeFilterException("Invalid program option: " + key + "=" + value);
            }
        });
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
                    PIPE_CAPACITY = pipeCapacity;
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        throw new PipeFilterException("1st arg: valid absolute path to input file\n2nd arg: positive integer");
    }
}
