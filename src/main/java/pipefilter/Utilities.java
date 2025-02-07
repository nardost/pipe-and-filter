package pipefilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Nardos Tessema
 *
 * Utility methods
 */
public class Utilities {

    /**
     * The pipeline output map key set contains all numbers ranging
     * from the minimum to the maximum occurrence frequencies
     * even if there are no terms with some frequencies in the range.
     *
     * This method removes frequency-term map entries with empty lists as the values.
     */
    public static Map<Integer, List<String>> trim(Map<Integer, List<String>> original) {
        Map<Integer, List<String>> trimmed = new TreeMap<>(Collections.reverseOrder());
        for(int f : original.keySet()) {
            if(!original.get(f).isEmpty()) {
                trimmed.put(f, original.get(f));
            }
        }
        return trimmed;
    }

    /**
     * Return the n most commonly occurring terms.
     * If there are ties, sort in alphabetical order.
     *
     * This method is needed in case the data structure used
     * to store the output of the Sink is a HashMap. in which case
     * the keys of the output Map are sorted in natural order.
     *
     * If a TreeMap with reverse order comparator is used instead,
     * this method is not necessary as the key entries will already
     * have be sorted in reverse order.
     *
     * (1) Traverse to the last entry in the Map
     * (2) Iterate backwards times and collect all
     *     entries with non-empty list of terms
     * (3) Stop when n non-empty entries are collected
     *
     * @param n the number of frequencies to display.
     * @return a map with the n most commonly occurring terms
     */
    public static Map<Integer, List<String>> mostCommonTerms(
            Map<Integer, List<String>> frequencies,
            final int n) {

        final int maximumFrequency = frequencies
                .keySet()
                .stream()
                .max(Integer::compareTo)
                .orElse(0);

        final SortedMap<Integer, List<String>> mostFrequentTerms = new TreeMap<>(Collections.reverseOrder());
        int i = 0;
        do {
            final List<String> l = frequencies.get(maximumFrequency - i);
            /*
             * There might not be n frequency categories,
             * in which case l becomes null...
             */
            if(Objects.isNull(l)) {
                break;
            }
            /*
             * If the list is empty, skip it.
             */
            if(!l.isEmpty()) {
                /*
                 * Sort in alphabetical order
                 */
                Collections.sort(l);
                mostFrequentTerms.put(maximumFrequency - i, l);
            }
            i++;
        } while(mostFrequentTerms.size() < n);
        return mostFrequentTerms;
    }

    /**
     * A generic method that stringifies the contents of a collection.
     * The toString() method of the objects inside the collection is used.
     * @param collection the collection whose elements are stringified.
     * @param limit the maximum number of elements to print
     * @param <E> the type of objects in the collection.
     * @return elements of the collection listed as a string
     *
     * Example: { 1, 2, 3, 4, 5, 6 }
     */
    public static <E> String prettyPrint(Collection<E> collection, final int limit) {
        StringBuilder sb = new StringBuilder("{");
        collection.stream().limit(limit).map(t -> " " + t + ",").forEach(sb::append);
        if(sb.length() > 1) {
            sb.replace(sb.length() - 1, sb.length(), " ");
        }
        sb.append("}");
        return sb.toString();
    }
    /**
     * Same method as the overloaded counterpart but lists all
     * elements of the collection instead of a limited number.
     * @param collection
     * @param <E>
     * @return
     */
    public static <E> String prettyPrint(Collection<E> collection) {
        return prettyPrint(collection, collection.size());
    }

    public static <E> String prettyPrint(E[] array) {
        return prettyPrint(Arrays.asList(array));
    }

    public static String prettyPrint(int[] ints) {
        return prettyPrint(Arrays.asList(ints));
    }

    public static <E> String prettyPrintMap(Map<Integer,? extends Collection<E>> map) {
        StringBuilder sb = new StringBuilder();
        final int WIDTH = 5;
        map.forEach((k, v) -> {
            sb.append(Utilities.pad(k, WIDTH)).append(" -> ");
            sb.append(Utilities.prettyPrint(v));
            sb.append("\n");
        });
        return sb.toString();
    }

    /**
     * Right-pad an integer with
     * spaces for fixed column format.
     * @param x - the number to be padded with spaces.
     * @return The padded string
     */
    public static String pad(final int x, final int WIDTH) {
        return String.format("%1$" + WIDTH + "s", x);
    }

}
