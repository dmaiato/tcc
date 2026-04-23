-- V1__init_schema.sql

CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    xp            INTEGER      NOT NULL DEFAULT 0,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE missions
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    title           VARCHAR(100) NOT NULL,
    briefing        TEXT         NOT NULL,
    ddl_script      TEXT         NOT NULL,
    dml_script      TEXT,
    techniques      TEXT[]       NOT NULL DEFAULT '{}',
    xp_reward       INTEGER      NOT NULL DEFAULT 100,
    expected_result JSONB        NOT NULL,
    ordered         BOOLEAN      NOT NULL DEFAULT FALSE,
    theme           VARCHAR(20)  NOT NULL,
    difficulty      VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE progress
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    mission_id   UUID    NOT NULL REFERENCES missions (id) ON DELETE CASCADE,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    UNIQUE (user_id, mission_id)
);