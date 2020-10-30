package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;
import static pipefilter.config.Configuration.STOP_WORDS;
import static pipefilter.config.Configuration.STOP_WORDS_MAP;

/**
 * @author Nardos Tessema
 *
 * A filter that removes stop words.
 *
 */
public class StopWordRemover implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    /**
     * The data structure to hold stop words. HashMap is chosen
     * to get O(1) lookup. If a List was used instead, the filter
     * would do a worst case O(N) lookup for every incoming word.
     */
    private static final Map<String, Boolean> stopWords = new HashMap<>();

    /**
     * Timing instrumentation instance variables
     */
    private long cumulativeInputBlockingTime;
    private long cumulativeOutputBlockingTime;
    private long totalProcessingTime;
    private long inputCounter;
    private long outputCounter;

    public StopWordRemover(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        inputCounter = 0;
        outputCounter = 0;
        cumulativeInputBlockingTime = 0;
        cumulativeOutputBlockingTime = 0;
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
                 * Discard if input is a stop word
                 */
                if(!STOP_WORDS_MAP.containsKey(word.toLowerCase())) {
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
