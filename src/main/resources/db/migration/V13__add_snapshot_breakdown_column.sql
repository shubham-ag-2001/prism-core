-- V13: Add dimension breakdown JSON column to prism_score_snapshot
-- Stores the full ScoringResult JSON for audit and UI breakdown purposes

ALTER TABLE prism_score_snapshot
    ADD COLUMN IF NOT EXISTS dimension_breakdown_json TEXT,
    ADD COLUMN IF NOT EXISTS score_band              VARCHAR(20),
    ADD COLUMN IF NOT EXISTS kill_switches_triggered TEXT,   -- JSON array of triggered kill switch keys
    ADD COLUMN IF NOT EXISTS alerts_json             TEXT;   -- JSON array of alert strings
