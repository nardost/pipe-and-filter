package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * @author Nardos Tessema
 *
 * A filter that removes numeric only words.
 *
 * Removes words that are composed purely of numeric characters.
 * It does not remove words with a mixture of alphabetic and numeric characters.
 *
 * Removes: 1, 480, 001
 * Does not remove: SE480, God101, P2P
 */
public class NumericOnlyWordRemover implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    private final String WORD_PATTERN = "\\d+";

    public NumericOnlyWordRemover(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
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
                if(word.equals(SENTINEL_VALUE)) {
                    output.put(SENTINEL_VALUE);
                    break;
                }
                if(!word.matches(WORD_PATTERN)) {
                    output.put(word);
                }
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        long elapsedTime = System.currentTimeMillis() - start;
        // System.out.printf("%1$-30s%2$9d%n", "numeric-only-word-remover", elapsedTime);
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
    }
}
