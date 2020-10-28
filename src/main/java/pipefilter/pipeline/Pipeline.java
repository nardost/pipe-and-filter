package pipefilter.pipeline;

/**
 * @author Nardos Tessema
 *
 * A representation of the ordered assembly of
 * a Pump, a series of Filters, and a Sink.
 */
public interface Pipeline {
    void run() throws InterruptedException;
}
