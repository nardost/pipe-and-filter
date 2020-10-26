package pipefilter;

import pipefilter.filter.Data;
import pipefilter.pipe.Pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static pipefilter.config.Configuration.SENTINEL_VALUE;

public class TestUtilities {

    public static void loadArrayIntoPipe(Pipe<String> pipe, String[] words) {
        Stream.of(words).forEach(word -> {
            try {
                pipe.put(word);
            } catch(InterruptedException ignored) {
            }
        });
    }

    /**
     * Transforms a string pipe into a string array
     * @param pipe the pipe whose contents will be returened as array
     * @return an array of strings
     */
    public static String[] getPipeContentAsArray(Pipe<String> pipe) {
        /*
         * take out the data from the output pipe
         * as a string of comma separated terms
         */
        StringBuilder result = new StringBuilder();
        String s = "";
        do {
            try {
                s = pipe.take();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            result.append(s).append(",");

        } while (!s.equals(SENTINEL_VALUE));
        /*
         * remove the last comma
         */
        result.replace(result.length() - 1, result.length() - 1, "");
        /*
         * split the string by comma
         */
        return result.toString().split(",");
    }

    /**
     * Gets items in a pipe as an array
     * @param pipe contains Data items
     * @return array of Data items
     */
    public static Data[] pipeToArrayOfData(Pipe<?> pipe) {
        List<Data> list = new ArrayList<>();
        Data item = null;
        do {
            try {
                item = (Data) pipe.take();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            list.add(item);
        } while(!Objects.requireNonNull(item).isSentinelValue());
        return list.toArray(new Data[0]);
    }
}
