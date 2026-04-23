package com.sqlab.domain.model;

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
                rows.stream().anyMatch(expected -> expected.equals(candidate))
        );
    }

    /**
     * Validação estrutural considerando ordem.
     * Usada em missões que exigem ORDER BY.
     */
    public boolean matchesOrdered(List<Map<String, Object>> submitted) {
        return rows.equals(submitted);
    }
}