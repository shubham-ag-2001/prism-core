-- V23: Onboarding, PAN verification, and platform (employer) provider_response
-- for the 6 band-demo users.
-- PANs: Arun=ABCDE9876A, Bhavya=ASDFG1234B, Chirag=QWERT4561P,
--       Dev=HJKLP1542D, Golu=MNVCB9657Q, Harish=ZXCVB1111H

-- ─── ONBOARDING ───────────────────────────────────────────────────────────────
INSERT INTO onboarding_data (user_id, pan_number, employer_name, employer_type, work_start_date, status, submitted_at)
SELECT v.uid, v.pan, v.employer, v.emp_type, v.wsd::date, 'COMPLETED', NOW()
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'ABCDE9876A', 'QuickBite',  'FOOD_DELIVERY', '2022-01-10'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'ASDFG1234B', 'RideFast',   'RIDE_HAILING',  '2021-06-15'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'QWERT4561P', 'FreelanceX', 'FREELANCER',    '2023-01-20'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'HJKLP1542D', 'HomeFixIt',  'HOME_SERVICES', '2020-09-05'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'MNVCB9657Q', 'ZomaDash',   'FOOD_DELIVERY', '2022-03-12'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'ZXCVB1111H', 'OlaCab',     'RIDE_HAILING',  '2021-02-28')
) AS v(uid, pan, employer, emp_type, wsd)
WHERE NOT EXISTS (SELECT 1 FROM onboarding_data od WHERE od.user_id = v.uid);

-- ─── PAN VERIFICATION ─────────────────────────────────────────────────────────
INSERT INTO pan_verifications (user_id, pan_number, verification_status, verification_response_json, verified_at)
SELECT v.uid, v.pan, 'VERIFIED', '{"status":"VALID","mock":true}', NOW()
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'ABCDE9876A'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'ASDFG1234B'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'QWERT4561P'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'HJKLP1542D'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'MNVCB9657Q'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'ZXCVB1111H')
) AS v(uid, pan)
WHERE NOT EXISTS (SELECT 1 FROM pan_verifications pv WHERE pv.user_id = v.uid);

-- ─── PLATFORM / EMPLOYER PROVIDER RESPONSE ────────────────────────────────────
-- These JSON blobs feed D2 (Activity), D4 (Social), D7 (Temporal).
-- Arun   → very poor activity → target Poor   (~400)
-- Bhavya → poor-ish           → target Fair   (~500)
-- Chirag → mediocre           → target Average (~600)
-- Dev    → decent             → target Good   (~700)
-- Golu   → strong             → target Very Good (~800)
-- Harish → excellent          → target Excellent (~880)

