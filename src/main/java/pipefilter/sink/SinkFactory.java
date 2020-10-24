package pipefilter.sink;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Registry.registeredSinks;

public class SinkFactory {

    public static <T, U> Sink<T, U> build(String name, Pipe<T> pipe, U output, CountDownLatch signal) {
        Class<?> c = registeredSinks.get(name);
        try {
            @SuppressWarnings("unchecked")
            Constructor<Sink<T, U>> constructor = (Constructor<Sink<T, U>>) c.getConstructors()[0];
            return constructor.newInstance(pipe, output, signal);
        }  catch (IllegalAccessException iae) {
            throw new PipeFilterException("Illegal access exception while building filter " + name);
        } catch (InvocationTargetException ite) {
            throw new PipeFilterException("Invocation target exception while building filter " + name);
        } catch (InstantiationException ie) {
            throw new PipeFilterException("Instantiation exception while building filter " + name);
        }
    }
}
