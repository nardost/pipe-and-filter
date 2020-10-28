package pipefilter.pump;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.io.BufferedReader;
import java.io.FileReader;
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
            // this.reader = new BufferedReader(new FileReader(filePath), 4_194_304); // 4MB buffer
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
            long start = System.currentTimeMillis();

            while ((line = reader.readLine()) != null) {
                pipe.put(line);
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
            pipe.put(SENTINEL_VALUE);
            long elapsedTime = System.currentTimeMillis() - start;
            // System.out.printf("%1$-30s%2$9d%n", "text-file-streamer-pump", elapsedTime);
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
