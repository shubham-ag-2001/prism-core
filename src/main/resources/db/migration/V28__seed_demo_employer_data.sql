-- V28: Seed employer platform data for 8 demo users to fix inflated D2/D4 scores.
-- Without this, the engine uses generous defaults (88% completion, 400-day tenure, 4.3 rating).

DELETE FROM provider_response WHERE user_id IN (
  'c1000000-0000-0000-0000-000000007001','c1000000-0000-0000-0000-000000007002',
  'c1000000-0000-0000-0000-000000007003','c1000000-0000-0000-0000-000000007004',
  'c1000000-0000-0000-0000-000000007005','c1000000-0000-0000-0000-000000007006',
  'c1000000-0000-0000-0000-000000007007','c1000000-0000-0000-0000-000000007008'
);

-- Arun → Poor: terrible employer stats
INSERT INTO provider_response (id, user_id, provider_type, platform_key, response_json, http_status, is_mock, fetched_at)
VALUES (gen_random_uuid(), 'c1000000-0000-0000-0000-000000007001', 'PLATFORM', 'QUICKBITE',
'{"platformKey":"QUICKBITE","platformName":"QuickBite","accountStatus":"ACTIVE","employerCategory":"FOOD_DELIVERY","isMock":true,"accountTenureDays":60,"orderCompletionRate":62.0,"totalCompletedOrders":80,"activeOrdersLast30Days":8,"ordersPerActiveDay":3.0,"activeDaysPerWeek":2.0,"accountAgeActiveRatio":0.20,"longestInactivityStreakDays":25,"gapFrequency90d":5,"peakHourParticipationRate":0.18,"cancellationRate":0.12,"cancellationSpikeFlag":true,"overallRating":3.1,"ratingLast30d":2.9,"ratingLast90d":3.2,"repeatCustomerRatio":0.04,"unresolvedDisputeCount":4,"disputeSpikeFlag":true,"responseRateToOffers":0.25,"consecutiveActiveMonths":1.0,"completionRateDrift":-10.0,"tenureWeightedReliabilityScore":15.0,"totalEarningsLast90DaysRupees":8000}',
200, true, NOW());

-- Bhavya → Fair: weak employer stats
INSERT INTO provider_response (id, user_id, provider_type, platform_key, response_json, http_status, is_mock, fetched_at)
VALUES (gen_random_uuid(), 'c1000000-0000-0000-0000-000000007002', 'PLATFORM', 'RIDEFAST',
'{"platformKey":"RIDEFAST","platformName":"RideFast","accountStatus":"ACTIVE","employerCategory":"RIDE_HAILING","isMock":true,"accountTenureDays":120,"orderCompletionRate":68.0,"totalCompletedOrders":200,"activeOrdersLast30Days":15,"ordersPerActiveDay":4.0,"activeDaysPerWeek":3.0,"accountAgeActiveRatio":0.30,"longestInactivityStreakDays":18,"gapFrequency90d":4,"peakHourParticipationRate":0.25,"cancellationRate":0.08,"cancellationSpikeFlag":false,"overallRating":3.4,"ratingLast30d":3.2,"ratingLast90d":3.5,"repeatCustomerRatio":0.06,"unresolvedDisputeCount":3,"disputeSpikeFlag":false,"responseRateToOffers":0.35,"consecutiveActiveMonths":2.0,"completionRateDrift":-7.0,"tenureWeightedReliabilityScore":22.0,"totalEarningsLast90DaysRupees":16000}',
200, true, NOW());

-- Chirag → Average: mediocre employer stats
INSERT INTO provider_response (id, user_id, provider_type, platform_key, response_json, http_status, is_mock, fetched_at)
VALUES (gen_random_uuid(), 'c1000000-0000-0000-0000-000000007003', 'PLATFORM', 'FREELANCEX',
'{"platformKey":"FREELANCEX","platformName":"FreelanceX","accountStatus":"ACTIVE","employerCategory":"FREELANCER","isMock":true,"accountTenureDays":200,"orderCompletionRate":74.0,"totalCompletedOrders":350,"activeOrdersLast30Days":22,"ordersPerActiveDay":5.0,"activeDaysPerWeek":3.5,"accountAgeActiveRatio":0.40,"longestInactivityStreakDays":12,"gapFrequency90d":3,"peakHourParticipationRate":0.35,"cancellationRate":0.05,"cancellationSpikeFlag":true,"overallRating":3.7,"ratingLast30d":3.6,"ratingLast90d":3.8,"repeatCustomerRatio":0.10,"unresolvedDisputeCount":2,"disputeSpikeFlag":false,"responseRateToOffers":0.45,"consecutiveActiveMonths":4.0,"completionRateDrift":-4.0,"tenureWeightedReliabilityScore":35.0,"totalEarningsLast90DaysRupees":28000}',
200, true, NOW());

