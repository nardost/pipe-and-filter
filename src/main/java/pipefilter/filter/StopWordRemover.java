package pipefilter.filter;

import pipefilter.pipe.Pipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;
import static pipefilter.config.Configuration.STOP_WORDS;

/**
 * @author Nardos Tessema
 *
 * A filter that removes stop words.
 *
 */
public class StopWordRemover implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    /**
     * The data structure to hold stop words. HashMap is chosen
     * to get O(1) lookup. If a List was used instead, the filter
     * would do a worst case O(N) lookup for every incoming word.
     */
    private static final Map<String, Boolean> stopWords = new HashMap<>();

    public StopWordRemover(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
        /*
         * Get the stop words to be removed.
         */
        loadStopWords();
    }

    @Override
    public void filter() {
        while(true) {
            try {
                final String word = input.take();
                if(word.equals(SENTINEL_VALUE)) {
                    output.put(SENTINEL_VALUE);
                    break;
                }
                /*
                 * Discard if input is a stop word
                 */
                if(!stopWords.containsKey(word.toLowerCase())) {
                    output.put(word);
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
    }

    /**
     * The list of stop words are loaded into a HashMap
     * to get an O(1) lookup / key searching.
     * List of words from https://www.ranks.nl/stopwords and
     * prepared into a text file by Professor Engelhardt.
     */
    private void loadStopWords() {
        /*
         * The boolean value can be used to turn on/off the status
         * of a word as a stop word. In this implementation, the value
         * is not used since the stop word remover merely checks the
         * presence of the key in the map. Boolean is chosen because
         * it has the least space requirement.
         */
        Arrays.stream(STOP_WORDS).forEach(w -> stopWords.put(w.toLowerCase(), true));
    }
}
