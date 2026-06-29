package com.sqlab.infrastructure.adapter.out.persistence.spec;

import com.sqlab.infrastructure.adapter.out.persistence.entity.MissionJpaEntity;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class ScenarioSpecifications {

    public static Specification<ScenarioJpaEntity> withFilters(String name, String themeName) {
        return Specification.allOf(
                nameContains(name),
                themeEquals(themeName),
                enabledTrue(),
                withAllMissionsEnabled()
        );
    }

    private static Specification<ScenarioJpaEntity> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("title")), "%" + name.toLowerCase() + "%");
        };
    }

    private static Specification<ScenarioJpaEntity> themeEquals(String themeName) {
        return (root, query, cb) -> {
            if (themeName == null) return cb.conjunction();
            return cb.equal(cb.upper(root.get("theme").get("name")), themeName.toUpperCase());
        };
    }

    private static Specification<ScenarioJpaEntity> enabledTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("enabled"));
    }

    private static Specification<ScenarioJpaEntity> enabledFilter(Boolean enabled) {
        return (root, query, cb) -> {
            if (enabled == null) return cb.conjunction();
            return enabled ? cb.isTrue(root.get("enabled")) : cb.isFalse(root.get("enabled"));
        };
    }

    private static Specification<ScenarioJpaEntity> withAllMissionsEnabled() {
        return (root, query, cb) -> {
            Subquery<Integer> disabledSub = query.subquery(Integer.class);
            var disabledRoot = disabledSub.from(MissionJpaEntity.class);
            disabledSub.select(cb.literal(1))
                    .where(cb.and(
                            cb.equal(disabledRoot.get("scenario").get("id"), root.get("id")),
                            cb.isFalse(disabledRoot.get("enabled")),
                            cb.isNotNull(disabledRoot.get("scenario"))
                    ));

            Subquery<Integer> enabledSub = query.subquery(Integer.class);
            var enabledRoot = enabledSub.from(MissionJpaEntity.class);
            enabledSub.select(cb.literal(1))
                    .where(cb.and(
                            cb.equal(enabledRoot.get("scenario").get("id"), root.get("id")),
                            cb.isTrue(enabledRoot.get("enabled")),
                            cb.isNotNull(enabledRoot.get("scenario"))
                    ));

            return cb.and(
                    cb.not(cb.exists(disabledSub)),
                    cb.exists(enabledSub)
            );
        };
    }

    public static Specification<ScenarioJpaEntity> withFiltersAdmin(String name, String themeName, Boolean enabled) {
        return Specification.allOf(
                nameContains(name),
                themeEquals(themeName),
                enabledFilter(enabled)
        );
    }

    private ScenarioSpecifications() {
    }
}
