INSERT INTO users (name, email, password, role_id, enabled, created_at, updated_at)
SELECT
    'Dev User',
    'dev@learnsoft.local',
    '$2a$10$F08Z3n2OOSIVm/tyhB1l9ON7hLQxU7y3IsXTgsuj1ldVS7f8JHAm.',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'),
    TRUE,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'dev@learnsoft.local'
)
ON CONFLICT (email) DO UPDATE
SET name = EXCLUDED.name,
    password = EXCLUDED.password,
    role_id = EXCLUDED.role_id,
    enabled = TRUE,
    updated_at = NOW();
