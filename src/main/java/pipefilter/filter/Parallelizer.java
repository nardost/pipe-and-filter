package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * @author Nardos Tessema
 *
 * A filter that parallelizes a stream.
 *
 * It takes an input item from an input pipe and puts
 * it into N parallel output pipes in Round-Robin turn.
 *
 *                              +--(****)-->[**]--
 *                              +--(pipe)-->[F1]--
 *  --(pipe)-->[Parallelizer]-->+--(pipe)-->[F1]--
 *                              +--(pipe)-->[F1]--
 *                              +--(****)-->[**]--
 *
 */
public class Parallelizer implements Filter<String, String> {

    private final Pipe<String> input;
    private final List<Pipe<String>> outputs;
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

    public Parallelizer(Pipe<String> input, List<Pipe<String>> outputs, CountDownLatch doneSignal) {
        this.input = input;
        this.outputs = outputs;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        inputCounter = 0L;
        outputCounter = 0L;
        cumulativeInputBlockingTime = 0L;
        cumulativeOutputBlockingTime = 0L;
        long start = System.currentTimeMillis();

        int round = 0;
        while(true) {
            long beforeInputPipe;
            long beforeOutputPipe;
            try {
                beforeInputPipe = System.currentTimeMillis();
                final String word = input.take();
                cumulativeInputBlockingTime += System.currentTimeMillis() - beforeInputPipe;
                inputCounter++;

                /*
                 * If input stream has ended, put the
                 * sentinel on all N output pipes.
                 */
                if(word.equals(SENTINEL_VALUE)) {
                    for (Pipe<String> output : outputs) {
                        beforeOutputPipe = System.currentTimeMillis();
                        output.put(word);
                        cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                    }
                    break;
                }
                /*
                 * put to the output pipes in Round-Robin turn
                 */
                beforeOutputPipe = System.currentTimeMillis();
                outputs.get(round).put(word);
                cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                outputCounter++;

                round = (round + 1) % outputs.size();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        totalProcessingTime = System.currentTimeMillis() - start;
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
        System.out.printf("%1$-26s + %2$10s + %3$11s + %4$9s + %5$8s + %6$8s%n", getClass().getSimpleName(), cumulativeInputBlockingTime, cumulativeOutputBlockingTime, totalProcessingTime, inputCounter, outputCounter);
    }
}
