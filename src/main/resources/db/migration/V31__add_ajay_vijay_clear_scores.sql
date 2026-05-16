-- V31: Clear scores for 8 existing demo users + seed Ajay & Vijay (new test users for recs flow)
-- Ajay  → phone 7000000009, UUID c1000000-0000-0000-0000-000000007009, ~Good band (680-720)
-- Vijay → phone 7000000010, UUID c1000000-0000-0000-0000-000000007010, ~Average band (580-640)
-- Platform/Device signals intentionally OMITTED — user must trigger /providers/employer/fetch
-- and /providers/device-info/ingest themselves to test real-time derivation.

-- ═══════════════════════════════════════════════════════════════════════════════
-- PART 1: Clear calculated scores for all 8 existing demo users
-- ═══════════════════════════════════════════════════════════════════════════════
DELETE FROM scoring_job
WHERE user_id IN (
    'c1000000-0000-0000-0000-000000007001',
    'c1000000-0000-0000-0000-000000007002',
    'c1000000-0000-0000-0000-000000007003',
    'c1000000-0000-0000-0000-000000007004',
    'c1000000-0000-0000-0000-000000007005',
    'c1000000-0000-0000-0000-000000007006',
    'c1000000-0000-0000-0000-000000007007',
    'c1000000-0000-0000-0000-000000007008'
);

DELETE FROM prism_score_snapshot
WHERE user_id IN (
    'c1000000-0000-0000-0000-000000007001',
    'c1000000-0000-0000-0000-000000007002',
    'c1000000-0000-0000-0000-000000007003',
    'c1000000-0000-0000-0000-000000007004',
    'c1000000-0000-0000-0000-000000007005',
    'c1000000-0000-0000-0000-000000007006',
    'c1000000-0000-0000-0000-000000007007',
    'c1000000-0000-0000-0000-000000007008'
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- PART 2: Create Ajay & Vijay user accounts
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO users (id, phone, role, is_active)
SELECT v.id, v.phone, v.role, v.is_active
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007009'::uuid, '7000000009', 'GIG_WORKER', TRUE),
    ('c1000000-0000-0000-0000-000000007010'::uuid, '7000000010', 'GIG_WORKER', TRUE)
) AS v(id, phone, role, is_active)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = v.id);

INSERT INTO user_profiles (user_id, full_name, email, city, state)
SELECT v.uid, v.full_name, v.email, v.city, v.state
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007009'::uuid, 'Ajay',  'ajay@demo.prism',  'Pune',      'Maharashtra'),
    ('c1000000-0000-0000-0000-000000007010'::uuid, 'Vijay', 'vijay@demo.prism', 'Hyderabad', 'Telangana')
) AS v(uid, full_name, email, city, state)
WHERE NOT EXISTS (SELECT 1 FROM user_profiles up WHERE up.user_id = v.uid);

-- ═══════════════════════════════════════════════════════════════════════════════
-- PART 3: Onboarding & PAN verification for Ajay & Vijay
-- ═══════════════════════════════════════════════════════════════════════════════
INSERT INTO onboarding_data (user_id, pan_number, employer_name, employer_type, work_start_date, status, submitted_at)
SELECT v.uid, v.pan, v.employer, v.emp_type, v.wsd::date, 'COMPLETED', NOW()
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007009'::uuid, 'AJAYP1234K', 'SwiftMart',  'QUICK_COMMERCE', '2023-03-01'),
    ('c1000000-0000-0000-0000-000000007010'::uuid, 'VIJAY5678L', 'RideFast',   'RIDE_HAILING',   '2023-07-15')
) AS v(uid, pan, employer, emp_type, wsd)
WHERE NOT EXISTS (SELECT 1 FROM onboarding_data od WHERE od.user_id = v.uid);

INSERT INTO pan_verifications (user_id, pan_number, verification_status, verification_response_json, verified_at)
SELECT v.uid, v.pan, 'VERIFIED', '{"status":"VALID","mock":true}', NOW()
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007009'::uuid, 'AJAYP1234K'),
    ('c1000000-0000-0000-0000-000000007010'::uuid, 'VIJAY5678L')
) AS v(uid, pan)
WHERE NOT EXISTS (SELECT 1 FROM pan_verifications pv WHERE pv.user_id = v.uid);

