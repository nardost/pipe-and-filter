package pipefilter.pipeline;

import pipefilter.filter.FilterFactory;
import pipefilter.pipe.BlockingQueuePipe;
import pipefilter.pipe.Pipe;
import pipefilter.sink.Sink;
import pipefilter.pump.Source;
import pipefilter.sink.TermFrequencyCounter;
import pipefilter.pump.TextFileSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.*;

public class TermFrequencyPipeline implements Pipeline {

    private final String input;
    private final Map<String, Integer> output;
    private final List<Thread> pipelineElements;
    private final CountDownLatch doneSignal;

    public TermFrequencyPipeline(String input, Map<String, Integer> output, String[] filters) {

        final int activeElements = filters.length + 2;

        this.input = input;
        this.output = output;
        this.pipelineElements = new LinkedList<>();
        this.doneSignal = new CountDownLatch(activeElements);

        composePipeline(input, output, filters, activeElements);
    }

    @Override
    public void run() throws InterruptedException {
        pipelineElements.forEach(Thread::start);
        doneSignal.await();
    }

    private void composePipeline(String input, Map<String, Integer> output, String[] filters, int activeElements) {
        /*
         * (Source) -[p1]-> (F1) -[p2]-> (F2) -[p3]-> (Sink)
         */
        final List<Pipe<String>> pipes = new ArrayList<>(activeElements - 1);
        for(int i = 0; i < activeElements; i++) {
            pipes.add(new BlockingQueuePipe<>(new ArrayBlockingQueue<>(PIPE_CAPACITY)));
        }

        int index = 0;
        final Source<String> source = new TextFileSource(input, pipes.get(index), doneSignal);
        pipelineElements.add(new Thread(source));
        for(String filter : filters) {
            Pipe<String> in = pipes.get(index);
            Pipe<String> out = pipes.get(index + 1);
            pipelineElements.add(new Thread(FilterFactory.build(filter, in, out, doneSignal)));
            index++;
        }
        final Sink<String> sink = new TermFrequencyCounter(pipes.get(index), output, doneSignal);
        pipelineElements.add(new Thread(sink));
    }
}
