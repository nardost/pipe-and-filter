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

    /**
     * Timing instrumentation code.
     * The following instance variables are not
     * part of the application. They are there just
     * for measuring times spent in pipes, filters, etc.
     *
     * To clean the instrumentation code later on, begin by
     * deleting these instance variable declarations, and the
     * rest of the instrumentation code will be clearly
     * visible with the help of the IDE error highlighting.
     */
    private long cumulativeInputBlockingTime;
    private long cumulativeOutputBlockingTime;
    private long totalProcessingTime;
    private long inputCounter;
    private long outputCounter;

    public NonAlphaNumericWordRemover(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
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
                final String word = input.take();
                cumulativeInputBlockingTime += System.currentTimeMillis() - beforeInputPipe;
                inputCounter++;

                if(word.equals(SENTINEL_VALUE)) {
                    beforeOutputPipe = System.currentTimeMillis();
                    output.put(SENTINEL_VALUE);
                    cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                    break;
                }
                /*
                 * Discard if word does not match word pattern
                 */
                if(word.matches(WORD_PATTERN)) {
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
