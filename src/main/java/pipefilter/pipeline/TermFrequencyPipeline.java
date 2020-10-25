package pipefilter.pipeline;

import pipefilter.filter.FilterFactory;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;
import pipefilter.pump.PumpFactory;
import pipefilter.sink.SinkFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * The Pipeline class abstracts the entirety of the
 * source, sink, pipes and filters arrangement.
 * It is the single point where the pipeline operation
 * could be triggered.
 */
public class TermFrequencyPipeline implements Pipeline {

    private final String input;
    private final Map<String, Integer> output;
    private final List<Thread> pipelineComponents;
    private final CountDownLatch doneSignal;

    public TermFrequencyPipeline(String input, Map<String, Integer> output, String[] pipeline) {

        this.input = input;
        this.output = output;
        this.pipelineComponents = new LinkedList<>();
        this.doneSignal = new CountDownLatch(pipeline.length);

        compose(input, output, pipeline);
    }

    @Override
    public void run() throws InterruptedException {
        pipelineComponents.forEach(Thread::start);
        /*
         * Wait for all threads to be done before returning to the main thread.
         */
        doneSignal.await();
    }

    private void compose(String input, Map<String, Integer> output, String[] components) {
        Pipe<String> out = PipeFactory.build();
        Pipe<String> in = out;
        /*
         * Create the source and attach to pipeline
         */
        String name = components[0];
        pipelineComponents.add(new Thread(PumpFactory.build(name, input, out, doneSignal)));
        /*
         * Create the filters and attach to pipeline
         */
        for(int i = 1; i <= components.length - 2; i++) {
            name = components[i];
            out = PipeFactory.build();
            pipelineComponents.add(new Thread(FilterFactory.build(name, in, out, doneSignal)));
            in = out;
        }
        /*
         * Create the sink and attach to pipeline
         */
        name = components[components.length - 1];
        pipelineComponents.add(new Thread(SinkFactory.build(name, in, output, doneSignal)));
    }
}
