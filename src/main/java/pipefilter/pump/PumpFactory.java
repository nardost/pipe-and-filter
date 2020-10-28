package pipefilter.pump;

import pipefilter.exception.PipeFilterException;
import pipefilter.pipe.Pipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.CountDownLatch;

import static pipefilter.config.Registry.registeredPumps;

/**
 * @author Nardos Tessema
 *
 * A factory that builds Pumb objects
 */
public class PumpFactory {

    /**
     * @param name the unique identifier of the Pump implementation
     * @param input the input Pipe object
     * @param output the output Pipe object
     * @param signal countdown latch to signal completion of pumping
     * @param <T> the input type
     * @param <U> the output type
     * @return
     */
    public static <T, U> Pump<T, U> build(String name, T input, Pipe<U> output, CountDownLatch signal) {
        Class<?> c = registeredPumps.get(name);
        try {
            @SuppressWarnings("unchecked")
            Constructor<Pump<T, U>> constructor = (Constructor<Pump<T, U>>) c.getConstructors()[0];
            return constructor.newInstance(input, output, signal);
        } catch (IllegalAccessException iae) {
            throw new PipeFilterException("Illegal access exception while building pump " + name);
        } catch (InvocationTargetException ite) {
            throw new PipeFilterException("Invocation target exception while building pump " + name);
        } catch (InstantiationException ie) {
            throw new PipeFilterException("Instantiation exception while building pump " + name);
        } catch (NullPointerException npe) {
            // Pump not registered
            throw new PipeFilterException("NullPointerException while building pump " + name);
        }
    }

    /**
     * This method infers the input type of a pump by reflection.
     *
     * @param name the name of the pump in the registry
     * @return the input type of the pump
     */
    public static String getPumpInputType(String name) {
        ParameterizedType t = (ParameterizedType) registeredPumps.get(name).getGenericInterfaces()[0];
        return t.getActualTypeArguments()[0].getTypeName();
    }

    /**
     * This method infers the output type of a pump by reflection.
     *
     * @param name the name of the pump in the registry
     * @return the output type of the pump
     */
    public static String getPumpOutputType(String name) {
        ParameterizedType t = (ParameterizedType) registeredPumps.get(name).getGenericInterfaces()[0];
        return t.getActualTypeArguments()[1].getTypeName();
    }

}
