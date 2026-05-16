-- V29: Update employer platforms — remove Dunzo, add Grab and Others

DELETE FROM employer_platforms WHERE platform_key = 'DUNZO';

INSERT INTO employer_platforms (platform_key, display_name, category) VALUES
    ('GRAB',    'Grab',   'RIDE_HAILING'),
    ('OTHERS',  'Others', 'OTHERS')
ON CONFLICT (platform_key) DO NOTHING;
