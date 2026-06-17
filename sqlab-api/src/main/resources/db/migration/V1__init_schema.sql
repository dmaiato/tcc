-- V1__init_schema.sql
-- Schema completo normalizado (1NF, 2NF, 3NF, BCNF)
-- Junction table techniques → mission_techniques replaces TEXT[] (fixes 1NF)
-- themes como tabela separada garante integridade referencial (corrige 2NF)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    xp            INTEGER      NOT NULL DEFAULT 0,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_user_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_xp_non_negative CHECK (xp >= 0)
);

CREATE TABLE themes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(255),
    emoji       VARCHAR(10)
);

CREATE TABLE scenarios (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title          VARCHAR(100) NOT NULL,
    description    TEXT         NOT NULL,
    theme_id       UUID         NOT NULL REFERENCES themes(id) ON DELETE RESTRICT,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    required_level INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_required_level_non_negative CHECK (required_level >= 0)
);

CREATE TABLE techniques (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE missions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(100) NOT NULL,
    briefing        TEXT         NOT NULL,
    objective       TEXT         NOT NULL,
    hint            TEXT,
    ddl_script      TEXT         NOT NULL,
    dml_script      TEXT,
    xp_reward       INTEGER      NOT NULL DEFAULT 100,
    expected_result JSONB        NOT NULL,
    ordered         BOOLEAN      NOT NULL DEFAULT FALSE,
    theme_id        UUID         NOT NULL REFERENCES themes(id) ON DELETE RESTRICT,
    difficulty      VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    scenario_id     UUID         REFERENCES scenarios(id) ON DELETE RESTRICT,
    order_index     INTEGER,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_mission_difficulty CHECK (difficulty IN ('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT')),
    CONSTRAINT ck_xp_reward_positive CHECK (xp_reward > 0),
    CONSTRAINT ck_expected_result_is_array CHECK (jsonb_typeof(expected_result) = 'array'),
    CONSTRAINT ck_scenario_consistency CHECK (
        (scenario_id IS NULL AND order_index IS NULL)
        OR (scenario_id IS NOT NULL AND order_index IS NOT NULL)
    )
);

CREATE TABLE mission_techniques (
    mission_id   UUID NOT NULL REFERENCES missions(id) ON DELETE CASCADE,
    technique_id UUID NOT NULL REFERENCES techniques(id) ON DELETE RESTRICT,
    PRIMARY KEY (mission_id, technique_id)
);

CREATE TABLE progress (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mission_id   UUID    NOT NULL REFERENCES missions(id) ON DELETE CASCADE,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, mission_id),
    CONSTRAINT ck_completed_consistency
        CHECK ((completed AND completed_at IS NOT NULL) OR (NOT completed AND completed_at IS NULL))
);

CREATE INDEX idx_missions_scenario_id ON missions(scenario_id);
CREATE INDEX idx_missions_theme_difficulty ON missions(theme_id, difficulty);
CREATE INDEX idx_missions_enabled ON missions(enabled);
CREATE INDEX idx_expected_result_gin ON missions USING GIN (expected_result);
CREATE INDEX idx_scenarios_theme_id ON scenarios(theme_id);
CREATE INDEX idx_progress_user_id_completed ON progress(user_id, completed);
CREATE INDEX idx_mission_techniques_technique_id ON mission_techniques(technique_id);
