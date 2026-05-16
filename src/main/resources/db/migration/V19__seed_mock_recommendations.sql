-- V19: Seed mock recommendations for the 15 demo users
-- Uses SELECT-based INSERT to avoid FK violations if users don't exist yet.

INSERT INTO score_recommendation (user_id, category, title, description)
SELECT u.id, v.category, v.title, v.description
FROM users u
JOIN (VALUES
    ('a1000000-0000-0000-0000-000000001001'::uuid, 'PROFILE',  'Link a Bank Account',       'Linking your primary bank account can boost your score by proving income stability.'),
    ('a1000000-0000-0000-0000-000000001001'::uuid, 'ACTIVITY', 'Maintain High Acceptance',  'Keep your order acceptance rate above 90% this month to improve your platform reliability score.'),
    ('a1000000-0000-0000-0000-000000001002'::uuid, 'RISK',     'Clear Prior Default',       'We noticed a prior loan default on your PAN. Settling existing debts will significantly improve your PRISM score over time.'),
    ('a1000000-0000-0000-0000-000000001002'::uuid, 'INCOME',   'Diversify Income',          'Consider working on a secondary platform during peak hours to increase overall monthly earnings.'),
    ('a1000000-0000-0000-0000-000000001003'::uuid, 'SPENDING', 'Reduce Debt-to-Income',     'Your debt-to-income ratio is currently 18%. Try to pay down active instalments to lower this ratio.'),
    ('a1000000-0000-0000-0000-000000001003'::uuid, 'ACTIVITY', 'Increase Weekly Active Days','You currently average 6 active days per week. Maintaining this will steadily improve your tenure score.'),
    ('a1000000-0000-0000-0000-000000001004'::uuid, 'ACTIVITY', 'Avoid Inactivity Gaps',     'You had a gap of 8 inactive days recently. Consistent weekly activity helps build a stronger reliability profile.'),
    ('a1000000-0000-0000-0000-000000001004'::uuid, 'RISK',     'Watch Cancellation Rate',   'Your recent cancellation rate spiked. Keeping cancellations low is critical for high reliability scores.'),
    ('a1000000-0000-0000-0000-000000001005'::uuid, 'SPENDING', 'Control Discretionary Spend','Your recent discretionary spending was elevated. Building a savings buffer can improve your financial stability rating.'),
    ('a1000000-0000-0000-0000-000000001005'::uuid, 'INCOME',   'Work During Peak Hours',    'Only 52% of your activity is during peak hours. Increasing this can maximize your earnings per hour.'),
    ('a1000000-0000-0000-0000-000000001011'::uuid, 'ACTIVITY', 'Excellent Completion Rate', 'You are doing great! Maintain your 92% completion rate to keep your PRISM score in the Good band.'),
    ('a1000000-0000-0000-0000-000000001011'::uuid, 'PROFILE',  'Update Contact Info',       'Ensure your email and secondary phone number are up to date in your profile.'),
    ('a1000000-0000-0000-0000-000000001012'::uuid, 'RISK',     'Platform Cancellation Spike','Your recent cancellation rate triggered a risk flag. Reduce order cancellations to recover your score.'),
    ('a1000000-0000-0000-0000-000000001012'::uuid, 'INCOME',   'Consistent Earnings',       'Try to maintain a consistent weekly earnings average to reduce income volatility.'),
    ('a1000000-0000-0000-0000-000000001013'::uuid, 'RISK',     'Clear Prior Default',       'We noticed a prior loan default on your PAN. Settling existing debts will significantly improve your PRISM score over time.'),
    ('a1000000-0000-0000-0000-000000001013'::uuid, 'ACTIVITY', 'Increase Orders Per Day',   'Try completing 2 more orders per active day to boost your overall activity metric.'),
    ('a1000000-0000-0000-0000-000000001014'::uuid, 'SPENDING', 'Healthy Debt Ratio',        'Your debt-to-income ratio is low. Keep it up!'),
    ('a1000000-0000-0000-0000-000000001014'::uuid, 'ACTIVITY', 'Avoid Inactivity Gaps',     'Consistent weekly activity helps build a stronger reliability profile.'),
    ('a1000000-0000-0000-0000-000000001015'::uuid, 'INCOME',   'Diversify Income',          'Consider working on a secondary platform during peak hours to increase overall monthly earnings.'),
    ('a1000000-0000-0000-0000-000000001015'::uuid, 'PROFILE',  'Link a Bank Account',       'Linking your primary bank account can boost your score by proving income stability.'),
    ('a1000000-0000-0000-0000-000000001016'::uuid, 'RISK',     'Clear Prior Default',       'We noticed a prior loan default on your PAN. Settling existing debts will significantly improve your PRISM score over time.'),
    ('a1000000-0000-0000-0000-000000001016'::uuid, 'SPENDING', 'Watch Discretionary Spend', 'Your recent discretionary spending was elevated. Building a savings buffer can improve your financial stability rating.'),
    ('a1000000-0000-0000-0000-000000001017'::uuid, 'ACTIVITY', 'Great Customer Ratings',    'Your 4.7 rating is excellent! Maintain this to get access to premium loan offers.'),
    ('a1000000-0000-0000-0000-000000001017'::uuid, 'INCOME',   'Maintain Income Stability', 'You have had 6 stable income windows. Keep this consistency.'),
    ('a1000000-0000-0000-0000-000000001018'::uuid, 'ACTIVITY', 'Increase Weekly Active Days','You currently average 4 active days per week. Try increasing this to 5 to steadily improve your tenure score.'),
    ('a1000000-0000-0000-0000-000000001018'::uuid, 'SPENDING', 'Reduce Debt-to-Income',     'Your debt-to-income ratio is currently 19%. Try to pay down active instalments to lower this ratio.'),
    ('a1000000-0000-0000-0000-000000001019'::uuid, 'ACTIVITY', 'Excellent Completion Rate', 'You are doing great! Maintain your completion rate to keep your PRISM score in the Good band.'),
    ('a1000000-0000-0000-0000-000000001019'::uuid, 'PROFILE',  'Update Contact Info',       'Ensure your email and secondary phone number are up to date in your profile.'),
    ('a1000000-0000-0000-0000-000000001020'::uuid, 'INCOME',   'Work During Peak Hours',    'Increasing your peak hour participation can maximize your earnings per hour.'),
    ('a1000000-0000-0000-0000-000000001020'::uuid, 'ACTIVITY', 'Avoid Inactivity Gaps',     'You had a gap of 5 inactive days recently. Consistent weekly activity helps build a stronger reliability profile.')
) AS v(uid, category, title, description) ON u.id = v.uid;
