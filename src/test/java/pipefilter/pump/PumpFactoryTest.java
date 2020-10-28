package pipefilter.pump;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pipefilter.pipe.PipeFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PumpFactoryTest {

    @TempDir
    Path path;

    @Test
    public void pumpFactoryThrowsPipeFilterExceptionIfPumpNotInRegistry() throws IOException {
        Path input = path.resolve("temp-file.txt");
        Files.write(input, Arrays.asList("just one line"));
        final String file = input.toAbsolutePath().toString();
        assertThatThrownBy(
                () -> PumpFactory.build(
                        "non-registered-pump",
                        file,
                        PipeFactory.build("java.lang.String"),
                        new CountDownLatch(1)))
                .hasMessageContaining("while building pump");
    }

    @Test
    public void pumpFactoryThrowsPipeFilterExceptionIfInputFileDoesNotExist() {
        assertThatThrownBy(
                () -> PumpFactory.build(
                        "text-streamer",
                        "non-existent-file.txt",
                        PipeFactory.build("java.lang.String"),
                        new CountDownLatch(1)))
                .hasMessageContaining("while building pump");
    }
}
