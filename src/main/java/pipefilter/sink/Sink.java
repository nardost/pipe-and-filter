package pipefilter.sink;

public interface Sink<T, U> extends Runnable {
    void drain();
}
