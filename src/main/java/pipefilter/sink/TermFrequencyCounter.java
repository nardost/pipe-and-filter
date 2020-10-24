package pipefilter.sink;

import pipefilter.pipe.Pipe;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.*;

public class TermFrequencyCounter implements Sink<String>, Runnable {

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
                if(input.equals(SENTINEL)) {
                    break;
                }
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
        doneSignal.countDown();
    }
}
