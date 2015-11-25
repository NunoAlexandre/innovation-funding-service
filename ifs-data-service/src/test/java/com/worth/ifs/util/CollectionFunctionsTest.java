package com.worth.ifs.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 *
 */
public class CollectionFunctionsTest {

    @Test
    public void test_flattenLists() {

        List<List<Integer>> deepList = asList(asList(1, 2, 3), asList(4, 5), asList(6));
        List<Integer> expectedFlatList = asList(1, 2, 3, 4, 5, 6);

        assertEquals(expectedFlatList, CollectionFunctions.flattenLists(deepList));
    }

    @Test
    public void test_flattenLists_nullSafe() {

        List<List<Integer>> deepList = asList(asList(1, 2, 3), null, asList(6));
        List<Integer> expectedFlatList = asList(1, 2, 3, 6);

        assertEquals(expectedFlatList, CollectionFunctions.flattenLists(deepList));
    }

    @Test
    public void test_flattenLists_emptyElements() {

        List<List<Integer>> deepList = asList(asList(1, 2, 3), asList(), asList(6));
        List<Integer> expectedFlatList = asList(1, 2, 3, 6);

        assertEquals(expectedFlatList, CollectionFunctions.flattenLists(deepList));
    }

    @Test
    public void test_flattenLists_allEmpty() {

        List<List<Integer>> deepList = asList(asList(), asList(), asList());
        List<Integer> expectedFlatList = asList();

        assertEquals(expectedFlatList, CollectionFunctions.flattenLists(deepList));
    }

    @Test
    public void test_combineLists() {

        List<Integer> list1 = asList(1, 2, 3);
        List<Integer> list2 = asList(4, 5);
        List<Integer> list3 = asList(6);

        List<Integer> expectedCombinedList = asList(1, 2, 3, 4, 5, 6);

        assertEquals(expectedCombinedList, CollectionFunctions.combineLists(list1, list2, list3));
    }

    @Test
    public void test_combineLists_nullSafe() {

        List<Integer> list1 = null;
        List<Integer> list2 = asList(4, 5);
        List<Integer> list3 = asList(6);

        List<Integer> expectedCombinedList = asList(4, 5, 6);

        assertEquals(expectedCombinedList, CollectionFunctions.combineLists(list1, list2, list3));
    }

    @Test
    public void test_combineLists_emptyElements() {

        List<Integer> list1 = asList();
        List<Integer> list2 = asList(4, 5);
        List<Integer> list3 = asList(6);

        List<Integer> expectedCombinedList = asList(4, 5, 6);

        assertEquals(expectedCombinedList, CollectionFunctions.combineLists(list1, list2, list3));
    }

    @Test
    public void test_forEachWithIndex() {

        final List<Integer> indicesSeen = new ArrayList<>();
        final List<String> valuesSeen = new ArrayList<>();
        final List<String> valuesToIterateOver = asList("string 1", "string 2", "string 3");

        CollectionFunctions.forEachWithIndex(valuesToIterateOver, (i, value) -> {
            indicesSeen.add(i);
            valuesSeen.add(value);
        });

        assertEquals(asList(0, 1, 2), indicesSeen);
        assertEquals(asList("string 1", "string 2", "string 3"), valuesSeen);
    }

    @Test
    public void test_forEachWithIndex_nullSafe() {

        final List<Integer> indicesSeen = new ArrayList<>();
        final List<String> valuesSeen = new ArrayList<>();
        final List<String> valuesToIterateOver = asList("string 1", null, "string 3");

        CollectionFunctions.forEachWithIndex(valuesToIterateOver, (i, value) -> {
            indicesSeen.add(i);
            valuesSeen.add(value);
        });

        assertEquals(asList(0, 1, 2), indicesSeen);
        assertEquals(asList("string 1", null, "string 3"), valuesSeen);
    }

