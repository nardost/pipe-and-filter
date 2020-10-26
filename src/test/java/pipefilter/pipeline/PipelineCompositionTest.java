package pipefilter.pipeline;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PipelineCompositionTest {

    @Test
    public void pipelineCreationWithIncompatibleComponentAssemblyThrowsPipeFilterException() {
        final String[] assembly = new String[] {
                "text-streamer",
                "term-frequency-counter",
                "stop-word-remover",
                "frequency-counter"
        };
        assertThatThrownBy(() -> PipelineFactory.build("", new HashMap<>(), assembly, "serial"))
                .hasMessageContaining("Pipe mismatch");
    }

    @Test
    public void pipelineCreationWithUnregisteredPumpThrowsPipeFilterException() {
        final String[] assembly = new String[] {
                "unknown-pump",
                "to-lower-case-transformer",
                "term-frequency-counter",
                "frequency-term-inverter"
        };
        assertThatThrownBy(() -> PipelineFactory.build("", new HashMap<>(), assembly, "serial"))
                .hasMessageContaining("Pump not in the registry");
    }

    @Test
    public void pipelineCreationWithUnregisteredFilterThrowsPipeFilterException() {
        final String[] assembly = new String[] {
                "text-streamer",
                "unknown-filter",
                "term-frequency-counter",
                "frequency-term-inverter"
        };
        assertThatThrownBy(() -> PipelineFactory.build("", new HashMap<>(), assembly, "serial"))
                .hasMessageContaining("Filter not in the registry");
    }

    @Test
    public void pipelineCreationWithUnregisteredSinkThrowsPipeFilterException() {
        final String[] assembly = new String[] {
                "text-streamer",
                "term-frequency-counter",
                "unknown-sink"
        };
        assertThatThrownBy(() -> PipelineFactory.build("", new HashMap<>(), assembly, "serial"))
                .hasMessageContaining("Sink not in the registry");
    }

    @Test
    public void pipelineCreationWithCompatibleComponentsDoesNotThrowAnyException() {
        final String[] assembly = new String[] {
                "text-streamer",
                "to-lower-case-transformer",
                "to-lower-case-transformer",
                "to-lower-case-transformer",
                "to-lower-case-transformer",
                "to-lower-case-transformer",
                "term-frequency-counter",
                "frequency-term-inverter"
        };
        PipelineFactory.build("", new HashMap<>(), assembly, "serial");
    }
}
