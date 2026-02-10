# P1 End-to-End UAT Scenarios

## Scope
Card `P1` validates release-critical persona workflows with runnable integration evidence.

Personas covered:
- Cashier
- Manager
- Inventory clerk
- Admin

## Executable UAT Suite
Primary suite:
- `pos-server/src/test/java/com/saulpos/server/uat/P1EndToEndUatIntegrationTest.java`

Covered scenarios:
1. Cashier opens shift, creates cart, checks out sale, and closes shift.
2. Manager performs high-threshold line price override and completes checkout.
3. Inventory clerk creates purchase order, approves and receives it, and verifies stock balance.
4. Admin provisions merchant/store/terminal and validates persisted configuration.

## Signed UAT Checklist
Execution date:
- `2026-02-10`

Environment:
- Local default test profile (`H2`) via Maven + Spring integration tests.

Sign-off:
- Product owner: `PENDING`
- Engineering owner: `PENDING`

Checklist:
- [x] Cashier workflow passes with deterministic sale + receipt outcome.
- [x] Manager sensitive override workflow passes with required permission.
- [x] Inventory receiving workflow passes with expected stock ledger impact.
- [x] Admin provisioning workflow passes and is queryable.
- [x] All UAT suite tests are runnable by CI (`mvn -pl pos-server test -Dtest=P1EndToEndUatIntegrationTest`).

## Evidence
- Test class assertions in `P1EndToEndUatIntegrationTest`.
- Test execution command and result are captured in this card implementation commit.
