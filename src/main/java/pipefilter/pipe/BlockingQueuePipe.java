package pipefilter.pipe;

import java.util.concurrent.BlockingQueue;

public class BlockingQueuePipe<T> implements Pipe<T> {

    private final BlockingQueue<T> pipe;

    public BlockingQueuePipe(BlockingQueue<T> pipe) {
        this.pipe = pipe;
    }

    @Override
    public T take() throws InterruptedException {
        return pipe.take();
    }

    @Override
    public void put(T t) throws InterruptedException {
        pipe.put(t);
    }
}
