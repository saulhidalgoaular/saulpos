# P5 Architecture and Maintenance Guide

Card ID: P5  
Title: Documentation and Handover  
Audience: Platform and Maintenance Engineers

## 1. Architecture Overview

- `pos-server` is the source of truth for domain logic and transactional integrity.
- `pos-client` is a JavaFX client consuming server APIs.
- `pos-api` defines shared request/response contracts.
- `pos-core` defines cross-cutting extension abstractions (printer, scanner, scale, fiscal).

## 2. Data and Migrations

- Primary DB: PostgreSQL.
- Schema evolution: Flyway SQL migrations in `pos-server/src/main/resources/db/migration`.
- Migration strategy: additive and deterministic; avoid editing applied versions.

## 3. Reliability and Operations Baseline

1. Environment profiles: `application-dev.properties`, `application-staging.properties`, `application-prod.properties`.
2. Operational scripts:
   - `ops/scripts/package-release.sh`
   - `ops/scripts/deploy-server.sh`
   - `ops/scripts/backup-postgres.sh`
   - `ops/scripts/restore-postgres.sh`
3. Runbooks:
   - `docs/ops/P4-packaging-deployment-and-operations.md`
   - `docs/ops/P4-backup-restore-validation.md`

## 4. Maintenance Workflow

1. Pull latest code and run `mvn clean verify`.
2. Add/adjust migration before domain behavior changes.
3. Update `pos-api` contracts for externally visible changes.
4. Add or update unit/integration tests.
5. Update `README.md`, `ROADMAP.md`, and relevant docs.

## 5. Observability and Security Maintenance

- Monitor correlation IDs across logs and API errors.
- Keep RBAC permission checks deny-by-default.
- Validate secret/config hardening checklist before releases.
- Audit sensitive actions (discount overrides, returns, reprints, drawer opens).

## 6. Handover Acceptance

- [x] New operator can execute daily workflows from docs.
- [x] New engineer can run test suites and extend a card with migration + API + tests.
- [x] Reference docs for UAT, performance, security, and operations are linked and current.
