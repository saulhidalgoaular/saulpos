# P2 Performance and Reliability Hardening

## Scope
Card `P2` verifies production-readiness baseline for checkout/lookup latency and reliability under retry/failure patterns.

## Defined p95 Targets
- Product lookup (`GET /api/catalog/products/search`): `<= 1000 ms` p95.
- Checkout commit (`POST /api/sales/checkout`): `<= 1800 ms` p95.

Targets are intentionally conservative for deterministic CI execution in the default in-memory integration profile and act as regression guardrails.

## Executable Reliability Suite
Primary suite:
- `pos-server/src/test/java/com/saulpos/server/uat/P2PerformanceReliabilityIntegrationTest.java`

Covered scenarios:
1. Burst lookup+checkout sequence computes measured p95 and enforces latency targets.
2. Idempotent replay of checkout with same `Idempotency-Key` returns same result and writes no duplicates.
3. Transient failed checkout (invalid allocation) can be retried successfully without orphan/duplicate persistence.

Run command:
- `mvn -pl pos-server test -Dtest=P2PerformanceReliabilityIntegrationTest`

## Load Test Scripts (k6)
Scripts:
- `docs/perf/k6/P2-peak-checkout-lookup.js`
- `docs/perf/k6/P2-peak-reporting.js`

These scripts cover peak checkout/lookup and reporting access patterns using configurable environment variables (`BASE_URL`, `AUTH_TOKEN`, IDs/date range).

## Evidence Notes
- Reliability checks enforce "no data corruption" through explicit table-count assertions over `sale`, `payment`, and `inventory_movement`.
- Restart/DB reconnect hardening remains represented through retry/idempotency reliability guards in integration tests and external load-script workflows.
