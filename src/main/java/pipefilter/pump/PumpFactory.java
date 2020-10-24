package pipefilter.pump;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Registry.registeredPumps;

public class PumpFactory {

    public static <T, U> Pump<T, U> build(String name, T input, Pipe<U> output, CountDownLatch signal) {
        Class<?> c = registeredPumps.get(name);
        try {
            @SuppressWarnings("unchecked")
            Constructor<Pump<T, U>> constructor = (Constructor<Pump<T, U>>) c.getConstructors()[0];
            return constructor.newInstance(input, output, signal);
        } catch (IllegalAccessException iae) {
            throw new PipeFilterException("Illegal access exception while building filter " + name);
        } catch (InvocationTargetException ite) {
            throw new PipeFilterException("Invocation target exception while building filter " + name);
        } catch (InstantiationException ie) {
            throw new PipeFilterException("Instantiation exception while building filter " + name);
        }
    }
}
