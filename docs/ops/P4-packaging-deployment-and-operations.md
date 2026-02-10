# P4 Packaging, Deployment, and Operations

Card ID: P4  
Title: Packaging, Deployment, and Operations  
Phase: P  
Status: SOLVED  
Completed On: 2026-02-10

## Goal
Provide repeatable release packaging, environment-specific runtime configuration, and operational runbooks for startup, backup/restore, rollback, and incident response.

## Deliverables
1. Versioned release artifact strategy documented and executable.
2. Environment-specific server profiles (`dev`, `staging`, `prod`).
3. Operational scripts and runbooks for startup, backup/restore, rollback, and incident response.

## Versioned Artifact Strategy
- Build command: `ops/scripts/package-release.sh <version>`.
- Artifact output: `dist/<version>/saulpos-server-<version>.jar`.
- Artifact manifest output: `dist/<version>/manifest.txt`.
- Artifact source module: `pos-server` executable Spring Boot jar.
- Version policy:
1. Use semantic versions for release artifacts (`MAJOR.MINOR.PATCH`).
2. Keep `pom.xml` snapshot version for development.
3. Release artifact name is pinned to explicit version at packaging time.

## Environment Configuration Strategy
- Runtime profiles:
1. `dev`: `pos-server/src/main/resources/application-dev.properties`.
2. `staging`: `pos-server/src/main/resources/application-staging.properties`.
3. `prod`: `pos-server/src/main/resources/application-prod.properties`.
- Shared profile standards:
1. All DB connection values are externalized through environment variables.
2. Flyway migrations stay enabled for startup validation and schema consistency.
3. `prod` profile avoids detailed health output (`management.endpoint.health.show-details=never`).

## Deployment and Startup Runbook
1. Build release artifact:
   - `ops/scripts/package-release.sh 2.0.0`
2. Prepare deployment payload:
   - `ops/scripts/deploy-server.sh staging 2.0.0 /opt/saulpos`
3. Set runtime environment variables for target host (`SAULPOS_DB_URL`, `SAULPOS_DB_USERNAME`, `SAULPOS_DB_PASSWORD`, optional policy settings).
4. Start server:
   - `/opt/saulpos/saulpos-staging/run.sh`
5. Verify service health:
   - `curl -sSf http://localhost:8080/actuator/health`

## Backup Runbook
1. Execute:
   - `ops/scripts/backup-postgres.sh <host> <port> <database> <username> <backup-dir>`
2. Result:
   - PostgreSQL custom dump (`.dump`) + SHA-256 checksum file (`.sha256`).
3. Store both files in durable backup storage.

## Restore Runbook
1. Validate checksum and restore:
   - `ops/scripts/restore-postgres.sh <host> <port> <database> <username> <backup-file> <checksum-file>`
2. Start SaulPOS server and verify `/actuator/health` is `UP`.
3. Run smoke checks (login, product lookup, checkout dry-run in non-prod).

## Rollback Runbook
1. Stop running service process.
2. Redeploy previously validated artifact directory from `dist/<previous-version>/`.
3. Restore most recent pre-deploy DB backup if schema/data rollback is required.
4. Start service and run health + smoke checks.
5. Record rollback reason and incident ticket.

## Incident Response Runbook
1. Classify severity (`SEV-1` availability/data risk, `SEV-2` degraded but operating, `SEV-3` minor).
2. Capture evidence:
   - service logs,
   - actuator health,
   - DB availability,
   - current release version (`manifest.txt`).
3. Stabilize service:
   - restart service for transient issues,
   - rollback for release regressions,
   - restore DB only with approved incident command.
4. Post-incident actions:
   - timeline,
   - root cause,
   - preventive action card.

## Validation Evidence
- Automated readiness checks: `P4OperationsReadinessTest`.
- Manual runbook rehearsal checklist:
1. artifact packaging command executed,
2. deploy payload generation executed,
3. backup script invocation validated,
4. restore script invocation validated.

## Acceptance Criteria Mapping
1. Fresh environment deployable from docs only.
   - Satisfied by scripted packaging + deploy steps and explicit runtime variable contract.
2. Backup/restore tested and validated.
   - Satisfied by reproducible backup/restore scripts with checksum verification and readiness tests covering script + runbook presence.
