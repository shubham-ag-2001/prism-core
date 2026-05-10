-- V5: Score Snapshots, Jobs, and Sub-scores
CREATE TABLE prism_score_snapshot (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    final_score     INT,
    scoring_status  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rule_set_version VARCHAR(20) NOT NULL DEFAULT 'v1.0',
    triggered_by    VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    failure_reason  TEXT,
    computed_at     TIMESTAMP,
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_snapshot_user_id ON prism_score_snapshot(user_id);
CREATE INDEX idx_snapshot_status  ON prism_score_snapshot(scoring_status);

CREATE TABLE scoring_job (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    snapshot_id    UUID        REFERENCES prism_score_snapshot(id),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    completed_at   TIMESTAMP,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_scoring_job_user ON scoring_job(user_id);

CREATE TABLE dimension_score (
    id             UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_id    UUID            NOT NULL REFERENCES prism_score_snapshot(id) ON DELETE CASCADE,
    dimension_key  VARCHAR(50)     NOT NULL,
    raw_score      DECIMAL(8,4),
    weighted_score DECIMAL(8,4),
    created_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE vector_score (
    id             UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_id    UUID            NOT NULL REFERENCES prism_score_snapshot(id) ON DELETE CASCADE,
    vector_key     VARCHAR(100)    NOT NULL,
    dimension_key  VARCHAR(50)     NOT NULL,
    raw_score      DECIMAL(8,4),
    weighted_score DECIMAL(8,4),
    created_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE feature_flag_value (
    id                UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_id       UUID            NOT NULL REFERENCES prism_score_snapshot(id) ON DELETE CASCADE,
    flag_key          VARCHAR(150)    NOT NULL,
    vector_key        VARCHAR(100)    NOT NULL,
    computed_value    TEXT,
    score_contribution DECIMAL(8,4),
    created_at        TIMESTAMP       NOT NULL DEFAULT NOW()
);