-- Dev → Good: solid employer stats
INSERT INTO provider_response (id, user_id, provider_type, platform_key, response_json, http_status, is_mock, fetched_at)
VALUES (gen_random_uuid(), 'c1000000-0000-0000-0000-000000007004', 'PLATFORM', 'HOMEFIXIT',
'{"platformKey":"HOMEFIXIT","platformName":"HomeFixIt","accountStatus":"ACTIVE","employerCategory":"HOME_SERVICES","isMock":true,"accountTenureDays":380,"orderCompletionRate":86.0,"totalCompletedOrders":600,"activeOrdersLast30Days":35,"ordersPerActiveDay":7.0,"activeDaysPerWeek":5.0,"accountAgeActiveRatio":0.58,"longestInactivityStreakDays":6,"gapFrequency90d":2,"peakHourParticipationRate":0.52,"cancellationRate":0.02,"cancellationSpikeFlag":false,"overallRating":4.2,"ratingLast30d":4.3,"ratingLast90d":4.2,"repeatCustomerRatio":0.18,"unresolvedDisputeCount":0,"disputeSpikeFlag":false,"responseRateToOffers":0.65,"consecutiveActiveMonths":8.0,"completionRateDrift":-1.5,"tenureWeightedReliabilityScore":55.0,"totalEarningsLast90DaysRupees":30000}',
200, true, NOW());

-- Golu → Very Good: strong employer stats
INSERT INTO provider_response (id, user_id, provider_type, platform_key, response_json, http_status, is_mock, fetched_at)
VALUES (gen_random_uuid(), 'c1000000-0000-0000-0000-000000007005', 'PLATFORM', 'ZOMADASH',
'{"platformKey":"ZOMADASH","platformName":"ZomaDash","accountStatus":"ACTIVE","employerCategory":"FOOD_DELIVERY","isMock":true,"accountTenureDays":600,"orderCompletionRate":93.0,"totalCompletedOrders":1200,"activeOrdersLast30Days":55,"ordersPerActiveDay":9.0,"activeDaysPerWeek":6.0,"accountAgeActiveRatio":0.72,"longestInactivityStreakDays":3,"gapFrequency90d":1,"peakHourParticipationRate":0.65,"cancellationRate":0.01,"cancellationSpikeFlag":false,"overallRating":4.7,"ratingLast30d":4.75,"ratingLast90d":4.65,"repeatCustomerRatio":0.25,"unresolvedDisputeCount":0,"disputeSpikeFlag":false,"responseRateToOffers":0.80,"consecutiveActiveMonths":14.0,"completionRateDrift":-0.5,"tenureWeightedReliabilityScore":78.0,"totalEarningsLast90DaysRupees":56000}',
200, true, NOW());

-- Harish → Excellent: top-tier employer stats
INSERT INTO provider_response (id, user_id, provider_type, platform_key, response_json, http_status, is_mock, fetched_at)
VALUES (gen_random_uuid(), 'c1000000-0000-0000-0000-000000007006', 'PLATFORM', 'OLACAB',
'{"platformKey":"OLACAB","platformName":"OlaCab","accountStatus":"ACTIVE","employerCategory":"RIDE_HAILING","isMock":true,"accountTenureDays":900,"orderCompletionRate":97.0,"totalCompletedOrders":2500,"activeOrdersLast30Days":80,"ordersPerActiveDay":12.0,"activeDaysPerWeek":7.0,"accountAgeActiveRatio":0.88,"longestInactivityStreakDays":1,"gapFrequency90d":0,"peakHourParticipationRate":0.82,"cancellationRate":0.005,"cancellationSpikeFlag":false,"overallRating":4.9,"ratingLast30d":4.92,"ratingLast90d":4.88,"repeatCustomerRatio":0.32,"unresolvedDisputeCount":0,"disputeSpikeFlag":false,"responseRateToOffers":0.92,"consecutiveActiveMonths":24.0,"completionRateDrift":0.5,"tenureWeightedReliabilityScore":92.0,"totalEarningsLast90DaysRupees":75000}',
200, true, NOW());

