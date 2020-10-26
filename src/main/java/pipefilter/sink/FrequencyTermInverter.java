package pipefilter.sink;

import pipefilter.filter.TermFrequency;
import pipefilter.pipe.Pipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Nardos Tessema
 *
 * A sink that effectively inverts a Term-Frequency mapping
 * into a Frequency-Term mapping of terms & their frequencies.
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
 * are naturally sorted in ascending order without any explicit sorting operation.
 * Reason: terms are counted as they occur in the pipe. So, the first time a
 * term appears, its frequency is 1, and it belongs in the map under key = 1.
 * Next time it appears in the pipe, its frequency is 2 (key = 2). Then 3,
 * (key = 3) and so on => the keys of the map are incrementally created...
 *
 * If the underlying map of output is a TreeMap, the keys will be sorted in
 * natural/reverse order depending on the comparator used to instantiate the TreeMap.
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
    }

    @Override
    public void run() {
        drain();
        doneSignal.countDown();
    }
}
