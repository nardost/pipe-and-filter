package pipefilter.pipe;

public interface Pipe<T> {
    T take() throws InterruptedException;
    void put(T t) throws InterruptedException;
}
