-- V1: Users and Profiles
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    phone       VARCHAR(15) NOT NULL UNIQUE,
    role        VARCHAR(20) NOT NULL DEFAULT 'GIG_WORKER',
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE user_profiles (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name     VARCHAR(100),
    email         VARCHAR(100),
    date_of_birth DATE,
    city          VARCHAR(100),
    state         VARCHAR(100),
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_profiles_user_id UNIQUE (user_id)
);
