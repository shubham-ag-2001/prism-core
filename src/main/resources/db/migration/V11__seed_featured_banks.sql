-- V11: Seed Featured Banks (Dummy names only — no real bank data)

INSERT INTO featured_banks (
    bank_name, tagline, logo_url, loan_page_url,
    min_loan_amount, max_loan_amount,
    interest_rate_from, interest_rate_to,
    processing_fee_pct, max_tenure_months,
    features_json, is_active, display_order
) VALUES
(
    'NexaFinance Bank',
    'Fast loans built for gig workers',
    'https://assets.prism.app/banks/nexafinance.png',
    'https://nexafinance.example.com/gig-loans',
    10000, 500000,
    10.5, 16.0,
    1.0, 36,
    '["No credit history required","Instant disbursal in 24h","Zero prepayment charges"]',
    TRUE, 1
),
(
    'FlexCredit Union',
    'Flexible repayment on your schedule',
    'https://assets.prism.app/banks/flexcredit.png',
    'https://flexcredit.example.com/personal-loans',
    5000, 300000,
    12.0, 20.0,
    1.5, 24,
    '["EMI holiday for first month","PRISM score accepted","Minimal documentation"]',
    TRUE, 2
),
(
    'GigCapital Ltd',
    'Empowering the gig economy',
    'https://assets.prism.app/banks/gigcapital.png',
    'https://gigcapital.example.com/loans',
    20000, 1000000,
    9.5, 14.5,
    0.5, 60,
    '["Largest loan amounts","Lowest processing fee","Dedicated gig worker support"]',
    TRUE, 3
),
(
    'SwiftLend Financial',
    'Same-day approval, next-day cash',
    'https://assets.prism.app/banks/swiftlend.png',
    'https://swiftlend.example.com/quick-loans',
    2000, 150000,
    14.0, 22.0,
    2.0, 18,
    '["Apply in 3 minutes","PRISM-verified applicants get priority","No salary slip needed"]',
    TRUE, 4
),
(
    'ZeroFee Creditors',
    'Zero processing fee, always',
    'https://assets.prism.app/banks/zerofee.png',
    'https://zerofee.example.com/gig-credit',
    15000, 750000,
    11.0, 17.5,
    0.0, 48,
    '["0% processing fee","Credit line model","Pay interest only on usage"]',
    TRUE, 5
);
