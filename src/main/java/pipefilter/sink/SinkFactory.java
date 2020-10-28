package pipefilter.sink;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Registry.registeredSinks;

/**
 * @author Nardos Tessema
 *
 * A factory that creates Sink objects
 */
public class SinkFactory {

    /**
     * @param name the unique identifier of the Sink implementation
     * @param pipe the input pipe of the sink
     * @param output the output of the sink (the data structure that holds the final results)
     * @param signal the countdown latch to signal completion of draining.
     * @param <T> the input type
     * @param <U> the output type
     * @return a Sink object
     */
    public static <T, U> Sink<T, U> build(String name, Pipe<T> pipe, U output, CountDownLatch signal) {
        Class<?> c = registeredSinks.get(name);
        try {
            @SuppressWarnings("unchecked")
            Constructor<Sink<T, U>> constructor = (Constructor<Sink<T, U>>) c.getConstructors()[0];
            return constructor.newInstance(pipe, output, signal);
        }  catch (IllegalAccessException iae) {
            throw new PipeFilterException("Illegal access exception while building sink " + name);
        } catch (InvocationTargetException ite) {
            throw new PipeFilterException("Invocation target exception while building sink " + name);
        } catch (InstantiationException ie) {
            throw new PipeFilterException("Instantiation exception while building sink " + name);
        }
    }

    /**
     * This method infers the input type of a sink by reflection.
     *
     * @param name the name of the sink in the registry
     * @return the input type of the sink
     */
    public static String getSinkInputType(String name) {
        ParameterizedType t = (ParameterizedType) registeredSinks.get(name).getGenericInterfaces()[0];
        return t.getActualTypeArguments()[0].getTypeName();
    }
    /**
     * This method infers the output type of a sink by reflection.
     *
     * @param name the name of the sink in the registry
     * @return the output type of the sink
     */
    public static String getSinkOutputType(String name) {
        ParameterizedType t = (ParameterizedType) registeredSinks.get(name).getGenericInterfaces()[0];
        return t.getActualTypeArguments()[1].getTypeName();
    }
}
