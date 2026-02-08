# SaulPOS v2: Antigravity Agentic Execution Roadmap

## 1. Mission
Build SaulPOS v2 as a production-grade, local-first POS for convenience stores in Latin America, with deterministic delivery that an agentic workflow can execute end-to-end.

## 2. Scope Baseline
- Monorepo modules: `pos-core`, `pos-api`, `pos-server`, `pos-client`.
- Source of truth: `pos-server` (headless, domain logic + transactional integrity).
- Client role: `pos-client` consumes REST APIs only.
- DB baseline: PostgreSQL primary; MariaDB/H2 compatible where feasible.
- Migration tool: Flyway.
- Deletion behavior: all persistence respects `DeletionStrategy` (soft/hard).

## 3. Execution Rules (Mandatory)
1. No card starts implementation without written acceptance criteria.
2. Every card includes API contract + DB model impact + tests.
3. All externally visible errors use stable machine-readable codes.
4. Concurrency-sensitive paths require explicit race-condition tests.
5. Each merged card updates docs and changelog notes.
6. No hidden requirements; assumptions are written as explicit decisions.

## 4. Work Card Protocol
Use this exact schema for every card:

```
Card ID:
Title:
Phase:
Goal:
Why:
Dependencies:
Impacted Modules:
Impacted Files (planned):
Data Model:
API Contract:
Validation Rules:
Business Rules:
Security/Permissions:
Observability:
Failure Modes:
Acceptance Criteria:
Test Plan:
Rollout Notes:
Out of Scope:
```

## 5. Global Definition of Done
1. Feature behavior implemented and reviewed.
2. Flyway migration present and repeatable.
3. `pos-api` contracts updated.
4. Unit tests and integration tests pass.
5. Logs/metrics/audit coverage added for core actions.
6. No P1/P2 open issues for the card.

## 6. Phase Map and Dependencies
1. `A -> B -> C -> D -> G -> H -> L -> M -> N`.
2. `E/F/I/J/K` can start after `B/C/D/G` baseline is stable.
3. No reporting (`L`) before stable source domains (`G/H/J`).

## 7. Detailed Work Cards by Phase

### Phase A: Foundation Hardening

#### Card A1: Runtime and Build Baseline
- Goal: Make local + CI environment reproducible.
- Dependencies: none.
- Impacted Modules: root, `pos-server`.
- Data Model: migration baseline version.
- API Contract: none.
- Business Rules: single command should bootstrap local stack.
- Acceptance Criteria:
1. `docker-compose.yml` provides PostgreSQL and optional Adminer.
2. Flyway baseline migration exists and applies on clean DB.
3. CI profile runs compile + tests + coverage.
- Test Plan:
1. Start stack from scratch and run `mvn clean verify`.
2. Verify Flyway history table creation and version tracking.

#### Card A2: Error Contract + Observability
- Goal: Standardize runtime diagnostics and API errors.
- Dependencies: A1.
- Impacted Modules: `pos-server`, `pos-api`.
- Data Model: optional `audit_event` table if introduced here.
- API Contract: RFC7807-compatible Problem Details response shape.
- Business Rules:
1. Every exception maps to deterministic code.
2. Correlation ID included in logs and response headers.
- Acceptance Criteria:
1. Global exception handler maps validation, auth, not-found, conflict, and generic errors.
2. Metrics endpoint available.
3. Structured logs include timestamp, level, correlationId, module, action.
- Test Plan:
1. Integration tests per error class asserting HTTP status + error code.
2. Unit tests for exception mappers.

#### Card A3: Security Foundation
- Goal: Establish authentication and baseline hardening.
- Dependencies: A1.
- Impacted Modules: `pos-server`, `pos-api`.
- Data Model: `user_account`, `role`, `permission`, `user_role`.
- API Contract: auth endpoints (`login`, token refresh/session renewal, logout).
- Business Rules:
1. Passwords hashed with strong algorithm.
2. Brute-force protection after configurable failed attempts.
- Acceptance Criteria:
1. Protected endpoints reject unauthorized requests.
2. Role-aware auth context available to services.
3. Audit entry recorded for login success/failure and logout.
- Test Plan:
1. Integration tests for auth lifecycle.
2. Unit tests for token/session service.

