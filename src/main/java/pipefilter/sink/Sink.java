package pipefilter.sink;

public interface Sink<T> extends Runnable {
    void drain();
}
