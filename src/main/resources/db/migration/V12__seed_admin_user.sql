-- V12: Seed Admin User

INSERT INTO users (id, phone, role, is_active) VALUES
    ('d9000000-0000-0000-0000-000000000001', '9999999999', 'ADMIN', TRUE);

INSERT INTO user_profiles (user_id, full_name, email, city, state) VALUES
    ('d9000000-0000-0000-0000-000000000001', 'PRISM Admin', 'admin@prism.app', 'Bangalore', 'Karnataka');
