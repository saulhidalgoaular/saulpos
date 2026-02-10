# P3 Secret and Configuration Hardening Checklist

Use this checklist for `P3` evidence. Mark each item as `DONE`, `N/A`, or `REMEDIATION REQUIRED`.

## Secret Management
- [ ] Runtime secrets (DB password, token keys, external provider credentials) are injected from environment or secret manager, not hardcoded.
- [ ] No secrets are committed in tracked files (`application*.properties`, scripts, docs, tests).
- [ ] Placeholder/default credentials are removed or blocked in non-test profiles.
- [ ] Secret rotation procedure exists and is tested for at least one credential type.
- [ ] Service account privileges follow least privilege for database and infrastructure access.

## Authentication and Authorization Configuration
- [ ] `app.security.max-failed-attempts` is set to a finite non-zero value in deployment configs.
- [ ] `app.security.lock-duration-minutes` is set and aligned with operational policy.
- [ ] Access-token and refresh-token TTLs are explicitly set and reviewed (`app.security.access-token-ttl-minutes`, `app.security.refresh-token-ttl-minutes`).
- [ ] Role/permission seed and mapping changes are reviewed for deny-by-default behavior.
- [ ] Sensitive endpoints are included in RBAC regression runs (`P3SecurityComplianceIntegrationTest` plus existing permission matrix suite).

## Auditability and Traceability
- [ ] Auth events (`LOGIN`, `LOGOUT`, failed login/lockout) are retained and queryable.
- [ ] Sensitive action events (for example no-sale drawer opens, overrides, reprints) are retained and queryable.
- [ ] Correlation IDs are propagated on write-path requests and visible in logs/audit rows.
- [ ] Audit log retention and access policy is documented for operations/compliance review.

## Runtime and Ops Hardening
- [ ] Production actuator exposure is restricted to required endpoints only.
- [ ] Non-production-only settings are not enabled in production profile.
- [ ] Backup/restore verification includes security-critical tables (auth session/audit and exception/audit event tables).
- [ ] Incident response runbook includes credential leak and unauthorized-access procedures.

## Sign-off
- Reviewer:
- Review date (UTC):
- Outstanding remediation items:
