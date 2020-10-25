package pipefilter.filter;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Registry.registeredFilters;

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

    public static <T, U> Filter<T, U> build(String name, Pipe<T> input, Pipe<U> output, CountDownLatch signal) {
        Class<?> c = registeredFilters.get(name);
        try {
            @SuppressWarnings("unchecked")
            Constructor<Filter<T, U>> constructor = (Constructor<Filter<T, U>>) c.getConstructors()[0];
            return constructor.newInstance(input, output, signal);
        } catch (IllegalAccessException iae) {
            throw new PipeFilterException("Illegal access exception while building filter " + name);
        } catch (InvocationTargetException ite) {
            throw new PipeFilterException("Invocation target exception while building filter " + name);
        } catch (InstantiationException ie) {
            throw new PipeFilterException("Instantiation exception while building filter " + name);
        }
    }

    /**
     * This method infers the input type of a filter by reflection.
     *
     * Example: Suppose there is a Filter named "some-filter" and
     *          defined as:
     *
     *   public class SomeFilter implements Filter<String, IntStream>
     *
     * inferFilterInput("some-filter") will return "java.lang.String"
     *
     * @param name the name of the filter in the registry
     * @return the input type of the filter
     */
    public static String getFilterInputType(String name) {
        ParameterizedType t = (ParameterizedType) registeredFilters.get(name).getGenericInterfaces()[0];
        return t.getActualTypeArguments()[0].getTypeName();
    }

    /**
     * This method infers the output type of a filter by reflection.
     *
     * Example: Suppose there is a Filter named "some-filter" and
     *          defined as:
     *
     *   public class SomeFilter implements Filter<String, IntStream>
     *
     * inferFilterOutput("some-filter") will return "java.util.stream.IntStream"
     *
     * @param name the name of the filter in the registry
     * @return the output type of the filter
     */
    public static String getFilterOutputType(String name) {
        ParameterizedType t = (ParameterizedType) registeredFilters.get(name).getGenericInterfaces()[0];
        return t.getActualTypeArguments()[1].getTypeName();
    }
}
