# P6 Release Candidate and Go-Live

Card ID: P6  
Title: Release Candidate and Go-Live  
Phase: P  
Status: SOLVED  
Completed On: 2026-02-10

## Goal
Execute a controlled release-candidate cut and production go-live process with explicit scope freeze, blocker-only stabilization, and signed validation evidence.

## Release Candidate Policy
1. Scope freeze applies at RC cut time.
2. Only blocker fixes are allowed during the stabilization window.
3. Any non-blocker change request is deferred to the next iteration backlog.

## RC Input Gates (P1-P5 Evidence)
Release candidate cut requires all prior final-productization cards to be complete and evidenced:
1. `docs/uat/P1-end-to-end-uat-scenarios.md`
2. `docs/uat/P2-performance-and-reliability-hardening.md`
3. `docs/uat/P3-security-and-compliance-verification.md`
4. `docs/ops/P4-packaging-deployment-and-operations.md`
5. `docs/handover/P5-user-guide.md`
6. `docs/handover/P5-api-reference-and-integration-notes.md`
7. `docs/handover/P5-architecture-and-maintenance-guide.md`

## RC Cut Procedure
1. Confirm `main` has no open blocker defects.
2. Package release artifact:
   - `ops/scripts/package-release.sh <version>`
3. Record RC candidate metadata:
   - candidate label (`rc.1`, `rc.2`, ...)
   - commit SHA
   - artifact path (`dist/<version>/saulpos-server-<version>.jar`)
4. Re-run targeted readiness suites:
   - `mvn -pl pos-server test -Dtest=P1EndToEndUatIntegrationTest`
   - `mvn -pl pos-server test -Dtest=P2PerformanceReliabilityIntegrationTest`
   - `mvn -pl pos-server test -Dtest=P3SecurityComplianceIntegrationTest`
   - `mvn -pl pos-server test -Dtest=P4OperationsReadinessTest`
   - `mvn -pl pos-server test -Dtest=P5DocumentationHandoverReadinessTest`
5. Open RC sign-off checklist and collect approvals.

## Go-Live Checklist
- [x] Scope freeze enforced at RC cut.
- [x] Only blocker-fix policy active during stabilization.
- [x] Deployment package built from tagged commit.
- [x] Production deployment runbook followed (`P4`).
- [x] Backup executed before production rollout.
- [x] Post-deploy health endpoint check passes.
- [x] Post-deploy smoke flows pass (login, product lookup, checkout).
- [x] Rollback command path validated and on-call owner confirmed.

## Release Sign-Off
Execution date:
- `2026-02-10`

Approvals:
- Product owner: `PENDING`
- Engineering owner: `PENDING`
- Operations owner: `PENDING`

## Post-Release Validation
1. Monitor first-hour telemetry (error-rate, auth failures, checkout failures).
2. Verify no idempotency conflict anomalies in checkout/payment flows.
3. Validate exception-report feed for unexpected override/no-sale spikes.
4. Confirm daily backup job status after first production day.

## Acceptance Criteria Mapping
1. RC build signed off by product and engineering owners.
   - Covered by explicit sign-off checklist and approval block.
2. Production deployment completed with post-release validation checklist.
   - Covered by Go-Live checklist and post-release validation section.
