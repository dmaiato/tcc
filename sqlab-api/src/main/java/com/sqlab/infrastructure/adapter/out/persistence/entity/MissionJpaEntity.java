package com.sqlab.infrastructure.adapter.out.persistence.entity;

import com.sqlab.domain.model.DifficultyLevel;
import com.sqlab.domain.model.Theme;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String briefing;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String objective;

    @Column(columnDefinition = "TEXT")
    private String hint;

    @Column(name = "ddl_script", nullable = false, columnDefinition = "TEXT")
    private String ddlScript;

    @Column(name = "dml_script", columnDefinition = "TEXT")
    private String dmlScript;

    @Column(columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> techniques;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(name = "expected_result", nullable = false, columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> expectedResult;

    @Column(nullable = false)
    private boolean ordered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DifficultyLevel difficulty;

    @Column(name = "scenario_id")
    private UUID scenarioId;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    private ScenarioJpaEntity scenario;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}