### Phase B: Identity, RBAC, and Store Structure

#### Card B1: Tenant and Location Model
- Goal: Model merchant/store/terminal identities cleanly.
- Dependencies: A3.
- Impacted Modules: `pos-server`, `pos-api`, `pos-core`.
- Data Model:
1. `merchant`.
2. `store_location`.
3. `terminal_device`.
4. `store_user_assignment`.
- API Contract: CRUD + activation/deactivation endpoints.
- Business Rules:
1. Terminal belongs to exactly one store.
2. User can be assigned to multiple stores with explicit role mapping.
- Acceptance Criteria:
1. Unique constraints for merchant code, store code, terminal code.
2. APIs enforce assignment integrity.
- Test Plan:
1. Repository tests for constraints.
2. Integration tests for assignment edge cases.

#### Card B2: Permission Matrix and Enforcement
- Goal: Enforce fine-grained permissions for POS operations.
- Dependencies: B1.
- Impacted Modules: `pos-server`, `pos-api`.
- Data Model: permission catalog + role-permission mapping.
- API Contract:
1. Role management endpoints.
2. Permission introspection endpoint for current user.
- Business Rules:
1. Deny by default.
2. Sensitive actions require explicit permissions.
- Acceptance Criteria:
1. Permission checks applied to sales, refunds, inventory adjustment, reports, configuration.
2. Permission denial returns stable authorization error contract.
- Test Plan:
1. Integration test matrix per role/action.
2. Unit tests for permission evaluator.

#### Card B3: Shift and Cash Session Lifecycle
- Goal: Track cashier shift state and cash drawer reconciliation.
- Dependencies: B2.
- Data Model:
1. `cash_shift`.
2. `cash_movement` (`OPEN`, `PAID_IN`, `PAID_OUT`, `CLOSE`).
- API Contract:
1. `POST /shifts/open`.
2. `POST /shifts/{id}/cash-movements`.
3. `POST /shifts/{id}/close`.
- Business Rules:
1. One open shift per cashier+terminal.
2. Closing requires counted cash and variance capture.
- Acceptance Criteria:
1. Open/close transitions validated by finite-state rules.
2. Reports can retrieve expected vs counted totals.
- Test Plan:
1. State transition unit tests.
2. Integration tests for invalid transition attempts.

### Phase C: Catalog for Convenience Stores

#### Card C1: Product and Variant Core
- Goal: Implement product master data for fast checkout.
- Dependencies: B1.
- Data Model:
1. `product`.
2. `product_variant`.
3. `product_barcode`.
4. `category`.
- API Contract: CRUD + list/filter + barcode lookup.
- Business Rules:
1. Product status active/inactive.
2. Multiple barcodes per sellable item allowed.
- Acceptance Criteria:
1. SKU uniqueness enforced per merchant.
2. Barcode lookup returns sellable unit in <= configured SLA for indexed DB.
- Test Plan:
1. Repository tests for unique constraints.
2. Integration tests for CRUD and lookup.

#### Card C2: Category and Department Taxonomy
- Goal: Support reporting-ready product grouping.
- Dependencies: C1.
- Data Model: hierarchical `category` with parent reference.
- API Contract: tree retrieval endpoint + move/reparent endpoint.
- Business Rules:
1. Cycles are forbidden.
2. Soft-deleted category cannot receive new products.
- Acceptance Criteria:
1. Tree validation blocks cyclic updates.
2. Product assignment rules enforced.
- Test Plan:
1. Unit tests for tree validation.
2. Integration tests for hierarchy operations.

