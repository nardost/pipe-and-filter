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

public class WordBoundaryTokenizerFilterTest {
    private Pipe<String> input;
    private Pipe<String> output;
    private CountDownLatch signal;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {

        /*
         * mock configuration values.
         */
        SENTINEL_VALUE = "$$$";
        PIPE_CAPACITY = 100;

        input = (Pipe<String>) PipeFactory.build("java.lang.String");
        output = (Pipe<String>) PipeFactory.build("java.lang.String");
        signal = new CountDownLatch(1);
    }

    @Test
    public void filterTokenizesStringIntoSeparateWords() {
        final String[] lines = new String[] {
                "SE480 - Computer Architecture I",
                "Pipe&Filter Pattern...",
                "The struggle itself towards the heights,",
                " is enough to fill a man's heart.",
                "One must imagine Sisyphus happy.",
                SENTINEL_VALUE
        };
        final String[] expected = new String[] {
                "SE480", " - ", "Computer", " ", "Architecture", " ", "I",
                "Pipe", "&", "Filter", " ", "Pattern", "...",
                "The", " ", "struggle", " ", "itself", " ", "towards", " ", "the", " ", "heights", "", "",
                " ", "is", " ", "enough", " ", "to", " ", "fill", " ", "a", " ", "man", "'", "s", " ", "heart", ".",
                "One", " ", "must", " ", "imagine", " ", "Sisyphus", " ", "happy", ".",
                SENTINEL_VALUE
        };
        TestUtilities.loadArrayIntoPipe(input, lines);
        WordBoundaryTokenizer filter = new WordBoundaryTokenizer(input, output, signal);
        filter.filter();
        final String[] actual = TestUtilities.getPipeContentAsArray(output);
        assertThat(expected).isEqualTo(actual);
    }
}
