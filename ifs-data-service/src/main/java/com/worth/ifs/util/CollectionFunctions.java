package com.worth.ifs.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Utility class to provide useful reusable Functions around Collections throughout the codebase
 */
public final class CollectionFunctions {

    private CollectionFunctions() {
    }

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CollectionFunctions.class);

    /**
     * Flatten the given 2-dimensional List into a 1-dimensional List
     *
     * @param lists
     * @param <T>
     * @return 1-dimensional list
     */
    public static <T> List<T> flattenLists(List<List<T>> lists) {
        return lists.stream()
                .filter(l -> l != null)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    /**
     * Combine the given Lists into a single List
     *
     * @param lists
     * @param <T>
     * @return combined List containing the elements of the given Lists, in the original list order
     */
    @SafeVarargs
    public static <T> List<T> combineLists(List<T>... lists) {
        return doCombineLists(lists);
    }

    private static <T> List<T> doCombineLists(List<T>... lists) {
        List<T> combinedList = new ArrayList<>();

        for (List<T> list : lists) {
            if (list != null && !list.isEmpty()) {
                combinedList.addAll(list);
            }
        }

        return combinedList;
    }

    /**
     * Combine the given List and varargs array into a single List
     *
     * @param list
     * @param otherElements
     * @param <T>
     * @return combined List containing the elements of the given Lists, in the original list order
     */
    @SafeVarargs
    public static <T> List<T> combineLists(List<T> list, T... otherElements) {
        List<T> startingList = new ArrayList<>();

        if (list != null && !list.isEmpty()) {
            startingList.addAll(list);
        }

        return doCombineLists(startingList, asList(otherElements));
    }

    /**
     * Combine the given element and varargs array into a single List
     *
     * @param firstElement
     * @param otherElements
     * @param <T>
     * @return combined List containing the elements of the given Lists, in the original list order
     */
    @SafeVarargs
    public static <T> List<T> combineLists(T firstElement, T... otherElements) {
        return doCombineLists(singletonList(firstElement), asList(otherElements));
    }

    /**
     * Combine the given element and list into a single List
     *
     * @param firstElement
     * @param otherElements
     * @param <T>
     * @return combined List containing the elements of the given Lists, in the original list order
     */
    public static <T> List<T> combineLists(T firstElement, List<T> otherElements) {
        return doCombineLists(singletonList(firstElement), otherElements);
    }


    /**
     * Provides a forEach method as per the default List.forEach() method, but with the addition of having an index
     * provided as well
     *
     * @param consumer
     * @param <T>
     * @return a Consumer that can then be applied to Lists to apply the consumer function provided
     */
    public static <T> Consumer<List<T>> forEachConsumer(BiConsumer<Integer, T> consumer) {
        return list -> IntStream.range(0, list.size()).forEach(i -> consumer.accept(i, list.get(i)));
    }

    /**
     * Provides a forEach method as per the default List.forEach() method, but with the addition of having an index
     * provided as well
     *
     * @param consumer
     * @param <T>
     */
    public static <T> void forEachWithIndex(List<T> list, BiConsumer<Integer, T> consumer) {
        forEachConsumer(consumer).accept(list);
    }

    /**
     * Provides a forEach method as per the default List.forEach() method, but with the addition of having an index
     * provided as well and the ability to return values
     *
     * @param function
     * @param <T>
     * @return a Function that can then be applied to Lists to apply the function provided in order to produce a result
     */
    public static <T, R> Function<List<T>, List<R>> forEachFunction(BiFunction<Integer, T, R> function) {
        return list -> IntStream.range(0, list.size()).mapToObj(i -> function.apply(i, list.get(i))).collect(toList());
    }

    /**
     * Provides a forEach method as per the default List.forEach() method, but with the addition of having an index
     * provided as well and the ability to return values
     *
     * @param function
     * @param <T>
     * @return a Function that can then be applied to Lists to apply the function provided in order to produce a result
     */
    public static <T, R> List<R> mapWithIndex(List<T> list, BiFunction<Integer, T, R> function) {
        return forEachFunction(function).apply(list);
    }

    /**
     * Returns a new List with the original list's contents reversed.  Leaves the original list intact.  Returns
     * an empty list if a null or empty list is supplied.
     *
     * @param original
     * @param <T>
     * @return
     */
    public static <T> List<T> reverse(List<T> original) {
        List<T> reversed = original != null ? new ArrayList<>(original) : new ArrayList<>();
        Collections.reverse(reversed);
        return reversed;
    }

    /**
     * Return the one and only element in the given list, otherwise throw an IllegalArgumentException
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T getOnlyElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("No elements were available in list " + list + ", so cannot return only element");
        }

        if (list.size() > 1) {
            throw new IllegalArgumentException("More than one element was available in list " + list + ", so cannot return only element");
        }

        return list.get(0);
    }

    /**
     * Return the one and only element in the given list, or empty if none exists
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> Optional<T> getOnlyElementOrEmpty(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }

        if (list.size() > 1) {
            throw new IllegalArgumentException("More than one element was available in list " + list + ", so cannot return only element");
        }

        return Optional.of(list.get(0));
    }

    /**
     * A simple wrapper around a 1-stage mapping function, to remove boilerplate from production code
     *
     * @param list
     * @param mappingFn
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> simpleMap(List<T> list, Function<T, R> mappingFn) {
        if (list == null || list.isEmpty()) {
            return emptyList();
        }
        return list.stream().map(mappingFn).collect(toList());
    }

    /**
     * A simple wrapper around a 1-stage mapping function, to remove boilerplate from production code
     *
     * @param list
     * @param mappingFn
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Set<R> simpleMapSet(Set<T> list, Function<T, R> mappingFn) {
        if (null == list || list.isEmpty()) {
            return Collections.emptySet();
        }
        return list.stream().map(mappingFn).collect(toSet());
    }

    /**
     * A simple wrapper around a 1-stage mapping function, to remove boilerplate from production code
     *
     * @param set
     * @param mappingFn
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> simpleMap(Set<T> set, Function<T, R> mappingFn) {
        if (set == null || set.isEmpty()) {
            return emptyList();
        }
        return set.stream().map(mappingFn).collect(toList());
    }

    /**
     * A shortcut to sort a list by natural order
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends Comparable> List<T> sort(Collection<T> list) {
        if (list == null || list.isEmpty()) {
            return emptyList();
        }

        List<T> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * A shortcut to sort a list by natural order
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List<T> sort(Collection<T> list, Comparator<T> comparator) {
        if (list == null || list.isEmpty()) {
            return emptyList();
        }

        List<T> sorted = new ArrayList<>(list);
        sorted.sort(comparator);
        return sorted;
    }

    /**
     * A andOnSuccess collector that preserves order
     *
     * @param keyMapper
     * @param valueMapper
     * @param <T>
     * @param <K>
     * @param <U>
     * @return
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        }, LinkedHashMap::new);
    }

    /**
     * A simple function to convert a list of items to a Map given a key generating function and a value generating function
     *
     * @param keyMapper
     * @param valueMapper
     * @param <T>
     * @param <K>
     * @param <U>
     * @return
     */
    public static <T, K, U> Map<K, U> simpleToMap(
            List<T> list,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {

        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        return list.stream().collect(toMap(keyMapper, valueMapper));
    }

    /**
     * A simple function to convert a list of items to a Map of keys against the original elements themselves, given a key generating function
     *
     * @param keyMapper
     * @param <T>
     * @param <K>
     * @return
     */
    public static <T, K> Map<K, T> simpleToMap(
            List<T> list,
            Function<? super T, ? extends K> keyMapper) {

        return simpleToMap(list, keyMapper, identity());
    }

    /**
     * A collector that maps a collection of pairs into a andOnSuccess.
     *
     * @param <R>
     * @param <T>
     * @return
     */
    public static <R, T> Map<R, T> pairsToMap(List<Pair<R, T>> pairs) {
        return simpleToMap(pairs, Pair::getKey, Pair::getValue);
    }

    /**
     * A collector that maps a collection of pairs into a andOnSuccess.
     *
     * @param <R>
     * @param <T>
     * @return
     */
    public static <R, T> Collector<Pair<R, T>, ?, Map<R, T>> pairsToMapCollector() {
        return toMap(Pair::getLeft, Pair::getRight);
    }

    /**
     * Function that takes a andOnSuccess entry and returns the value. Useful for collecting over collections of entry sets
     *
     * @param <R>
     * @param <T>
     * @return
     */
    public static <R, T> Function<Entry<R, T>, T> mapEntryValue() {
        return Entry::getValue;
    }

    /**
     * A simple wrapper around a 1-stage filter function, to remove boilerplate from production code
     *
     * @param list
     * @param filterFn
     * @param <T>
     * @return
     */
    public static <T> List<T> simpleFilter(Collection<T> list, Predicate<T> filterFn) {
        if (list == null || list.isEmpty()) {
            return emptyList();
        }
        return list.stream().filter(filterFn).collect(toList());
    }

    /**
     * A simple wrapper around a NEGATED 1-stage filter function, to remove boilerplate from production code
     *
     * @param list
     * @param filterFn
     * @param <T>
     * @return
     */
    public static <T> List<T> simpleFilterNot(List<T> list, Predicate<T> filterFn) {
        return simpleFilter(list, element -> !filterFn.test(element));
    }

    /**
     * A simple wrapper around a 1-stage filter function, to remove boilerplate from production code
     *
     * @param list
     * @param filterFn
     * @param <T>
     * @return
     */
    public static <T> Optional<T> simpleFindFirst(List<T> list, Predicate<T> filterFn) {
        return simpleFilter(list, filterFn).stream().findFirst();
    }

    /**
     * A simple wrapper around a String joining function.  Returns a string of the given list, separated by the given
     * joinString
     *
     * @param list
     * @param joinString
     * @param <T>
     * @return
     */
    public static <T> String simpleJoiner(List<T> list, String joinString) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return list.stream().map(element -> element != null ? element.toString() : "").collect(joining(joinString));
    }

    /**
     * Return the one and only element from the list supplied, or throw an IllegalArgumentException
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T onlyElement(List<T> list) {
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new IllegalArgumentException("Only expected a single element, but found " + list.size() + " - " + list);
    }

    /**
     * Return the one and only element from the list supplied, or return null
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T onlyElementOrNull(List<T> list) {
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Convert the given varargs to a LinkedHashSet
     *
     * @param items
     * @param <T>
     * @return
     */
    public static <T> Set<T> asLinkedSet(T... items) {
        LinkedHashSet<T> set = new LinkedHashSet<>();
        asList(items).forEach(set::add);
        return set;
    }

    /**
     * Given a list, this method will return a new list with the element at the given index removed
     *
     * @param list
     * @param index
     * @param <T>
     * @return
     */
    public static <T> List<T> removeElement(List<T> list, int index) {
        List<T> copy = new ArrayList<>(list);
        copy.remove(index);
        return copy;
    }

    /**
     * Given a list, this method will remove any duplicate entries from it and return the list in the same order as it was given
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List<T> removeDuplicates(List<T> list) {
        Set<T> set = new LinkedHashSet<>(list);
        return new ArrayList<>(set);
    }

    /**
     * Partitions a list based upon a given predicate, and returns the two lists in a Pair, with the "true" list being the
     * key (left) and the "false" list being the value (right)
     *
     * @param list
     * @param test
     * @param <T>
     * @return
     */
    public static <T> Pair<List<T>, List<T>> simplePartition(List<T> list, Predicate<T> test) {

        if (list == null || list.isEmpty()) {
            return Pair.of(emptyList(), emptyList());
        }

        Map<Boolean, List<T>> partitioned = list.stream().collect(partitioningBy(test));
        return Pair.of(partitioned.get(true), partitioned.get(false));
    }

    /**
     * Wrap a {@link BinaryOperator} with null checks
     * @param notNullSafe
     * @param <T>
     * @return
     */
    public static <T> BinaryOperator<T> nullSafe(final BinaryOperator<T> notNullSafe) {
        return (t1, t2) -> {
            if (t1 != null && t2 != null) {
                return notNullSafe.apply(t1, t2);
            } else if (t1 != null) {
                return t1;
            } else if (t2 != null) {
                return t2;
            }
            return null;
        };
    }

    /**
     * Given a list of elements, this method will find all possible permutations of those elements.
     * <p>
     * E.g. given (1, 2, 3), possible permutations are (1, 2, 3), (2, 1, 3), (3, 1, 2) etc...
     *
     * @param excludedWords
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> findPermutations(List<T> excludedWords) {
        List<List<List<T>>> allPermutations = mapWithIndex(excludedWords, (i, currentWord) -> findPermutations(emptyList(), currentWord, removeElement(excludedWords, i)));
        return flattenLists(allPermutations);
    }

    private static <T> List<List<T>> findPermutations(List<T> permutationStringSoFar, T currentWord, List<T> remainingWords) {

        List<T> newPermutationStringSoFar = combineLists(permutationStringSoFar, currentWord);

        if (remainingWords.isEmpty()) {
            return singletonList(newPermutationStringSoFar);
        }

        List<List<List<T>>> furtherPermutations = mapWithIndex(remainingWords, (i, remainingWord) -> findPermutations(newPermutationStringSoFar, remainingWord, removeElement(remainingWords, i)));
        return flattenLists(furtherPermutations);
    }

}