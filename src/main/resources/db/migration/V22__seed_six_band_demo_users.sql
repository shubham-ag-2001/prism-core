-- V22: Seed 6 demo users covering all score bands (Poor → Excellent)
-- Phone: 7000000001-7000000006  Names: Arun, Bhavya, Chirag, Dev, Golu, Harish
-- UUIDs: c1000000-0000-0000-0000-00000000700N

INSERT INTO users (id, phone, role, is_active)
SELECT v.id, v.phone, v.role, v.is_active
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007001'::uuid, '7000000001', 'GIG_WORKER', TRUE),
    ('c1000000-0000-0000-0000-000000007002'::uuid, '7000000002', 'GIG_WORKER', TRUE),
    ('c1000000-0000-0000-0000-000000007003'::uuid, '7000000003', 'GIG_WORKER', TRUE),
    ('c1000000-0000-0000-0000-000000007004'::uuid, '7000000004', 'GIG_WORKER', TRUE),
    ('c1000000-0000-0000-0000-000000007005'::uuid, '7000000005', 'GIG_WORKER', TRUE),
    ('c1000000-0000-0000-0000-000000007006'::uuid, '7000000006', 'GIG_WORKER', TRUE)
) AS v(id, phone, role, is_active)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = v.id);

INSERT INTO user_profiles (user_id, full_name, email, city, state)
SELECT v.uid, v.full_name, v.email, v.city, v.state
FROM (VALUES
    ('c1000000-0000-0000-0000-000000007001'::uuid, 'Arun',   'arun@demo.prism',   'Delhi',     'Delhi'),
    ('c1000000-0000-0000-0000-000000007002'::uuid, 'Bhavya', 'bhavya@demo.prism', 'Jaipur',    'Rajasthan'),
    ('c1000000-0000-0000-0000-000000007003'::uuid, 'Chirag', 'chirag@demo.prism', 'Ahmedabad', 'Gujarat'),
    ('c1000000-0000-0000-0000-000000007004'::uuid, 'Dev',    'dev@demo.prism',    'Mumbai',    'Maharashtra'),
    ('c1000000-0000-0000-0000-000000007005'::uuid, 'Golu',   'golu@demo.prism',   'Bangalore', 'Karnataka'),
    ('c1000000-0000-0000-0000-000000007006'::uuid, 'Harish', 'harish@demo.prism', 'Chennai',   'Tamil Nadu')
) AS v(uid, full_name, email, city, state)
WHERE NOT EXISTS (SELECT 1 FROM user_profiles up WHERE up.user_id = v.uid);
