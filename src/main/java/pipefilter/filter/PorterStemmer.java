package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

public class PorterStemmer implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    public PorterStemmer(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        while(true) {
            try {
                final String word = input.take();
                if(word.equals(SENTINEL_VALUE)) {
                    output.put(SENTINEL_VALUE);
                    break;
                }
                Stemmer stemmer = new Stemmer();
                stemmer.add(word.toCharArray(), word.length());
                stemmer.stem();
                final String stem = stemmer.toString();
                output.put(stem);
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
