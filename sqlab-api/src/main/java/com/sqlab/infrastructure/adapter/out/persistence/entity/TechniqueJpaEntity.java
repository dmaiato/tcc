package com.sqlab.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "techniques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechniqueJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "techniques")
    private Set<MissionJpaEntity> missions = new HashSet<>();
}
