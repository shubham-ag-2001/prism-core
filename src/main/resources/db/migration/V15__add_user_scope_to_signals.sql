-- V15: Add user_id to raw_signal and provider_response tables
-- This allows signals to be stored at ingest time (before a scoring job exists)
-- and read later when the scoring pipeline runs.

-- raw_signal: add user_id (nullable first, then backfill, then constrain)
ALTER TABLE raw_signal ADD COLUMN user_id UUID REFERENCES users(id) ON DELETE CASCADE;
CREATE INDEX idx_raw_signal_user_id ON raw_signal(user_id);

-- provider_response: add user_id + platform_key for employer lookups
ALTER TABLE provider_response ADD COLUMN user_id UUID REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE provider_response ADD COLUMN platform_key VARCHAR(50);
CREATE INDEX idx_provider_response_user_id ON provider_response(user_id);

-- Make snapshot_id nullable on both tables so pre-scoring storage is allowed
ALTER TABLE raw_signal ALTER COLUMN snapshot_id DROP NOT NULL;
ALTER TABLE provider_response ALTER COLUMN snapshot_id DROP NOT NULL;
