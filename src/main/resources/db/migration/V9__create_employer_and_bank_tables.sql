-- V9: Employer Platforms and Featured Banks

CREATE TABLE employer_platforms (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    platform_key VARCHAR(30) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    category     VARCHAR(50),       -- FOOD_DELIVERY, RIDE_HAILING, HOME_SERVICES, FREELANCER, etc.
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE featured_banks (
    id                   UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_name            VARCHAR(150)   NOT NULL,
    tagline              VARCHAR(255),
    logo_url             VARCHAR(500),
    loan_page_url        VARCHAR(500),
    min_loan_amount      BIGINT,
    max_loan_amount      BIGINT,
    interest_rate_from   DECIMAL(5,2),
    interest_rate_to     DECIMAL(5,2),
    processing_fee_pct   DECIMAL(4,2),
    max_tenure_months    INT,
    features_json        TEXT,          -- JSON array of highlight bullet points
    is_active            BOOLEAN        NOT NULL DEFAULT TRUE,
    display_order        INT            NOT NULL DEFAULT 0,
    created_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP      NOT NULL DEFAULT NOW()
);