#### Card C3: Price Books and Store Overrides
- Goal: Allow base and per-store pricing.
- Dependencies: C1, B1.
- Data Model:
1. `price_book`.
2. `price_book_item`.
3. `store_price_override`.
- API Contract: price retrieval endpoint by store/product.
- Business Rules:
1. Override precedence: store override > active price book > base product price.
2. No negative sell price.
- Acceptance Criteria:
1. Price resolution deterministic and auditable.
2. Effective date windows honored.
- Test Plan:
1. Unit tests for resolver precedence.
2. Integration tests across store contexts.

#### Card C4: Search and Lookup Performance
- Goal: Keep checkout item search fast under scale.
- Dependencies: C1.
- Data Model: indexes on normalized name, SKU, barcode.
- API Contract:
1. `GET /products/lookup?barcode=`.
2. `GET /products/search?q=`.
- Acceptance Criteria:
1. Indexes present in migration scripts.
2. Search pagination and deterministic ordering implemented.
- Test Plan:
1. Integration tests for pagination and sorting.
2. Performance smoke tests with seeded dataset.

### Phase D: Tax, Rounding, Receipt Numbering

#### Card D1: Tax Engine v1
- Goal: Compute tax correctly per line and totals.
- Dependencies: C1, C3.
- Data Model:
1. `tax_group`.
2. `store_tax_rule`.
- API Contract: tax preview endpoint for cart calculations.
- Business Rules:
1. Inclusive/exclusive tax modes.
2. Exempt and zero-rated handling.
- Acceptance Criteria:
1. Tax breakdown returned per line and receipt totals.
2. Tax results deterministic for same inputs.
- Test Plan:
1. Unit tests for each tax mode.
2. Integration tests with checkout flows.

#### Card D2: Rounding Policy
- Goal: Implement currency and tender rounding logic.
- Dependencies: D1.
- Data Model: `rounding_policy` per store and tender type.
- API Contract: rounding included in totals API.
- Business Rules:
1. Cash and card can have different rounding methods.
2. Rounding adjustment explicitly stored.
- Acceptance Criteria:
1. Final payable total includes explicit rounding line.
2. No hidden rounding in payment allocation.
- Test Plan:
1. Unit tests for midpoint and edge values.
2. Integration tests during checkout and refunds.

#### Card D3: Receipt Sequence Allocation
- Goal: Concurrency-safe, per-store/per-terminal receipt numbering.
- Dependencies: B1, D1.
- Data Model:
1. `receipt_series`.
2. `receipt_sequence`.
3. `receipt_header`.
- API Contract: checkout response includes immutable receipt number.
- Business Rules:
1. Number uniqueness guaranteed inside a series.
2. Number policy (gapped/gapless) explicitly documented.
- Acceptance Criteria:
1. Concurrent allocations produce no duplicates.
2. Uniqueness constraint exists on `(series_id, number)`.
- Test Plan:
1. Integration concurrency test with multi-threaded allocations.

### Phase E: Discounts and Promotions

#### Card E1: Discount Primitives
- Goal: Support line/cart fixed and percentage discounts.
- Dependencies: D1, B2.
- Data Model:
1. `discount_application`.
2. `discount_reason_code`.
- API Contract: apply/remove discount endpoints.
- Business Rules:
1. High discount thresholds require manager permission.
2. Every manual discount stores reason code.
- Acceptance Criteria:
1. Discount impacts reflected in tax and total calculations.
2. Audit entries written for manual overrides.
- Test Plan:
1. Unit tests for calculation order.
2. Integration tests for permissioned approvals.

#### Card E2: Promotion Engine v1
- Goal: Execute rule-based promotions.
- Dependencies: E1, C1.
- Data Model:
1. `promotion`.
2. `promotion_rule`.
3. `promotion_window`.
- API Contract: promo evaluation endpoint.
- Business Rules:
1. Time-window eligibility.
2. Conflict resolution strategy for overlapping promotions.
- Acceptance Criteria:
1. Deterministic winner selection for conflicting rules.
2. Promo explanation returned for receipt transparency.
- Test Plan:
1. Unit tests for each rule type.
2. Integration tests for overlapping scenarios.

