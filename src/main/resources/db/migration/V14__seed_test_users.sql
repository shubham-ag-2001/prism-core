-- V14: Seed Fresh Test Users for Scoring Engine Verification
-- These users have NO cached scores and NO onboarding data yet,
-- but the backend Orchestrator will detect their phone numbers
-- and inject specific mock inputs to demonstrate different score ranges.

-- 1. U001 (Arjun Mehta) - Expected: ~820-860 (Excellent)
INSERT INTO users (id, phone, role, is_active, created_at, updated_at)
VALUES ('b0000000-0000-0000-0000-000000000001', '8000000001', 'GIG_WORKER', true, NOW(), NOW());
INSERT INTO user_profiles (id, user_id, full_name, city, created_at, updated_at)
VALUES (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000001', 'Arjun Mehta', 'Mumbai', NOW(), NOW());

-- 2. U002 (Priya Nair) - Expected: ~580-630 (Average)
INSERT INTO users (id, phone, role, is_active, created_at, updated_at)
VALUES ('b0000000-0000-0000-0000-000000000002', '8000000002', 'GIG_WORKER', true, NOW(), NOW());
INSERT INTO user_profiles (id, user_id, full_name, city, created_at, updated_at)
VALUES (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000002', 'Priya Nair', 'Pune', NOW(), NOW());

-- 3. U003 (Salman Sheikh) - Expected: ~360-410 (Poor)
INSERT INTO users (id, phone, role, is_active, created_at, updated_at)
VALUES ('b0000000-0000-0000-0000-000000000003', '8000000003', 'GIG_WORKER', true, NOW(), NOW());
INSERT INTO user_profiles (id, user_id, full_name, city, created_at, updated_at)
VALUES (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000003', 'Salman Sheikh', 'Lucknow', NOW(), NOW());

-- 4. U004 (Deepak Rao) - Expected: ~620-660 (Good)
INSERT INTO users (id, phone, role, is_active, created_at, updated_at)
VALUES ('b0000000-0000-0000-0000-000000000004', '8000000004', 'GIG_WORKER', true, NOW(), NOW());
INSERT INTO user_profiles (id, user_id, full_name, city, created_at, updated_at)
VALUES (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000004', 'Deepak Rao', 'Bangalore', NOW(), NOW());

-- 5. U005 (Kill Switch Demo) - Expected: D5 goes to 55 minimum, triggers alert
INSERT INTO users (id, phone, role, is_active, created_at, updated_at)
VALUES ('b0000000-0000-0000-0000-000000000005', '8000000005', 'GIG_WORKER', true, NOW(), NOW());
INSERT INTO user_profiles (id, user_id, full_name, city, created_at, updated_at)
VALUES (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000005', 'Kill Switch Demo User', 'Delhi', NOW(), NOW());

-- 6. U006 (Score Cap Demo) - Expected: SPD03 is capped at 25 due to negative balance
INSERT INTO users (id, phone, role, is_active, created_at, updated_at)
VALUES ('b0000000-0000-0000-0000-000000000006', '8000000006', 'GIG_WORKER', true, NOW(), NOW());
INSERT INTO user_profiles (id, user_id, full_name, city, created_at, updated_at)
VALUES (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000006', 'Score Cap Demo User', 'Chennai', NOW(), NOW());
