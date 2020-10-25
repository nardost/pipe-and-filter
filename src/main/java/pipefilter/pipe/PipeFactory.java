package pipefilter.pipe;

import java.util.concurrent.ArrayBlockingQueue;

import static pipefilter.config.Configuration.PIPE_CAPACITY;

public class PipeFactory {

    public static <T> Pipe<T> build() {
        return new BlockingQueuePipe<>(new ArrayBlockingQueue<>(PIPE_CAPACITY));
    }
}