#### Card E3: Loyalty Hooks
- Goal: Create extension points without hard coupling.
- Dependencies: E2, F1.
- Data Model: optional `loyalty_event` abstraction.
- API Contract: interface contracts for earn/redeem operations.
- Acceptance Criteria:
1. Loyalty integration can be enabled/disabled by configuration.
2. Core checkout works even when loyalty provider unavailable.
- Test Plan:
1. Unit tests with stub providers.

### Phase F: Customers and Commercial Profiles

#### Card F1: Customer Master
- Goal: Store customer identity and compliance fields.
- Dependencies: B1.
- Data Model:
1. `customer`.
2. `customer_tax_identity`.
3. `customer_contact`.
- API Contract: CRUD + lookup by document/email/phone.
- Business Rules:
1. Document type/value uniqueness within merchant where required.
2. Flags for invoice-required and credit-enabled.
- Acceptance Criteria:
1. Validation supports optional fields without blocking rapid checkout.
2. Customer can be linked to sale at checkout.
- Test Plan:
1. Integration tests for validations and lookup.

#### Card F2: Customer Groups and Pricing Hooks
- Goal: Differentiate retail/wholesale or custom segments.
- Dependencies: F1, C3.
- Data Model: `customer_group`, customer-to-group mapping.
- API Contract: group assignment endpoints.
- Business Rules: group can influence price book resolution.
- Acceptance Criteria:
1. Group-based pricing precedence documented and enforced.
- Test Plan:
1. Unit tests for price resolution with group contexts.

#### Card F3: Customer History
- Goal: Expose customer-centric sales/return history.
- Dependencies: G2, G3.
- API Contract:
1. `GET /customers/{id}/sales`.
2. `GET /customers/{id}/returns`.
- Acceptance Criteria:
1. Paginated responses with date filters.
2. Secure access (authorized roles only).
- Test Plan:
1. Integration tests for filters and auth checks.

### Phase G: Sales Core

#### Card G1: Cart Lifecycle Service
- Goal: Make server authoritative for cart state.
- Dependencies: C1, C3, D1, E1.
- Data Model:
1. `sale_cart`.
2. `sale_cart_line`.
3. `cart_event` (optional but recommended).
- API Contract:
1. Create cart.
2. Add/update/remove lines.
3. Recalculate totals.
- Business Rules:
1. Cart mutation is idempotent where possible.
2. Recalculation is deterministic and side-effect safe.
- Acceptance Criteria:
1. Same inputs produce same totals.
2. Invalid product/quantity rejected with explicit error code.
- Test Plan:
1. Unit tests for calculation pipeline.
2. Integration tests for cart mutation flows.

#### Card G2: Atomic Checkout
- Goal: Commit sale, payment, receipt, and stock movement in one transaction.
- Dependencies: G1, D3, J1.
- Data Model:
1. `sale`.
2. `sale_line`.
3. `payment`.
4. inventory movement records.
- API Contract: `POST /sales/checkout`.
- Business Rules:
1. Receipt number allocated in same transaction.
2. Stock movement generated for each sellable line.
- Acceptance Criteria:
1. Partial commit is impossible.
2. Checkout response returns sale ID + receipt number + payment summary.
- Test Plan:
1. Integration tests with forced failure mid-transaction.
2. Concurrency tests for parallel checkouts.

#### Card G3: Returns and Refunds
- Goal: Handle partial/full returns with policy enforcement.
- Dependencies: G2, J2.
- Data Model:
1. `sale_return`.
2. `sale_return_line`.
3. refund payment records.
- API Contract:
1. Return by receipt lookup.
2. Submit return with reason and refund method.
- Business Rules:
1. Cannot return more than sold quantity.
2. Manager approval for restricted return windows.
- Acceptance Criteria:
1. Inventory and financial records updated consistently.
2. Return links to original sale.
- Test Plan:
1. Integration tests for partial and full returns.
2. Permission tests for approval workflow.

