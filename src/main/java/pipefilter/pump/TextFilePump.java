package pipefilter.pump;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.*;

/**
 * @author Nardos Tessema
 *
 * The Source of the text stream
 *
 * Reads a text file and streams the lines
 */
public class TextFilePump implements Pump<String, String>, Runnable {

    private final BufferedReader reader;
    private final Pipe<String> pipe;
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
    private long cumulativeLineReadingTime;
    private long cumulativeOutputBlockingTime;
    private long totalProcessingTime;
    private long inputCounter;
    private long outputCounter;

    public TextFilePump(String filePath, Pipe<String> pipe, CountDownLatch doneSignal) {
        try {
            this.pipe = pipe;
            // this.reader = new BufferedReader(new FileReader(filePath), 4_194_304); // 4MB buffer
            this.reader = Files.newBufferedReader(Paths.get(filePath));
            this.doneSignal = doneSignal;
        } catch (IOException ioe) {
            throw new PipeFilterException("I/O exception while reading file " + filePath);
        }
    }

    @Override
    public void pump() {
        cumulativeLineReadingTime = 0L;
        cumulativeOutputBlockingTime = 0L;
        long start = System.currentTimeMillis();
        try (reader) {
            long beforeReadingLine;
            long beforeOutputPipe;
            String line;
            beforeReadingLine = System.currentTimeMillis();
            while ((line = reader.readLine()) != null) {
                cumulativeLineReadingTime += System.currentTimeMillis() - beforeReadingLine;
                inputCounter++;

                beforeOutputPipe = System.currentTimeMillis();
                pipe.put(line);
                cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                outputCounter++;
                beforeReadingLine = System.currentTimeMillis();
            }

            /* // parallel stream implementation appears to be slower than the above.
            reader.lines().parallel().forEach(l -> {
                try {
                    pipe.put(l);
                } catch (InterruptedException ignored) {
                }
            });*/

            /*
             * A null line indicates the stream has ended. The
             * filter will put the sentinel value on the pipe
             * to notify the next component down the line that
             * the stream has ended.
             */
            beforeOutputPipe = System.currentTimeMillis();
            pipe.put(SENTINEL_VALUE);
            cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        totalProcessingTime = System.currentTimeMillis() - start;
    }

    @Override
    public void run() {
        pump();
        /*
         * Decrement the countdown latch when thread is done.
         */
        doneSignal.countDown();
        System.out.printf("%1$-26s | %2$10s | %3$11s | %4$9s | %5$8s | %6$8s%n", getClass().getSimpleName(), cumulativeLineReadingTime, cumulativeOutputBlockingTime, totalProcessingTime, inputCounter, outputCounter);
    }
}
