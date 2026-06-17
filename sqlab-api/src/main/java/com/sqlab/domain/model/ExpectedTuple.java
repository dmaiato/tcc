package com.sqlab.domain.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ExpectedTuple(List<Map<String, Object>> rows) {

    public ExpectedTuple(List<Map<String, Object>> rows) {
        this.rows = List.copyOf(rows);
    }

    public ValidationResult matchesUnordered(List<Map<String, Object>> submitted) {
        if (rows.size() != submitted.size()) {
            return ValidationResult.failed(
                    "Expected " + rows.size() + " row" + (rows.size() != 1 ? "s" : "") +
                    ", got " + submitted.size()
            );
        }

        ValidationResult columnCheck = checkColumns(submitted);
        if (columnCheck != null) return columnCheck;

        boolean allMatch = submitted.stream().allMatch(candidate ->
                rows.stream().anyMatch(expected -> mapsEqual(expected, candidate))
        );

        if (!allMatch) {
            return ValidationResult.failed("Values don't match expected result");
        }

        return ValidationResult.CORRECT;
    }

    public ValidationResult matchesOrdered(List<Map<String, Object>> submitted) {
        if (rows.size() != submitted.size()) {
            return ValidationResult.failed(
                    "Expected " + rows.size() + " row" + (rows.size() != 1 ? "s" : "") +
                    ", got " + submitted.size()
            );
        }

        ValidationResult columnCheck = checkColumns(submitted);
        if (columnCheck != null) return columnCheck;

        boolean positionalMatch = true;
        for (int i = 0; i < rows.size(); i++) {
            if (!mapsEqual(rows.get(i), submitted.get(i))) {
                positionalMatch = false;
                break;
            }
        }

        if (positionalMatch) {
            for (int i = 0; i < rows.size(); i++) {
                ValidationResult rowCheck = checkRow(i, rows.get(i), submitted.get(i));
                if (rowCheck != null) return rowCheck;
            }
            return ValidationResult.CORRECT;
        }

        boolean allDataMatches = submitted.stream().allMatch(candidate ->
                rows.stream().anyMatch(expected -> mapsEqual(expected, candidate))
        );
        if (allDataMatches) {
            return ValidationResult.failed("Rows are correct but in wrong order");
        }

        for (int i = 0; i < rows.size(); i++) {
            ValidationResult rowCheck = checkRow(i, rows.get(i), submitted.get(i));
            if (rowCheck != null) return rowCheck;
        }

        return ValidationResult.CORRECT;
    }

    private ValidationResult checkColumns(List<Map<String, Object>> submitted) {
        if (rows.isEmpty() || submitted.isEmpty()) return null;

        Set<String> expectedKeys = rows.get(0).keySet();
        Set<String> actualKeys = submitted.get(0).keySet();

        Set<String> missingKeys = new HashSet<>(expectedKeys);
        missingKeys.removeAll(actualKeys);
        if (!missingKeys.isEmpty()) {
            return ValidationResult.failed(
                    "Missing column" + (missingKeys.size() != 1 ? "s" : "") + ": " +
                    String.join(", ", missingKeys)
            );
        }

        Set<String> extraKeys = new HashSet<>(actualKeys);
        extraKeys.removeAll(expectedKeys);
        if (!extraKeys.isEmpty()) {
            return ValidationResult.failed(
                    "Unexpected column" + (extraKeys.size() != 1 ? "s" : "") + ": " +
                    String.join(", ", extraKeys)
            );
        }

        return null;
    }

    private ValidationResult checkRow(int index, Map<String, Object> expected, Map<String, Object> actual) {
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            if (!valuesEqual(entry.getValue(), actual.get(entry.getKey()))) {
                return ValidationResult.failed(
                        "Row " + (index + 1) + ": column '" + entry.getKey() +
                        "' expected '" + entry.getValue() + "', got '" +
                        actual.get(entry.getKey()) + "'"
                );
            }
        }
        return null;
    }

    private static boolean mapsEqual(Map<String, Object> a, Map<String, Object> b) {
        if (a.size() != b.size()) return false;
        for (Map.Entry<String, Object> entry : a.entrySet()) {
            if (!valuesEqual(entry.getValue(), b.get(entry.getKey()))) return false;
        }
        return true;
    }

    private static boolean valuesEqual(Object expected, Object actual) {
        if (expected == actual) return true;
        if (expected == null || actual == null) return false;

        BigDecimal eNum = toBigDecimal(expected);
        BigDecimal aNum = toBigDecimal(actual);
        if (eNum != null && aNum != null) {
            return eNum.compareTo(aNum) == 0;
        }

        return expected.equals(actual);
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        if (value instanceof String s) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
