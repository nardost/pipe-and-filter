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

    /**
     * Timing instrumentation code.
     * The following instance variables are not
     * part of the application. They are there just
     * for measuring times spent in pipes, filters, etc.
     *
     * To clean the instrumentation code later on, begin by
     * deleting these instance variable declarations, and the
     * rest of the instrumentation code will be clearly
     * visible with the help of the IDE error highlighting.
     */
    private long cumulativeInputBlockingTime;
    private long cumulativeOutputBlockingTime;
    private long totalProcessingTime;
    private long inputCounter;
    private long outputCounter;

    public OpenNLPStemmer(Pipe<String> input, Pipe<String> output, CountDownLatch doneSignal) {
        this.input = input;
        this.output = output;
        this.doneSignal = doneSignal;
    }

    @Override
    public void filter() {
        inputCounter = 0L;
        outputCounter = 0L;
        cumulativeInputBlockingTime = 0L;
        cumulativeOutputBlockingTime = 0L;
        long start = System.currentTimeMillis();
        while(true) {
            long beforeInputPipe;
            long beforeOutputPipe;
            try {
                beforeInputPipe = System.currentTimeMillis();
                final String word = input.take();
                cumulativeInputBlockingTime += System.currentTimeMillis() - beforeInputPipe;
                inputCounter++;

                if(word.equals(SENTINEL_VALUE)) {
                    beforeOutputPipe = System.currentTimeMillis();
                    output.put(SENTINEL_VALUE);
                    cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                    break;
                }
                /*
                 * Stem the word with Porter stemmer
                 */
                Stemmer stemmer = new PorterStemmer();
                final String stem = stemmer.stem(word).toString();
                beforeOutputPipe = System.currentTimeMillis();
                output.put(stem);
                cumulativeOutputBlockingTime += System.currentTimeMillis() - beforeOutputPipe;
                outputCounter++;
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        totalProcessingTime = System.currentTimeMillis() - start;
    }

    @Override
    public void run() {
        filter();
        doneSignal.countDown();
        System.out.printf("%1$-26s | %2$10s | %3$11s | %4$9s | %5$8s | %6$8s%n", getClass().getSimpleName(), cumulativeInputBlockingTime, cumulativeOutputBlockingTime, totalProcessingTime, inputCounter, outputCounter);
    }
}
