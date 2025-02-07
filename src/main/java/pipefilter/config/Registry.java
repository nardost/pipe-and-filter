package pipefilter.config;

import pipefilter.filter.Filter;
import pipefilter.filter.NumericOnlyWordRemover;
import pipefilter.filter.OpenNLPStemmer;
import pipefilter.filter.PorterStemmer;
import pipefilter.filter.StopWordRemover;
import pipefilter.filter.TermFrequencyCounter;
import pipefilter.filter.TextPreprocessor;
import pipefilter.filter.ToLowerCaseTransformer;
import pipefilter.filter.WordBoundaryTokenizer;
import pipefilter.filter.NonAlphaNumericWordRemover;
import pipefilter.pump.Pump;
import pipefilter.pump.TextFilePump;
import pipefilter.sink.Sink;
import pipefilter.sink.FrequencyTermInverter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nardos Tessema
 *
 * A registry of available Pumps, Filters and Sinks.
 * Each component is uniquely identified by a name.
 * The unique name of the components is used by the
 * corresponding factory to create instances by reflection.
 */
public class Registry {
    /**
     * All available filters
     */
    public static final Map<String, Class<? extends Filter<?, ?>>> registeredFilters = new HashMap<>();

    static {
        registeredFilters.put("tokenizer", WordBoundaryTokenizer.class);
        registeredFilters.put("non-alphanumeric-word-remover", NonAlphaNumericWordRemover.class);
        registeredFilters.put("numeric-only-word-remover", NumericOnlyWordRemover.class);
        registeredFilters.put("to-lower-case-transformer", ToLowerCaseTransformer.class);
        registeredFilters.put("stop-word-remover", StopWordRemover.class);
        registeredFilters.put("opennlp-porter-stemmer", OpenNLPStemmer.class);
        registeredFilters.put("en-porter-stemmer", PorterStemmer.class);
        registeredFilters.put("term-frequency-counter", TermFrequencyCounter.class);
        registeredFilters.put("text-preprocessor", TextPreprocessor.class);
    }

    /**
     * All available pumps
     */
    public static final Map<String, Class<? extends Pump<?, ?>>> registeredPumps = new HashMap<>();

    static {
        registeredPumps.put("text-streamer", TextFilePump.class);
    }

    /**
     * All available sinks
     */
    public static final Map<String, Class<? extends Sink<?, ?>>> registeredSinks = new HashMap<>();

    static  {
        registeredSinks.put("frequency-term-inverter", FrequencyTermInverter.class);
    }

    /**
     * Parallelizable Components
     */
    public static Map<String, Boolean> parallelizable = new HashMap<>();

    static {
        parallelizable.put("tokenizer", true);
        parallelizable.put("text-preprocessor", true);
        parallelizable.put("stop-word-remover", true);
        parallelizable.put("en-porter-stemmer", true);
    }
}
