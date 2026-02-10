INSERT INTO app_permission (code, description)
SELECT 'CASH_DRAWER_OPEN', 'Allows opening the cash drawer without sale finalization'
WHERE NOT EXISTS (SELECT 1 FROM app_permission WHERE code = 'CASH_DRAWER_OPEN');

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM app_role r
JOIN app_permission p ON p.code = 'CASH_DRAWER_OPEN'
WHERE r.code = 'MANAGER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM app_role r
JOIN app_permission p ON p.code = 'CASH_DRAWER_OPEN'
WHERE r.code = 'CASHIER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);
