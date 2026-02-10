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
1. `A -> B -> C -> D -> G -> H -> L -> M -> N -> O -> P`.
2. `E/F/I/J/K` can start after `B/C/D/G` baseline is stable.
3. No reporting (`L`) before stable source domains (`G/H/J`).
4. UI phase (`O`) starts only after stable auth, catalog, cart, and checkout APIs (`B/C/G`).
5. Final release phase (`P`) starts only after all mandatory cards and UAT pass.

## 7. Detailed Work Cards by Phase

### Phase A: Foundation Hardening

#### Card A1: Runtime and Build Baseline [SOLVED]
- Goal: Make local + CI environment reproducible.
- Dependencies: none.
- Impacted Modules: root, `pos-server`.
- Data Model: migration baseline version.
- API Contract: none.
- Business Rules:
1. Single command should compile and run tests in containerized dev environments without nested Docker.
2. Integration tests must be runnable via JUnit profiles (default in-memory DB, optional external PostgreSQL).
- Acceptance Criteria:
1. Maven test profiles are defined for default no-Docker execution and optional external PostgreSQL compatibility checks.
2. Flyway baseline migration exists and applies on clean DB.
3. CI profile runs compile + tests + coverage without Docker-in-Docker dependency.
- Test Plan:
1. Run `mvn clean verify` using default JUnit test profile in the current containerized dev environment.
2. Run optional PostgreSQL compatibility suite (for example `mvn -Pit-postgres verify`) against a pre-provisioned external PostgreSQL instance.
3. Verify Flyway history table creation and version tracking.

#### Card A2: Error Contract + Observability [SOLVED]
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

#### Card A3: Security Foundation [SOLVED]
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

#### Card B1: Tenant and Location Model [SOLVED]
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

#### Card B2: Permission Matrix and Enforcement [SOLVED]
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

#### Card B3: Shift and Cash Session Lifecycle [SOLVED]
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

#### Card C1: Product and Variant Core [SOLVED]
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

#### Card C2: Category and Department Taxonomy [SOLVED]
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

#### Card C3: Price Books and Store Overrides [SOLVED]
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

#### Card C4: Search and Lookup Performance [SOLVED]
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

#### Card C5: Unit/Weight/Open-Price Item Modes [SOLVED]
- Goal: Support common convenience-store selling modes.
- Dependencies: C1, C3.
- Data Model:
1. `product_sale_mode` (`UNIT`, `WEIGHT`, `OPEN_PRICE`).
2. `product_uom` and quantity precision rules.
- API Contract: product configuration fields + validation for decimal quantity and open-price entry.
- Business Rules:
1. `WEIGHT` products allow decimal quantities with precision limits.
2. `OPEN_PRICE` entry requires permission and min/max policy validation.
- Acceptance Criteria:
1. Totals, tax, and rounding remain deterministic for decimal quantity and open-price items.
2. Manual open-price entries are audited with actor and reason where required.
- Test Plan:
1. Unit tests for quantity precision and validation.
2. Integration tests for weighted and open-price checkout flows.

### Phase D: Tax, Rounding, Receipt Numbering

#### Card D1: Tax Engine v1 [SOLVED]
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

#### Card D2: Rounding Policy [SOLVED]
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

#### Card D3: Receipt Sequence Allocation [SOLVED]
- Goal: Concurrency-safe, per-store/per-terminal receipt numbering.
- Dependencies: B1, D1.
- Data Model:
1. `receipt_series`.
2. `receipt_sequence`.
3. `receipt_header`.
- API Contract: `POST /api/receipts/allocate` returns immutable, series-scoped receipt number allocation.
- Business Rules:
1. Number uniqueness guaranteed inside a series.
2. Number policy (gapped/gapless) explicitly documented.
3. Allocation endpoint requires `SALES_PROCESS` permission.
- Acceptance Criteria:
1. Concurrent allocations produce no duplicates.
2. Uniqueness constraint exists on `(series_id, number)`.
3. Allocation response includes series metadata + formatted immutable receipt number.
- Test Plan:
1. Integration concurrency test with multi-threaded allocations.

### Phase E: Discounts and Promotions

#### Card E1: Discount Primitives [SOLVED]
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

#### Card E2: Promotion Engine v1 [SOLVED]
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

#### Card E3: Loyalty Hooks [SOLVED]
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

#### Card F1: Customer Master [SOLVED]
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

#### Card F2: Customer Groups and Pricing Hooks [SOLVED]
- Goal: Differentiate retail/wholesale or custom segments.
- Dependencies: F1, C3.
- Data Model: `customer_group`, customer-to-group mapping.
- API Contract: group assignment endpoints.
- Business Rules: group can influence price book resolution.
- Acceptance Criteria:
1. Group-based pricing precedence documented and enforced.
- Test Plan:
1. Unit tests for price resolution with group contexts.

#### Card F3: Customer History [SOLVED]
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

#### Card G1: Cart Lifecycle Service [SOLVED]
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

#### Card G2: Atomic Checkout [SOLVED]
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

#### Card G3: Returns and Refunds [SOLVED]
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

#### Card G4: Suspended/Parked Sales [SOLVED]
- Goal: Let operators park and resume carts during busy checkout operations.
- Dependencies: G1, B3.
- Data Model:
1. cart status extensions (`ACTIVE`, `PARKED`, `EXPIRED`, `CANCELLED`).
2. `parked_cart_reference` metadata.
- API Contract:
1. park cart endpoint.
2. resume parked cart endpoint.
3. list parked carts endpoint by store/terminal.
- Business Rules:
1. Resuming a parked cart enforces terminal/cashier policy constraints.
2. Parked carts expire per configurable policy and cannot be checked out after expiry without explicit restore flow.
- Acceptance Criteria:
1. Resume operation restores lines/totals/promotions deterministically.
2. Park/resume/cancel events are fully audited.
- Test Plan:
1. Integration tests for park/resume/expiry lifecycle.
2. Concurrency tests for simultaneous resume attempts.

#### Card G5: Void and Price Override Controls [SOLVED]
- Goal: Standardize controlled line void, transaction cancel, and price override flows.
- Dependencies: G1, B2, E1.
- Data Model:
1. `sale_override_event`.
2. `void_reason_code` and approval metadata.
- API Contract:
1. line void endpoint.
2. line price override endpoint.
3. cart cancel/void endpoint before final checkout.
- Business Rules:
1. Restricted override thresholds require manager approval.
2. Reason code is mandatory for voids and overrides.
- Acceptance Criteria:
1. Totals and tax are recomputed correctly after void/override actions.
2. Override and void actions are traceable in audit and reporting domains.
- Test Plan:
1. Unit tests for override validation and recomputation order.
2. Integration tests for permissioned approval workflows.

### Phase H: Inventory Ledger

#### Card H1: Inventory Movement Ledger [SOLVED]
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

#### Card H2: Stock Adjustments [SOLVED]
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

#### Card H3: Stocktake [SOLVED]
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

#### Card H4: Transfer Orders [SOLVED]
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

#### Card H5: Lot and Expiry Tracking [SOLVED]
- Goal: Add lot-level traceability and expiry controls for regulated/perishable items.
- Dependencies: H1, C1, I2.
- Data Model:
1. `inventory_lot`.
2. `inventory_lot_balance`.
3. lot-to-movement linkage records.
- API Contract:
1. receiving endpoints accept lot/expiry details.
2. stock query endpoints can return lot-level balances and expiry state.
- Business Rules:
1. Lot tracking is configurable per product.
2. Expired lots are blocked from sale except with explicit override permission if policy allows.
- Acceptance Criteria:
1. FEFO allocation is supported for lot-tracked products.
2. Traceability exists from sale/return back to original receipt lot.
- Test Plan:
1. Unit tests for FEFO selection logic.
2. Integration tests for receiving, selling, and expiring lot scenarios.

### Phase I: Purchasing and Suppliers

#### Card I1: Supplier Master [SOLVED]
- Goal: Define vendor records and commercial terms.
- Dependencies: B1.
- Data Model: `supplier`, `supplier_contact`, `supplier_terms`.
- API Contract: CRUD + search endpoints.
- Acceptance Criteria:
1. Supplier status and unique identifiers enforced.

#### Card I2: Purchase Orders and Receiving [SOLVED]
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

#### Card I3: Costing v1 [SOLVED]
- Goal: Maintain weighted average and last cost.
- Dependencies: I2.
- Data Model: cost fields on product/store inventory context.
- Business Rules: receiving updates cost according to policy.
- Acceptance Criteria:
1. Cost updates are deterministic and auditable.
- Test Plan:
1. Unit tests for weighted average calculations.

#### Card I4: Supplier Returns [SOLVED]
- Goal: Return damaged/expired inventory to suppliers with full traceability.
- Dependencies: I2, H1.
- Data Model:
1. `supplier_return`.
2. `supplier_return_line`.
3. supplier return reference document metadata.
- API Contract: create/approve/post supplier return endpoints.
- Business Rules:
1. Return quantity cannot exceed available quantity from received stock.
2. Posting a supplier return creates outbound inventory movements and financial references.
- Acceptance Criteria:
1. Supplier return lifecycle is tracked (`DRAFT`, `APPROVED`, `POSTED`).
2. Cost and stock impacts are auditable.
- Test Plan:
1. Integration tests for partial/full supplier returns.
2. Unit tests for quantity eligibility rules.

