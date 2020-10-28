package pipefilter.sink;

import pipefilter.Utilities;
import pipefilter.filter.TermFrequency;
import pipefilter.pipe.Pipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Nardos Tessema
 *
 * A Sink that inverts a Term-Frequency mapping into a
 * Frequency-Term mapping of terms & their frequencies.
 *
 *      TERM | f
 *   ------------                 f | TERMS (not sorted)
 *     "boy" | 2                 --------------------------
 *    "girl" | 3                  1 | "woman", "cat", "dog"
 *     "man" | 3     =======>     2 | "boy"
 *   "woman" | 1                  3 | "girl", "man"
 *     "cat" | 1
 *     "dog" | 1
 *
 * Input: a stream of TermFrequency objects
 * Output: a map that maps frequencies to a list of terms
 *         key:   frequency of terms
 *         value: list of terms with the key as their common frequency.
 *
 * If the underlying map of output is a HashMap, the keys of the output map
 * are sorted in ascending order without any further sorting operation.
 * Reason: terms are counted as they occur in the pipe. So, the first time a
 * term appears, its frequency is 1, and it belongs in the map under key = 1.
 * Next time it appears in the pipe, its frequency is 2 (key = 2). Then 3,
 * (key = 3) and so on => the keys of the map are incrementally created...
 *
 * If the underlying map of the output is a TreeMap, the keys will be sorted in
 * natural/reverse order depending on the comparator used to instantiate the TreeMap.
 *
 * Since the keys are needed to be sorted in descending order for this application,
 * TreeMap with a reverse order comparator is the best choice as no further sorting
 * operations are needed.
 */
public class FrequencyTermInverter implements Sink<TermFrequency, Map<Integer, List<String>>> {

    private final Pipe<TermFrequency> input;
    private final Map<Integer, List<String>> output;
    private final CountDownLatch doneSignal;

    public FrequencyTermInverter(Pipe<TermFrequency> input, Map<Integer, List<String>> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void drain() {
        long start = System.currentTimeMillis();
        while(true) {
            try {
                final TermFrequency tf = input.take();
                /*
                 * If input is sentinel value, be done.
                 */
                if(tf.isSentinelValue()) {
                    break;
                }
                /*
                 * If frequency is a new high, get a new list with the term
                 * as its only element. Otherwise, insert the term in the
                 * list with frequency as the key.
                 */
                if(!output.containsKey(tf.frequency)) {
                    output.put(tf.frequency, new ArrayList<>(Collections.singletonList(tf.term)));
                } else {
                    output.get(tf.frequency).add(tf.term);
                }
                /*
                 * If this is not the first occurrence of the term (frequency > 1),
                 * the term is also in the previous list (term is inserted in two lists).
                 * Get the previous list and remove term so that term lives only
                 * in its highest-frequency-so-far list.
                 */
                if(tf.frequency > 1) {
                    output.get(tf.frequency - 1).remove(tf.term);
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        long elapsedTime = System.currentTimeMillis() - start;
        // System.out.printf("%1$-30s%2$9d%n", "frequency-term-inverter-sink", elapsedTime);
    }

    @Override
    public void run() {
        drain();
        /*
         * This is the last component in the pipeline,
         * and it is done with its draining operations.
         *
         * ==> the text processing has completed
         * ==> the output is complete and can be consumed
         */

        // Show the ten most frequently occurring terms
        final int N_MOST_COMMON = 10;
        System.out.printf("%nThe %d most commonly occurring terms:%n%n", N_MOST_COMMON);

        // If TreeMap with reverse order comparator is used for output
        output.keySet()
                .stream()
                .filter(k -> !output.get(k).isEmpty())
                .limit(N_MOST_COMMON)
                .forEach(k -> System.out.printf("%1$6d -> %2$s%n", k, Utilities.prettyPrint(output.get(k))));

        // If HashMap is used for the output
        // Map<Integer, List<String>> mostCommon = Utilities.mostCommonTerms(output, N_MOST_COMMON);
        // System.out.println(Utilities.prettyPrintMap(mostCommon));

        // To discard entries with empty lists
        // Map<Integer, List<String>> trimmedAndSorted = Utilities.trim(output);

        // To display entire output map
        // System.out.println(Utilities.prettyPrintMap(frequencies));

        // notify awaiting thread that draining has completed
        doneSignal.countDown();
    }
}
