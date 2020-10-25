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

public class Registry {
    /**
     * Register available filters here. Give each Filter a unique name which
     * will be used by the factory to create instances by reflection.
     */
    public static final Map<String, Class<? extends Filter<?, ?>>> registeredFilters = new HashMap<>();

    static {
        registeredFilters.put("tokenizer", Tokenizer.class);
    }

    public static final Map<String, Class<? extends Pump<?, ?>>> registeredPumps = new HashMap<>();

    static {
        registeredPumps.put("text-streamer", TextFilePump.class);
        registeredPumps.put("fake-pump", FakePump.class);
    }

    public static final Map<String, Class<? extends Sink<?, ?>>> registeredSinks = new HashMap<>();

    static  {
        registeredSinks.put("frequency-counter", TermFrequencyCounter.class);
    }
}
