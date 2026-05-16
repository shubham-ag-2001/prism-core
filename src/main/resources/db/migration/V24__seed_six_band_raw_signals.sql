-- V24: Seed raw SMS, PAN, and DEVICE signals for the six band demo users
-- Calibrated to hit: Poor / Fair / Average / Good / Very Good / Excellent
-- Idempotency guard: user_id + provider_type + signal_key (matches V21 pattern)

INSERT INTO raw_signal (id, user_id, provider_type, signal_key, signal_value, created_at)
SELECT gen_random_uuid(), v.uid, v.prov, v.sk, v.sv, NOW()
FROM (VALUES
    -- Arun 7000000001 → Poor (<450): rsk01_prior_default kill-switch + bad device + bad SMS
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'PAN',    'rsk01_prior_default',       'true'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'PAN',    'debt_to_income_ratio',      '0.80'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'PAN',    'longest_ontime_payment_streak', '0'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'DEVICE', 'is_device_rooted',          'true'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'DEVICE', 'has_location_spoof_app',    'true'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'DEVICE', 'has_record_fabrication_tools','true'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'DEVICE', 'credit_hungry_score',       '85'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'avg_weekly_credit_sms_90d', '900'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'income_cv',                 '0.62'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'negative_balance_flag',     'true'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'missed_instalment_count_90d','5'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'spend_to_earn_ratio',       '1.20'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'spend_volatility_index',    '0.80'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'low_income_week_count_90d', '8'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'avg_recovery_weeks',        '6'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'income_stability_windows',  '0'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'spend_ratio_drift',         '15.0'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'income_last_30d',           '2000'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'SMS',    'income_last_90d',           '8000'),

    -- Bhavya 7000000002 → Fair (450-549): no kill-switch but weak device + weak SMS
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'PAN',    'rsk01_prior_default',       'false'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'PAN',    'debt_to_income_ratio',      '0.60'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'PAN',    'longest_ontime_payment_streak', '1'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'DEVICE', 'is_device_rooted',          'true'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'DEVICE', 'has_unofficial_apks',       'true'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'DEVICE', 'credit_hungry_score',       '70'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'avg_weekly_credit_sms_90d', '1800'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'income_cv',                 '0.50'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'negative_balance_flag',     'true'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'missed_instalment_count_90d','3'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'spend_to_earn_ratio',       '0.95'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'low_income_week_count_90d', '5'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'avg_recovery_weeks',        '4'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'income_stability_windows',  '1'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'spend_ratio_drift',         '10.0'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'income_last_30d',           '5000'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SMS',    'income_last_90d',           '16000'),

    -- Chirag 7000000003 → Average (550-649): mediocre signals, no kill-switch
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'PAN',    'rsk01_prior_default',       'false'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'PAN',    'debt_to_income_ratio',      '0.40'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'PAN',    'longest_ontime_payment_streak', '3'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'DEVICE', 'is_device_rooted',          'false'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'DEVICE', 'has_unofficial_apks',       'true'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'DEVICE', 'is_developer_mode_on',      'true'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'DEVICE', 'credit_hungry_score',       '45'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'avg_weekly_credit_sms_90d', '3000'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'income_cv',                 '0.38'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'negative_balance_flag',     'false'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'missed_instalment_count_90d','1'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'spend_to_earn_ratio',       '0.78'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'low_income_week_count_90d', '3'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'avg_recovery_weeks',        '3'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'income_stability_windows',  '3'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'spend_ratio_drift',         '6.0'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'income_last_30d',           '9000'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SMS',    'income_last_90d',           '28000'),

    -- Dev 7000000004 → Good (650-749): solid profile, no red flags
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'PAN',    'rsk01_prior_default',       'false'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'PAN',    'debt_to_income_ratio',      '0.22'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'PAN',    'longest_ontime_payment_streak', '7'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'DEVICE', 'is_device_rooted',          'false'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'DEVICE', 'credit_hungry_score',       '20'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'avg_weekly_credit_sms_90d', '4200'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'income_cv',                 '0.22'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'negative_balance_flag',     'false'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'missed_instalment_count_90d','0'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'spend_to_earn_ratio',       '0.60'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'low_income_week_count_90d', '1'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'avg_recovery_weeks',        '2'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'income_stability_windows',  '5'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'spend_ratio_drift',         '3.0'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'income_last_30d',           '14000'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SMS',    'income_last_90d',           '42000'),

    -- Golu 7000000005 → Very Good (750-849): strong consistent signals
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'PAN',    'rsk01_prior_default',       'false'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'PAN',    'debt_to_income_ratio',      '0.12'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'PAN',    'longest_ontime_payment_streak', '14'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'DEVICE', 'is_device_rooted',          'false'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'DEVICE', 'credit_hungry_score',       '10'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'avg_weekly_credit_sms_90d', '5500'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'income_cv',                 '0.15'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'negative_balance_flag',     'false'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'missed_instalment_count_90d','0'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'spend_to_earn_ratio',       '0.42'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'low_income_week_count_90d', '0'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'avg_recovery_weeks',        '1'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'income_stability_windows',  '7'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'spend_ratio_drift',         '1.5'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'income_last_30d',           '19000'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SMS',    'income_last_90d',           '56000'),

    -- Harish 7000000006 → Excellent (850-900): top-tier everything
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'PAN',    'rsk01_prior_default',       'false'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'PAN',    'debt_to_income_ratio',      '0.05'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'PAN',    'longest_ontime_payment_streak', '24'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'DEVICE', 'is_device_rooted',          'false'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'DEVICE', 'credit_hungry_score',       '5'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'avg_weekly_credit_sms_90d', '8000'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'income_cv',                 '0.08'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'negative_balance_flag',     'false'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'missed_instalment_count_90d','0'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'spend_to_earn_ratio',       '0.25'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'low_income_week_count_90d', '0'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'avg_recovery_weeks',        '1'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'income_stability_windows',  '10'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'spend_ratio_drift',         '0.5'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'income_last_30d',           '26000'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'SMS',    'income_last_90d',           '75000')

) AS v(uid, prov, sk, sv)
WHERE EXISTS (SELECT 1 FROM users u WHERE u.id = v.uid)
  AND NOT EXISTS (
    SELECT 1 FROM raw_signal rs
    WHERE rs.user_id = v.uid
      AND rs.provider_type = v.prov
      AND rs.signal_key = v.sk
  );