-- ═══════════════════════════════════════════════════════════════════════════════
-- PART 4: SMS-derived raw_signals for Ajay (target ~Good, 680-720)
--   Profile: Consistent earner, decent income, minor spending issues
--   PAN: clean history, no prior default
--   DEVICE: intentionally omitted — let the app submit
-- ═══════════════════════════════════════════════════════════════════════════════
DELETE FROM raw_signal WHERE user_id = 'c1000000-0000-0000-0000-000000007009';

INSERT INTO raw_signal (id, user_id, provider_type, signal_key, signal_value, created_at)
VALUES
    -- PAN bureau signals
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','PAN','rsk01_prior_default',           'false', NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','PAN','debt_to_income_ratio',          '0.35',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','PAN','longest_ontime_payment_streak', '9',     NOW()),

    -- SMS-derived income signals (D1)
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','avg_weekly_credit_sms_90d',     '4200',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','income_cv',                     '0.24',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','income_last_30d',               '17000', NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','income_last_90d',               '51000', NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','income_stability_windows',      '5',     NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','low_income_week_count_90d',     '2',     NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','avg_recovery_weeks',            '2',     NOW()),

    -- SMS-derived spending signals (D3)
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','spend_to_earn_ratio',           '0.72',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','spend_volatility_index',        '0.30',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','negative_balance_flag',         'false', NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','missed_instalment_count_90d',   '1',     NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007009','SMS','spend_ratio_drift',             '3.5',   NOW());

-- ═══════════════════════════════════════════════════════════════════════════════
-- PART 5: SMS-derived raw_signals for Vijay (target ~Average, 580-640)
--   Profile: Irregular income, overspends some months, new gig worker
--   PAN: no prior default but high DTI
--   DEVICE: intentionally omitted
-- ═══════════════════════════════════════════════════════════════════════════════
DELETE FROM raw_signal WHERE user_id = 'c1000000-0000-0000-0000-000000007010';

INSERT INTO raw_signal (id, user_id, provider_type, signal_key, signal_value, created_at)
VALUES
    -- PAN bureau signals
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','PAN','rsk01_prior_default',           'false', NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','PAN','debt_to_income_ratio',          '0.55',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','PAN','longest_ontime_payment_streak', '4',     NOW()),

    -- SMS-derived income signals (D1)
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','avg_weekly_credit_sms_90d',     '2800',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','income_cv',                     '0.42',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','income_last_30d',               '11000', NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','income_last_90d',               '30000', NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','income_stability_windows',      '2',     NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','low_income_week_count_90d',     '4',     NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','avg_recovery_weeks',            '3',     NOW()),

    -- SMS-derived spending signals (D3)
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','spend_to_earn_ratio',           '0.91',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','spend_volatility_index',        '0.52',  NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','negative_balance_flag',         'false', NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','missed_instalment_count_90d',   '2',     NOW()),
    (gen_random_uuid(),'c1000000-0000-0000-0000-000000007010','SMS','spend_ratio_drift',             '7.2',   NOW());

-- ═══════════════════════════════════════════════════════════════════════════════
-- NOTE: No DEVICE signals or PLATFORM (employer) provider_response seeded for
-- Ajay or Vijay by design. To test the recommendation flow end-to-end:
--   1. Login as Ajay (7000000009) / Vijay (7000000010), OTP = 1234
--   2. POST /providers/employer/fetch with any platformKey to seed employer data
--   3. POST /providers/device-info/ingest to seed device flags
--   4. POST /score/fetch  → triggers scoring pipeline (cache miss)
--   5. Poll /score/job/{jobId} until COMPLETE
--   6. POST /score/recommendations/generate → triggers LLM job
--   7. Poll /score/recommendations/status/{jobId} until DONE
-- ═══════════════════════════════════════════════════════════════════════════════
