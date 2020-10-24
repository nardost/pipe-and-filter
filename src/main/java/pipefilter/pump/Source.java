package pipefilter.pump;

public interface Source<T> extends Runnable {
    void pump();
}
