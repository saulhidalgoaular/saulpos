-- SaulPOS local bootstrap data for clean installations.
-- Run this after Flyway migrations have been applied at least once.
-- Default login created by this script:
--   username: admin
--   password: Pass123!

BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'MANAGER') THEN
        RAISE EXCEPTION 'Role MANAGER not found. Run server once so Flyway migrations are applied before bootstrap.';
    END IF;
END
$$;

INSERT INTO user_account (username, password_hash, is_active)
VALUES (
    'admin',
    '$2y$10$l6OGE8RbCChwJlo5OcHqe.zNFf.e6TcdM057VYYJQ4iBiug6PIt3K',
    TRUE
)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u
JOIN app_role r ON r.code = 'MANAGER'
WHERE u.username = 'admin'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO merchant (code, name, is_active)
VALUES ('DEMO', 'Demo Merchant', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO store_location (merchant_id, code, name, is_active)
SELECT m.id, 'DEMO-STORE', 'Demo Store', TRUE
FROM merchant m
WHERE m.code = 'DEMO'
ON CONFLICT (code) DO NOTHING;

INSERT INTO terminal_device (store_location_id, code, name, is_active)
SELECT s.id, 'POS-01', 'POS Terminal 01', TRUE
FROM store_location s
WHERE s.code = 'DEMO-STORE'
ON CONFLICT (code) DO NOTHING;

INSERT INTO store_user_assignment (user_id, store_location_id, role_id, is_active)
SELECT u.id, s.id, r.id, TRUE
FROM user_account u
JOIN store_location s ON s.code = 'DEMO-STORE'
JOIN app_role r ON r.code = 'MANAGER'
WHERE u.username = 'admin'
ON CONFLICT (user_id, store_location_id, role_id) DO NOTHING;

COMMIT;