### Phase H: Inventory Ledger

#### Card H1: Inventory Movement Ledger
- Goal: Move from direct quantity edits to event ledger.
- Dependencies: G2, C1.
- Data Model:
1. `inventory_movement`.
2. `stock_balance` (materialized or computed view/service).
- Business Rules:
1. Every stock change has a typed movement and reference.
2. Movement is immutable after creation.
- Acceptance Criteria:
1. On-hand stock is reproducible from movements.
2. Movement reference to source document enforced.
- Test Plan:
1. Unit tests for balance calculation.
2. Integration tests for sale/return/adjustment effects.

#### Card H2: Stock Adjustments
- Goal: Controlled manual correction flow.
- Dependencies: H1, B2.
- Data Model: `stock_adjustment` + reason code.
- API Contract: create/approve/post adjustment endpoints.
- Business Rules:
1. Large adjustments require manager approval.
2. Every adjustment requires reason code.
- Acceptance Criteria:
1. Posted adjustment always creates movement entries.
- Test Plan:
1. Integration tests for approval path and postings.

#### Card H3: Stocktake
- Goal: Support periodic cycle counts.
- Dependencies: H1.
- Data Model:
1. `stocktake_session`.
2. `stocktake_line`.
- API Contract: create/start/finalize stocktake.
- Business Rules:
1. Count snapshot timestamp fixed at start.
2. Variance postings generated at finalize.
- Acceptance Criteria:
1. Variance report available by product/category.
- Test Plan:
1. Integration tests for count variance posting.

#### Card H4: Transfer Orders
- Goal: Track stock movement between stores.
- Dependencies: H1, B1.
- Data Model:
1. `stock_transfer`.
2. `stock_transfer_line`.
- API Contract: draft -> ship -> receive workflow endpoints.
- Business Rules:
1. Source and destination stores must differ.
2. Transfer closes only when all lines reconciled.
- Acceptance Criteria:
1. Out and in movements paired and traceable.
- Test Plan:
1. Integration tests for partial receive and completion.

### Phase I: Purchasing and Suppliers

#### Card I1: Supplier Master
- Goal: Define vendor records and commercial terms.
- Dependencies: B1.
- Data Model: `supplier`, `supplier_contact`, `supplier_terms`.
- API Contract: CRUD + search endpoints.
- Acceptance Criteria:
1. Supplier status and unique identifiers enforced.

#### Card I2: Purchase Orders and Receiving
- Goal: Convert purchase intent into stock increases.
- Dependencies: I1, H1.
- Data Model:
1. `purchase_order`.
2. `purchase_order_line`.
3. `goods_receipt`.
- API Contract: PO create/approve/receive.
- Business Rules:
1. Partial receive supported.
2. Receipt updates stock via inventory movements.
- Acceptance Criteria:
1. PO status reflects real lifecycle.
- Test Plan:
1. Integration tests for partial/full receiving.

#### Card I3: Costing v1
- Goal: Maintain weighted average and last cost.
- Dependencies: I2.
- Data Model: cost fields on product/store inventory context.
- Business Rules: receiving updates cost according to policy.
- Acceptance Criteria:
1. Cost updates are deterministic and auditable.
- Test Plan:
1. Unit tests for weighted average calculations.

### Phase J: Payments, Gift Cards, Store Credit

#### Card J1: Tender and Split Payments
- Goal: Support common payment combinations.
- Dependencies: D2, G1.
- Data Model: `payment`, `payment_allocation`.
- API Contract: payment allocation in checkout request.
- Business Rules:
1. Sum of tenders must equal payable total.
2. Change calculation for cash where applicable.
- Acceptance Criteria:
1. Split/partial payment scenarios validated.
- Test Plan:
1. Unit tests for allocation validation.
2. Integration tests on checkout.

