package pipefilter.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pipefilter.TestUtilities;
import pipefilter.pipe.Pipe;
import pipefilter.pipe.PipeFactory;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static pipefilter.config.Configuration.PIPE_CAPACITY;
import static pipefilter.config.Configuration.SENTINEL_VALUE;

public class NumericOnlyWordRemoverFilterTest {

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
        PIPE_CAPACITY = 20;

        input = (Pipe<String>) PipeFactory.build("java.lang.String");
        output = (Pipe<String>) PipeFactory.build("java.lang.String");
        signal = new CountDownLatch(1);
    }

    @Test
    public void filterRemovesNonAlphaNumericOnlyWords() {
        /*
         * instantiate the filter
         */
        final NumericOnlyWordRemover filter = new NumericOnlyWordRemover(input, output, signal);
        final String[] words = new String[] {
                "God101", "god", "101",
                "se480", "se", "480",
                "P2P", "P", "2", "P",
                "@#$", "1-0-1", "_480",
                SENTINEL_VALUE
        };
        final String[] expected = new String[] {
                "God101", "god",
                "se480", "se",
                "P2P", "P", "P",
                "@#$", "1-0-1", "_480",
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
