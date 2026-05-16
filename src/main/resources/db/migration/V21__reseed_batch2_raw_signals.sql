-- V21__repair_and_reseed_batch2.sql
-- Fully idempotent repair migration

-- =====================================================
-- USERS
-- =====================================================

INSERT INTO users (id, phone, role, is_active)
SELECT *
FROM (
    VALUES
    ('a1000000-0000-0000-0000-000000001011'::uuid, '9000000111', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001012'::uuid, '9000000112', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001013'::uuid, '9000000113', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001014'::uuid, '9000000114', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001015'::uuid, '9000000115', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001016'::uuid, '9000000116', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001017'::uuid, '9000000117', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001018'::uuid, '9000000118', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001019'::uuid, '9000000119', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000001020'::uuid, '9000000120', 'GIG_WORKER', TRUE)
) AS v(id, phone, role, is_active)
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE u.id = v.id
);

-- =====================================================
-- RAW SIGNALS
-- =====================================================

INSERT INTO raw_signal (
    id,
    user_id,
    provider_type,
    signal_key,
    signal_value,
    created_at
)
SELECT
    gen_random_uuid(),
    v.uid,
    v.provider_type,
    v.signal_key,
    v.signal_value,
    NOW()
FROM (
    VALUES

    -- USER 1011
    ('a1000000-0000-0000-0000-000000001011'::uuid, 'PAN', 'rsk01_prior_default', 'false'),
    ('a1000000-0000-0000-0000-000000001011'::uuid, 'SMS', 'avg_weekly_credit_sms_90d', '3200'),

    -- USER 1012
    ('a1000000-0000-0000-0000-000000001012'::uuid, 'PAN', 'rsk01_prior_default', 'false'),
    ('a1000000-0000-0000-0000-000000001012'::uuid, 'SMS', 'avg_weekly_credit_sms_90d', '3500'),

    -- USER 1013
    ('a1000000-0000-0000-0000-000000001013'::uuid, 'PAN', 'rsk01_prior_default', 'true'),
    ('a1000000-0000-0000-0000-000000001013'::uuid, 'SMS', 'avg_weekly_credit_sms_90d', '3000')

) AS v(uid, provider_type, signal_key, signal_value)

WHERE EXISTS (
    SELECT 1 FROM users u WHERE u.id = v.uid
)

AND NOT EXISTS (
    SELECT 1
    FROM raw_signal rs
    WHERE rs.user_id = v.uid
      AND rs.provider_type = v.provider_type
      AND rs.signal_key = v.signal_key
);
