package com.sqlab.infrastructure.adapter.out.persistence.spec;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class MissionSpecifications {

    public static Specification<MissionJpaEntity> withFilters(
            String name, String themeName, DifficultyLevel difficulty, String scenarioScope) {
        return Specification.allOf(
                nameContains(name),
                themeEquals(themeName),
                difficultyEquals(difficulty),
                scenarioScopeFilter(scenarioScope),
                enabledTrue(),
                withoutDisabledScenarios()
        );
    }

    private static Specification<MissionJpaEntity> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("title")), "%" + name.toLowerCase() + "%");
        };
    }

    private static Specification<MissionJpaEntity> themeEquals(String themeName) {
        return (root, query, cb) -> {
            if (themeName == null) return cb.conjunction();
            return cb.equal(cb.upper(root.get("theme").get("name")), themeName.toUpperCase());
        };
    }

    private static Specification<MissionJpaEntity> difficultyEquals(DifficultyLevel difficulty) {
        return (root, query, cb) -> {
            if (difficulty == null) return cb.conjunction();
            return cb.equal(root.get("difficulty"), difficulty);
        };
    }

    private static Specification<MissionJpaEntity> scenarioScopeFilter(String scope) {
        return (root, query, cb) -> {
            if (scope == null || "ALL".equalsIgnoreCase(scope)) return cb.conjunction();
            if ("IN_SCENARIO".equalsIgnoreCase(scope)) return cb.isNotNull(root.get("scenario"));
            if ("STANDALONE".equalsIgnoreCase(scope)) return cb.isNull(root.get("scenario"));
            return cb.conjunction();
        };
    }

    private static Specification<MissionJpaEntity> enabledTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("enabled"));
    }

    private static Specification<MissionJpaEntity> enabledFilter(Boolean enabled) {
        return (root, query, cb) -> {
            if (enabled == null) return cb.conjunction();
            return enabled ? cb.isTrue(root.get("enabled")) : cb.isFalse(root.get("enabled"));
        };
    }

    private static Specification<MissionJpaEntity> withoutDisabledScenarios() {
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            var subRoot = subquery.from(MissionJpaEntity.class);
            subquery.select(subRoot.get("scenario").get("id"))
                    .where(cb.and(
                            cb.isFalse(subRoot.get("enabled")),
                            cb.isNotNull(subRoot.get("scenario"))
                    ));
            return cb.or(
                    cb.isNull(root.get("scenario")),
                    cb.in(root.get("scenario").get("id")).value(subquery).not()
            );
        };
    }

    public static Specification<MissionJpaEntity> withFiltersAdmin(
            String name, String themeName, DifficultyLevel difficulty, String scenarioScope, Boolean enabled) {
        return Specification.allOf(
                nameContains(name),
                themeEquals(themeName),
                difficultyEquals(difficulty),
                scenarioScopeFilter(scenarioScope),
                enabledFilter(enabled)
        );
    }

    private MissionSpecifications() {
    }
}
