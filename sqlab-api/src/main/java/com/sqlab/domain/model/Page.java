package com.sqlab.domain.model;

import java.util.List;

public record Page<T>(List<T> content, int totalElements, int totalPages, int number, int size) {

    public boolean hasNext() {
        return number < totalPages - 1;
    }
}
