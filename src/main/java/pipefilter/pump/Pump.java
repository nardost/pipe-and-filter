package pipefilter.pump;

public interface Pump<T, U> extends Runnable {
    void pump();
}