INSERT INTO provider_response (id, user_id, provider_type, fetched_at, response_json)
SELECT gen_random_uuid(), v.uid, 'PLATFORM', NOW(), v.rjson
FROM (VALUES

-- Arun — POOR profile: short tenure, low completion, many disputes, high cancellation
('c1000000-0000-0000-0000-000000007001'::uuid,
 '{"accountTenureDays":60,"orderCompletionRate":55,"ordersPerActiveDay":2,"activeDaysPerWeek":2,"accountAgeActiveRatio":0.18,"longestInactivityStreakDays":22,"gapFrequency90d":6,"peakHourParticipationRate":0.20,"cancellationRate":0.95,"cancellationSpikeFlag":true,"overallRating":2.8,"ratingLast30d":2.70,"ratingLast90d":2.80,"repeatCustomerRatio":0.03,"unresolvedDisputeCount":4,"disputeSpikeFlag":true,"responseRateToOffers":0.28,"totalEarningsLast90DaysRupees":8000,"consecutiveActiveMonths":1,"completionRateDrift":-9.0,"spendRatioDrift":11.0,"tenureWeightedReliabilityScore":18}'),

-- Bhavya — FAIR profile: slightly better but still low income/activity
('c1000000-0000-0000-0000-000000007002'::uuid,
 '{"accountTenureDays":120,"orderCompletionRate":65,"ordersPerActiveDay":3,"activeDaysPerWeek":3,"accountAgeActiveRatio":0.28,"longestInactivityStreakDays":15,"gapFrequency90d":5,"peakHourParticipationRate":0.32,"cancellationRate":0.85,"cancellationSpikeFlag":true,"overallRating":3.3,"ratingLast30d":3.25,"ratingLast90d":3.30,"repeatCustomerRatio":0.07,"unresolvedDisputeCount":2,"disputeSpikeFlag":true,"responseRateToOffers":0.38,"totalEarningsLast90DaysRupees":14000,"consecutiveActiveMonths":2,"completionRateDrift":-6.0,"spendRatioDrift":7.5,"tenureWeightedReliabilityScore":30}'),

-- Chirag — AVERAGE profile: moderate across the board
('c1000000-0000-0000-0000-000000007003'::uuid,
 '{"accountTenureDays":250,"orderCompletionRate":78,"ordersPerActiveDay":6,"activeDaysPerWeek":4,"accountAgeActiveRatio":0.46,"longestInactivityStreakDays":8,"gapFrequency90d":3,"peakHourParticipationRate":0.46,"cancellationRate":0.72,"cancellationSpikeFlag":true,"overallRating":3.9,"ratingLast30d":3.95,"ratingLast90d":3.90,"repeatCustomerRatio":0.12,"unresolvedDisputeCount":1,"disputeSpikeFlag":false,"responseRateToOffers":0.55,"totalEarningsLast90DaysRupees":28000,"consecutiveActiveMonths":3,"completionRateDrift":-4.0,"spendRatioDrift":5.0,"tenureWeightedReliabilityScore":48}'),

-- Dev — GOOD profile: solid numbers, minor blemishes
('c1000000-0000-0000-0000-000000007004'::uuid,
 '{"accountTenureDays":380,"orderCompletionRate":86,"ordersPerActiveDay":8,"activeDaysPerWeek":5,"accountAgeActiveRatio":0.58,"longestInactivityStreakDays":5,"gapFrequency90d":2,"peakHourParticipationRate":0.58,"cancellationRate":0.60,"cancellationSpikeFlag":false,"overallRating":4.3,"ratingLast30d":4.32,"ratingLast90d":4.30,"repeatCustomerRatio":0.19,"unresolvedDisputeCount":0,"disputeSpikeFlag":false,"responseRateToOffers":0.68,"totalEarningsLast90DaysRupees":40000,"consecutiveActiveMonths":5,"completionRateDrift":-1.8,"spendRatioDrift":2.8,"tenureWeightedReliabilityScore":68}'),

-- Golu — VERY GOOD profile: high completion, good earnings, low risk
('c1000000-0000-0000-0000-000000007005'::uuid,
 '{"accountTenureDays":600,"orderCompletionRate":93,"ordersPerActiveDay":10,"activeDaysPerWeek":6,"accountAgeActiveRatio":0.76,"longestInactivityStreakDays":2,"gapFrequency90d":1,"peakHourParticipationRate":0.70,"cancellationRate":0.42,"cancellationSpikeFlag":false,"overallRating":4.7,"ratingLast30d":4.72,"ratingLast90d":4.70,"repeatCustomerRatio":0.28,"unresolvedDisputeCount":0,"disputeSpikeFlag":false,"responseRateToOffers":0.82,"totalEarningsLast90DaysRupees":55000,"consecutiveActiveMonths":8,"completionRateDrift":-0.5,"spendRatioDrift":1.2,"tenureWeightedReliabilityScore":86}'),

-- Harish — EXCELLENT profile: top-tier across all dimensions
('c1000000-0000-0000-0000-000000007006'::uuid,
 '{"accountTenureDays":900,"orderCompletionRate":97,"ordersPerActiveDay":13,"activeDaysPerWeek":7,"accountAgeActiveRatio":0.92,"longestInactivityStreakDays":1,"gapFrequency90d":0,"peakHourParticipationRate":0.85,"cancellationRate":0.30,"cancellationSpikeFlag":false,"overallRating":4.9,"ratingLast30d":4.92,"ratingLast90d":4.90,"repeatCustomerRatio":0.38,"unresolvedDisputeCount":0,"disputeSpikeFlag":false,"responseRateToOffers":0.95,"totalEarningsLast90DaysRupees":75000,"consecutiveActiveMonths":14,"completionRateDrift":-0.2,"spendRatioDrift":0.5,"tenureWeightedReliabilityScore":96}')

) AS v(uid, rjson)
WHERE NOT EXISTS (
    SELECT 1 FROM provider_response pr
    WHERE pr.user_id = v.uid AND pr.provider_type = 'PLATFORM'
);
