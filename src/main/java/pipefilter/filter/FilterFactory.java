package pipefilter.filter;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class FilterFactory {

    private static final Map<String, Class<? extends Filter<String, String>>> registeredFilters = new HashMap<>();

    static {
        registeredFilters.put("tokenizer", Tokenizer.class);
    }

    public static Filter<String, String> build(String filter, Pipe<String> input, Pipe<String> output, CountDownLatch signal) {
        Class<?> c = registeredFilters.get(filter);
        try {
            @SuppressWarnings("unchecked")
            Constructor<Filter<String, String>> constructor = (Constructor<Filter<String, String>>) c.getConstructors()[0];
            return constructor.newInstance(input, output, signal);
        } catch (IllegalAccessException iae) {
            throw new PipeFilterException("Illegal access exception while building filter " + filter);
        } catch (InvocationTargetException ite) {
            throw new PipeFilterException("Invocation target exception while building filter " + filter);
        } catch (InstantiationException ie) {
            throw new PipeFilterException("Instantiation exception while building filter " + filter);
        }
    }
}
