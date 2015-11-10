package com.worth.ifs.util;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 *
 */
public class EitherTest {

    @Test
    public void test_leftProjection() {

        Either<String, Double> left = Either.left("a left value");
        assertTrue(left.isLeft());
        assertFalse(left.isRight());
        assertEquals("a left value", left.getLeft());

        try {
            left.getRight();
            fail("Should not be able to retrieve a Right value from a Left");
        } catch (NoSuchElementException e) {
            // expected behaviour
        }
    }

    @Test
    public void test_rightProjection() {

        Either<String, Double> right = Either.right(15.6);
        assertTrue(right.isRight());
        assertFalse(right.isLeft());
        assertEquals(Double.valueOf(15.6), right.getRight());

        try {
            right.getLeft();
            fail("Should not be able to retrieve a Left value from a Right");
        } catch (NoSuchElementException e) {
            // expected behaviour
        }
    }

    @Test
    public void test_mapOverLeftProjection_shouldReturnOriginalLeftValue() {

        Either<String, Double> left = Either.left("original left value, left unmapped");
        assertEquals(Either.left("original left value, left unmapped"), left.map(rightValue -> Either.right(rightValue + 1)));
    }

    @Test
    public void test_mapOverRightProjection_shouldReturnMappedValue_ifMappedValueIsRight() {

        Either<String, Double> right = Either.right(15.6);
        assertEquals(Either.right(16.6), right.map(rightValue -> Either.right(rightValue + 1)));
    }


    @Test
    public void test_mapOverRightProjection_shouldReturnMappedValue_ifMappedValueIsLeft() {

        Either<String, Double> right = Either.right(15.6);
        assertEquals(Either.left("The mapped left value"), right.map(rightValue -> Either.left("The mapped left value")));
    }
}
