package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * @author Nardos Tessema
 *
 * A filter that removes non-word characters.
 *
 * A word character is defined as [a-zA-Z_0-9] (in Java, '\w').
 *
 * The responsibility of this filter is too tiny that it doesn't
 * deserve to be a filter by itself. In the second part of the project,
 * I inted to merge this and other small tasks together.
 * I made it a filter here just to demo the chaining of several filters.
 */
public class NonAlphaNumericWordRemover implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    private static final String WORD_PATTERN = "\\w+";

    public NonAlphaNumericWordRemover(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
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
                /*
                 * Discard if word does not match word pattern
                 */
                if(word.matches(WORD_PATTERN)) {
                    output.put(word);
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        long elapsedTime = System.currentTimeMillis() - start;
        // System.out.printf("%1$-30s%2$9d%n", "non-alphanumeric-word-remover", elapsedTime);
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
    }
}
