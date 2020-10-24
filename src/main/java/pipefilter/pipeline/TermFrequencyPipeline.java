package pipefilter.pipeline;

import pipefilter.filter.FilterFactory;
import pipefilter.pipe.BlockingQueuePipe;
import pipefilter.pipe.Pipe;
import pipefilter.pump.PumpFactory;
import pipefilter.sink.Sink;
import pipefilter.pump.Pump;
import pipefilter.sink.SinkFactory;
import pipefilter.sink.TermFrequencyCounter;
import pipefilter.pump.TextFilePump;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.*;

/**
 * The Pipeline class abstracts the entirety of the
 * source, sink, pipes and filters arrangement.
 * It is the single point where the pipeline operation
 * could be triggered.
 */
public class TermFrequencyPipeline implements Pipeline {

    private final String input;
    private final Map<String, Integer> output;
    private final List<Thread> pipelineElements;
    private final CountDownLatch doneSignal;

    public TermFrequencyPipeline(String input, Map<String, Integer> output, String[] pipeline) {

        this.input = input;
        this.output = output;
        this.pipelineElements = new LinkedList<>();
        this.doneSignal = new CountDownLatch(pipeline.length);

        composePipeline(input, output, pipeline);
    }

    @Override
    public void run() throws InterruptedException {
        pipelineElements.forEach(Thread::start);
        /*
         * Wait for all threads to be done.
         */
        doneSignal.await();
    }

    private void composePipeline(String input, Map<String, Integer> output, String[] components) {
        /*
         * Create the pipes first. (n pipeline components) => (n - 1 pipes)
         */
        final List<Pipe<String>> pipes = new ArrayList<>( components.length - 1);
        for(int i = 0; i < components.length - 1; i++) {
            pipes.add(new BlockingQueuePipe<>(new ArrayBlockingQueue<>(PIPE_CAPACITY)));
        }
        /*
         * Create the pump (the source) and attach it to the pipeline.
         */
        pipelineElements.add(new Thread(PumpFactory.build(components[0], input, pipes.get(0), doneSignal)));
        /*
         * Create the filters and add them to the pipeline.
         */
        for(int i = 1; i < components.length - 1; i++) {
            final String filter = components[i];
            Pipe<String> in = pipes.get(i - 1);
            Pipe<String> out = pipes.get(i);
            pipelineElements.add(new Thread(FilterFactory.build(filter, in, out, doneSignal)));
        }
        /*
         * Create the sink and attach it to the pipeline.
         */
        pipelineElements.add(new Thread(SinkFactory.build(components[components.length - 1], pipes.get(components.length - 2), output, doneSignal)));
    }

    private void compose(String input, Map<String, Integer> output, String[] components) {
        /*
         * Create the pipes first. (n pipeline components) => (n - 1 pipes)
         */
        final List<Pipe<String>> pipes = new ArrayList<>( components.length - 1);
        for(int i = 0; i < components.length - 1; i++) {
            pipes.add(new BlockingQueuePipe<>(new ArrayBlockingQueue<>(PIPE_CAPACITY)));
        }
        /*
         * Create the pump (the source) and attach it to the pipeline.
         */
        pipelineElements.add(new Thread(PumpFactory.build(components[0], input, pipes.get(0), doneSignal)));
        /*
         * Create the filters and add them to the pipeline.
         */
        for(int i = 1; i < components.length - 1; i++) {
            final String name = components[i];
            Pipe<String> in = pipes.get(i - 1);
            Pipe<String> out = pipes.get(i);
            pipelineElements.add(new Thread(FilterFactory.build(name, in, out, doneSignal)));
        }
        /*
         * Create the sink and attach it to the pipeline.
         */
        pipelineElements.add(new Thread(SinkFactory.build(components[components.length - 1], pipes.get(components.length - 2), output, doneSignal)));
    }
}
