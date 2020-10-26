package pipefilter.filter;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * A class that wraps a term and its frequency.
 */
public class TermFrequency implements Data {

    public String term;
    public int frequency;

    public TermFrequency() {
    }

    public TermFrequency(String term, int frequency) {
        this.term = term;
        this.frequency = frequency;
    }

    @Override
    public boolean isSentinelValue() {
        return term.equals(SENTINEL_VALUE);
    }
}
