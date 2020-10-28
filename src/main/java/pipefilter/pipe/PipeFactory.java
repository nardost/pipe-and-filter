package pipefilter.pipe;

import pipefilter.exception.PipeFilterException;
import pipefilter.filter.TermFrequency;

import java.util.concurrent.ArrayBlockingQueue;

import static pipefilter.config.Configuration.PIPE_CAPACITY;

/**
 * ArrayBlockingQueue is chosen as the pipe.
 * The capacity of the pipes is configurable.
 *
 * @see pipefilter.config.Configuration
 *
 * The data type of the pipe comes as an input to the
 * factory method. There could be an entry for every conceivable data type.
 * I have defined only a few just to demonstrate the possibilities.
 */
public class PipeFactory {

    /**
     * @param type the type of the data the Pipe holds
     * @return a Pipe object
     */
    public static Pipe<?> build(String type) {
        if(type.equals("java.lang.String")) {
            return new BlockingQueuePipe<String>(new ArrayBlockingQueue<>(PIPE_CAPACITY));
        }
        if(type.equals("pipefilter.filter.TermFrequency")) {
            return new BlockingQueuePipe<TermFrequency>(new ArrayBlockingQueue<>(PIPE_CAPACITY));
        }
        if(type.equals("java.lang.Integer")) {
            return new BlockingQueuePipe<Integer>(new ArrayBlockingQueue<>(PIPE_CAPACITY));
        }
        if(type.equals("java.lang.Double")) {
            return new BlockingQueuePipe<Double>(new ArrayBlockingQueue<>(PIPE_CAPACITY));
        }
        throw new PipeFilterException("Unknown pipe type: " + type);
    }
}
