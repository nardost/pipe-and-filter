package pipefilter.filter;

public interface Filter<T, U> extends Runnable {
    void process();
}
