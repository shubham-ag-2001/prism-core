-- V7: Seed Scoring Config — Dimensions, Vectors, Feature Flags (from PRISM spreadsheet)

-- ─── DIMENSIONS ──────────────────────────────────────────────────────────────
INSERT INTO dimension_config (dimension_key, display_name, display_order) VALUES
    ('INCOME',              'Income',              1),
    ('ACTIVITY',            'Activity',            2),
    ('SPENDING',            'Spending',            3),
    ('SOCIAL',              'Social',              4),
    ('RISK',                'Risk',                5),
    ('IDENTITY',            'Identity',            6),
    ('TEMPORAL_CONSISTENCY','Temporal Consistency', 7);

-- ─── VECTORS — INCOME ────────────────────────────────────────────────────────
INSERT INTO vector_config (vector_key, dimension_key, display_name, display_order) VALUES
    ('income_weekly_mean',           'INCOME', 'Weekly Income Mean (90d)',               1),
    ('income_cv',                    'INCOME', 'Income Coefficient of Variation',        2),
    ('income_source_diversity',      'INCOME', 'Income Source Diversity',                3),
    ('income_mom_growth',            'INCOME', 'Month-over-Month Growth',                4),
    ('income_recovery_speed',        'INCOME', 'Income Recovery Speed',                  5),
    ('income_30_60_90_comparison',   'INCOME', 'Income in Last 30d vs 60d vs 90d',       6),
    ('income_seasonal_adjustment',   'INCOME', 'Seasonal Income Adjustment',             7);

-- ─── VECTORS — ACTIVITY ──────────────────────────────────────────────────────
INSERT INTO vector_config (vector_key, dimension_key, display_name, display_order) VALUES
    ('activity_platform_tenure',     'ACTIVITY', 'Platform Tenure (days)',               1),
    ('activity_completion_rate',     'ACTIVITY', 'Order Completion Rate (%)',            2),
    ('activity_orders_per_day',      'ACTIVITY', 'Orders Per Active Day',                3),
    ('activity_active_days_week',    'ACTIVITY', 'Active Days Per Week',                 4),
    ('activity_account_age_ratio',   'ACTIVITY', 'Account Age vs Active Days Ratio',     5),
    ('activity_gap_days',            'ACTIVITY', 'Gap Days',                             6),
    ('activity_peak_hour_rate',      'ACTIVITY', 'Peak-Hour Participation Rate',         7),
    ('activity_cancellation_ratio',  'ACTIVITY', 'Cancellation Ratio',                  8);

-- ─── VECTORS — SPENDING ──────────────────────────────────────────────────────
INSERT INTO vector_config (vector_key, dimension_key, display_name, display_order) VALUES
    ('spending_earn_ratio',          'SPENDING', 'Spend-to-Earn Ratio',                  1),
    ('spending_savings_buffer',      'SPENDING', 'Savings Buffer Estimate',              2),
    ('spending_large_txn_freq',      'SPENDING', 'Large Transaction Frequency',          3),
    ('spending_volatility',          'SPENDING', 'Spend Volatility Index',               4),
    ('spending_wallet_topup_freq',   'SPENDING', 'Wallet Top-Up Frequency',              5),
    ('spending_category_dist',       'SPENDING', 'Category Spend Distribution',          6),
    ('spending_remittance_index',    'SPENDING', 'Remittance & Recurring Obligation Index', 7),
    ('spending_30_60_90_comparison', 'SPENDING', 'Spending in Last 30d vs 60d vs 90d',  8),
    ('spending_instalment_behaviour','SPENDING', 'Instalment Payment Behaviour',         9);

-- ─── VECTORS — SOCIAL ────────────────────────────────────────────────────────
INSERT INTO vector_config (vector_key, dimension_key, display_name, display_order) VALUES
    ('social_avg_customer_rating',   'SOCIAL', 'Average Customer Rating',               1),
    ('social_rating_trend',          'SOCIAL', 'Rating Trend (30d vs 90d)',             2),
    ('social_repeat_customer_ratio', 'SOCIAL', 'Repeat Customer Ratio',                 3),
    ('social_response_rate',         'SOCIAL', 'Response Rate to Offers',               4),
    ('social_dispute_complaint_rate','SOCIAL', 'Dispute and Complaint Rate',             5);

-- ─── VECTORS — RISK ──────────────────────────────────────────────────────────
INSERT INTO vector_config (vector_key, dimension_key, display_name, display_order) VALUES
    ('risk_prior_loan_default',      'RISK', 'Prior Loan Default Flag',                 1),
    ('risk_account_takeover',        'RISK', 'Account Takeover Risk Score',             2),
    ('risk_multi_account',           'RISK', 'Multiple Account Detection',              3),
    ('risk_device_integrity',        'RISK', 'Device Integrity & Behavioural Manipulation Risk Score', 4);

-- ─── VECTORS — IDENTITY ──────────────────────────────────────────────────────
INSERT INTO vector_config (vector_key, dimension_key, display_name, display_order) VALUES
    ('identity_kyc_tier',            'IDENTITY', 'KYC Verification Tier',               1),
    ('identity_city_tier',           'IDENTITY', 'City Tier & Cost-of-Living Normalisation Index', 2),
    ('identity_phone_vintage',       'IDENTITY', 'Phone Number Vintage',                3);