    @Test
    public void test_forEachWithIndex_emptyValues() {

        final List<Integer> indicesSeen = new ArrayList<>();
        final List<String> valuesSeen = new ArrayList<>();
        final List<String> valuesToIterateOver = asList();

        CollectionFunctions.forEachWithIndex(valuesToIterateOver, (i, value) -> {
            indicesSeen.add(i);
            valuesSeen.add(value);
        });

        assertEquals(asList(), indicesSeen);
        assertEquals(asList(), valuesSeen);
    }

    @Test
    public void test_mapWithIndex() {

        final List<Integer> indicesSeen = new ArrayList<>();
        final List<String> valuesToIterateOver = asList("string 1", "string 2", "string 3");

        List<String> newStrings = CollectionFunctions.mapWithIndex(valuesToIterateOver, (i, value) -> {
            indicesSeen.add(i);
            return value + " changed";
        });

        assertEquals(asList(0, 1, 2), indicesSeen);
        assertEquals(asList("string 1 changed", "string 2 changed", "string 3 changed"), newStrings);
    }


    @Test
    public void test_mapWithIndex_nullSafe() {

        final List<Integer> indicesSeen = new ArrayList<>();
        final List<String> valuesToIterateOver = asList("string 1", null, "string 3");

        List<String> newStrings = CollectionFunctions.mapWithIndex(valuesToIterateOver, (i, value) -> {
            indicesSeen.add(i);
            return value != null ? value + " changed" : null;
        });

        assertEquals(asList(0, 1, 2), indicesSeen);
        assertEquals(asList("string 1 changed", null, "string 3 changed"), newStrings);
    }


    @Test
    public void test_mapWithIndex_emptyElements() {

        final List<Integer> indicesSeen = new ArrayList<>();
        final List<String> valuesToIterateOver = asList();

        List<String> newStrings = CollectionFunctions.mapWithIndex(valuesToIterateOver, (i, value) -> {
            indicesSeen.add(i);
            return value + " changed";
        });

        assertEquals(asList(), indicesSeen);
        assertEquals(asList(), newStrings);
    }

    @Test
    public void test_reverse() {

        List<String> originalList = asList("string 1", "another string 2", "string 3");
        List<String> reversed = CollectionFunctions.reverse(originalList);
        assertEquals(asList("string 3", "another string 2", "string 1"), reversed);
    }

    @Test
    public void test_reverse_nullElements() {

        List<String> originalList = asList(null, null, "string 3");
        List<String> reversed = CollectionFunctions.reverse(originalList);
        assertEquals(asList("string 3", null, null), reversed);
    }

    @Test
    public void test_reverse_nullSafe() {
        assertEquals(new ArrayList<>(), CollectionFunctions.reverse(null));
    }

    @Test
    public void test_reverse_empty() {
        assertEquals(new ArrayList<>(), CollectionFunctions.reverse(new ArrayList<>()));
    }

    @Test
    public void test_getOnlyElement() {
        assertEquals("hi there", CollectionFunctions.getOnlyElement(asList("hi there")));
    }

    @Test
    public void test_nullElement() {
        List<Object> nullElementList = new ArrayList<>();
        nullElementList.add(null);
        assertNull(CollectionFunctions.getOnlyElement(nullElementList));
    }

    @Test
    public void test_getOnlyElement_tooManyElements() {
        try {
            CollectionFunctions.getOnlyElement(asList("hi there", "goodbye!"));
            fail("Should have thrown an IllegalArgumentException as there were too many elements");
        } catch (IllegalArgumentException e) {
            // expected behaviour
        }
    }

    @Test
    public void test_getOnlyElement_notEnoughElements() {
        try {
            CollectionFunctions.getOnlyElement(asList());
            fail("Should have thrown an IllegalArgumentException as there weren't enough elements");
        } catch (IllegalArgumentException e) {
            // expected behaviour
        }
    }

    @Test
    public void test_getOnlyElement_notEnoughElements_nullList() {
        try {
            CollectionFunctions.getOnlyElement(null);
            fail("Should have thrown an IllegalArgumentException as there weren't enough elements (null list)");
        } catch (IllegalArgumentException e) {
            // expected behaviour
        }
    }
}
