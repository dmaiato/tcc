package com.sqlab.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Representa o resultado esperado de uma missão como uma lista ordenada de tuplas.
 * A ordem é relevante para missões que exigem ORDER BY.
 */
public record ExpectedTuple(List<Map<String, Object>> rows) {

    /**
     * Validação estrutural sem considerar ordem.
     * Usada em missões de consulta simples (SELECT, JOIN, etc.).
     */
    public boolean matchesUnordered(List<Map<String, Object>> submitted) {
        if (rows.size() != submitted.size()) return false;
        return submitted.stream().allMatch(candidate ->
                rows.stream().anyMatch(expected -> mapsEqual(expected, candidate))
        );
    }

    /**
     * Validação estrutural considerando ordem.
     * Usada em missões que exigem ORDER BY.
     */
    public boolean matchesOrdered(List<Map<String, Object>> submitted) {
        if (rows.size() != submitted.size()) return false;
        for (int i = 0; i < rows.size(); i++) {
            if (!mapsEqual(rows.get(i), submitted.get(i))) return false;
        }
        return true;
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