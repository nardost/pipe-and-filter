package pipefilter.config;

import pipefilter.filter.Filter;
import pipefilter.filter.Tokenizer;
import pipefilter.pump.FakePump;
import pipefilter.pump.Pump;
import pipefilter.pump.TextFilePump;
import pipefilter.sink.Sink;
import pipefilter.sink.TermFrequencyCounter;

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
        registeredFilters.put("tokenizer", Tokenizer.class);
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
    }
}
