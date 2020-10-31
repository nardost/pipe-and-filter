package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * An active filter that does three tasks in one:
 * (1) Removes non-alphanumeric words
 * (2) Removes numeric only words
 * (3) Transforms to lower case
 *
 * Those tasks were previously done by three separate
 * filters. As the task are too small, they have all
 * been merged into one filter to minimize latency that
 * may result from too many filters blocking on I/O pipes.
 */
public class TextPreprocessor implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    private static final String ALPHANUMERIC_WORD_PATTERN = "\\w+";
    private static final String NUMERIC_ONLY_WORD_PATTERN = "\\d+";


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


    public TextPreprocessor(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
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
                 * 1. Removes non-alphanumeric words
                 * 2. Removes numeric only words
                 * 3. Transforms to lower case
                 */
                if(!word.matches(NUMERIC_ONLY_WORD_PATTERN) && word.matches(ALPHANUMERIC_WORD_PATTERN)) {
                    beforeOutputPipe = System.currentTimeMillis();
                    output.put(word.toLowerCase());
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
