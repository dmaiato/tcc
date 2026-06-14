package com.sqlab.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpectedTupleTest {

    @SafeVarargs
    private static List<Map<String, Object>> rows(Map<String, Object>... entries) {
        return List.of(entries);
    }

    private static Map<String, Object> map(Object... entries) {
        if (entries.length % 2 != 0) throw new IllegalArgumentException();
        var map = new java.util.LinkedHashMap<String, Object>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put((String) entries[i], entries[i + 1]);
        }
        return map;
    }

    @Nested
    class Construction {

        @Test
        void constructorCopiesRows() {
            var mutable = new java.util.ArrayList<>(rows(map("a", 1)));
            var tuple = new ExpectedTuple(mutable);
            mutable.clear();
            assertFalse(tuple.rows().isEmpty());
        }

        @Test
        void rowsIsImmutable() {
            var tuple = new ExpectedTuple(rows(map("a", 1)));
            assertThrows(UnsupportedOperationException.class,
                    () -> tuple.rows().add(map("b", 2)));
        }
    }

    @Nested
    class MatchesUnordered {

        @Test
        void correctWhenAllRowsMatch() {
            var tuple = new ExpectedTuple(rows(
                    map("id", 1, "name", "Alice"),
                    map("id", 2, "name", "Bob")
            ));
            var submitted = rows(
                    map("id", 2, "name", "Bob"),
                    map("id", 1, "name", "Alice")
            );
            assertTrue(tuple.matchesUnordered(submitted).correct());
        }

        @Test
        void failsWhenRowCountDiffers() {
            var tuple = new ExpectedTuple(rows(map("id", 1)));
            var submitted = List.<Map<String, Object>>of();
            var result = tuple.matchesUnordered(submitted);
            assertFalse(result.correct());
            assertTrue(result.feedback().contains("Expected 1 row"));
        }

        @Test
        void failsWhenValuesDontMatch() {
            var tuple = new ExpectedTuple(rows(map("id", 1, "name", "Alice")));
            var submitted = rows(map("id", 1, "name", "Bob"));
            var result = tuple.matchesUnordered(submitted);
            assertFalse(result.correct());
            assertTrue(result.feedback().contains("Values don't match"));
        }

        @Test
        void failsOnMissingColumn() {
            var tuple = new ExpectedTuple(rows(map("id", 1, "name", "Alice")));
            var submitted = rows(map("id", 1));
            var result = tuple.matchesUnordered(submitted);
            assertFalse(result.correct());
            assertTrue(result.feedback().contains("Missing column"));
        }

        @Test
        void failsOnExtraColumn() {
            var tuple = new ExpectedTuple(rows(map("id", 1)));
            var submitted = rows(map("id", 1, "extra", "x"));
            var result = tuple.matchesUnordered(submitted);
            assertFalse(result.correct());
            assertTrue(result.feedback().contains("Unexpected column"));
        }

        @Test
        void numericTypesCompareCorrectly() {
            var tuple = new ExpectedTuple(rows(map("v", 42)));
            var submitted = rows(map("v", 42L));
            assertTrue(tuple.matchesUnordered(submitted).correct());
        }

        @Test
        void integerAndDecimalStringAreEqual() {
            var tuple = new ExpectedTuple(rows(map("v", 100)));
            var submitted = rows(map("v", "100"));
            assertTrue(tuple.matchesUnordered(submitted).correct());
        }

        @Test
        void emptyRowsMatch() {
            var tuple = new ExpectedTuple(List.of());
            var submitted = List.<Map<String, Object>>of();
            assertTrue(tuple.matchesUnordered(submitted).correct());
        }

        @Test
        void emptyRowsDoNotMatchNonEmpty() {
            var tuple = new ExpectedTuple(List.of());
            var submitted = rows(map("id", 1));
            assertFalse(tuple.matchesUnordered(submitted).correct());
        }

        @Test
        void singleRowMatch() {
            var tuple = new ExpectedTuple(rows(map("id", 1, "val", "x")));
            var submitted = rows(map("id", 1, "val", "x"));
            assertTrue(tuple.matchesUnordered(submitted).correct());
        }
    }

    @Nested
    class MatchesOrdered {

        @Test
        void correctWhenExactOrder() {
            var tuple = new ExpectedTuple(rows(
                    map("id", 1, "name", "Alice"),
                    map("id", 2, "name", "Bob")
            ));
            var submitted = rows(
                    map("id", 1, "name", "Alice"),
                    map("id", 2, "name", "Bob")
            );
            assertTrue(tuple.matchesOrdered(submitted).correct());
        }

        @Test
        void failsWhenWrongOrder() {
            var tuple = new ExpectedTuple(rows(
                    map("id", 1, "name", "Alice"),
                    map("id", 2, "name", "Bob")
            ));
            var submitted = rows(
                    map("id", 2, "name", "Bob"),
                    map("id", 1, "name", "Alice")
            );
            var result = tuple.matchesOrdered(submitted);
            assertFalse(result.correct());
            assertTrue(result.feedback().contains("wrong order"));
        }

        @Test
        void failsWhenCompletelyDifferent() {
            var tuple = new ExpectedTuple(rows(
                    map("id", 1, "name", "Alice"),
                    map("id", 2, "name", "Bob")
            ));
            var submitted = rows(
                    map("id", 99, "name", "Zebra"),
                    map("id", 42, "name", "Yak")
            );
            var result = tuple.matchesOrdered(submitted);
            assertFalse(result.correct());
        }

        @Test
        void failsWhenRowCountDiffers() {
            var tuple = new ExpectedTuple(rows(map("id", 1)));
            var submitted = List.<Map<String, Object>>of();
            assertFalse(tuple.matchesOrdered(submitted).correct());
        }

        @Test
        void givesPositionalFeedback() {
            var tuple = new ExpectedTuple(rows(
                    map("id", 1, "name", "Alice"),
                    map("id", 2, "name", "Bob")
            ));
            var submitted = rows(
                    map("id", 1, "name", "Alice"),
                    map("id", 2, "name", "WRONG")
            );
            var result = tuple.matchesOrdered(submitted);
            assertFalse(result.correct());
            assertTrue(result.feedback().contains("Row 2"));
            assertTrue(result.feedback().contains("WRONG"));
        }
    }

    @Nested
    class NumericComparison {

        @Test
        void integerAndLongAreEqual() {
            assertTrue(matchSingleRow(map("v", 42), map("v", 42L)));
        }

        @Test
        void integerAndBigDecimalAreEqual() {
            assertTrue(matchSingleRow(map("v", 42), map("v", new java.math.BigDecimal("42"))));
        }

        @Test
        void integerAndStringNumberAreEqual() {
            assertTrue(matchSingleRow(map("v", 100), map("v", "100")));
        }

        @Test
        void doublePrecisionMatters() {
            var result = matchSingleRowResult(map("v", 0.1 + 0.2), map("v", 0.3));
            assertFalse(result.correct(), "0.1+0.2 = " + (0.1 + 0.2) + " != 0.3 in double precision");
        }

        @Test
        void stringVsIntegerNotEqual() {
            assertFalse(matchSingleRow(map("v", "42"), map("v", 43)));
        }

        @Test
        void nullVsValueNotEqual() {
            assertFalse(matchSingleRow(map("v", (Object) null), map("v", 1)));
        }

        @Test
        void bothNullAreEqual() {
            assertTrue(matchSingleRow(map("v", (Object) null), map("v", (Object) null)));
        }

        private boolean matchSingleRow(Map<String, Object> expected, Map<String, Object> actual) {
            return matchSingleRowResult(expected, actual).correct();
        }

        private ValidationResult matchSingleRowResult(Map<String, Object> expected, Map<String, Object> actual) {
            return new ExpectedTuple(List.of(expected)).matchesUnordered(List.of(actual));
        }
    }

    @Nested
    class EqualsAndHashCode {

        @Test
        void equivalentTuplesAreEqual() {
            var a = new ExpectedTuple(rows(map("x", 1)));
            var b = new ExpectedTuple(rows(map("x", 1)));
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void differentTuplesNotEqual() {
            var a = new ExpectedTuple(rows(map("x", 1)));
            var b = new ExpectedTuple(rows(map("x", 2)));
            assertNotEquals(a, b);
        }
    }

    @Nested
    class ToString {

        @Test
        void containsRows() {
            var tuple = new ExpectedTuple(rows(map("a", 1)));
            var str = tuple.toString();
            assertTrue(str.contains("a"));
            assertTrue(str.contains("1"));
        }
    }
}
