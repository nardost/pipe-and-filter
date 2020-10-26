package pipefilter.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pipefilter.TestUtilities;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static pipefilter.config.Configuration.PIPE_CAPACITY;
import static pipefilter.config.Configuration.SENTINEL_VALUE;

public class ToLowerCaseTransformerFilterTest {

    private Pipe<String> input;
    private Pipe<String> output;
    private CountDownLatch signal;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {

        /*
         * mock configuration values
         */
        SENTINEL_VALUE = "$$$";
        PIPE_CAPACITY = 40;

        input = (Pipe<String>) PipeFactory.build("java.lang.String");
        output = (Pipe<String>) PipeFactory.build("java.lang.String");
        signal = new CountDownLatch(1);
    }

    @Test
    public void filterTransformsWordsToLowerCase() {
        final String[] words = new String[] {
                "Paradoxically", "the", "ability", "to", "be", "ALONE", "IS",
                "the", "condition", "for", "the", "ability", "to", "love",
                "ERICH", "FROMM",
                "The", "Art", "of", "Loving",
                SENTINEL_VALUE
        };
        final String[] expected = new String[] {
                "paradoxically", "the", "ability", "to", "be", "alone", "is",
                "the", "condition", "for", "the", "ability", "to", "love",
                "erich", "fromm",
                "the", "art", "of", "loving",
                SENTINEL_VALUE
        };
        TestUtilities.loadArrayIntoPipe(input, words);
        ToLowerCaseTransformer filter = new ToLowerCaseTransformer(input, output, signal);
        filter.filter();
        final String[] actual = TestUtilities.getPipeContentAsArray(output);
        assertThat(expected).isEqualTo(actual);
    }
}
