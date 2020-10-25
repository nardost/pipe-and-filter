package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * @author Nardos Tessema
 *
 * A tokenizer filter.
 *
 * This filter splits a line of text into an array of words.
 * The splitting is done by the word boundary character '\b'.
 *
 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html">Java RegEx<a/>
 * @see <a href="https://www.regular-expressions.info/wordboundaries.html">Regular Expressions - Word Boundaries</a>
 */
public class WordBoundaryTokenizer implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    private static final String WORD_BOUNDARY = "\\b";

    public WordBoundaryTokenizer(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        while(true) {
            try {
                final String line = input.take();
                if(line.equals(SENTINEL_VALUE)) {
                    output.put(SENTINEL_VALUE);
                    break;
                }
                /*
                 * Split line by word boundary.
                 */
                final String[] words = line.split(WORD_BOUNDARY);
                for(String word : words) {
                    output.put(word);
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
    }
}