### Phase J: Payments, Gift Cards, Store Credit

#### Card J1: Tender and Split Payments [SOLVED]
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

#### Card J2: Payment State Machine [SOLVED]
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

#### Card K1: Offline Policy Definition [SOLVED]
- Goal: Define explicit supported offline modes for v1.
- Dependencies: G2.
- Deliverable: architecture decision record in repo.
- Acceptance Criteria:
1. Policy defines LAN dependency expectations and fail behavior.
2. Policy maps to technical controls and user-facing messages.

#### Card K2: Idempotent Event Ingestion [SOLVED]
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

#### Card L1: Sales and Returns Reports [SOLVED]
- Goal: Produce operational sales insights.
- Dependencies: G2, G3.
- API Contract: aggregated report endpoints with filters.
- Acceptance Criteria:
1. Reports by day/store/terminal/cashier/category/tax group.
2. Discount and return breakout included.

#### Card L2: Inventory Reports [SOLVED]
- Goal: Expose stock health and shrinkage.
- Dependencies: H1, H2, H3.
- API Contract: stock-on-hand, low-stock, movement report endpoints.
- Acceptance Criteria:
1. Filters by store/category/supplier.
2. Export-ready tabular shape.

#### Card L3: Cash and Shift Reports [SOLVED]
- Goal: Reconcile cash operations reliably.
- Dependencies: B3, J1.
- API Contract: end-of-day and shift-level cash reports.
- Acceptance Criteria:
1. Expected vs counted cash and variance reason visibility.

#### Card L4: CSV Export [SOLVED]
- Goal: Enable operational data extraction.
- Dependencies: L1-L3.
- API Contract: export endpoints per report type.
- Acceptance Criteria:
1. CSV includes headers, deterministic column order, UTF-8 output.

#### Card L5: Exception and Override Reports [SOLVED]
- Goal: Provide operational oversight for sensitive POS actions.
- Dependencies: G5, B3, J2.
- API Contract: endpoints for void/override/no-sale/refund-exception reporting.
- Acceptance Criteria:
1. Filters available by date/store/cashier/terminal/reason code.
2. Report rows include actor, approver, terminal, and correlation ID.
- Test Plan:
1. Integration tests for filter combinations and authorization.
2. Reconciliation tests against source transaction/audit records.

### Phase M: Hardware Integration Layer

#### Card M1: Printer Abstraction and Templates [SOLVED]
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

#### Card M4: Receipt Reprint and Journal Retrieval
- Goal: Support common receipt recovery workflows without mutating financial history.
- Dependencies: M1, G2, B2.
- Data Model: optional `receipt_print_event`.
- API Contract:
1. receipt retrieval endpoint by sale/receipt number.
2. receipt reprint endpoint.
- Business Rules:
1. Reprint permission is role-controlled and every reprint is audited.
2. Reprint operations never alter sale totals, payments, or fiscal state.
- Acceptance Criteria:
1. Reprinted receipts are marked as `COPY` with operator and timestamp.
2. Reprint failures are retriable and visible to operators.
- Test Plan:
1. Integration tests for authorized/unauthorized reprint attempts.
2. Unit tests for print event/audit persistence.

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

### Phase O: UI/UX Delivery (`pos-client`)

#### Card O1: UI Architecture and Design System
- Goal: Define client architecture and reusable UI foundation.
- Dependencies: B1, C1.
- Impacted Modules: `pos-client`.
- Deliverables:
1. Screen map and navigation model.
2. Shared component library (buttons, inputs, tables, dialogs, toasts).
3. State management strategy and API client abstraction.
- Acceptance Criteria:
1. Documented UI architecture with folder conventions and state boundaries.
2. Reusable components support keyboard-first POS workflows.
3. Theme tokens and typography scale centralized.
- Test Plan:
1. Component rendering tests.
2. Accessibility checks for focus order and contrast.

#### Card O2: Authentication and Session UI
- Goal: Implement login/logout/session-expiry experiences.
- Dependencies: A3, O1.
- API Contract: consumes auth endpoints from `A3`.
- Acceptance Criteria:
1. Login handles success, invalid credentials, locked account, and expired session.
2. Session timeout/refresh behavior is visible and user-safe.
3. Unauthorized navigation redirects to login.
- Test Plan:
1. UI integration tests for auth flows.
2. Contract tests against auth DTOs.