-- Ishan → Average (new to credit): below-average employer stats
INSERT INTO provider_response (id, user_id, provider_type, platform_key, response_json, http_status, is_mock, fetched_at)
VALUES (gen_random_uuid(), 'c1000000-0000-0000-0000-000000007007', 'PLATFORM', 'SWIFTMART',
'{"platformKey":"SWIFTMART","platformName":"SwiftMart","accountStatus":"ACTIVE","employerCategory":"FOOD_DELIVERY","isMock":true,"accountTenureDays":180,"orderCompletionRate":72.0,"totalCompletedOrders":280,"activeOrdersLast30Days":18,"ordersPerActiveDay":4.5,"activeDaysPerWeek":3.5,"accountAgeActiveRatio":0.38,"longestInactivityStreakDays":14,"gapFrequency90d":3,"peakHourParticipationRate":0.30,"cancellationRate":0.06,"cancellationSpikeFlag":false,"overallRating":3.6,"ratingLast30d":3.5,"ratingLast90d":3.7,"repeatCustomerRatio":0.08,"unresolvedDisputeCount":1,"disputeSpikeFlag":false,"responseRateToOffers":0.40,"consecutiveActiveMonths":3.0,"completionRateDrift":-5.0,"tenureWeightedReliabilityScore":30.0,"totalEarningsLast90DaysRupees":26000}',
200, true, NOW());

-- Jaya → Good (new to credit): decent employer stats
INSERT INTO provider_response (id, user_id, provider_type, platform_key, response_json, http_status, is_mock, fetched_at)
VALUES (gen_random_uuid(), 'c1000000-0000-0000-0000-000000007008', 'PLATFORM', 'RIDEQUICK',
'{"platformKey":"RIDEQUICK","platformName":"RideQuick","accountStatus":"ACTIVE","employerCategory":"RIDE_HAILING","isMock":true,"accountTenureDays":320,"orderCompletionRate":84.0,"totalCompletedOrders":500,"activeOrdersLast30Days":30,"ordersPerActiveDay":6.5,"activeDaysPerWeek":4.5,"accountAgeActiveRatio":0.52,"longestInactivityStreakDays":7,"gapFrequency90d":2,"peakHourParticipationRate":0.48,"cancellationRate":0.025,"cancellationSpikeFlag":false,"overallRating":4.1,"ratingLast30d":4.15,"ratingLast90d":4.1,"repeatCustomerRatio":0.15,"unresolvedDisputeCount":0,"disputeSpikeFlag":false,"responseRateToOffers":0.60,"consecutiveActiveMonths":6.0,"completionRateDrift":-2.0,"tenureWeightedReliabilityScore":48.0,"totalEarningsLast90DaysRupees":38000}',
200, true, NOW());

-- Also clear old snapshots and scoring jobs so scores are fresh
DELETE FROM scoring_job WHERE user_id IN (
  'c1000000-0000-0000-0000-000000007001','c1000000-0000-0000-0000-000000007002',
  'c1000000-0000-0000-0000-000000007003','c1000000-0000-0000-0000-000000007004',
  'c1000000-0000-0000-0000-000000007005','c1000000-0000-0000-0000-000000007006',
  'c1000000-0000-0000-0000-000000007007','c1000000-0000-0000-0000-000000007008'
);
DELETE FROM prism_score_snapshot WHERE user_id IN (
  'c1000000-0000-0000-0000-000000007001','c1000000-0000-0000-0000-000000007002',
  'c1000000-0000-0000-0000-000000007003','c1000000-0000-0000-0000-000000007004',
  'c1000000-0000-0000-0000-000000007005','c1000000-0000-0000-0000-000000007006',
  'c1000000-0000-0000-0000-000000007007','c1000000-0000-0000-0000-000000007008'
);
