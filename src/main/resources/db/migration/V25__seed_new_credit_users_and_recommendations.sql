-- V25: New-to-credit users (Ishan=7000000007, Jaya=7000000008)
-- and recommendations for all 8 demo users (6 band + 2 new credit).
-- "New to credit" = PAN verified for KYC only; NO credit history raw_signals.

-- ─── USERS ────────────────────────────────────────────────────────────────────
INSERT INTO users (id, phone, role, is_active)
SELECT v.id, v.phone, v.role, v.is_active
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007007'::uuid, '7000000007', 'GIG_WORKER', TRUE),
    ('c1000000-0000-0000-0000-000000007008'::uuid, '7000000008', 'GIG_WORKER', TRUE)
) AS v(id, phone, role, is_active)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = v.id);

-- ─── PROFILES ─────────────────────────────────────────────────────────────────
INSERT INTO user_profiles (user_id, full_name, email, city, state)
SELECT v.uid, v.full_name, v.email, v.city, v.state
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'Ishan', 'ishan@demo.prism', 'Pune',    'Maharashtra'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'Jaya',  'jaya@demo.prism',  'Kolkata', 'West Bengal')
) AS v(uid, full_name, email, city, state)
WHERE NOT EXISTS (SELECT 1 FROM user_profiles up WHERE up.user_id = v.uid);

-- ─── ONBOARDING (PAN for KYC only) ────────────────────────────────────────────
INSERT INTO onboarding_data (user_id, pan_number, employer_name, employer_type, work_start_date, status, submitted_at)
SELECT v.uid, v.pan, v.employer, v.emp_type, v.wsd::date, 'COMPLETED', NOW()
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'LKJHG2345N', 'SwiftMart',  'FOOD_DELIVERY', '2024-01-15'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'BVNMQ7654R', 'RideQuick',  'RIDE_HAILING',  '2023-11-01')
) AS v(uid, pan, employer, emp_type, wsd)
WHERE NOT EXISTS (SELECT 1 FROM onboarding_data od WHERE od.user_id = v.uid);

-- ─── PAN VERIFICATION (KYC identity check only, no credit history) ────────────
INSERT INTO pan_verifications (user_id, pan_number, verification_status, verification_response_json, verified_at)
SELECT v.uid, v.pan, 'VERIFIED', '{"status":"VALID","creditHistory":false,"mock":true}', NOW()
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'LKJHG2345N'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'BVNMQ7654R')
) AS v(uid, pan)
WHERE NOT EXISTS (SELECT 1 FROM pan_verifications pv WHERE pv.user_id = v.uid);

-- ─── EMPLOYER / PLATFORM DATA ─────────────────────────────────────────────────
-- Ishan → Average (~580): moderate, new worker
-- Jaya  → Good   (~660): solid, growing quickly
INSERT INTO provider_response (id, user_id, provider_type, fetched_at, response_json)
SELECT gen_random_uuid(), v.uid, 'PLATFORM', NOW(), v.rjson
FROM (VALUES
('c1000000-0000-0000-0000-000000007007'::uuid,
 '{"accountTenureDays":180,"orderCompletionRate":75,"ordersPerActiveDay":5,"activeDaysPerWeek":4,"accountAgeActiveRatio":0.40,"longestInactivityStreakDays":10,"gapFrequency90d":4,"peakHourParticipationRate":0.45,"cancellationRate":0.78,"cancellationSpikeFlag":false,"overallRating":3.8,"ratingLast30d":3.82,"ratingLast90d":3.80,"repeatCustomerRatio":0.10,"unresolvedDisputeCount":1,"disputeSpikeFlag":false,"responseRateToOffers":0.50,"totalEarningsLast90DaysRupees":26000,"consecutiveActiveMonths":3,"completionRateDrift":-3.0,"spendRatioDrift":4.0,"tenureWeightedReliabilityScore":44}'),
('c1000000-0000-0000-0000-000000007008'::uuid,
 '{"accountTenureDays":320,"orderCompletionRate":87,"ordersPerActiveDay":8,"activeDaysPerWeek":5,"accountAgeActiveRatio":0.55,"longestInactivityStreakDays":6,"gapFrequency90d":2,"peakHourParticipationRate":0.60,"cancellationRate":0.58,"cancellationSpikeFlag":false,"overallRating":4.3,"ratingLast30d":4.32,"ratingLast90d":4.30,"repeatCustomerRatio":0.18,"unresolvedDisputeCount":0,"disputeSpikeFlag":false,"responseRateToOffers":0.65,"totalEarningsLast90DaysRupees":38000,"consecutiveActiveMonths":5,"completionRateDrift":-2.0,"spendRatioDrift":3.0,"tenureWeightedReliabilityScore":62}')
) AS v(uid, rjson)
WHERE NOT EXISTS (SELECT 1 FROM provider_response pr WHERE pr.user_id = v.uid AND pr.provider_type = 'PLATFORM');

