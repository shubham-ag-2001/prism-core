-- V8: Seed Mock Users for Hackathon Demo
-- 5 GIG_WORKER users + 1 LENDER
-- Phone numbers are the login credentials (OTP mock will print to console)

INSERT INTO users (id, phone, role, is_active) VALUES
    ('a1000000-0000-0000-0000-000000000001', '9000000001', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000000002', '9000000002', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000000003', '9000000003', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000000004', '9000000004', 'GIG_WORKER', TRUE),
    ('a1000000-0000-0000-0000-000000000005', '9000000005', 'GIG_WORKER', TRUE),
    ('b2000000-0000-0000-0000-000000000001', '9000000010', 'LENDER',     TRUE);

INSERT INTO user_profiles (user_id, full_name, email, city, state) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Arjun Sharma',   'arjun@demo.prism',  'Mumbai',    'Maharashtra'),
    ('a1000000-0000-0000-0000-000000000002', 'Priya Patel',    'priya@demo.prism',  'Bangalore', 'Karnataka'),
    ('a1000000-0000-0000-0000-000000000003', 'Ravi Kumar',     'ravi@demo.prism',   'Delhi',     'Delhi'),
    ('a1000000-0000-0000-0000-000000000004', 'Sneha Iyer',     'sneha@demo.prism',  'Chennai',   'Tamil Nadu'),
    ('a1000000-0000-0000-0000-000000000005', 'Mohit Gupta',    'mohit@demo.prism',  'Hyderabad', 'Telangana'),
    ('b2000000-0000-0000-0000-000000000001', 'Demo Lender',    'lender@demo.prism', 'Mumbai',    'Maharashtra');

INSERT INTO onboarding_data (user_id, pan_number, employer_name, employer_type, work_start_date, status, submitted_at) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'ABCDE1234F', 'Grab Delivery',   'FOOD_DELIVERY', '2022-03-15', 'COMPLETED', NOW()),
    ('a1000000-0000-0000-0000-000000000002', 'FGHIJ5678K', 'Grab Rides',      'RIDE_HAILING',  '2021-11-01', 'COMPLETED', NOW()),
    ('a1000000-0000-0000-0000-000000000003', 'LMNOP9012Q', 'Swiggy',          'FOOD_DELIVERY', '2023-01-10', 'COMPLETED', NOW()),
    ('a1000000-0000-0000-0000-000000000004', 'RSTUV3456W', 'Freelancer',      'FREELANCER',    '2020-07-22', 'COMPLETED', NOW()),
    ('a1000000-0000-0000-0000-000000000005', 'XYZAB7890C', 'Urban Company',   'HOME_SERVICES', '2022-09-05', 'COMPLETED', NOW());

INSERT INTO pan_verifications (user_id, pan_number, verification_status, verification_response_json, verified_at) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'ABCDE1234F', 'VERIFIED', '{"status":"VALID","mock":true}', NOW()),
    ('a1000000-0000-0000-0000-000000000002', 'FGHIJ5678K', 'VERIFIED', '{"status":"VALID","mock":true}', NOW()),
    ('a1000000-0000-0000-0000-000000000003', 'LMNOP9012Q', 'VERIFIED', '{"status":"VALID","mock":true}', NOW()),
    ('a1000000-0000-0000-0000-000000000004', 'RSTUV3456W', 'VERIFIED', '{"status":"VALID","mock":true}', NOW()),
    ('a1000000-0000-0000-0000-000000000005', 'XYZAB7890C', 'VERIFIED', '{"status":"VALID","mock":true}', NOW());

-- Seed PRISM scores for 3 users so lender can demo score lookup immediately
INSERT INTO prism_score_snapshot (id, user_id, final_score, scoring_status, rule_set_version, triggered_by, computed_at, expires_at)
VALUES
    ('c3000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 742, 'COMPLETE', 'v1.0', 'MANUAL', NOW(), NOW() + INTERVAL '30 days'),
    ('c3000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000002', 681, 'COMPLETE', 'v1.0', 'MANUAL', NOW(), NOW() + INTERVAL '30 days'),
    ('c3000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000003', 598, 'COMPLETE', 'v1.0', 'MANUAL', NOW(), NOW() + INTERVAL '30 days');
