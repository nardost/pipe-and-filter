package pipefilter.filter;

/**
 * @author Nardos Tessema
 *
 * An ACTIVE filter interface.
 *
 * A filter takes data from an input pipe, transforms it
 * or uses it in some way and puts data on an output pipe.
 *
 * Filters are active and, therefore, Runnable.
 *
 * A filter that implements this interface must have EXACTLY ONE
 * constructor that takes THREE arguments in the following order:
 *
 *    1st arg: the input pipe
 *    2nd arg: the output pipe
 *    3rd arg: a CountDownLatch to signal completion of filtering activity
 *
 * This constraint must be strictly followed because the creation of a filter
 * and its input/output pipes is done dynamically through the Java reflection API
 * by inferring types from the constructor arguments.
 *
 * @param <T> the input type
 * @param <U> the output type
 */
public interface Filter<T, U> extends Runnable {
    void filter();
}
