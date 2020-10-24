package pipefilter.filter;

import pipefilter.config.Configuration;
import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

class Tokenizer implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    public Tokenizer(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void process() {
        while(true) {
            try {
                final String line = input.take();
                if(line.equals(Configuration.SENTINEL)) {
                    output.put(Configuration.SENTINEL);
                    break;
                }
                final String[] words = line.split("\\s+");
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
        process();
        doneSignal.countDown();
    }
}
