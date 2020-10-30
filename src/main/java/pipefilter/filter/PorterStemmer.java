package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * @author Nardos Tessema
 *
 * A filter that stems English words into their
 * root terms using Porter algorithm.
 *
 * The Porter implementation is the one that is provided
 * by the instructor (from the official website of Porter Algorithm).
 *
 * @see pipefilter.filter.Stemmer
 * @see <a href="https://tartarus.org/martin/PorterStemmer/index.html>Porter</a>
 */
public class PorterStemmer implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    /**
     * Timing instrumentation instance variables
     */
    private long cumulativeInputBlockingTime;
    private long cumulativeOutputBlockingTime;
    private long totalProcessingTime;
    private long inputCounter;
    private long outputCounter;

    public PorterStemmer(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
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
                Stemmer stemmer = new Stemmer();
                stemmer.add(word.toCharArray(), word.length());
                stemmer.stem();
                final String stem = stemmer.toString();

                beforeOutputPipe = System.currentTimeMillis();
                output.put(stem);
                cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                outputCounter++;
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
