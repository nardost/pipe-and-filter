package pipefilter.filter;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;
import pipefilter.pipe.Pipe;

import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

/**
 * @author Nardos Tessema
 *
 * A filter that stems English words into their root terms.
 *
 * This filter uses the Apache Open NLP implementation of the
 * Porter stemming algorithm to stem words.
 *
 * This filter is written as an alternative to the one that
 * uses the Porter implementation downloaded from Porter's homepage.
 *
 * I haven't seen any differences in the outputs of the two so far.
 * However, ...
 * I believe this one is more dependable than a copy-pasted code because it:
 *       - comes from the reputed Apache OpenNLP project (tested, etc...)
 *       - introduces dependency in the code base properly (the Maven way)
 *
 * The maven dependency is:
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
        long start = System.currentTimeMillis();
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
        long elapsedTime = System.currentTimeMillis() - start;
        // System.out.printf("%1$-30s%2$9d%n", "opennlp-stemmer", elapsedTime);
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
    }
}
