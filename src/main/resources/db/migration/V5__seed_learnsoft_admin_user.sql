INSERT INTO users (name, email, password, role_id, enabled, created_at, updated_at)
SELECT
    'LearnSoft Demo',
    'learnsoft@edu.uy',
    '$2a$10$pYy9Cg67P5xiKBf7.Q.K1O6hzbNQfDL.iRzEm040i8NpHVU.3N7ta',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'),
    TRUE,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'learnsoft@edu.uy'
)
ON CONFLICT (email) DO UPDATE
SET name = EXCLUDED.name,
    password = EXCLUDED.password,
    role_id = EXCLUDED.role_id,
    enabled = TRUE,
    updated_at = NOW();