#### Card J2: Payment State Machine
- Goal: Model payment lifecycle cleanly.
- Dependencies: J1.
- Data Model: payment status enum + transition log.
- API Contract: endpoints for void/refund/capture actions.
- Business Rules: invalid state transitions rejected.
- Acceptance Criteria:
1. Transition history traceable for audit.
- Test Plan:
1. Unit tests for state machine.
2. Integration tests for reversal/refund.

#### Card J3: Gift Card (Optional)
- Goal: Enable issuance and redemption.
- Dependencies: J1, F1.
- Data Model: `gift_card`, `gift_card_transaction`.
- Acceptance Criteria:
1. Balance cannot go below zero.
2. All redemptions linked to sale/refund context.

#### Card J4: Store Credit (Optional)
- Goal: Issue credit and redeem in future sales.
- Dependencies: G3, F1.
- Data Model: `store_credit_account`, transactions.
- Acceptance Criteria:
1. Credit ledger reconciles with refunds and redemptions.

### Phase K: Local-First Resilience and Sync Policy

#### Card K1: Offline Policy Definition
- Goal: Define explicit supported offline modes for v1.
- Dependencies: G2.
- Deliverable: architecture decision record in repo.
- Acceptance Criteria:
1. Policy defines LAN dependency expectations and fail behavior.
2. Policy maps to technical controls and user-facing messages.

#### Card K2: Idempotent Event Ingestion
- Goal: Protect server from duplicate client submissions.
- Dependencies: K1.
- Data Model: `idempotency_key` or equivalent event dedupe store.
- API Contract: idempotency header/key requirements.
- Business Rules:
1. Duplicate key with same payload returns original result.
2. Duplicate key with different payload returns conflict.
- Acceptance Criteria:
1. Replay-safe checkout and payment endpoints.
- Test Plan:
1. Integration tests for duplicate submissions.

### Phase L: Reporting and Exports

#### Card L1: Sales and Returns Reports
- Goal: Produce operational sales insights.
- Dependencies: G2, G3.
- API Contract: aggregated report endpoints with filters.
- Acceptance Criteria:
1. Reports by day/store/terminal/cashier/category/tax group.
2. Discount and return breakout included.

#### Card L2: Inventory Reports
- Goal: Expose stock health and shrinkage.
- Dependencies: H1, H2, H3.
- API Contract: stock-on-hand, low-stock, movement report endpoints.
- Acceptance Criteria:
1. Filters by store/category/supplier.
2. Export-ready tabular shape.

#### Card L3: Cash and Shift Reports
- Goal: Reconcile cash operations reliably.
- Dependencies: B3, J1.
- API Contract: end-of-day and shift-level cash reports.
- Acceptance Criteria:
1. Expected vs counted cash and variance reason visibility.

#### Card L4: CSV Export
- Goal: Enable operational data extraction.
- Dependencies: L1-L3.
- API Contract: export endpoints per report type.
- Acceptance Criteria:
1. CSV includes headers, deterministic column order, UTF-8 output.

### Phase M: Hardware Integration Layer

#### Card M1: Printer Abstraction and Templates
- Goal: Decouple receipt generation from hardware adapters.
- Dependencies: G2.
- Impacted Modules: `pos-core`, `pos-server`.
- API Contract: server-side print request endpoint.
- Business Rules:
1. Template rendering independent of printer transport.
2. Print failures must not rollback completed checkout.
- Acceptance Criteria:
1. ESC/POS adapter implemented behind interface.
2. Retry/error status available.

#### Card M2: Cash Drawer Integration
- Goal: Support drawer open commands with permissions.
- Dependencies: M1, B2.
- Acceptance Criteria:
1. Drawer open action permission-protected and audited.

#### Card M3: Scanner/Scale Extension Interfaces
- Goal: Prepare integration points without blocking v1.
- Dependencies: C4.
- Acceptance Criteria:
1. Interfaces documented and stubs available.

### Phase N: LATAM Fiscal Plugin Layer

