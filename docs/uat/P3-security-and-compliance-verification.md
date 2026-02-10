# P3 Security and Compliance Verification

## Scope
Card `P3` verifies:
1. RBAC regression behavior for sensitive operations.
2. Sensitive action audit-trail completeness.
3. Secret/configuration hardening checklist coverage.

## Executable Verification Suite
Primary suite:
- `pos-server/src/test/java/com/saulpos/server/uat/P3SecurityComplianceIntegrationTest.java`

Covered scenarios:
1. Deny-by-default checks return `POS-4030` for unauthorized role-management, cash-drawer, and exception-report access.
2. Authorized permissions (`CONFIGURATION_MANAGE`, `CASH_DRAWER_OPEN`, `REPORT_VIEW`) unlock those operations.
3. Auth lifecycle audit records (`LOGIN`, `LOGOUT`) and no-sale drawer audit events are persisted and queryable through exception-report APIs.

Run command:
- `mvn -pl pos-server test -Dtest=P3SecurityComplianceIntegrationTest`

## Secret and Configuration Hardening
Checklist document:
- `docs/security/P3-secret-configuration-hardening-checklist.md`

Execution expectation:
1. Checklist is reviewed before release candidate cut.
2. Any unchecked item must have an owner and remediation date.