#### Card O3: Shift Open/Close and Cash Controls UI
- Goal: Build shift lifecycle UI with cash reconciliation.
- Dependencies: B3, O1.
- Acceptance Criteria:
1. Cashier can open shift with opening float.
2. Paid-in/paid-out actions require reason capture.
3. Shift close shows expected vs counted variance and confirmation.
- Test Plan:
1. UI integration tests for shift state transitions.

#### Card O4: Product Search and Cart Screen
- Goal: Deliver the main POS selling screen.
- Dependencies: C1, C4, G1, O1.
- Acceptance Criteria:
1. Fast barcode input path with keyboard scanner flow.
2. Product search with pagination and quick add to cart.
3. Cart line edit/remove and total recalculation feedback.
4. Clear error handling for unavailable products and invalid quantities.
- Test Plan:
1. UI integration tests for scan/search/add/edit/remove paths.
2. Performance smoke tests for large catalogs.

#### Card O5: Checkout and Payments UI
- Goal: Complete payment capture and sale completion UX.
- Dependencies: D2, G2, J1, O4.
- Acceptance Criteria:
1. Supports cash/card/split tender entry.
2. Shows rounding adjustments and amount due/change.
3. Handles checkout failures with recoverable retry guidance.
4. Displays receipt number and post-sale actions.
- Test Plan:
1. Integration tests for successful and failed checkout paths.
2. Edge-case tests for split tender validation.

#### Card O6: Returns and Refunds UI
- Goal: Enable cashier and manager return workflows.
- Dependencies: G3, O1.
- Acceptance Criteria:
1. Lookup sale by receipt number and display eligible return lines.
2. Partial quantity return supported with reason capture.
3. Manager-approval path available when policy requires it.
- Test Plan:
1. Integration tests for partial/full return and approval scenarios.

#### Card O7: Backoffice UI (Catalog, Pricing, Customers)
- Goal: Provide operational maintenance screens.
- Dependencies: C1, C3, F1, O1.
- Acceptance Criteria:
1. Catalog CRUD with validation feedback.
2. Pricing override management by store.
3. Customer create/edit/search with tax identity fields.
- Test Plan:
1. UI tests for form validation and CRUD operations.

#### Card O8: Reporting and Export UI
- Goal: Surface report filters, results, and export actions.
- Dependencies: L1, L2, L3, L4, O1.
- Acceptance Criteria:
1. Reports filter by date/store/terminal/cashier where applicable.
2. Tabular results load with pagination or streaming strategy.
3. Export actions trigger CSV downloads with user feedback.
- Test Plan:
1. Integration tests for filter and export actions.

#### Card O9: Hardware Interaction UI
- Goal: Expose print and drawer actions safely in UI.
- Dependencies: M1, M2, O4.
- Acceptance Criteria:
1. Print action status (queued/success/failure) visible to operator.
2. Drawer open action shown only for authorized roles.
- Test Plan:
1. Integration tests with mocked hardware adapters.

#### Card O10: Offline/Degraded Mode UX
- Goal: Ensure predictable user behavior during service degradation.
- Dependencies: K1, K2, O1.
- Acceptance Criteria:
1. Connectivity state clearly visible.
2. Action-level feedback explains retry, queued, or blocked operations.
3. No silent data loss from client interactions.
- Test Plan:
1. Integration tests with simulated API outages and recoveries.

#### Card O11: Suspended Sales and Override UX
- Goal: Deliver cashier-friendly controls for parked carts and sensitive line operations.
- Dependencies: G4, G5, O4.
- Acceptance Criteria:
1. Cashier can park, list, and resume carts from the selling screen with keyboard-first flow.
2. Line void and price override paths capture reason and approval context.
3. Restricted actions are hidden/disabled for unauthorized roles.
- Test Plan:
1. UI integration tests for park/resume and override flows.
2. Authorization UI tests for role-specific behavior.

#### Card O12: Lot/Expiry and Supplier Return UX
- Goal: Expose lot-aware inventory and supplier-return workflows in backoffice.
- Dependencies: H5, I4, O7.
- Acceptance Criteria:
1. Receiving UI captures lot number and expiry for configured products.
2. Stock views show expiry-aware balances and warnings.
3. Supplier return workflow supports draft, approval, and posting.
- Test Plan:
1. UI integration tests for lot capture and validation.
2. End-to-end tests for supplier return lifecycle.

#### Card O13: Receipt Reprint and Exception Monitoring UX
- Goal: Surface receipt recovery and exception oversight in operational UI.
- Dependencies: M4, L5, O8.
- Acceptance Criteria:
1. Authorized users can search historical receipts and trigger reprint.
2. Exception views show voids, overrides, no-sale drawer opens, and refund anomalies.
3. Drill-down provides actor, approver, reason, and terminal context.
- Test Plan:
1. UI integration tests for receipt lookup/reprint.
2. UI tests for exception filter/report interactions.

### Phase P: Final Productization and Release

#### Card P1: End-to-End UAT Scenarios
- Goal: Validate complete business workflows before release.
- Dependencies: All mandatory domain and UI cards.
- Deliverables:
1. UAT suite for cashier, manager, inventory clerk, and admin personas.
2. Signed UAT checklist with pass/fail evidence.
- Acceptance Criteria:
1. Core workflows pass without manual DB fixes.
2. Blocking defects triaged and resolved.

#### Card P2: Performance and Reliability Hardening
- Goal: Verify production readiness under realistic load.
- Dependencies: P1.
- Deliverables:
1. Load test scripts for peak checkout/reporting patterns.
2. Reliability tests for restart, DB reconnect, and transient failures.
- Acceptance Criteria:
1. Checkout and lookup meet defined p95 targets.
2. No data corruption in failure-recovery tests.

#### Card P3: Security and Compliance Verification
- Goal: Confirm baseline security posture and auditability.
- Dependencies: P1.
- Deliverables:
1. RBAC regression test run.
2. Sensitive action audit verification.
3. Secret/configuration hardening checklist.
- Acceptance Criteria:
1. No high-severity security findings open.
2. Required audit trails are complete and queryable.

#### Card P4: Packaging, Deployment, and Operations
- Goal: Produce repeatable deployment artifacts and runbooks.
- Dependencies: P2, P3.
- Deliverables:
1. Versioned release artifact strategy.
2. Environment configs for dev/staging/prod.
3. Ops runbooks: startup, backup/restore, rollback, incident response.
- Acceptance Criteria:
1. Fresh environment can be deployed from docs only.
2. Backup/restore tested and validated.

#### Card P5: Documentation and Handover
- Goal: Complete product and technical documentation for sustained operation.
- Dependencies: P4.
- Deliverables:
1. User guide (cashier/manager/admin).
2. API reference and integration notes.
3. Architecture and maintenance guide.
- Acceptance Criteria:
1. New operator can execute daily tasks using docs alone.
2. New engineer can run and extend system using docs alone.

#### Card P6: Release Candidate and Go-Live
- Goal: Execute final release process after iterative AI implementation cycles.
- Dependencies: P1-P5.
- Business Rules:
1. Freeze scope at RC cut.
2. Only blocker fixes allowed during stabilization window.
- Acceptance Criteria:
1. RC build signed off by product and engineering owners.
2. Production deployment completed with post-release validation checklist.

## 8. Cross-Phase Testing Strategy
1. Unit tests for all pure business logic.
2. Integration tests for all REST endpoints and transactional boundaries.
3. Default integration path uses JUnit test profiles without Docker dependency (in-memory DB + mocks where appropriate).
4. Optional PostgreSQL compatibility suite runs against pre-provisioned external DB environments.
5. Concurrency tests for receipt sequence, checkout, and stock ledger.
6. Contract tests for `pos-api` DTO compatibility.
7. Migration tests on empty and non-empty databases.
8. End-to-end UI tests for all critical cashier and manager workflows.

## 9. Cross-Phase Non-Functional Targets
1. API p95 response targets defined per critical endpoint.
2. Checkout path is fully auditable.
3. All critical writes include correlation IDs and actor identity.
4. Security events logged (auth failures, forbidden actions, sensitive overrides).

## 10. Execution Runbook for Agents
1. Select next unblocked card by dependency order.
2. Produce mini-design note in PR description.
3. Implement in order: migration -> domain -> API -> UI (if applicable).
4. Add tests before finishing card.
5. Run `mvn clean verify` (default no-Docker JUnit profile).
6. Update roadmap progress table and card status.
7. After every 3-5 cards, run an iteration review:
1. Validate against acceptance criteria.
2. Capture defects and architecture corrections.
3. Re-plan remaining cards with explicit deltas.
8. Before moving to `P`, run full regression on API + UI + migrations.

