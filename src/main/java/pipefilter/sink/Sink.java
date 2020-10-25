package pipefilter.sink;

/**
 * @author Nardos Tessema
 *
 * An ACTIVE sink interface.
 *
 * A sink takes input data, transforms it in some way, and stores
 * the result in the final output data structure of the pipeline.
 *
 * Sinks are active and, therefore, Runnable.
 *
 * A sink that implements this interface must have EXACTLY ONE constructor
 * that takes THREE arguments in the following order:
 *
 *   1st arg: the input pipe
 *   2nd arg: the output data structure
 *   3rd arg: a CountDownLatch to signal completion of draining activity
 *
 * This constraint must be strictly followed because the creation of a sink
 * and its input pipe is done dynamically through the Java reflection API by
 * inferring types from the constructor arguments.
 *
 * @param <T> the input type
 * @param <U> the output type
 */
public interface Sink<T, U> extends Runnable {
    void drain();
}
