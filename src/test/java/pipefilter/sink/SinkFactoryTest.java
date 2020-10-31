package pipefilter.sink;

import org.junit.jupiter.api.Test;
import pipefilter.pipe.PipeFactory;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SinkFactoryTest {

    @Test
    public void whenSinkIdIsNotFoundInTheRegistrySinkFactoryThrowsPipeFilterException() {
        assertThatThrownBy(
                () -> SinkFactory.build(
                        "unknown-sink",
                        PipeFactory.build("java.lang.String", 1),
                        new HashMap<>(),
                        new CountDownLatch(1)))
                .hasMessageContaining("Sink not found in the registry");
    }
}
