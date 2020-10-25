package pipefilter.config;

import pipefilter.filter.Filter;
import pipefilter.filter.OpenNLPStemmer;
import pipefilter.filter.PorterStemmer;
import pipefilter.filter.StopWordRemover;
import pipefilter.filter.ToLowerCaseTransformer;
import pipefilter.filter.WordBoundaryTokenizer;
import pipefilter.filter.NonWordCharacterCleaner;
import pipefilter.pump.FakePump;
import pipefilter.pump.Pump;
import pipefilter.pump.TextFilePump;
import pipefilter.sink.Sink;
import pipefilter.sink.TermFrequencyCounter;
import pipefilter.sink.FrequencyTermInverter;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry of available Pumps, Filters and Sinks.
 * Each component is uniquely identified by a name.
 * The unique name of the components is used by the
 * corresponding factory to create instance by reflection.
 */
public class Registry {
    /**
     * All available filters
     */
    public static final Map<String, Class<? extends Filter<?, ?>>> registeredFilters = new HashMap<>();

    static {
        registeredFilters.put("tokenizer", WordBoundaryTokenizer.class);
        registeredFilters.put("non-word-char-cleaner", NonWordCharacterCleaner.class);
        registeredFilters.put("to-lower-case-transformer", ToLowerCaseTransformer.class);
        registeredFilters.put("stop-word-remover", StopWordRemover.class);
        registeredFilters.put("opennlp-porter-stemmer", OpenNLPStemmer.class);
        registeredFilters.put("porter-stemmer", PorterStemmer.class);
        registeredFilters.put("term-frequency-counter", pipefilter.filter.TermFrequencyCounter.class);
    }

    /**
     * All available pumps
     */
    public static final Map<String, Class<? extends Pump<?, ?>>> registeredPumps = new HashMap<>();

    static {
        registeredPumps.put("text-streamer", TextFilePump.class);
        registeredPumps.put("fake-pump", FakePump.class);
    }

    /**
     * All available sinks
     */
    public static final Map<String, Class<? extends Sink<?, ?>>> registeredSinks = new HashMap<>();

    static  {
        registeredSinks.put("frequency-counter", TermFrequencyCounter.class);
        registeredSinks.put("frequency-term-inverter", FrequencyTermInverter.class);
    }
}
