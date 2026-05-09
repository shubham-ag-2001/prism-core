-- V6: Provider Responses, Raw Signals, and Audit Log
CREATE TABLE provider_response (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_id          UUID        NOT NULL REFERENCES prism_score_snapshot(id) ON DELETE CASCADE,
    provider_type        VARCHAR(30) NOT NULL,
    request_payload_json TEXT,
    response_json        TEXT,
    http_status          INT,
    is_mock              BOOLEAN     NOT NULL DEFAULT TRUE,
    fetched_at           TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE raw_signal (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_id   UUID         NOT NULL REFERENCES prism_score_snapshot(id) ON DELETE CASCADE,
    provider_type VARCHAR(30)  NOT NULL,
    signal_key    VARCHAR(150) NOT NULL,
    signal_value  TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_raw_signal_snapshot ON raw_signal(snapshot_id);

CREATE TABLE audit_log (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type  VARCHAR(50) NOT NULL,
    entity_id    VARCHAR(100),
    action       VARCHAR(50) NOT NULL,
    actor_id     UUID,
    payload_json TEXT,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