-- ─── VECTORS — TEMPORAL CONSISTENCY ──────────────────────────────────────────
INSERT INTO vector_config (vector_key, dimension_key, display_name, display_order) VALUES
    ('temporal_active_months_streak','TEMPORAL_CONSISTENCY', 'Consecutive Active Months Streak',      1),
    ('temporal_income_stability',    'TEMPORAL_CONSISTENCY', 'Income Stability Window',               2),
    ('temporal_behavioural_drift',   'TEMPORAL_CONSISTENCY', 'Behavioural Drift Index',               3),
    ('temporal_pattern_recovery',    'TEMPORAL_CONSISTENCY', 'Pattern Recovery Speed',                4),
    ('temporal_ontime_payments',     'TEMPORAL_CONSISTENCY', 'Longest Streak of On-Time Payments',   5),
    ('temporal_weighted_reliability','TEMPORAL_CONSISTENCY', 'Tenure-Weighted Reliability Score',     6);

-- ─── FEATURE FLAGS ───────────────────────────────────────────────────────────
INSERT INTO feature_flag_config (flag_key, vector_key, display_name, data_type, derivation_source) VALUES
-- Income
    ('avg_weekly_credit_sms_90d',        'income_weekly_mean',         'Avg Weekly Credit (SMS, 90d)',         'NUMERIC',  'SMS'),
    ('income_spike_ratio',               'income_weekly_mean',         'Income Spike Ratio',                   'NUMERIC',  'SMS'),
    ('low_income_week_count_90d',        'income_recovery_speed',      'Low Income Week Count (90d)',           'NUMERIC',  'SMS'),
    ('avg_recovery_weeks',               'income_recovery_speed',      'Avg Recovery Weeks',                   'NUMERIC',  'SMS'),
    ('platform_income_source_count',     'income_seasonal_adjustment', 'Platform Income Source Count',         'NUMERIC',  'SMS'),
    ('has_non_gig_income',               'income_seasonal_adjustment', 'Has Non-Gig Income',                   'BOOLEAN',  'SMS'),
-- Spending
    ('avg_month_end_balance_sms',        'spending_savings_buffer',    'Avg Month-End Balance (SMS)',           'NUMERIC',  'SMS'),
    ('negative_balance_flag',            'spending_savings_buffer',    'Negative Balance Flag',                'BOOLEAN',  'SMS'),
    ('discretionary_spend_ratio',        'spending_category_dist',     'Discretionary Spend Ratio',            'NUMERIC',  'SMS'),
    ('gambling_fantasy_spend_flag',      'spending_category_dist',     'Gambling / Fantasy Spend Flag',        'BOOLEAN',  'SMS'),
    ('monthly_remittance_amount',        'spending_remittance_index',  'Monthly Remittance Amount',            'NUMERIC',  'SMS'),
    ('remittance_consistency_score',     'spending_remittance_index',  'Remittance Consistency Score',         'NUMERIC',  'SMS'),
    ('missed_instalment_count_90d',      'spending_instalment_behaviour','Missed Instalment Count (90d)',       'NUMERIC',  'SMS'),
    ('active_instalment_count',          'spending_instalment_behaviour','Active Instalment Count',             'NUMERIC',  'SMS'),
-- Activity
    ('longest_inactivity_streak_days',   'activity_gap_days',          'Longest Inactivity Streak (days)',     'NUMERIC',  'PLATFORM'),
    ('gap_frequency_90d',                'activity_gap_days',          'Gap Frequency (90d)',                  'NUMERIC',  'PLATFORM'),
    ('late_cancellation_ratio',          'activity_cancellation_ratio','Late Cancellation Ratio',              'NUMERIC',  'PLATFORM'),
    ('cancellation_spike_flag',          'activity_cancellation_ratio','Cancellation Spike Flag',              'BOOLEAN',  'PLATFORM'),
-- Social
    ('unresolved_dispute_count',         'social_dispute_complaint_rate','Unresolved Dispute Count',           'NUMERIC',  'PLATFORM'),
    ('dispute_spike_flag',               'social_dispute_complaint_rate','Dispute Spike Flag',                 'BOOLEAN',  'PLATFORM'),
-- Risk
    ('unusual_login_location_flag',      'risk_account_takeover',      'Unusual Login Location Flag',          'BOOLEAN',  'SELF'),
    ('multiple_device_login_flag',       'risk_account_takeover',      'Multiple Device Login Flag',           'BOOLEAN',  'SELF'),
    ('has_location_spoof',               'risk_device_integrity',      'Has Location Spoof',                   'BOOLEAN',  'SELF'),
    ('has_record_fabrication_tools',     'risk_device_integrity',      'Has Record Fabrication Tools',         'BOOLEAN',  'SELF'),
    ('has_unofficial_apks',              'risk_device_integrity',      'Has Unofficial APKs',                  'BOOLEAN',  'SELF'),
    ('credit_hungry_score',              'risk_device_integrity',      'Credit Hungry Score',                  'NUMERIC',  'SELF'),
-- Temporal
    ('completion_rate_drift',            'temporal_behavioural_drift', 'Completion Rate Drift',                'NUMERIC',  'PLATFORM'),
    ('spend_ratio_drift',                'temporal_behavioural_drift', 'Spend Ratio Drift',                    'NUMERIC',  'SMS'),
    ('post_disruption_income_recovery_days', 'temporal_pattern_recovery', 'Post-Disruption Income Recovery Days', 'NUMERIC', 'SMS'),
    ('post_disruption_activity_recovery_days','temporal_pattern_recovery','Post-Disruption Activity Recovery Days','NUMERIC','PLATFORM');
