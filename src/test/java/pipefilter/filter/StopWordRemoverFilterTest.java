package pipefilter.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pipefilter.TestUtilities;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static pipefilter.config.Configuration.*;

public class StopWordRemoverFilterTest {

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
        STOP_WORDS = new String[] { "a", "am", "an", "and", "at", "on", "or", "so", "the" };


        input = (Pipe<String>) PipeFactory.build("java.lang.String");
        output = (Pipe<String>) PipeFactory.build("java.lang.String");
        signal = new CountDownLatch(1);
    }

    @Test
    public void filterRemovesStopWords() {
        final String[] words = new String[] {
                "I", "am", "an", "architect",
                "She", "has", "a", "cat", "and", "two", "dogs",
                "He", "came", "at", "17:00UTC",
                "The", "book", "is", "on", "the", "shelf", "or", "under", "the", "desk",
                "So", "on", "and", "so", "forth",
                SENTINEL_VALUE
        };
        final String[] expected = new String[] {
                "I", "architect",
                "She", "has", "cat", "two", "dogs",
                "He", "came", "17:00UTC",
                "book", "is", "shelf", "under", "desk",
                "forth",
                SENTINEL_VALUE
        };
        TestUtilities.loadArrayIntoPipe(input, words);
        StopWordRemover filter = new StopWordRemover(input, output, signal);
        filter.filter();
        final String[] actual = TestUtilities.getPipeContentAsArray(output);
        assertThat(expected).isEqualTo(actual);
    }
}
