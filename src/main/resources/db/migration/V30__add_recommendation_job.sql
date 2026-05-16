-- V30: Add async recommendation job tracking to score_recommendation table

-- Job status tracking for async LLM recommendation generation
CREATE TABLE IF NOT EXISTS recommendation_job (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users(id),
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- PENDING | RUNNING | DONE | FAILED
    error_message TEXT,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rec_job_user_id ON recommendation_job(user_id);

-- Add source column to score_recommendation to track how recs were generated
ALTER TABLE score_recommendation
    ADD COLUMN IF NOT EXISTS source VARCHAR(10) NOT NULL DEFAULT 'MOCK',   -- MOCK | LLM
    ADD COLUMN IF NOT EXISTS job_id UUID REFERENCES recommendation_job(id);
