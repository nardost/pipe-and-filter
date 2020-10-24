package pipefilter.filter;

public interface Filter<E, F> extends Runnable {
    void process();
}
