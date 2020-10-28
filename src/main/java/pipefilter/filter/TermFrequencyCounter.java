package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * @author Nardos Tessema
 *
 * A filter that counts the number of occurence of terms.
 */
public class TermFrequencyCounter implements Filter<String, TermFrequency> {

    private final Pipe<String> input;
    private final Pipe<TermFrequency> output;
    private final CountDownLatch doneSignal;

    /**
     * A map of terms that have already occurred and been counted.
     * The key is the term and the value is the count.
     */
    private final Map<String, Integer> countedTerms = new HashMap<>();

    public TermFrequencyCounter(Pipe<String> input, Pipe<TermFrequency> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        long start = System.currentTimeMillis();
        while(true) {
            try {
                final String word = input.take();
                TermFrequency tf = new TermFrequency();
                /*
                 * If input is the sentinel value, construct a
                 * TermFrequency object with term = SENTINEL_VALUE
                 * and put it in the output to signal the end of stream.
                 */
                if(word.equals(SENTINEL_VALUE)) {
                    tf.term = SENTINEL_VALUE;
                    output.put(tf);
                    break;
                }
                /*
                 * - If term is new, set frequency to 1.
                 * - If term has occurred before, increment frequency by 1.
                 * - Put the term into the map of already encountered terms.
                 *   Note: putting an entry in a map:
                 *         * creates a new entry if the key does not exist.
                 *         * updates the value if the key exists in the map.
                 */
                final int frequency = countedTerms.getOrDefault(word, 0);
                tf.term = word;
                tf.frequency = 1 + frequency;
                countedTerms.put(tf.term, tf.frequency);
                output.put(tf);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        long elapsedTime = System.currentTimeMillis() - start;
        // System.out.printf("%1$-30s%2$9d%n", "term-frequency-counter", elapsedTime);
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
    }
}
