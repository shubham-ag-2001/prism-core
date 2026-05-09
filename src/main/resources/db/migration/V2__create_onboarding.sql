-- V2: Onboarding and PAN Verification
CREATE TABLE onboarding_data (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pan_number        VARCHAR(10),
    employer_name     VARCHAR(200),
    employer_type     VARCHAR(50),
    work_start_date   DATE,
    work_history_json TEXT,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_at      TIMESTAMP,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_onboarding_user_id UNIQUE (user_id)
);

CREATE TABLE pan_verifications (
    id                        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                   UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pan_number                VARCHAR(10) NOT NULL,
    verification_status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verification_response_json TEXT,
    verified_at               TIMESTAMP,
    created_at                TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pan_verifications_user_id ON pan_verifications(user_id);
