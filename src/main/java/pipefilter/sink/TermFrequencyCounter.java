package pipefilter.sink;

import pipefilter.pipe.Pipe;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.*;

public class TermFrequencyCounter implements Sink<String>, Runnable {

    private final Pipe<String> pipe;
    private final Map<String, Integer> termFrequencies;
    private final CountDownLatch doneSignal;

    public TermFrequencyCounter(Pipe<String> pipe, Map<String, Integer> termFrequencies, CountDownLatch doneSignal) {
        this.pipe = pipe;
        this.termFrequencies = termFrequencies;
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
                final int frequency = termFrequencies.get(input) != null ? termFrequencies.get(input) : 0;
                termFrequencies.put(input, 1 + frequency);
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
