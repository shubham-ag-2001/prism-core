-- V20: Create and seed mock SMS payloads for UI testing
-- demo_ui_payloads stores the exact SMS JSON body to send to /providers/sms/ingest per user.

CREATE TABLE IF NOT EXISTS demo_ui_payloads (
    user_id          UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    sms_payload_json TEXT NOT NULL
);

-- Insert only for users that actually exist (safe JOIN pattern avoids FK errors)
INSERT INTO demo_ui_payloads (user_id, sms_payload_json)
SELECT u.id, v.payload
FROM users u
JOIN (VALUES
    ('a1000000-0000-0000-0000-000000001001'::uuid, '{"messages":[{"sender":"VM-HDFCBK","body":"Rs. 3500 credited to a/c X1234 (Zomato Payout)","timestamp":1715263800000},{"sender":"AD-ICICIB","body":"Rs. 150 spent on Swiggy","timestamp":1715177400000}]}'),
    ('a1000000-0000-0000-0000-000000001002'::uuid, '{"messages":[{"sender":"VM-SBINB","body":"Rs. 4200 credited to a/c X5678 (Uber Payout)","timestamp":1715263800000},{"sender":"AD-HDFCBK","body":"EMI of Rs. 2000 bounced due to insufficient funds.","timestamp":1715177400000}]}'),
    ('a1000000-0000-0000-0000-000000001003'::uuid, '{"messages":[{"sender":"VM-ICICIB","body":"Rs. 3000 credited to a/c X9012 (Upwork)","timestamp":1715263800000},{"sender":"AD-SBINB","body":"Outstanding personal loan due: Rs. 4500","timestamp":1715177400000}]}'),
    ('a1000000-0000-0000-0000-000000001004'::uuid, '{"messages":[{"sender":"VM-HDFCBK","body":"Rs. 3800 credited to a/c X3456 (Urban Company)","timestamp":1715263800000}]}'),
    ('a1000000-0000-0000-0000-000000001005'::uuid, '{"messages":[{"sender":"VM-AXISBK","body":"Rs. 3100 credited to a/c X7890 (Swiggy Payout)","timestamp":1715263800000},{"sender":"AD-PAYTM","body":"Wallet recharged with Rs. 500","timestamp":1715177400000}]}'),
    ('a1000000-0000-0000-0000-000000001011'::uuid, '{"messages":[{"sender":"VM-HDFCBK","body":"Rs. 4500 credited to a/c X1234 (Zomato)","timestamp":1715263800000}]}'),
    ('a1000000-0000-0000-0000-000000001012'::uuid, '{"messages":[{"sender":"VM-ICICIB","body":"Rs. 3900 credited to a/c X5678 (Uber)","timestamp":1715263800000}]}'),
    ('a1000000-0000-0000-0000-000000001013'::uuid, '{"messages":[{"sender":"VM-SBINB","body":"Rs. 2500 credited to a/c X9012 (Upwork)","timestamp":1715263800000},{"sender":"AD-KOTAK","body":"Loan EMI Overdue notice","timestamp":1715177400000}]}'),
    ('a1000000-0000-0000-0000-000000001014'::uuid, '{"messages":[{"sender":"VM-AXISBK","body":"Rs. 4100 credited to a/c X3456 (Urban Company)","timestamp":1715263800000}]}'),
    ('a1000000-0000-0000-0000-000000001015'::uuid, '{"messages":[{"sender":"VM-HDFCBK","body":"Rs. 3600 credited to a/c X7890 (Swiggy)","timestamp":1715263800000}]}'),
    ('a1000000-0000-0000-0000-000000001016'::uuid, '{"messages":[{"sender":"VM-ICICIB","body":"Rs. 4800 credited to a/c X1234 (Uber)","timestamp":1715263800000},{"sender":"AD-HDFCBK","body":"Credit card payment missed.","timestamp":1715177400000}]}'),
    ('a1000000-0000-0000-0000-000000001017'::uuid, '{"messages":[{"sender":"VM-SBINB","body":"Rs. 4300 credited to a/c X5678 (Upwork)","timestamp":1715263800000}]}'),
    ('a1000000-0000-0000-0000-000000001018'::uuid, '{"messages":[{"sender":"VM-AXISBK","body":"Rs. 3200 credited to a/c X9012 (Urban Company)","timestamp":1715263800000}]}'),
    ('a1000000-0000-0000-0000-000000001019'::uuid, '{"messages":[{"sender":"VM-HDFCBK","body":"Rs. 4000 credited to a/c X3456 (Zomato)","timestamp":1715263800000}]}'),
    ('a1000000-0000-0000-0000-000000001020'::uuid, '{"messages":[{"sender":"VM-ICICIB","body":"Rs. 3700 credited to a/c X7890 (Uber)","timestamp":1715263800000}]}')
) AS v(uid, payload) ON u.id = v.uid;
