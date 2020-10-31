package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * @author Nardos Tessema
 *
 * A filter that serializes a parallel stream.
 *
 * It takes N input items from N input pipes in Round-Robin
 * turn and and puts all N items into a single output pipe.
 *             -->[**]--(****)--+
 *    +--(pipe)-->[F1]--(pipe)--+
 * -->+--(pipe)-->[F1]--(pipe)--+-->[Serializer]--(pipe)-->
 *    +--(pipe)-->[F1]--(pipe)--+
 *             -->[**]--(****)--+
 */
public class Serializer implements Filter<String, String> {

    private final List<Pipe<String>> inputs;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    /**
     * Timing instrumentation code.
     * The following instance variables are not
     * part of the application. They are there just
     * for measuring times spent in pipes, filters, etc.
     *
     * To clean the instrumentation code later on, begin by
     * deleting these instance variable declarations, and the
     * rest of the instrumentation code will be clearly
     * visible with the help of the IDE error highlighting.
     */
    private long cumulativeInputBlockingTime;
    private long cumulativeOutputBlockingTime;
    private long totalProcessingTime;
    private long inputCounter;
    private long outputCounter;

    public Serializer(List<Pipe<String>> inputs, Pipe<String> output, CountDownLatch doneSignal) {
        this.inputs = inputs;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        inputCounter = 0L;
        outputCounter = 0L;
        cumulativeInputBlockingTime = 0L;
        cumulativeOutputBlockingTime = 0L;
        long start = System.currentTimeMillis();
        try {
            long beforeInputPipe;
            long beforeOutputPipe;
            int round = 0;
            while(inputs.size() > 0) {
                beforeInputPipe = System.currentTimeMillis();
                final String word = inputs.get(round).take();
                cumulativeInputBlockingTime += System.currentTimeMillis() - beforeInputPipe;
                inputCounter++;

                if(word.equals(SENTINEL_VALUE)) {
                    /*
                     * The pipe has exhausted its stream.
                     * Remove the pipe form the list of inputs
                     * so as not to block on it in the future.
                     */
                    inputs.remove(round);
                } else {
                    beforeOutputPipe = System.currentTimeMillis();
                    output.put(word);
                    cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                    outputCounter++;
                }
                /*
                 * take from input pipes in Round-Robin
                 */
                round = inputs.size() > 0 ? (round + 1) % inputs.size() : -1;
            }
            /*
             * all input pipes have exhausted their stream.
             * i.e. the parallel filters are all done...
             */
            beforeOutputPipe = System.currentTimeMillis();
            output.put(SENTINEL_VALUE);
            cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        totalProcessingTime = System.currentTimeMillis() - start;
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
        System.out.printf("%1$-26s - %2$10s - %3$11s - %4$9s - %5$8s - %6$8s%n", getClass().getSimpleName(), cumulativeInputBlockingTime, cumulativeOutputBlockingTime, totalProcessingTime, inputCounter, outputCounter);
    }
}
