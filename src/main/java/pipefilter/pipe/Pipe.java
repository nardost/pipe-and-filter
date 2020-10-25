package pipefilter.pipe;

/**
 * @author Nardos Tessema
 *
 * A pipe interface.
 *
 * A pipe is the queue that two adjacent pipeline
 * components use to share data.
 *
 * Two adjacent components have a Producer/Consumer
 * type of relationship.
 * Pipes must, therefore, be implemented as blocking queues.
 *
 * The type of the data is determined by the output/input
 * type of the filters it serves.
 *
 * Note that:
 *
 * (1) The output type of the pump is the same as the input
 *     type of the first filter in the chain.
 * (2) The output type of a filter is the same as the input
 *     type of the next filter in the chain.
 * (3) The input type of the sink is the same as the output
 *     type of the last filter in the chain.
 *
 * @param <T> the type the pipe data structure holds
 */
public interface Pipe<T> {
    T take() throws InterruptedException;
    void put(T t) throws InterruptedException;
}
