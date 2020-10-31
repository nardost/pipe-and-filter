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
 *                             +--(....)
 *                             +--(pipe)-->[F1]--
 *  --(pipe)-->[Parallelizer]--+--(pipe)-->[F1]--
 *                             +--(pipe)-->[F1]--
 *                             +--(....)
 *
 */
public class Parallelizer implements Filter<String, String> {

    private final Pipe<String> input;
    private final List<Pipe<String>> outputs;
    private final CountDownLatch doneSignal;

    public Parallelizer(Pipe<String> input, List<Pipe<String>> outputs, CountDownLatch doneSignal) {
        this.input = input;
        this.outputs = outputs;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        int round = 0;
        while(true) {
            try {
                final String word = input.take();
                /*
                 * If input stream has ended, put the
                 * sentinel on all N output pipes.
                 */
                if(word.equals(SENTINEL_VALUE)) {
                    for (Pipe<String> output : outputs) {
                        output.put(word);
                    }
                    break;
                }
                /*
                 * put to the output pipes in Round-Robin turn
                 */
                outputs.get(round).put(word);
                round = (round + 1) % outputs.size();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
        System.out.printf("%1$-26s + %2$10s + %3$11s + %4$9s + %5$8s + %6$8s%n", getClass().getSimpleName(), "*", "*", "*", "*", "*");
    }
}