#### Card N1: Fiscal Provider SPI
- Goal: Keep fiscal integrations modular and country-isolated.
- Dependencies: G2, D3.
- Impacted Modules: `pos-core`, `pos-server`.
- Data Model:
1. `fiscal_document`.
2. `fiscal_event`.
- API Contract:
1. `issueInvoice(saleId)`.
2. `cancelInvoice(fiscalId, reason)`.
3. `issueCreditNote(returnId)`.
- Business Rules:
1. Core sale can run with fiscal provider disabled when compliant with deployment policy.
2. Fiscal outcomes persisted for audit and retries.
- Acceptance Criteria:
1. SPI contract stable and tested with stub provider.
2. Checkout supports invoice-required toggle + required customer fields.

#### Card N2: Country Providers (Future Track)
- Goal: Implement country-specific fiscal modules independently.
- Dependencies: N1.
- Acceptance Criteria:
1. Country module can be added without changing sales core contracts.

## 8. Cross-Phase Testing Strategy
1. Unit tests for all pure business logic.
2. Integration tests for all REST endpoints and transactional boundaries.
3. Concurrency tests for receipt sequence, checkout, and stock ledger.
4. Contract tests for `pos-api` DTO compatibility.
5. Migration tests on empty and non-empty databases.

## 9. Cross-Phase Non-Functional Targets
1. API p95 response targets defined per critical endpoint.
2. Checkout path is fully auditable.
3. All critical writes include correlation IDs and actor identity.
4. Security events logged (auth failures, forbidden actions, sensitive overrides).

## 10. Execution Runbook for Agents
1. Select next unblocked card by dependency order.
2. Produce mini-design note in PR description.
3. Implement migrations first, then domain, then API layer.
4. Add tests before finishing card.
5. Run `mvn clean verify`.
6. Update roadmap progress table and card status.

## 11. Progress Tracking Table
| Card ID | Status | Owner | PR | Notes |
|---|---|---|---|---|
| A1 | TODO |  |  |  |
| A2 | TODO |  |  |  |
| A3 | TODO |  |  |  |
| B1 | TODO |  |  |  |
| B2 | TODO |  |  |  |
| B3 | TODO |  |  |  |
| C1 | TODO |  |  |  |
| C2 | TODO |  |  |  |
| C3 | TODO |  |  |  |
| C4 | TODO |  |  |  |
| D1 | TODO |  |  |  |
| D2 | TODO |  |  |  |
| D3 | TODO |  |  |  |
| E1 | TODO |  |  |  |
| E2 | TODO |  |  |  |
| E3 | TODO |  |  |  |
| F1 | TODO |  |  |  |
| F2 | TODO |  |  |  |
| F3 | TODO |  |  |  |
| G1 | TODO |  |  |  |
| G2 | TODO |  |  |  |
| G3 | TODO |  |  |  |
| H1 | TODO |  |  |  |
| H2 | TODO |  |  |  |
| H3 | TODO |  |  |  |
| H4 | TODO |  |  |  |
| I1 | TODO |  |  |  |
| I2 | TODO |  |  |  |
| I3 | TODO |  |  |  |
| J1 | TODO |  |  |  |
| J2 | TODO |  |  |  |
| J3 | TODO |  |  |  |
| J4 | TODO |  |  |  |
| K1 | TODO |  |  |  |
| K2 | TODO |  |  |  |
| L1 | TODO |  |  |  |
| L2 | TODO |  |  |  |
| L3 | TODO |  |  |  |
| L4 | TODO |  |  |  |
| M1 | TODO |  |  |  |
| M2 | TODO |  |  |  |
| M3 | TODO |  |  |  |
| N1 | TODO |  |  |  |
| N2 | TODO |  |  |  |

## 12. Immediate Next Three Cards
1. `B1` Tenant and location model.
2. `C1` Product and variant core.
3. `D3 + G2` Receipt sequence + atomic checkout integration.
