-- V16: Score Recommendations
-- Stores AI-generated recommendations to improve the user's PRISM score.

CREATE TABLE score_recommendation (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category    VARCHAR(50)  NOT NULL, -- e.g., PROFILE, ACTIVITY, SPENDING, INCOME
    title       VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_score_rec_user_id ON score_recommendation(user_id);
