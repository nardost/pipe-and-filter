package pipefilter.pump;

/**
 * @author Nardos Tessema
 *
 * An ACTIVE pump interface.
 *
 * A pump takes input, transforms it in some way
 * and puts the results into a pipe.
 *
 * Pumps are active and, therefore, Runnable.
 *
 * A pump that implements this interface must have
 * EXACTLY ONE constructor that takes THREE arguments
 * in the following order:
 *
 *   1st arg: the input (eg. filename, etc.)
 *   2nd arg: the output pipe
 *   3rd arg: a CountDownLatch to signal completion of pumping activity
 *
 * This constraint must be strictly followed because the creation of a pump
 * and its output pipe is done dynamically through the Java reflection API by
 * inferring types from the constructor arguments.
 *
 * @param <T> the input type
 * @param <U> the output type
 */
public interface Pump<T, U> extends Runnable {
    void pump();
}
