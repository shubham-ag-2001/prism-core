-- V4: Scoring Configuration Tables
CREATE TABLE dimension_config (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    dimension_key  VARCHAR(50) NOT NULL UNIQUE,
    display_name   VARCHAR(100) NOT NULL,
    description    TEXT,
    is_active      BOOLEAN     NOT NULL DEFAULT TRUE,
    display_order  INT         NOT NULL DEFAULT 0,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE vector_config (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    vector_key     VARCHAR(100) NOT NULL UNIQUE,
    dimension_key  VARCHAR(50)  NOT NULL REFERENCES dimension_config(dimension_key),
    display_name   VARCHAR(150) NOT NULL,
    description    TEXT,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order  INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE feature_flag_config (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    flag_key           VARCHAR(150) NOT NULL UNIQUE,
    vector_key         VARCHAR(100) NOT NULL REFERENCES vector_config(vector_key),
    display_name       VARCHAR(200) NOT NULL,
    data_type          VARCHAR(30)  NOT NULL,
    derivation_source  VARCHAR(30)  NOT NULL,
    is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE scoring_rule_config (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key       VARCHAR(200)   NOT NULL,
    config_level     VARCHAR(20)    NOT NULL,
    weight           DECIMAL(5,4)   NOT NULL,
    rule_set_version VARCHAR(20)    NOT NULL DEFAULT 'v1.0',
    effective_from   TIMESTAMP      NOT NULL DEFAULT NOW(),
    effective_to     TIMESTAMP,
    is_active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_scoring_rule_key ON scoring_rule_config(config_key, rule_set_version);
