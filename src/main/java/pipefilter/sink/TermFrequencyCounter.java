package pipefilter.sink;

import pipefilter.pipe.Pipe;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.*;

public class TermFrequencyCounter implements Sink<String, Map<String, Integer>>, Runnable {

    private final Pipe<String> pipe;
    private final Map<String, Integer> terms;
    private final CountDownLatch doneSignal;

    public TermFrequencyCounter(Pipe<String> pipe, Map<String, Integer> terms, CountDownLatch doneSignal) {
        this.pipe = pipe;
        this.terms = terms;
        this.doneSignal = doneSignal;
    }

    @Override
    public void drain() {
        while(true) {
            try {
                final String input = pipe.take();
                /*
                 * If the filter takes the sentinel value from
                 * the pipe, it will assume the stream has ended.
                 */
                if(input.equals(SENTINEL_VALUE)) {
                    break;
                }
                /*
                 * A new term will have a frequency of 1. An already existing
                 * term will have its term frequency incremented by 1.
                 */
                final int frequency = terms.get(input) != null ? terms.get(input) : 0;
                terms.put(input, 1 + frequency);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        drain();
        /*
         * Decrement the countdown latch when thread is done.
         */
        doneSignal.countDown();
    }
}
