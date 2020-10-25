package pipefilter.pump;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.*;

public class TextFilePump implements Pump<String, String>, Runnable {

    private final BufferedReader reader;
    private final Pipe<String> pipe;
    private final CountDownLatch doneSignal;

    public TextFilePump(String filePath, Pipe<String> pipe, CountDownLatch doneSignal) {
        try {
            this.pipe = pipe;
            this.reader = Files.newBufferedReader(Paths.get(filePath));
            this.doneSignal = doneSignal;
        } catch (IOException ioe) {
            throw new PipeFilterException("I/O exception while reading file " + filePath);
        }
    }

    @Override
    public void pump() {
        try (reader) {
            String line;
            while ((line = reader.readLine()) != null) {
                pipe.put(line);
            }
            /*
             * A null line indicates the stream has ended. The
             * filter will put the sentinel value on the pipe
             * to notify the next component down the line that
             * the stream has ended.
             */
            pipe.put(SENTINEL_VALUE);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        pump();
        /*
         * Decrement the countdown latch when thread is done.
         */
        doneSignal.countDown();
    }
}