## 11. Progress Tracking Table
| Card ID | Status | Owner | PR | Notes |
|---|---|---|---|---|
| A1 | DONE |  |  | Runtime/build baseline hardened with repeatable migrations and test execution profiles |
| A2 | DONE |  |  | RFC7807 error contract, correlation ID propagation, metrics, and structured logging implemented |
| A3 | DONE |  |  | Authentication/session lifecycle, brute-force protection, and auth audit trail implemented |
| B1 | DONE |  |  | Implemented tenant/location model with APIs, migration, and integration tests |
| B2 | DONE |  |  | Permission catalog, role-permission APIs, and deny-by-default authorization matrix implemented |
| B3 | DONE |  |  | Shift open/cash movements/close lifecycle with reconciliation and transition checks implemented |
| C1 | DONE |  |  | Product/variant/barcode core with merchant SKU constraints and lookup APIs implemented |
| C2 | DONE |  |  | Category hierarchy tree, reparenting, and cycle/inactive-assignment validation implemented |
| C3 | DONE |  |  | Price resolution endpoint implemented with deterministic precedence (store override > active price book > base price), effective windows, migration `V8`, and unit/integration tests |
| C4 | DONE |  |  | Implemented `/api/catalog/products/search` with pagination and deterministic ordering, plus migration `V9` indexes for normalized SKU/name/barcode lookup |
| C5 | DONE |  |  | Added sale mode configuration (`UNIT`, `WEIGHT`, `OPEN_PRICE`) with UOM/precision/open-price policy validation, migration `V10`, and open-price entry validation + audit flow (`open_price_entry_audit`) with unit/integration coverage |
| D1 | DONE |  |  | Implemented `tax_group` + `store_tax_rule` with migration `V11`, product tax-group assignment (`product.tax_group_id`), and `POST /api/tax/preview` with deterministic inclusive/exclusive/exempt/zero-rated line+total tax breakdown and unit/integration coverage |
| D2 | DONE |  |  | Added store+tender `rounding_policy` data model with migration `V12`; tax totals API (`POST /api/tax/preview`) now returns explicit `roundingAdjustment` and `totalPayable` with tender-aware rounding details and unit/integration coverage |
| D3 | DONE |  |  | Implemented receipt-series allocation endpoint (`POST /api/receipts/allocate`) with migration `V13`, series/sequence/header model, `SALES_PROCESS` authorization, and integration + concurrency coverage for duplicate-free numbering |
| E1 | DONE |  |  | Implemented discount primitives with migration `V14`, manual-reason-code enforcement, apply/remove/preview APIs, manager-threshold override permission checks, and unit/integration coverage for calculation order and approvals |
| E2 | DONE |  |  | Implemented promotion engine v1 with migration `V15`, `POST /api/promotions/evaluate`, time-window eligibility, deterministic overlap winner selection (priority > discount > id), and promo explanation output with unit/integration coverage |
| E3 | DONE |  |  | Implemented loyalty hook extension points with migration `V17` (`loyalty_event`), config toggle (`app.loyalty.enabled`), earn/redeem APIs (`/api/loyalty/earn`, `/api/loyalty/redeem`), stub provider SPI, and resilient `DISABLED`/`UNAVAILABLE` fallback behavior validated by integration tests |
| F1 | DONE |  |  | Implemented customer master with migration `V16`, customer/tax-identity/contact models, CRUD + lookup APIs (`document`/`email`/`phone`), merchant-scoped document uniqueness, and integration coverage for optional-field validation and lookup flows |
| F2 | DONE |  |  | Implemented `customer_group` + `customer_group_assignment` with migration `V18`, customer group create/list + assignment APIs, and customer-context pricing precedence `store override > customer-group price book > standard price book > base price` via `GET /api/catalog/prices/resolve` |
| F3 | DONE |  |  | Implemented customer sales/returns history APIs (`GET /api/customers/{id}/sales`, `GET /api/customers/{id}/returns`) with pagination + `from`/`to` filters, migration `V28` (`sale.customer_id`), checkout customer-link support (`SaleCheckoutRequest.customerId`), and integration/security coverage for filters and authorization |
| G1 | DONE |  |  | Implemented cart lifecycle APIs (`POST /api/sales/carts`, line add/update/remove, recalculate, get), migration `V19` (`sale_cart`, `sale_cart_line`), deterministic totals recomputation (pricing+tax+rounding), idempotent add-line `lineKey` handling, and unit/integration coverage for quantity policy, invalid product/quantity errors, and cart mutation flow |
| G2 | DONE |  |  | Implemented atomic checkout with migration `V24` (`sale`, `sale_line`, `inventory_movement`), in-transaction receipt allocation + sale persistence on `POST /api/sales/checkout`, cart transition to `CHECKED_OUT`, checkout response `saleId`/`receiptNumber`, and integration/concurrency coverage for single-commit behavior under parallel attempts |
| G3 | DONE |  |  | Implemented returns and refunds with migration `V27` (`sale_return`, `sale_return_line`, `sale_return_refund`), receipt-based lookup (`GET /api/refunds/lookup`), return submission (`POST /api/refunds/submit`) with quantity-eligibility and restricted-window manager-override checks, plus inventory `RETURN` postings and integration/unit permission coverage |
| G4 | DONE |  |  | Implemented suspended cart lifecycle with migration `V20` (`parked_cart_reference`, `sale_cart_event`), park/resume/cancel/list APIs (`/api/sales/carts/{id}/park|resume|cancel`, `/api/sales/carts/parked`), expiry policy (`app.sales.parked-cart-expiry-minutes`), cashier+terminal resume constraints, and integration/concurrency/auth coverage |
| G5 | DONE |  |  | Implemented controlled line/cart void and line price-override flows with migration `V21` (`void_reason_code`, `sale_override_event`), mandatory reason-code validation, manager-threshold permission enforcement (`DISCOUNT_OVERRIDE`) for restricted overrides, and integration coverage for totals/tax recomputation plus authorization/audit traces |
| H1 | DONE |  |  | Implemented inventory ledger APIs (`POST /api/inventory/movements`, `GET /api/inventory/movements`, `GET /api/inventory/balances`) with migration `V26` extending typed movement/reference enforcement (`SALE`, `RETURN`, `ADJUSTMENT`) and computed stock-balance projection from immutable movement history, covered by unit/integration permission-matrix tests |
| H2 | DONE |  |  | Implemented stock adjustment workflow with migration `V29` (`stock_adjustment`), create/approve/post APIs (`POST /api/inventory/adjustments`, `/api/inventory/adjustments/{id}/approve`, `/api/inventory/adjustments/{id}/post`), manager-threshold approval enforcement, and integration coverage ensuring posted adjustments write immutable `ADJUSTMENT` ledger movements |
| H3 | DONE |  |  | Implemented stocktake workflow with migration `V30` (`stocktake_session`, `stocktake_line`), create/start/finalize + variance-report APIs (`/api/inventory/stocktakes`), fixed snapshot-at-start behavior, and finalize variance postings as immutable `ADJUSTMENT` movements (`reference_type=STOCKTAKE`) with integration coverage by product/category |
| H4 | DONE |  |  | Implemented transfer workflow with migration `V31` (`stock_transfer`, `stock_transfer_line`), transfer APIs (`POST /api/inventory/transfers`, `GET /api/inventory/transfers/{id}`, `POST /api/inventory/transfers/{id}/ship`, `POST /api/inventory/transfers/{id}/receive`), and paired inventory movement traceability via `STOCK_TRANSFER_OUT`/`STOCK_TRANSFER_IN` with integration coverage for partial receive and completion |
| H5 | DONE |  |  | Implemented lot-aware inventory model with migration `V33` (`inventory_lot`, `inventory_lot_balance`, `inventory_movement_lot`) and product flag `lot_tracking_enabled`; purchase receiving now accepts lot/expiry inputs for lot-tracked products, checkout uses FEFO allocation with expired-lot blocking plus manager override policy, inventory movement/balance endpoints expose lot-level traceability and expiry state, and unit/integration coverage validates receiving/selling/expiry paths |
| I1 | DONE |  |  | Implemented supplier master with migration `V22` (`supplier`, `supplier_contact`, `supplier_terms`), CRUD/search APIs (`/api/suppliers`), merchant-scoped unique code/tax-identifier enforcement, activate/deactivate lifecycle, and integration/security-matrix coverage |
| I2 | DONE |  |  | Implemented purchase-order receiving flow with migration `V32` (`purchase_order`, `purchase_order_line`, `goods_receipt`), APIs (`POST /api/inventory/purchase-orders`, `GET /api/inventory/purchase-orders/{id}`, `POST /api/inventory/purchase-orders/{id}/approve`, `POST /api/inventory/purchase-orders/{id}/receive`), and inventory posting integration via `PURCHASE_RECEIPT` reference movements with integration coverage for partial/full receiving and lifecycle validation |
| I3 | DONE |  |  | Implemented costing v1 with migration `V34` (`inventory_product_cost`), purchase-receipt `unitCost` capture, deterministic weighted-average/last-cost updates per store+product, and unit/integration coverage for calculation and audit persistence |
| I4 | DONE |  |  | Implemented supplier-return workflow with migration `V35` (`supplier_return`, `supplier_return_line`), APIs (`POST /api/inventory/supplier-returns`, `GET /api/inventory/supplier-returns/{id}`, `POST /api/inventory/supplier-returns/{id}/approve`, `POST /api/inventory/supplier-returns/{id}/post`), eligibility validation against received-minus-returned quantities, and outbound `SUPPLIER_RETURN` inventory movement posting with integration coverage |
| J1 | DONE |  |  | Implemented checkout payment allocation flow with migration `V23` (`payment`, `payment_allocation`), checkout API contract (`POST /api/sales/checkout`), deterministic split/cash-change validation rules, and unit/integration coverage for allocation and authorization scenarios |
| J2 | DONE |  |  | Implemented payment lifecycle state machine with migration `V25` (`payment.status`, `payment_transition`), new lifecycle APIs (`GET /api/payments/{id}`, `POST /api/payments/{id}/capture|void|refund`), explicit transition validation with stable conflict errors, and unit/integration + permission-matrix coverage for transition history/auditability |
| J3 | TODO |  |  |  |
| J4 | TODO |  |  |  |
| K1 | DONE |  |  | Defined and documented v1 offline policy via ADR (`docs/adr/ADR-0001-offline-policy-v1.md`) and added machine-readable endpoint `GET /api/system/offline-policy` with integration coverage for auth and policy payload |
| K2 | DONE |  |  | Implemented idempotent ingestion with migration `V36` (`idempotency_key_event`), enforced `Idempotency-Key` API contract on checkout/payment transition endpoints, deterministic replay for same-payload retries, and stable conflict behavior (`POS-4009`) for key reuse with different payloads validated by integration tests |
| L1 | DONE |  |  | Implemented aggregated sales/returns reporting endpoint (`GET /api/reports/sales`, alias `/api/reports/sales-returns`) with date/store/terminal/cashier/category/tax-group filters, day/store/terminal/cashier/category/tax-group breakouts, summary totals including return and discount breakout (`discountGross`), and integration/security coverage |
| L2 | DONE |  |  | Implemented inventory reporting endpoints (`GET /api/reports/inventory/stock-on-hand`, `GET /api/reports/inventory/low-stock`, `GET /api/reports/inventory/movements`) with store/category/supplier filters (plus `from`/`to` for movement and `minimumQuantity` for low-stock), tabular export-ready row DTOs in `pos-api`, and integration/security coverage for filters, validation, and authorization |
| L3 | DONE |  |  | Implemented cash reporting endpoints (`GET /api/reports/cash/shifts`, `GET /api/reports/cash/end-of-day`) with store/terminal/cashier/date filters, shift-level expected-vs-counted variance visibility, and end-of-day variance reason aggregation by business date/store with integration/security coverage |
| L4 | DONE |  |  | Implemented CSV export endpoints for sales, inventory, and cash reports with deterministic UTF-8 headers/column order (`/api/reports/sales/export`, `/api/reports/inventory/*/export`, `/api/reports/cash/*/export`) and integration coverage for content type, file disposition, and authorization |
| L5 | DONE |  |  | Implemented exception reporting endpoints (`GET /api/reports/exceptions`, `/api/reports/exceptions/export`) with filters (`from`/`to`/store/terminal/cashier/reason/eventType), reconciliation-ready rows including actor/approver/terminal/correlation/reference, migration `V37` (`no_sale_drawer_event`), and integration/security coverage for filter combinations, invalid range validation, and authorization |
| M1 | DONE |  |  | Implemented printer abstraction in `pos-core` (`PrinterAdapter`, `PrintJob`, `PrintResult`), server-side receipt template rendering and ESC/POS adapter in `pos-server`, plus `POST /api/receipts/print` with explicit success/failure + retryable status and authorization/integration/unit coverage |
| M2 | TODO |  |  |  |
| M3 | TODO |  |  |  |
| M4 | TODO |  |  |  |
| N1 | TODO |  |  |  |
| N2 | TODO |  |  |  |
| O1 | TODO |  |  |  |
| O2 | TODO |  |  |  |
| O3 | TODO |  |  |  |
| O4 | TODO |  |  |  |
| O5 | TODO |  |  |  |
| O6 | TODO |  |  |  |
| O7 | TODO |  |  |  |
| O8 | TODO |  |  |  |
| O9 | TODO |  |  |  |
| O10 | TODO |  |  |  |
| O11 | TODO |  |  |  |
| O12 | TODO |  |  |  |
| O13 | TODO |  |  |  |
| P1 | TODO |  |  |  |
| P2 | TODO |  |  |  |
| P3 | TODO |  |  |  |
| P4 | TODO |  |  |  |
| P5 | TODO |  |  |  |
| P6 | TODO |  |  |  |

## 12. Immediate Next Three Cards
1. `M2` Cash drawer integration.
2. `M3` Scanner/Scale extension interfaces.
3. `M4` Receipt reprint and journal retrieval.

## 13. Final Product Readiness Checklist
1. All mandatory cards (`A` to `P`, excluding optional cards) are `DONE`.
2. UAT pass evidence attached.
3. Performance/reliability thresholds met.
4. Security and audit checks passed.
5. Backup/restore and rollback rehearsed.
6. Documentation complete and validated by non-authors.
7. Go-live checklist executed and signed.