-- ─── RAW SIGNALS: SMS (income/spending) ───────────────────────────────────────
-- NOTE: NO PAN raw_signals for Ishan/Jaya (new to credit — no credit history)
INSERT INTO raw_signal (id, user_id, provider_type, signal_key, signal_value, created_at)
SELECT gen_random_uuid(), v.uid, v.prov, v.sk, v.sv, NOW()
FROM (VALUES
    -- Ishan → Average signals
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'avg_weekly_credit_sms_90d',  '2800'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'income_cv',                  '0.32'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'negative_balance_flag',      'false'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'missed_instalment_count_90d','0'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'spend_to_earn_ratio',        '0.75'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'low_income_week_count_90d',  '3'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'avg_recovery_weeks',         '3'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'income_stability_windows',   '3'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'spend_ratio_drift',          '4.0'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'income_last_30d',            '8500'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'SMS', 'income_last_90d',            '26000'),
    -- Ishan → Clean device
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'DEVICE', 'is_device_rooted',        'false'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'DEVICE', 'credit_hungry_score',     '18'),

    -- Jaya → Good signals
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'avg_weekly_credit_sms_90d',  '4000'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'income_cv',                  '0.20'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'negative_balance_flag',      'false'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'missed_instalment_count_90d','0'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'spend_to_earn_ratio',        '0.60'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'low_income_week_count_90d',  '1'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'avg_recovery_weeks',         '2'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'income_stability_windows',   '5'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'spend_ratio_drift',          '2.5'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'income_last_30d',            '13000'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'SMS', 'income_last_90d',            '38000'),
    -- Jaya → Clean device
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'DEVICE', 'is_device_rooted',        'false'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'DEVICE', 'credit_hungry_score',     '12')
) AS v(uid, prov, sk, sv)
WHERE EXISTS (SELECT 1 FROM users u WHERE u.id = v.uid)
  AND NOT EXISTS (
    SELECT 1 FROM raw_signal rs
    WHERE rs.user_id = v.uid AND rs.provider_type = v.prov AND rs.signal_key = v.sk
  );

