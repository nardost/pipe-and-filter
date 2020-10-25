package pipefilter.filter;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;
import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * A stemmer filter.
 *
 * This filter uses the Apache Open NLP implementation of the
 * Porter stemming algorithm to stem words.
 *
 * This filter is written as an alternative to another one that
 * uses the Porter implementation downloaded from Porter's homepage.
 *
 * The maven dependency used is:
 *
 * <dependency>
 *   <groupId>org.apache.opennlp</groupId>
 *   <artifactId>opennlp-tools</artifactId>
 *   <version>1.9.3</version>
 * </dependency>
 *
 * @see <a href="https://opennlp.apache.org/">The Apache Open NLP Home Page</a>
 * @see <a href="https://opennlp.apache.org/docs/1.9.3/apidocs/opennlp-tools/index.html">The Javadoc</a>
 */
public class OpenNLPStemmer implements Filter<String, String> {

    private final Pipe<String> input;
    private final Pipe<String> output;
    private final CountDownLatch doneSignal;

    public OpenNLPStemmer(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
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
                 * Stem the word with Porter stemmer
                 */
                Stemmer stemmer = new PorterStemmer();
                final String stem = stemmer.stem(word).toString();
                output.put(stem);
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
}
