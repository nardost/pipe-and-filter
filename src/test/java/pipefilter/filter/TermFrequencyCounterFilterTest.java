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

public class TermFrequencyCounterFilterTest {
    private Pipe<String> input;
    private Pipe<TermFrequency> output;
    private CountDownLatch signal;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {

        /*
         * mock configuration values
         */
        SENTINEL_VALUE = "$$$";
        PIPE_CAPACITY = 40;


        input = (Pipe<String>) PipeFactory.build("java.lang.String", 100);
        output = (Pipe<TermFrequency>) PipeFactory.build("pipefilter.filter.TermFrequency", 100);
        signal = new CountDownLatch(1);
    }

    @Test
    public void filterCountsIncomingWords() {
        final String[] words = new String[] {
                "I", "felt", "happy", "because", "I", "saw",
                "the", "others", "were", "happy", "and", "because",
                "I", "knew", "I", "should", "feel", "happy", "but",
                "I", "was", "not", "really", "happy",
                SENTINEL_VALUE
        };
        final String[] expected = new String[] {
                "I:1",
                "felt:1",
                "happy:1",
                "because:1",
                "I:2",
                "saw:1",
                "the:1",
                "others:1",
                "were:1",
                "happy:2",
                "and:1",
                "because:2",
                "I:3",
                "knew:1",
                "I:4",
                "should:1",
                "feel:1",
                "happy:3",
                "but:1",
                "I:5",
                "was:1",
                "not:1",
                "really:1",
                "happy:4",
                SENTINEL_VALUE + ":0"
        };
        TestUtilities.loadArrayIntoPipe(input, words);
        TermFrequencyCounter filter = new TermFrequencyCounter(input, output, signal);
        filter.filter();
        final Data[] actualData = TestUtilities.pipeToArrayOfData(output);
        final String[] actual = new String[actualData.length];
        int index = 0;
        for(Data tf : actualData) {
            String term = ((TermFrequency) tf).term;
            int frequency = ((TermFrequency) tf).frequency;
            actual[index++] = term + ":" + frequency;
        }
        assertThat(expected).isEqualTo(actual);
    }
}
