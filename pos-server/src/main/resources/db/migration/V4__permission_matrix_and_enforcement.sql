INSERT INTO app_permission (code, description)
SELECT 'SALES_PROCESS', 'Allows checkout and sale processing operations'
WHERE NOT EXISTS (SELECT 1 FROM app_permission WHERE code = 'SALES_PROCESS');

INSERT INTO app_permission (code, description)
SELECT 'REFUND_PROCESS', 'Allows refund and return processing operations'
WHERE NOT EXISTS (SELECT 1 FROM app_permission WHERE code = 'REFUND_PROCESS');

INSERT INTO app_permission (code, description)
SELECT 'INVENTORY_ADJUST', 'Allows inventory adjustment operations'
WHERE NOT EXISTS (SELECT 1 FROM app_permission WHERE code = 'INVENTORY_ADJUST');

INSERT INTO app_permission (code, description)
SELECT 'REPORT_VIEW', 'Allows report and analytics access'
WHERE NOT EXISTS (SELECT 1 FROM app_permission WHERE code = 'REPORT_VIEW');

INSERT INTO app_permission (code, description)
SELECT 'CONFIGURATION_MANAGE', 'Allows role and system configuration operations'
WHERE NOT EXISTS (SELECT 1 FROM app_permission WHERE code = 'CONFIGURATION_MANAGE');

INSERT INTO app_role (code, description)
SELECT 'MANAGER', 'Manager role with broad operational access'
WHERE NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'MANAGER');

INSERT INTO app_role (code, description)
SELECT 'CASHIER', 'Cashier role focused on checkout operations'
WHERE NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'CASHIER');

INSERT INTO app_role (code, description)
SELECT 'STOCKER', 'Inventory operator role'
WHERE NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'STOCKER');

INSERT INTO app_role (code, description)
SELECT 'ANALYST', 'Reporting role with read-only report access'
WHERE NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'ANALYST');

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM app_role r
JOIN app_permission p ON p.code = 'SALES_PROCESS'
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
JOIN app_permission p ON p.code = 'REFUND_PROCESS'
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
JOIN app_permission p ON p.code = 'INVENTORY_ADJUST'
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
JOIN app_permission p ON p.code = 'REPORT_VIEW'
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
JOIN app_permission p ON p.code = 'CONFIGURATION_MANAGE'
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
JOIN app_permission p ON p.code = 'SALES_PROCESS'
WHERE r.code = 'CASHIER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM app_role r
JOIN app_permission p ON p.code = 'REFUND_PROCESS'
WHERE r.code = 'CASHIER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM app_role r
JOIN app_permission p ON p.code = 'INVENTORY_ADJUST'
WHERE r.code = 'STOCKER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM app_role r
JOIN app_permission p ON p.code = 'REPORT_VIEW'
WHERE r.code = 'ANALYST'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);
