package pipefilter.pipe;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PipeFactoryTest {

    @Test
    public void pipeFactoryThrowsPipeFilterExceptionIfTypeUnknown() {
        final String pipeType = "some.unknown.Type";
        assertThatThrownBy(() -> PipeFactory.build(pipeType, 10))
                .hasMessageContaining("Unknown pipe type");
    }

    @Test
    public void pipeFactoryDoesNotThrowAnyExceptionForKnowPipeType() {
        final String pipeType = "java.lang.String";
        PipeFactory.build(pipeType, 1);
    }
}