-- ─── RECOMMENDATIONS (all 8 demo users) ───────────────────────────────────────
INSERT INTO score_recommendation (user_id, category, title, description)
SELECT u.id, v.category, v.title, v.description
FROM users u
JOIN (VALUES
    -- Arun (Poor) — kill-switch triggered, bad device, low income
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'RISK',     'Clear Your Prior Loan Default',     'A prior loan default was detected on your PAN. Settling outstanding debts is the single most impactful step to improve your PRISM score.'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'RISK',     'Remove Suspicious Apps',            'Suspicious and location-spoofing apps were found on your device. Uninstalling them will restore your device integrity score.'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'INCOME',   'Increase Weekly Earnings',          'Your average weekly credit of ₹900 is critically low. Try to take more orders or work on additional platforms to stabilize income.'),
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'ACTIVITY', 'Improve Order Completion Rate',     'Your completion rate of 55% is very low. Aim for 80%+ to significantly improve your platform reliability score.'),

    -- Bhavya (Fair) — rooted device, low income, missed payments
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'RISK',     'Secure Your Device',                'Your device is rooted, which raises a security concern. Using a non-rooted device will improve your device integrity score.'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'SPENDING', 'Clear Missed Instalments',          '3 missed instalments were detected. Clearing these overdue payments is the fastest way to move out of the Fair band.'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'INCOME',   'Stabilize Weekly Income',           'Your weekly earnings are inconsistent. Try to maintain a steady schedule to reduce income volatility.'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'ACTIVITY', 'Reduce Order Cancellations',        'A high cancellation rate was detected. Keeping cancellations below 15% will noticeably improve your activity score.'),

    -- Chirag (Average) — mediocre all-around, some cancellation issues
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'ACTIVITY', 'Improve Completion Rate to 85%+',  'Your current completion rate of 78% puts you in the Average band. Reaching 85% will push you into Good.'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'SPENDING', 'Reduce Your Debt-to-Income Ratio', 'Your debt-to-income ratio of 40% is above the ideal 30%. Pay down active loans gradually to improve your spending score.'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'RISK',     'Remove Unofficial APKs',            'Unofficial APKs were detected on your device. Remove them to eliminate a risk flag in your score.'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'INCOME',   'Work More Consistently',            'You had 3 low-income weeks in the last 90 days. Consistent work weeks directly improve your income stability score.'),

    -- Dev (Good) — solid, minor improvement areas
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'INCOME',   'Diversify Your Income Sources',    'You currently rely on one platform. Adding a second gig platform will improve your income diversity score.'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'ACTIVITY', 'Boost Peak Hour Participation',    'You participate in peak hours 58% of the time. Increasing this to 70%+ can lift you from Good to Very Good.'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'SPENDING', 'Keep Your Debt Ratio Low',         'Your debt-to-income ratio of 22% is healthy. Continue managing instalments well to maintain your score.'),

    -- Golu (Very Good) — strong profile
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'ACTIVITY', 'Excellent Consistency — Keep It Up','Your 93% completion rate and 6 active days per week are outstanding. Maintain this to reach Excellent band.'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'INCOME',   'Maintain Income Stability',         'You have 7 stable income windows — great! Keep your weekly income consistent to push into Excellent.'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'SPENDING', 'Build a Savings Buffer',            'Your spending ratio is healthy. Consider moving some earnings into savings to further strengthen your financial profile.'),

    -- Harish (Excellent) — top performer
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'PROFILE',  'You Qualify for Premium Loan Rates','Your Excellent PRISM score makes you eligible for our best loan rates and highest credit limits.'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'INCOME',   'Stellar Income Profile',            'Your weekly earnings of ₹8000 and 97% completion rate are in the top 5% of all gig workers on the platform.'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'ACTIVITY', 'Top Performer Badge',               'You have been active 7 days a week with zero inactivity gaps. You are a model PRISM member.'),

    -- Ishan (Average, new to credit)
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'PROFILE',  'Start Building Your Credit History', 'You are new to credit. Taking and repaying a small loan on time is the fastest way to unlock better loan offers.'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'ACTIVITY', 'Maintain Platform Activity',        'Keep your order completion rate above 80% and active days above 4 per week to build a strong reliability score.'),
    ('c1000000-0000-0000-0000-000000007007'::uuid, 'INCOME',   'Grow Your Weekly Earnings',         'Your current weekly earnings of ₹2800 are a good start. Gradually increasing this will move you from Average to Good band.'),

    -- Jaya (Good, new to credit)
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'PROFILE',  'Great Start — Build Credit Now',    'Your platform performance is already in the Good band. Taking a small loan will establish a credit history and further boost your score.'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'INCOME',   'Consistent Earnings Are Paying Off', 'Your weekly income of ₹4000 with low volatility is a strong signal. Keep this consistency to reach Very Good.'),
    ('c1000000-0000-0000-0000-000000007008'::uuid, 'ACTIVITY', 'Excellent Platform Engagement',      'Your 87% completion rate and 5 active days per week are impressive for a relatively new worker. Keep it up!')
) AS v(uid, category, title, description) ON u.id = v.uid
WHERE NOT EXISTS (
    SELECT 1 FROM score_recommendation sr
    WHERE sr.user_id = v.uid AND sr.title = v.title
);
