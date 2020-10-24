package pipefilter.filter;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * FilterFactory creates filters by reflection (dynamically).
 * The list of defined filters is available to the factory.
 * It uses the list to determine which particular Filter to create.
 *
 * A Filter of type Filter<T, U> is expected to have a single
 * constructor with the following signature:
 * public TheOnlyConstructor(Pipe<T> input, Pipe<U> output, CountDownLatch countdown);
 * This constraint is important for the factory to be able to create
 * Filters dynamically by reflection.
 */
public class FilterFactory {

    /**
     * Register available filters here. Give each Filter a unique name which
     * will be used by the factory to create instances by reflection.
     */
    private static final Map<String, Class<? extends Filter<?, ?>>> registeredFilters = new HashMap<>();

    static {
        registeredFilters.put("tokenizer", Tokenizer.class);
    }

    public static <T, U> Filter<T, U> build(String filter, Pipe<T> input, Pipe<U> output, CountDownLatch signal) {
        Class<?> c = registeredFilters.get(filter);
        try {
            @SuppressWarnings("unchecked")
            Constructor<Filter<T, U>> constructor = (Constructor<Filter<T, U>>) c.getConstructors()[0];
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
