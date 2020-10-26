package pipefilter.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pipefilter.TestUtilities;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static pipefilter.config.Configuration.PIPE_CAPACITY;
import static pipefilter.config.Configuration.SENTINEL_VALUE;

public class NonAlphaNumericWordRemoverFilterTest {

    private Pipe<String> input;
    private Pipe<String> output;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {

        /*
         * configure system for test
         */
        SENTINEL_VALUE = "$$$";
        PIPE_CAPACITY = 20;

        input = (Pipe<String>) PipeFactory.build("java.lang.String");
        output = (Pipe<String>) PipeFactory.build("java.lang.String");
    }

    @Test
    public void filterRemovesNonAlphaNumericWords() {
        /*
         * instantiate the filter
         */
        final CountDownLatch signal = new CountDownLatch(1);
        final NonAlphaNumericWordRemover filter = new NonAlphaNumericWordRemover(input, output, signal);
        final String[] words = new String[] {
                "God#101", "god", "101",
                "se:480", "se", "480",
                "{P2P}", "P", "2", "P",
                "@#$", "1-0-1", "_480",
                SENTINEL_VALUE
        };
        final String[] expected = new String[] {
                "god", "101",
                "se", "480",
                "P", "2", "P",
                "_480",
                SENTINEL_VALUE
        };
        /*
         * feed the input pump with data
         */
        TestUtilities.loadArrayIntoPipe(input, words);
        /*
         * run the filter
         */
        filter.filter();
        /*
         * get pipe content as array and assert expected = actual
         */
        String[] actual = TestUtilities.getPipeContentAsArray(output);
        assertThat(expected).isEqualTo(actual);

    }
}
