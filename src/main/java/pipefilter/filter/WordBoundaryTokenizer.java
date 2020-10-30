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

    /**
     * Timing instrumentation instance variables
     */
    private long cumulativeInputBlockingTime;
    private long cumulativeOutputBlockingTime;
    private long totalProcessingTime;
    private long inputCounter;
    private long outputCounter;

    public WordBoundaryTokenizer(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        inputCounter = 0L;
        outputCounter = 0L;
        cumulativeInputBlockingTime = 0L;
        cumulativeOutputBlockingTime = 0L;
        long start = System.currentTimeMillis();
        while(true) {
            long beforeInputPipe;
            long beforeOutputPipe;
            try {
                beforeInputPipe = System.currentTimeMillis();
                final String line = input.take();
                cumulativeInputBlockingTime += System.currentTimeMillis() - beforeInputPipe;
                inputCounter++;

                if(line.equals(SENTINEL_VALUE)) {
                    beforeOutputPipe = System.currentTimeMillis();
                    output.put(SENTINEL_VALUE);
                    cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                    break;
                }
                /*
                 * Split line by word boundary.
                 */

                final String[] words = line.split(WORD_BOUNDARY);
                for(String word : words) {
                    beforeOutputPipe = System.currentTimeMillis();
                    output.put(word);
                    cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                    outputCounter++;
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        totalProcessingTime = System.currentTimeMillis() - start;
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
        System.out.printf("%1$-26s | %2$10s | %3$11s | %4$9s | %5$8s | %6$8s%n", getClass().getSimpleName(), cumulativeInputBlockingTime, cumulativeOutputBlockingTime, totalProcessingTime, inputCounter, outputCounter);
    }
}
