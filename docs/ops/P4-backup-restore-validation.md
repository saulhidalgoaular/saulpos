# P4 Backup and Restore Validation Evidence

Execution Date: 2026-02-10

## Scope
Validate that backup and restore operational flow for SaulPOS is documented, script-driven, and reproducible.

## Inputs
- Backup script: `ops/scripts/backup-postgres.sh`
- Restore script: `ops/scripts/restore-postgres.sh`
- Primary runbook: `docs/ops/P4-packaging-deployment-and-operations.md`

## Validation Checklist
1. Backup script accepts host/port/database/user/backup-dir arguments.
2. Backup script produces timestamped dump artifact and checksum file.
3. Restore script verifies checksum before invoking `pg_restore`.
4. Restore script uses `--clean --if-exists` for deterministic replay.
5. Runbook includes explicit backup and restore command examples.

## Result
All checklist items are satisfied by committed scripts and runbook artifacts.

## Notes
This validation covers runbook and script readiness in-repo. Environment-specific PostgreSQL execution should be performed as part of release deployment rehearsal in staging.
