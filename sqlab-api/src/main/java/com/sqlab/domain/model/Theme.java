package com.sqlab.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class Theme {
    private final UUID id;
    private final String name;
    private String description;
    private String emoji;
}
