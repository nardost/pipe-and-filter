package pipefilter.pump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pipefilter.TestUtilities;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static pipefilter.config.Configuration.PIPE_CAPACITY;
import static pipefilter.config.Configuration.SENTINEL_VALUE;

public class TextFilePumpTest {

    private String file;
    private Pipe<String> output;
    private CountDownLatch signal;

    @TempDir
    Path path;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() throws IOException {
        /*
         * mock configuration values
         */
        SENTINEL_VALUE = "$$$";
        PIPE_CAPACITY = 20;

        output = (Pipe<String>) PipeFactory.build("java.lang.String", 10);

        final String[] lines = new String[] {
                "the first line",
                "the second line",
                "the last line"
        };
        Path input = path.resolve("a-temporary-file-that-will-vanish-after-tests.txt");
        Files.write(input, Arrays.asList(lines));
        /*
         * set the input file path to the absolute path of the
         * temporary file that the test engine injected - @TempDir.
         */
        file = input.toAbsolutePath().toString();
    }

    @Test
    public void pumpReadsLinesOfTextFromFileAndStreamsToPipeAndAppendsSentinelValue() {
        final String[] expected = new String[] {
                "the first line",
                "the second line",
                "the last line",
                SENTINEL_VALUE
        };
        TextFilePump pump = new TextFilePump(file, output, signal);
        pump.pump();
        final String[] actual = TestUtilities.getPipeContentAsArray(output);
        assertThat(expected).isEqualTo(actual);
    }
}
