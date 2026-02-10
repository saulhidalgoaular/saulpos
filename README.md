# SaulPOS v2

SaulPOS v2 is a local-first, API-driven point-of-sale platform for convenience stores in Latin America.
The project is being rebuilt as a modular monorepo with deterministic backend behavior, testable business rules, and clear roadmap-driven delivery.

## Product Overview

SaulPOS v2 is designed for multi-store retail operations that need:
- Fast checkout behavior with server-authoritative business logic.
- Strong authentication and role-based access control.
- Deterministic money/cash workflows (shift lifecycle and reconciliation).
- Auditable operations through stable error contracts, correlation IDs, and structured logs.
- Controlled schema evolution through Flyway migrations.

Current implementation status is concentrated on roadmap foundation + early core domains:
- `A1` Runtime and build baseline.
- `A2` Error contract and observability.
- `A3` Security foundation.
- `B1` Tenant/location/terminal/store-assignment model.
- `B2` Permission matrix and enforcement.
- `B3` Shift and cash session lifecycle.
- `C1` Product and variant core.
- `C2` Category and department taxonomy.
- `C3` Price books and store overrides.
- `C4` Search and lookup performance.
- `C5` Unit/weight/open-price item modes.
- `D1` Tax engine v1.
- `D2` Rounding policy.
- `D3` Receipt sequence allocation.
- `E1` Discount primitives.
- `E2` Promotion engine v1.
- `E3` Loyalty hooks.
- `F1` Customer master.
- `F2` Customer groups and pricing hooks.
- `F3` Customer history.
- `I1` Supplier master.
- `I2` Purchase orders and receiving.
- `I3` Costing v1.
- `I4` Supplier returns.
- `J1` Tender and split payments.
- `J2` Payment state machine.
- `J3` Gift card issuance and redemption.
- `J4` Store-credit issuance and redemption.
- `K1` Offline policy definition.
- `K2` Idempotent event ingestion.
- `L1` Sales and returns reports.
- `L2` Inventory reports.
- `L3` Cash and shift reports.
- `L4` CSV export.
- `L5` Exception and override reports.
- `M1` Printer abstraction and templates.
- `M2` Cash drawer integration.
- `M4` Receipt reprint and journal retrieval.
- `M3` Scanner/scale extension interfaces.
- `N1` Fiscal provider SPI.
- `N2` Country fiscal provider module selection.
- `O1` UI architecture and design system.
- `O2` Authentication and session UI.
- `O3` Shift open/close and cash controls UI.
- `O4` Product search and cart screen.
- `O5` Checkout and payments UI.
- `O6` Returns and refunds UI.
- `O7` Backoffice UI (catalog, pricing, customers).
- `O8` Reporting and export UI.
- `O9` Hardware interaction UI.
- `O10` Offline/degraded mode UX.
- `O11` Suspended sales and override UX.
- `O12` Lot/expiry and supplier return UX.
- `O13` Receipt reprint and exception monitoring UX.
- `P1` End-to-end UAT scenarios.
- `P2` Performance and reliability hardening.
- `P3` Security and compliance verification.
- `P4` Packaging, deployment, and operations.
- `P5` Documentation and handover.
- `G1` Cart lifecycle service.
- `G2` Atomic checkout.
- `G3` Returns and refunds.
- `G4` Suspended/parked sales.
- `G5` Void and price override controls.
- `H1` Inventory movement ledger.
- `H2` Stock adjustments.
- `H3` Stocktake.
- `H4` Transfer orders.
- `H5` Lot and expiry tracking.

## Monorepo Architecture

Modules:
- `pos-core`: shared cross-cutting abstractions (soft-delete, printer adapter, scanner/scale/fiscal extension contracts).
- `pos-api`: transport contracts (request/response DTOs) shared by server/client.
- `pos-server`: Spring Boot backend with domain logic, persistence, security, and REST APIs.
- `pos-client`: JavaFX client module with screen-map/navigation foundation, reusable UI primitives, centralized theme tokens, authentication/session UX, shift-control workflow baseline, cashier workstation flows for selling/checkout/returns, backoffice operations for catalog/pricing/customers/lot-expiry/supplier-returns, reporting/export actions, hardware actions, receipt recovery/exception monitoring workflows, and degraded-mode connectivity guidance (`O1` + `O2` + `O3` + `O4` + `O5` + `O6` + `O7` + `O8` + `O9` + `O10` + `O11` + `O12` + `O13`).

Backend source of truth:
- All critical business behavior is implemented in `pos-server`.
- `pos-client` is intentionally "dumb" and should only consume APIs.

Client UI foundation (`O1`) is documented in `docs/ui/O1-ui-architecture-and-design-system.md`.

### Client Authentication and Session UX (`O2`)
- Login screen submits credentials to `POST /api/auth/login` and maps deterministic auth failures (`POS-4011`, `POS-4012`, `POS-4013`) into explicit operator feedback.
- Session bootstrap resolves current user context through `GET /api/security/me` after successful login.
- Protected navigation targets are guarded in client state and redirect to `LOGIN` if no authenticated session exists.
- Session expiry is visible in the top shell, and refresh handling uses `POST /api/auth/refresh` with safe fallback to login when refresh fails/expired.
- Logout action is exposed in shell header and calls `POST /api/auth/logout` before local session reset.

### Client Shift Open/Close and Cash Controls UI (`O3`)
- Shift screen now supports:
  - opening a shift (`cashierUserId`, `terminalDeviceId`, opening float),
  - loading an existing shift by ID,
  - recording paid-in and paid-out movements with reason note capture,
  - closing/reconciling shift with counted cash and close note.
- Client shift operations consume server contracts:
  - `POST /api/shifts/open`
  - `POST /api/shifts/{id}/cash-movements`
  - `POST /api/shifts/{id}/close`
  - `GET /api/shifts/{id}`
- UI feedback includes shift status, open/close totals, expected/counted cash, and variance visibility after each operation.

### Client Product Search and Cart Screen (`O4`)
- Sell screen now supports:
  - creating a cart from cashier/store/terminal context,
  - loading an existing cart by ID,
  - scanner-first barcode entry (Enter key) that resolves barcode lookup then adds line to cart,
  - paginated product search with quick-add to cart,
  - cart line quantity update and line removal,
  - explicit totals recalculation and operator-facing error feedback.
- Client sell operations consume server contracts:
  - `GET /api/catalog/products/lookup?merchantId={id}&barcode={value}`
  - `GET /api/catalog/products/search?merchantId={id}&q={query}&active={bool}&page={n}&size={n}`
  - `POST /api/sales/carts`
  - `GET /api/sales/carts/{id}`
  - `POST /api/sales/carts/{id}/lines`
  - `PUT /api/sales/carts/{id}/lines/{lineId}`
  - `DELETE /api/sales/carts/{id}/lines/{lineId}`
  - `POST /api/sales/carts/{id}/recalculate`
- Tests cover API contract mapping and sell coordinator success/error paths for scan/search/add/update/remove/recalculate flows.

### Client Checkout and Payments UI (`O5`)
- Checkout screen now supports:
  - tender capture for cash/card and split combinations,
  - payable/allocation/due/change visibility before submission,
  - checkout commit with receipt feedback and payment status summary after completion,
  - recoverable validation feedback for mismatched split tenders and invalid cash tendered amounts.
- Client checkout operation consumes server contract:
  - `POST /api/sales/checkout` (with generated `Idempotency-Key` header per request)
- Tests now cover:
  - HTTP checkout contract mapping and idempotency header propagation,
  - sell coordinator checkout success path and split/insufficient-tender validation failures.

### Client Returns and Refunds UI (`O6`)
- Returns screen now supports:
  - receipt lookup (`receiptNumber`) to resolve sale context and eligible return lines,
  - partial or full quantity return submission with reason-code capture,
  - refund tender capture (`CASH`/`CARD`) with optional reference and note fields,
  - explicit manager-approval guidance when return-window policy rejects cashier-level submission.
- Client returns operations consume server contracts:
  - `GET /api/refunds/lookup?receiptNumber={value}`
  - `POST /api/refunds/submit`
- Tests now cover:
  - HTTP refund lookup/submit contract mapping,
  - returns coordinator success/error paths for lookup, submit, and manager-approval-required flows.

### Client Backoffice UI (`O7`)
- Backoffice screen now supports:
  - catalog search/list by merchant and query,
  - product create/update actions (SKU, name, base price, barcodes),
  - customer list and lookup by document/email/phone criteria,
  - customer create/update actions with invoice/credit flags, tax identity, and contacts,
  - store-context price resolution preview (`storeLocationId` + `productId` + optional `customerId`).
- Client backoffice operations consume server contracts:
  - `GET /api/catalog/products`
  - `POST /api/catalog/products`
  - `PUT /api/catalog/products/{id}`
  - `GET /api/catalog/prices/resolve`
  - `GET /api/customers`
  - `GET /api/customers/lookup`
  - `POST /api/customers`
  - `PUT /api/customers/{id}`
- Tests now cover:
  - HTTP backoffice contract mapping for catalog/pricing/customer endpoints,
  - backoffice coordinator state/message handling for product load, customer validation, and price resolution flows.

### Client Reporting and Export UI (`O8`)
- Reporting screen now supports:
  - shared filter entry (`from`, `to`, `storeLocationId`, `terminalDeviceId`, `cashierUserId`, plus category/supplier/reason/event-type where applicable),
  - report loading actions for sales/returns, inventory movements, cash shifts, and exceptions,
  - streaming table preview strategy capped to a deterministic maximum row window,
  - CSV export actions for sales, inventory movements, cash shifts, and exceptions with explicit operator feedback.
- Client reporting operations consume server contracts:
  - `GET /api/reports/sales`
  - `GET /api/reports/inventory/movements`
  - `GET /api/reports/cash/shifts`
  - `GET /api/reports/exceptions`
  - `GET /api/reports/sales/export`
  - `GET /api/reports/inventory/movements/export`
  - `GET /api/reports/cash/shifts/export`
  - `GET /api/reports/exceptions/export`
- Tests now cover:
  - HTTP reporting contract mapping for filter queries and CSV export endpoints,
  - reporting coordinator filter propagation, streaming-preview state updates, and export feedback/error handling.

### Client Hardware Interaction UI (`O9`)
- Hardware screen now supports:
  - receipt print trigger with explicit operator-visible status progression (`queued`, `success`, `failure`),
  - hardware permission refresh via current-user permission introspection,
  - drawer-open action with terminal/reason capture and authorization gating.
- Client hardware operations consume server contracts:
  - `GET /api/security/permissions/current`
  - `POST /api/receipts/print`
  - `POST /api/receipts/drawer/open`
- Tests now cover:
  - HTTP hardware contract mapping for permission lookup, receipt print, and drawer open endpoints,
  - hardware coordinator behavior for permission gating plus success/failure action status messaging.

### Client Offline/Degraded Mode UX (`O10`)
- Shell header now shows explicit connectivity status (`ONLINE`/`OFFLINE`) with operator-facing status text and retry control.
- Client connectivity checks are policy-aware:
  - `GET /api/system/offline-policy` provides operation-level offline behavior/messages.
  - `GET /actuator/health` drives live connectivity status.
- Transactional actions now respect offline policy guards:
  - login blocks when `AUTH_LOGIN` is `ONLINE_ONLY`,
  - cart mutation paths block when `CART_MUTATION` is `ONLINE_ONLY`,
  - checkout blocks when `CHECKOUT` is `ONLINE_ONLY`.
- Tests now cover:
  - HTTP offline-policy contract mapping,
  - connectivity coordinator online/offline transitions and policy-message behavior,
  - auth and sell coordinator blocking behavior while offline.

### Client Suspended Sales and Override UX (`O11`)
- Sell screen now supports:
  - parked-cart controls to park active carts, list parked carts by store/terminal, and resume selected parked carts,
  - sensitive line actions for line void and line price override with mandatory reason-code capture,
  - permission-aware action gating so restricted operators see controls disabled unless `SALES_PROCESS`/override permissions are available.
- Client sell operations consume server contracts:
  - `POST /api/sales/carts/{id}/park`
  - `GET /api/sales/carts/parked?storeLocationId={id}&terminalDeviceId={id}`
  - `POST /api/sales/carts/{id}/resume`
  - `POST /api/sales/carts/{id}/lines/{lineId}/void`
  - `POST /api/sales/carts/{id}/lines/{lineId}/price-override`
  - `GET /api/security/permissions/current` (sell permission refresh/gating)
- Tests now cover:
  - HTTP sell contract mapping for parked-cart and sensitive-line endpoints,
  - sell coordinator permission refresh plus park/list/resume/void/override success and unauthorized-blocking flows.

### Client Lot/Expiry and Supplier Return UX (`O12`)
- Backoffice screen now supports:
  - lot/expiry-aware stock-balance loading for a store (optional product filter, lot-level toggle),
  - supplier return draft creation with required supplier/store/product/quantity/unit-cost fields,
  - supplier return lookup by ID,
  - supplier return approval and posting actions with operator note capture.
- Client backoffice operations consume server contracts:
  - `GET /api/inventory/balances?storeLocationId={id}&productId={id?}&lotLevel={bool}`
  - `POST /api/inventory/supplier-returns`
  - `GET /api/inventory/supplier-returns/{id}`
  - `POST /api/inventory/supplier-returns/{id}/approve`
  - `POST /api/inventory/supplier-returns/{id}/post`
- Tests now cover:
  - HTTP contract mapping for inventory balances and supplier return lifecycle endpoints,
  - backoffice coordinator state/message behavior for lot-balance load and supplier-return create/approve/post flows.

### Client Receipt Reprint and Exception Monitoring UX (`O13`)
- Hardware screen now supports:
  - historical receipt journal lookup by receipt number or sale ID,
  - permission-aware receipt reprint action (gated by `RECEIPT_REPRINT`),
  - operator feedback that combines lookup/reprint status with receipt context.
- Reporting exception preview now includes drill-down context fields:
  - actor, approver, cashier, reason, terminal, reference, and correlation identifiers.
- Client receipt and exception operations consume server contracts:
  - `GET /api/receipts/journal/by-number/{receiptNumber}`
  - `GET /api/receipts/journal/by-sale/{saleId}`
  - `POST /api/receipts/reprint`
  - `GET /api/reports/exceptions`
- Tests now cover:
  - HTTP contract mapping for receipt journal/reprint endpoints,
  - hardware coordinator permission + journal lookup + reprint flows,
  - reporting coordinator exception drill-down preview rendering.

### Release UAT Scenarios (`P1`)
- Added executable persona-based UAT suite in `pos-server`:
  - `P1EndToEndUatIntegrationTest` covers cashier, manager, inventory clerk, and admin flows.
- Added UAT checklist/evidence document:
  - `docs/uat/P1-end-to-end-uat-scenarios.md`.
- Suite execution command:
  - `mvn -pl pos-server test -Dtest=P1EndToEndUatIntegrationTest`.

### Performance and Reliability Hardening (`P2`)
- Added executable `P2` reliability/performance guard suite in `pos-server`:
  - `P2PerformanceReliabilityIntegrationTest` validates burst p95 targets and retry/idempotency no-corruption behavior.
- Added `P2` evidence and target documentation:
  - `docs/uat/P2-performance-and-reliability-hardening.md`.
- Added reusable k6 load scripts for peak checkout/lookup and reporting:
  - `docs/perf/k6/P2-peak-checkout-lookup.js`
  - `docs/perf/k6/P2-peak-reporting.js`
- Suite execution command:
  - `mvn -pl pos-server test -Dtest=P2PerformanceReliabilityIntegrationTest`.

### Security and Compliance Verification (`P3`)
- Added executable `P3` security/compliance verification suite in `pos-server`:
  - `P3SecurityComplianceIntegrationTest` validates RBAC deny-by-default/allow-by-permission paths and sensitive-action audit persistence/queryability.
- Added `P3` evidence and hardening checklist documentation:
  - `docs/uat/P3-security-and-compliance-verification.md`
  - `docs/security/P3-secret-configuration-hardening-checklist.md`
- Suite execution command:
  - `mvn -pl pos-server test -Dtest=P3SecurityComplianceIntegrationTest`.

### Packaging, Deployment, and Operations (`P4`)
- Added environment-specific runtime profiles in `pos-server`:
  - `application-dev.properties`
  - `application-staging.properties`
  - `application-prod.properties`
- Added operational scripts:
  - `ops/scripts/package-release.sh`
  - `ops/scripts/deploy-server.sh`
  - `ops/scripts/backup-postgres.sh`
  - `ops/scripts/restore-postgres.sh`
- Added operations runbook and validation notes:
  - `docs/ops/P4-packaging-deployment-and-operations.md`
  - `docs/ops/P4-backup-restore-validation.md`
- Readiness test command:
  - `mvn -pl pos-server test -Dtest=P4OperationsReadinessTest`.

### Documentation and Handover (`P5`)
- Added operations-facing and engineering-facing handover documents:
  - `docs/handover/P5-user-guide.md`
  - `docs/handover/P5-api-reference-and-integration-notes.md`
  - `docs/handover/P5-architecture-and-maintenance-guide.md`
- Added readiness test coverage for document presence and required sections:
  - `mvn -pl pos-server test -Dtest=P5DocumentationHandoverReadinessTest`.

## Implemented Domain Capabilities

### Foundation and Observability
- RFC7807-compatible error payloads with stable machine-readable codes (`POS-xxxx`).
- Correlation ID propagation through `X-Correlation-ID` and log MDC.
- Structured JSON logging (Logstash encoder).
- Actuator health/info/metrics endpoints enabled.

### Authentication and Security
- Token-based session auth with access + refresh token lifecycle:
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
  - `POST /api/auth/logout`
- Protected auth context endpoint:
  - `GET /api/security/me`
- Security controls:
  - BCrypt password hashing.
  - Failed-attempt lockout policy.
  - Auth audit records for login success/failure and logout.

### Role and Permission Matrix
- Current permissions and catalog:
  - `GET /api/security/permissions/current`
  - `GET /api/security/permissions/catalog`
- Role management:
  - `GET /api/security/roles`
  - `POST /api/security/roles`
  - `PUT /api/security/roles/{id}/permissions`
- Deny-by-default enforcement for sensitive domains (sales/refunds/inventory/reports/configuration).

### Identity and Store Structure
- Merchant, store, terminal, and store-user assignment APIs:
  - `/api/identity/merchants`
  - `/api/identity/stores`
  - `/api/identity/terminals`
  - `/api/identity/store-user-assignments`
- Supports create/list/get/update plus activate/deactivate lifecycle endpoints.
- Integrity rules include unique codes and assignment consistency.

### Customer Master
- Customer APIs:
  - `POST /api/customers`
  - `GET /api/customers`
  - `GET /api/customers/{id}`
  - `PUT /api/customers/{id}`
  - `POST /api/customers/{id}/activate`
  - `POST /api/customers/{id}/deactivate`
  - `GET /api/customers/lookup?merchantId={id}&documentType={type}&documentValue={value}`
  - `GET /api/customers/lookup?merchantId={id}&email={email}`
  - `GET /api/customers/lookup?merchantId={id}&phone={phone}`
- Customer model:
  - `customer`
  - `customer_tax_identity`
  - `customer_contact`
- Enforced rules include merchant-scoped document uniqueness, optional customer profile fields for rapid checkout capture, and invoice/credit customer flags.

### Customer Groups and Pricing Hooks
- Customer group APIs:
  - `POST /api/customers/groups`
  - `GET /api/customers/groups?merchantId={id}&active={bool}`
  - `PUT /api/customers/{id}/groups`
  - `GET /api/customers/{id}/groups`
- Customer model now includes group membership support:
  - `customer_group`
  - `customer_group_assignment`
- Customer responses now include assigned groups, and assignment validation enforces merchant consistency between customer and group.

### Customer History
- Customer history APIs:
  - `GET /api/customers/{id}/sales?from={isoDateTime?}&to={isoDateTime?}&page={n}&size={n}`
  - `GET /api/customers/{id}/returns?from={isoDateTime?}&to={isoDateTime?}&page={n}&size={n}`
- History model linkage:
  - `sale.customer_id` (nullable) links checkout sales to customer profiles.
- Enforced rules:
  - history results are paginated with deterministic ordering by newest timestamp then id,
  - date filters (`from`/`to`) are optional and validated,
  - endpoints are restricted to authorized customer-domain roles.

### Supplier Master
- Supplier APIs:
  - `POST /api/suppliers`
  - `GET /api/suppliers?merchantId={id?}&active={bool?}&q={query?}`
  - `GET /api/suppliers/{id}`
  - `PUT /api/suppliers/{id}`
  - `POST /api/suppliers/{id}/activate`
  - `POST /api/suppliers/{id}/deactivate`
- Supplier model:
  - `supplier`
  - `supplier_contact`
  - `supplier_terms`
- Enforced rules include merchant-scoped uniqueness for supplier code and normalized tax identifier, plus explicit active/inactive lifecycle control.

### Purchase Orders and Receiving
- Purchase order APIs:
  - `POST /api/inventory/purchase-orders`
  - `GET /api/inventory/purchase-orders/{id}`
  - `POST /api/inventory/purchase-orders/{id}/approve`
  - `POST /api/inventory/purchase-orders/{id}/receive`
- Purchase model:
  - `purchase_order`
  - `purchase_order_line`
  - `goods_receipt`
- Enforced rules:
  - purchase order lifecycle is explicit (`DRAFT` -> `APPROVED` -> `PARTIALLY_RECEIVED`/`RECEIVED`),
  - partial receiving is cumulative by line and cannot exceed ordered quantity,
  - supplier/store/product merchant consistency is enforced before receiving,
  - receiving posts immutable positive inventory movements with `PURCHASE_RECEIPT` reference type,
  - each receive line includes `unitCost`, which drives deterministic inventory costing updates.

### Costing v1
- Costing model:
  - `inventory_product_cost` (store+product context)
- Costing behavior:
  - each purchase receipt updates `weightedAverageCost` and `lastCost`,
  - costing updates are linked to the latest receipt reference and movement for auditability.
- Inventory balance responses now include:
  - `weightedAverageCost`
  - `lastCost`

### Supplier Returns
- Supplier return APIs:
  - `POST /api/inventory/supplier-returns`
  - `GET /api/inventory/supplier-returns/{id}`
  - `POST /api/inventory/supplier-returns/{id}/approve`
  - `POST /api/inventory/supplier-returns/{id}/post`
- Supplier return model:
  - `supplier_return`
  - `supplier_return_line`
- Enforced rules:
  - supplier return lifecycle is explicit (`DRAFT` -> `APPROVED` -> `POSTED`),
  - return quantity per line cannot exceed supplier-received eligible quantity (received minus already returned),
  - posting records immutable negative inventory movements with `SUPPLIER_RETURN` reference type for stock and cost traceability.

### Shift and Cash Session Lifecycle
- `POST /api/shifts/open`
- `POST /api/shifts/{id}/cash-movements`
- `POST /api/shifts/{id}/close`
- `GET /api/shifts/{id}`
- Business constraints include:
  - one open shift per cashier + terminal,
  - finite-state transition checks,
  - counted-vs-expected reconciliation with variance capture.

### Sales Cart Lifecycle
- Cart APIs:
  - `POST /api/sales/carts`
  - `GET /api/sales/carts/{id}`
  - `POST /api/sales/carts/{id}/lines`
  - `PUT /api/sales/carts/{id}/lines/{lineId}`
  - `DELETE /api/sales/carts/{id}/lines/{lineId}`
  - `POST /api/sales/carts/{id}/recalculate`
- Cart model:
  - `sale_cart`
  - `sale_cart_line`
- Enforced rules:
  - cart mutation and totals recomputation are deterministic for the same line inputs,
  - invalid product IDs return stable not-found errors,
  - invalid quantity by sale mode (for example decimal quantity on `UNIT`) returns stable validation errors,
  - idempotent add-line behavior is supported via `lineKey` per cart.

### Suspended/Parked Sales
- Park/resume/cancel APIs:
  - `POST /api/sales/carts/{id}/park`
  - `POST /api/sales/carts/{id}/resume`
  - `POST /api/sales/carts/{id}/cancel`
  - `GET /api/sales/carts/parked?storeLocationId={id}&terminalDeviceId={id?}`
- Extended cart model and audit support:
  - cart statuses include `PARKED` and `EXPIRED` in addition to active lifecycle states,
  - `parked_cart_reference` stores park reference, parked timestamp, and expiry window,
  - `sale_cart_event` captures `PARKED`, `RESUMED`, `CANCELLED`, and policy-driven `EXPIRED` transitions.
- Enforced rules:
  - resume/cancel requires matching cashier+terminal assignment for the cart,
  - parked carts expire by policy (`app.sales.parked-cart-expiry-minutes`),
  - park/resume/cancel transitions are auditable with actor and correlation context.

### Void and Price Override Controls
- Controlled cart override APIs:
  - `POST /api/sales/carts/{id}/lines/{lineId}/void`
  - `POST /api/sales/carts/{id}/lines/{lineId}/price-override`
  - `POST /api/sales/carts/{id}/void`
- Override/void audit model:
  - `void_reason_code`
  - `sale_override_event`
- Enforced rules:
  - reason code is mandatory for line void, line override, and cart void actions,
  - price overrides above configured threshold require explicit `DISCOUNT_OVERRIDE` permission,
  - line void and price override actions always trigger deterministic tax/totals recomputation.

### Tender and Split Payments
- Checkout payment API:
  - `POST /api/sales/checkout` (requires `Idempotency-Key` header)
- Payment model:
  - `payment`
  - `payment_allocation`

### Sales and Returns Reporting
- Aggregated reporting APIs:
  - `GET /api/reports/sales`
  - `GET /api/reports/sales-returns` (alias)
- CSV export API:
  - `GET /api/reports/sales/export`
- Supported filters:
  - `from`, `to`, `storeLocationId`, `terminalDeviceId`, `cashierUserId`, `categoryId`, `taxGroupId`
- Report outputs:
  - summary totals for sales and returns,
  - explicit return breakout (`returnGross`) and discount breakout (`discountGross`),
  - grouped breakdowns by day, store, terminal, cashier, category, and tax group.
  - CSV exports return `text/csv;charset=UTF-8` with deterministic header and column order.
- Enforced rules:
  - sum of allocated tenders must equal cart payable total,
  - cash allocations support explicit tendered amount and deterministic change calculation,
  - non-cash allocations must not over/under tender relative to the allocated amount.
- Checkout payment allocations are persisted as deterministic snapshots per active cart.

### Inventory Reporting
- Inventory reporting APIs:
  - `GET /api/reports/inventory/stock-on-hand`
  - `GET /api/reports/inventory/low-stock`
  - `GET /api/reports/inventory/movements`
- CSV export APIs:
  - `GET /api/reports/inventory/stock-on-hand/export`
  - `GET /api/reports/inventory/low-stock/export`
  - `GET /api/reports/inventory/movements/export`
- Supported filters:
  - `storeLocationId`, `categoryId`, `supplierId`
  - movement report also supports `from` and `to`
  - low-stock report requires `minimumQuantity`
- Report outputs:
  - stock-on-hand tabular rows with quantity on hand, cost snapshots, and stock value,
  - low-stock tabular rows with shortage quantity against requested minimum,
  - movement tabular rows including movement type, source reference, and timestamp.

### Cash and Shift Reporting
- Cash reporting APIs:
  - `GET /api/reports/cash/shifts`
  - `GET /api/reports/cash/end-of-day`
- CSV export APIs:
  - `GET /api/reports/cash/shifts/export`
  - `GET /api/reports/cash/end-of-day/export`
- Supported filters:
  - `from`, `to`, `storeLocationId`, `terminalDeviceId`, `cashierUserId`
- Report outputs:
  - shift-level rows with opening/paid-in/paid-out totals plus expected close cash, counted close cash, variance amount, and variance reason,
  - shift summary totals across the filtered dataset,
  - end-of-day store buckets with shift count, expected/counted totals, net variance, and aggregated variance reason counts.

### Exception and Override Reporting
- Exception reporting APIs:
  - `GET /api/reports/exceptions`
- CSV export API:
  - `GET /api/reports/exceptions/export`
- Supported filters:
  - `from`, `to`, `storeLocationId`, `terminalDeviceId`, `cashierUserId`, `reasonCode`, `eventType`
- Event coverage:
  - `LINE_VOID`, `CART_VOID`, `PRICE_OVERRIDE`
  - `NO_SALE` (drawer-open events from `no_sale_drawer_event`)
  - `REFUND_EXCEPTION` (refund payment transitions)
- Report outputs:
  - rows include actor, approver (when available), terminal context, reason code, note, correlation ID, and source reference number.

### Payment State Machine
- Payment lifecycle APIs:
  - `GET /api/payments/{id}`
  - `POST /api/payments/{id}/capture` (requires `Idempotency-Key` header)
  - `POST /api/payments/{id}/void` (requires `Idempotency-Key` header)
  - `POST /api/payments/{id}/refund` (requires `Idempotency-Key` header)
- Payment lifecycle model:
  - `payment.status` (`AUTHORIZED`, `CAPTURED`, `VOIDED`, `REFUNDED`)
  - `payment_transition`
- Enforced rules:
  - invalid payment state transitions are rejected with stable conflict errors,
  - every payment transition stores actor, correlation context, and timestamp for auditability.

### Gift Cards
- Gift card APIs:
  - `POST /api/gift-cards/issue`
  - `POST /api/gift-cards/{cardNumber}/redeem`
  - `GET /api/gift-cards/{cardNumber}?merchantId={id}`
- Gift card model:
  - `gift_card`
  - `gift_card_transaction`
- Enforced rules:
  - gift-card balance cannot go below zero on redemption,
  - every redemption must be linked to exactly one financial context (`saleId` or `saleReturnId`),
  - sale/refund context merchant must match the gift-card merchant.

### Store Credit
- Store-credit APIs:
  - `POST /api/store-credits/issue`
  - `POST /api/store-credits/{accountId}/redeem`
  - `GET /api/store-credits/{accountId}?merchantId={id}`
- Store-credit model:
  - `store_credit_account`
  - `store_credit_transaction`
- Enforced rules:
  - one ledger account per merchant/customer (`merchant_id`, `customer_id`),
  - issue transactions are linked to a refund context (`saleReturnId`) for the same customer and merchant,
  - redeem transactions are linked to a sale context (`saleId`) for the same customer and merchant,
  - store-credit balance cannot go below zero.

### Offline Policy Definition
- Offline policy contract API:
  - `GET /api/system/offline-policy`
- Policy behavior in v1:
  - transactional flows (`AUTH_LOGIN`, `CART_MUTATION`, `CHECKOUT`) are `ONLINE_ONLY`,
  - cached reference viewing (`CATALOG_REFERENCE_VIEW`) is `DEGRADED_READ_ONLY`.
- Technical controls and user-facing fail behavior are standardized and documented in:
  - `docs/adr/ADR-0001-offline-policy-v1.md`

### Idempotent Event Ingestion
- Idempotency persistence model:
  - `idempotency_key_event`
- Enforced idempotency rules:
  - duplicate `Idempotency-Key` with same request payload replays the original response,
  - duplicate `Idempotency-Key` with a different payload returns stable conflict (`POS-4009`),
  - checkout and payment transition endpoints are replay-safe.

### Atomic Checkout
- Checkout flow now commits sale, payment snapshot, receipt allocation, and inventory movement records in one transaction.
- Checkout request supports optional `customerId` linkage to persist customer-centric sale history.
- Checkout request supports optional `invoiceRequired` toggle with required customer tax identity validation.
- Checkout persistence model:
  - `sale`
  - `sale_line`
  - `inventory_movement`
- `POST /api/sales/checkout` response now includes:
  - `saleId`
  - `receiptNumber`
- Enforced rules:
  - checkout requires an active cart with at least one line,
  - successful checkout transitions the cart status to `CHECKED_OUT`,
  - each checkout line writes a negative `SALE` inventory movement for traceability.

### Returns and Refunds
- Return APIs:
  - `GET /api/refunds/lookup?receiptNumber={receiptNumber}`
  - `POST /api/refunds/submit`
- Return/refund model:
  - `sale_return`
  - `sale_return_line`
  - `sale_return_refund`
- Enforced rules:
  - partial and full returns are supported by sale line,
  - return quantity eligibility is enforced across cumulative prior returns,
  - restricted return windows require manager-level override permission,
  - each submitted return creates positive `RETURN` inventory movements linked by immutable return reference.

### Inventory Movement Ledger
- Inventory ledger APIs:
  - `POST /api/inventory/movements`
  - `GET /api/inventory/movements?storeLocationId={id}&productId={id?}`
  - `GET /api/inventory/balances?storeLocationId={id}&productId={id?}`
- Inventory model and projection:
  - `inventory_movement` now supports typed movement/reference combinations (`SALE`, `RETURN`, `ADJUSTMENT`) with enforced source-document metadata.
  - `stock_balance` view provides computed on-hand balances from movement history.
- Enforced rules:
  - manual `SALE` movement creation is blocked (sale movements remain system-generated by checkout),
  - return movements require positive quantity and `SALE_RETURN` reference type,
  - manual adjustment movements require `STOCK_ADJUSTMENT` reference type and non-zero quantity,
  - transfer workflow writes paired `ADJUSTMENT` movements using `STOCK_TRANSFER_OUT` and `STOCK_TRANSFER_IN`,
  - system-generated stocktake variance postings use `ADJUSTMENT` movements with `STOCKTAKE` reference type.
- Ledger query responses include deterministic per-product running balance, and stock-on-hand is reproducible from movement aggregation.

### Stock Adjustments
- Stock adjustment APIs:
  - `POST /api/inventory/adjustments`
  - `POST /api/inventory/adjustments/{id}/approve`
  - `POST /api/inventory/adjustments/{id}/post`
- Stock adjustment model:
  - `stock_adjustment`
- Enforced rules:
  - reason code is mandatory for all manual adjustments,
  - adjustments at or above `app.inventory.adjustment-manager-approval-threshold` require manager approval (`CONFIGURATION_MANAGE`) before posting,
  - posting an adjustment always writes an immutable `ADJUSTMENT` inventory movement with `STOCK_ADJUSTMENT` reference metadata.

### Stocktake
- Stocktake APIs:
  - `POST /api/inventory/stocktakes`
  - `POST /api/inventory/stocktakes/{id}/start`
  - `POST /api/inventory/stocktakes/{id}/finalize`
  - `GET /api/inventory/stocktakes/{id}/variance`
- Stocktake model:
  - `stocktake_session`
  - `stocktake_line`
- Enforced rules:
  - stocktake sessions follow deterministic status flow (`DRAFT` -> `STARTED` -> `FINALIZED`),
  - expected quantities are snapshotted at start time and remain fixed through finalize,
  - finalize requires counted quantities for the full stocktake product set and posts immutable variance movements with `STOCKTAKE` references,
  - variance reporting is exposed by product and aggregated by category.

### Transfer Orders
- Transfer APIs:
  - `POST /api/inventory/transfers`
  - `GET /api/inventory/transfers/{id}`
  - `POST /api/inventory/transfers/{id}/ship`
  - `POST /api/inventory/transfers/{id}/receive`
- Transfer model:
  - `stock_transfer`
  - `stock_transfer_line`
- Enforced rules:
  - source and destination store must be different and belong to the same merchant,
  - transfer status flow is deterministic (`DRAFT` -> `SHIPPED` -> `PARTIALLY_RECEIVED` -> `RECEIVED`),
  - receive operations are cumulative per line and cannot exceed shipped quantity,
  - out/in inventory postings are paired and traceable through transfer-scoped references.

### Lot and Expiry Tracking
- Lot-aware receiving and inventory APIs:
  - `POST /api/inventory/purchase-orders/{id}/receive` now accepts lot entries per receive line (`lotCode`, `expiryDate`, `quantity`) for lot-tracked products.
  - `GET /api/inventory/balances?storeLocationId={id}&productId={id?}&lotLevel=true` returns lot-level balances with expiry state (`ACTIVE`, `EXPIRED`, `NO_EXPIRY`).
  - `GET /api/inventory/movements` includes lot allocations per movement for receipt/sale/return traceability.
- Lot model:
  - `inventory_lot`
  - `inventory_lot_balance`
  - `inventory_movement_lot`
- Enforced rules:
  - lot tracking is configurable per product (`product.lot_tracking_enabled`),
  - FEFO consumption is enforced for lot-tracked sales (earliest expiry first),
  - expired lots are blocked from sale unless `app.inventory.expiry-override-enabled=true` and the actor has `CONFIGURATION_MANAGE`,
  - sale and return movements keep lot-level linkage to original receipt lots for traceability.

### Catalog and Category Hierarchy
- Product APIs:
  - `POST /api/catalog/products`
  - `GET /api/catalog/products`
  - `GET /api/catalog/products/search?merchantId={id}&q={query}&page={n}&size={n}`
  - `GET /api/catalog/products/{id}`
  - `PUT /api/catalog/products/{id}`
  - `POST /api/catalog/products/{id}/activate`
  - `POST /api/catalog/products/{id}/deactivate`
  - `GET /api/catalog/products/lookup?merchantId={id}&barcode={code}`
- Category hierarchy APIs:
  - `GET /api/catalog/categories/tree?merchantId={id}`
  - `POST /api/catalog/categories/{id}/reparent`
- Enforced rules include merchant-scoped SKU uniqueness, cycle-safe category trees, and no new product assignment to inactive categories.
- Search endpoint provides deterministic pagination (ordered by normalized SKU then ID) and supports query matching by SKU, name, or barcode.

### Unit, Weight, and Open-Price Item Modes
- Product configuration now supports:
  - `saleMode`: `UNIT`, `WEIGHT`, `OPEN_PRICE`
  - `quantityUom`: `UNIT`, `KILOGRAM`, `GRAM`, `POUND`
  - `quantityPrecision` rules by mode
  - open-price policy: `openPriceMin`, `openPriceMax`, `openPriceRequiresReason`
- Open-price entry policy validation endpoint:
  - `POST /api/catalog/products/{id}/open-price/validate`
- Manual open-price validations are audited in `open_price_entry_audit` with actor, reason, correlation ID, and timestamp.

### Pricing (Price Books and Store Overrides)
- Price resolution API:
  - `GET /api/catalog/prices/resolve?storeLocationId={id}&productId={id}&customerId={id?}&at={isoDateTime}`
- Resolution precedence:
  - store override,
  - customer-group scoped price book (when `customerId` is provided and customer has active group assignments),
  - active price book item (effective window),
  - product base price fallback.
- Deterministic and auditable resolution is returned with source metadata (`STORE_OVERRIDE`, `CUSTOMER_GROUP_PRICE_BOOK`, `PRICE_BOOK`, `BASE_PRICE`).

### Tax Engine v1
- Tax preview API for cart calculations:
  - `POST /api/tax/preview`
- Supports store-configured tax modes:
  - `INCLUSIVE`
  - `EXCLUSIVE`
- Supports tax treatments:
  - standard rated
  - exempt
  - zero-rated
- Returns deterministic line-level breakdown (`netAmount`, `taxAmount`, `grossAmount`) plus totals.
- Tax configuration model:
  - `tax_group`
  - `store_tax_rule`
- product-level tax-group assignment via `product.tax_group_id`

### Rounding Policy
- Store/tender rounding policy model:
  - `rounding_policy`
- `POST /api/tax/preview` now supports optional `tenderType` (`CASH`, `CARD`) and returns explicit rounding fields:
  - `roundingAdjustment`
  - `totalPayable`
  - `rounding` detail (`applied`, `method`, `increment`, `originalAmount`, `roundedAmount`, `adjustment`)
- Rounding is policy-driven per store and tender, and always returned as an explicit totals line (no hidden adjustment).

### Receipt Sequence Allocation
- Receipt allocation API:
  - `POST /api/receipts/allocate`
- Allocates immutable receipt numbers per terminal-backed series with explicit policy metadata (`GAPLESS`/`GAPPED`).
- Concurrency-safe sequence allocation with unique constraints on `(series_id, number)` and `receipt_number`.
- Allocation endpoint is permission-protected (`SALES_PROCESS`) and returns both numeric and formatted receipt identifiers.

### Receipt Printing (M1)
- Receipt print API:
  - `POST /api/receipts/print`
- Printer abstraction introduced in `pos-core` (`PrinterAdapter`, `PrintJob`, `PrintResult`) so rendering/dispatch is decoupled from hardware transport.
- Server-side receipt template rendering is deterministic and includes store/terminal/cashier context, line details, totals, and optional `COPY` marker.
- Default ESC/POS adapter is implemented behind the abstraction and returns explicit status (`SUCCESS`/`FAILED`) with retryability metadata.
- Print endpoint is permission-protected (`SALES_PROCESS`) and never mutates checkout/sale financial state.

### Cash Drawer Integration (M2)
- Cash drawer API:
  - `POST /api/receipts/drawer/open`
- Drawer-open requests dispatch an ESC/POS pulse command through the same printer abstraction used by receipt printing.
- Access is permission-protected with `CASH_DRAWER_OPEN`.
- Every drawer-open request is audited in `no_sale_drawer_event` with terminal/store context, actor username, reason code, note/reference, and correlation ID for exception reporting reconciliation.

### Receipt Reprint and Journal Retrieval (M4)
- Receipt journal APIs:
  - `GET /api/receipts/journal/by-sale/{saleId}`
  - `GET /api/receipts/journal/by-number/{receiptNumber}`
- Receipt reprint API:
  - `POST /api/receipts/reprint`
- Reprint operations are role-controlled with explicit `RECEIPT_REPRINT` permission.
- Reprinted receipts are marked as `COPY` and include the reprint operator and timestamp in the rendered template.
- Every copy reprint attempt (success/failure) is audited in `receipt_print_event` with sale/receipt context, actor, adapter status, retryability, and correlation ID.

### Scanner and Scale Extension Interfaces (M3)
- Hardware extension contracts introduced in `pos-core`:
  - scanner: `ScannerAdapter`, `ScanRequest`, `ScanResult`, `ScanStatus`
  - scale: `ScaleAdapter`, `ScaleReadRequest`, `ScaleReadResult`, `ScaleReadStatus`
- Default no-op stubs are available in `pos-server`:
  - `NoOpScannerAdapter`
  - `NoOpScaleAdapter`
- Stubs return deterministic unsupported/failure responses so hardware-specific integrations can be added later without coupling checkout/reporting domains to concrete scanner/scale vendors.

### Discount Primitives
- Discount APIs:
  - `POST /api/discounts/apply`
  - `POST /api/discounts/{id}/remove`
  - `POST /api/discounts/preview`
- Supports line and cart discounts with `FIXED` and `PERCENTAGE` primitives.
- Manual discounts require reason codes from `discount_reason_code`.
- High-threshold discounts require explicit `DISCOUNT_OVERRIDE` permission.
- Discount preview returns subtotal-before, total-discount, subtotal-after, line-level discount allocations, and tax-adjusted totals.
- Manual discount actions are persisted in `discount_application` with actor, reason, timestamps, and active/removed lifecycle state.

### Promotion Engine v1
- Promotion evaluation API:
  - `POST /api/promotions/evaluate`
- Promotion model:
  - `promotion`
  - `promotion_rule`
  - `promotion_window`
- Supports rule-based eligibility with active time windows and deterministic overlap conflict resolution.
- Current rule types:
  - `PRODUCT_PERCENTAGE`
  - `CART_FIXED`
- Winner selection strategy for overlapping promotions:
  - highest `priority`,
  - then highest computed discount amount,
  - then lowest promotion id.
- Response includes applied promotion explanation text for receipt transparency.

### Loyalty Hooks
- Loyalty hook APIs:
  - `POST /api/loyalty/earn`
  - `POST /api/loyalty/redeem`
- Loyalty extension model:
  - `loyalty_event`
- Integration behavior:
  - configuration toggle `app.loyalty.enabled` enables/disables provider calls,
  - when disabled, operations return deterministic `DISABLED` status and continue,
  - provider runtime failures return deterministic `UNAVAILABLE` status without breaking the calling flow.
- Includes a default stub provider contract (`LoyaltyProvider`) for earn/redeem extension points without hard coupling.

### Fiscal Provider SPI (N1)
- Fiscal plugin contracts introduced in `pos-core`:
  - `FiscalProvider`
  - `FiscalIssueInvoiceCommand`
  - `FiscalCancelInvoiceCommand`
  - `FiscalIssueCreditNoteCommand`
  - `FiscalProviderResult`
- Fiscal persistence model:
  - `fiscal_document`
  - `fiscal_event`
- Checkout behavior:
  - `POST /api/sales/checkout` supports `invoiceRequired` toggle,
  - invoice-required checkout validates required customer fiscal fields (active customer tax identity),
  - fiscal outcomes are persisted for retry/audit visibility.
- Deployment policy behavior:
  - when `app.fiscal.enabled=false`, checkout can still proceed if `app.fiscal.allow-invoice-with-disabled-provider=true`, and a deterministic `SKIPPED` fiscal outcome is recorded.
- Includes a default stub fiscal provider (`StubFiscalProvider`) so country implementations can be added without changing sales core contracts.

### Country Fiscal Provider Modules (N2)
- Fiscal country modules can be added by registering `CountryFiscalProviderFactory` beans in `pos-server`.
- Runtime selection uses `app.fiscal.country-code`:
  - blank value keeps `StubFiscalProvider`,
  - matching country code selects that country module provider,
  - unknown country code or duplicate country registrations fail fast at startup.
- This keeps the sales core contract unchanged (`FiscalProvider` remains the integration boundary).

## Data and Migration Strategy

- Flyway migrations live in `pos-server/src/main/resources/db/migration`.
- Implemented migration chain:
  - `V1__baseline.sql`
  - `V2__security_foundation.sql`
  - `V3__tenant_and_location_model.sql`
  - `V4__permission_matrix_and_enforcement.sql`
  - `V5__shift_and_cash_session_lifecycle.sql`
  - `V6__product_and_variant_core.sql`
  - `V7__category_department_taxonomy.sql`
  - `V8__price_books_and_store_overrides.sql`
  - `V9__catalog_search_and_lookup_performance.sql`
  - `V10__unit_weight_open_price_item_modes.sql`
  - `V11__tax_engine_v1.sql`
  - `V12__rounding_policy.sql`
  - `V13__receipt_sequence_allocation.sql`
  - `V14__discount_primitives.sql`
  - `V15__promotion_engine_v1.sql`
  - `V16__customer_master.sql`
  - `V17__loyalty_hooks.sql`
  - `V18__customer_groups_and_pricing_hooks.sql`
  - `V19__cart_lifecycle_service.sql`
  - `V20__suspended_parked_sales.sql`
  - `V21__void_and_price_override_controls.sql`
  - `V22__supplier_master.sql`
  - `V23__tender_and_split_payments.sql`
  - `V24__atomic_checkout.sql`
  - `V25__payment_state_machine.sql`
  - `V26__inventory_movement_ledger.sql`
  - `V27__returns_and_refunds.sql`
  - `V28__customer_history.sql`
  - `V29__stock_adjustments.sql`
  - `V30__stocktake.sql`
  - `V31__transfer_orders.sql`
  - `V32__purchase_orders_and_receiving.sql`
  - `V33__lot_and_expiry_tracking.sql`
  - `V34__inventory_costing_v1.sql`
  - `V35__supplier_returns.sql`
  - `V36__idempotent_event_ingestion.sql`
  - `V37__exception_and_override_reports.sql`
  - `V38__cash_drawer_integration.sql`
  - `V39__receipt_reprint_and_journal.sql`
  - `V40__fiscal_provider_spi.sql`
  - `V41__gift_cards.sql`
  - `V42__store_credit_accounts.sql`
- Deletion policy is configurable with:
  - `app.deletion-strategy=soft` (default)
  - `app.deletion-strategy=hard`

## Local Development

### Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL (default runtime configuration points to PostgreSQL on `localhost:5432`)

### Build all modules

```bash
mvn clean install
```

### Run full test suite (default profile / in-memory test profile)

```bash
mvn clean verify
```

### Optional PostgreSQL compatibility test run

```bash
mvn -Pit-postgres verify
```

### Run backend

```bash
cd pos-server
mvn spring-boot:run
```

### Run JavaFX client

```bash
cd pos-client
mvn javafx:run
```

Client architecture notes:
- `docs/ui/O1-ui-architecture-and-design-system.md`

## Configuration Highlights

Main runtime properties are in `pos-server/src/main/resources/application.properties`.
Key settings include:
- `server.port=8080`
- `spring.datasource.*` (PostgreSQL defaults)
- `app.security.max-failed-attempts`
- `app.security.lock-duration-minutes`
- `app.security.access-token-ttl-minutes`
- `app.security.refresh-token-ttl-minutes`
- `app.sales.parked-cart-expiry-minutes`
- `app.sales.price-override-approval-threshold-percent`
- `app.inventory.adjustment-manager-approval-threshold`
- `app.inventory.expiry-override-enabled`
- `app.fiscal.enabled`
- `app.fiscal.allow-invoice-with-disabled-provider`
- `app.fiscal.country-code`
- `management.endpoints.web.exposure.include=health,info,metrics`

## Testing Coverage (Implemented Domains)

`pos-client` currently includes:
- Screen-map/navigation integrity tests for deterministic route flow (`ScreenRegistryTest`).
- Design-system component catalog tests for reusable primitive coverage and keyboard order (`DesignSystemCatalogTest`).
- Theme contrast tests for readability thresholds (`ThemeTokensTest`).
- App state store tests for authenticated session transitions (`AppStateStoreTest`).

`pos-server` currently includes:
- Integration tests for auth lifecycle, brute-force lockout, identity APIs, permission matrix, shift lifecycle, and catalog flows.
- Integration tests for price resolution precedence and effective-date windows.
- Integration tests for catalog search pagination, deterministic ordering, and barcode search matching.
- Integration tests for weighted item configuration and open-price policy validation/audit flow.
- Unit tests for pricing resolver precedence and fallback behavior.
- Unit tests for sale-mode quantity precision and open-price policy validation.
- Unit tests for tax calculation modes (`INCLUSIVE`, `EXCLUSIVE`, exempt, zero-rated).
- Unit tests for rounding policy midpoint and edge behavior (`NEAREST`, `UP`, `DOWN`).
- Unit tests for discount calculation order (line then cart) and manager-threshold enforcement.
- Unit tests for promotion rule evaluation (`PRODUCT_PERCENTAGE`, `CART_FIXED`) and deterministic overlap winner selection.
- Concurrency coverage for open-shift race conditions.
- Repository and validator tests for catalog and category constraints.
- Error-handling integration tests for stable error contracts.
- Integration tests for `POST /api/tax/preview` covering deterministic line/total breakdown and missing-rule validation.
- Integration tests for tax preview rounding output (`roundingAdjustment`, `totalPayable`) with and without tender-specific policy.
- Integration tests for promotion evaluation endpoint, overlapping promotion conflicts, and explanation payloads.
- Integration tests for receipt allocation sequencing, authorization enforcement, and concurrent allocation race safety.
- Integration tests for cash drawer open authorization and persisted no-sale drawer audit events.
- Integration tests for discount apply/remove/preview flows, reason-code validation, and high-threshold permission enforcement.
- Integration tests for customer-group create/list/assignment flows and cross-merchant assignment rejection.
- Integration tests for customer-context price resolution precedence (`CUSTOMER_GROUP_PRICE_BOOK` vs generic price books).
- Integration tests for supplier CRUD/search flows, merchant-scoped unique identifier conflicts, and status lifecycle endpoints.
- Integration tests for customer sales/returns history pagination and date filters.
- Unit tests for pricing resolution with customer-group contexts.
- Integration tests for cart lifecycle create/add/update/remove/recalculate flows and idempotent line-key behavior.
- Integration tests for parked cart lifecycle (park/resume/cancel), parked list filtering, and expiry-policy behavior.
- Concurrency integration tests for simultaneous parked-cart resume attempts.
- Integration tests for line/cart void and line price-override flows including override-event auditing.
- Integration tests for manager-threshold override enforcement and sales authorization checks on new override endpoints.
- Integration tests for checkout split-payment allocation validation, persisted payment snapshots, and atomic sale/line/inventory movement persistence.
- Integration tests for payment lifecycle transitions (`authorize -> capture -> refund`) and invalid transition rejection with persisted transition history audit.
- Integration tests for gift-card issue/redeem/get API flow, redemption sale-context linkage, and overdraw conflict behavior.
- Integration tests for store-credit issue/redeem/get API flow, refund/sale context reconciliation, and overdraw conflict behavior.
- Concurrency integration test for parallel checkout attempts on the same cart (single success + conflict for competing request).
- Unit tests for cart quantity policy validation by sale mode and precision.
- Unit tests for tender allocation validation and cash-change calculation.
- Unit tests for payment state transition rules.
- Unit tests for gift-card issue/redeem rules, including non-negative balance enforcement and sale-linked redemption context.
- Integration tests for offline policy contract endpoint authorization and payload (`GET /api/system/offline-policy`).
- Unit tests for inventory balance calculation and deterministic 3-decimal quantity normalization.
- Integration tests for inventory ledger sale/return/adjustment effects, running-balance output, and stock-balance aggregation.
- Integration tests for stock adjustment create/approve/post flow, manager-approval threshold enforcement, and movement posting guarantees.
- Integration tests for stocktake create/start/finalize lifecycle, variance movement posting, and variance reporting by product/category.
- Integration tests for transfer draft/ship/receive lifecycle, partial receive reconciliation, and paired source/destination movement traceability.
- Integration tests for lot-aware purchase receiving, FEFO sale allocation, expiry blocking, and manager override flow.
- Permission-matrix integration coverage for inventory ledger endpoints.
- Permission-matrix integration coverage for stock adjustment endpoint authorization (`INVENTORY_ADJUST` vs `CONFIGURATION_MANAGE` approval path).
- Unit tests for FEFO lot selection ordering and expired-lot conflict behavior.
- Unit tests for default scanner/scale no-op adapters and deterministic unsupported/failure contract behavior.
- Unit tests for fiscal SPI service behavior (provider enabled/disabled policy and deterministic outcome persistence).
- Unit tests for fiscal country-provider selection and startup validation behavior (`FiscalConfigurationTest`).
- Integration tests for invoice-required checkout validation and fiscal outcome persistence when provider is disabled.
- Permission-matrix integration coverage for store-credit endpoint authorization (`SALES_PROCESS`/`REFUND_PROCESS`/`CONFIGURATION_MANAGE`).
- Integration tests for `P3` security/compliance verification covering RBAC regression and queryable sensitive-action audit evidence (`P3SecurityComplianceIntegrationTest`).
- Readiness tests for `P5` documentation/handover artifacts and README linkage (`P5DocumentationHandoverReadinessTest`).

## Project Planning and Status

- Detailed execution plan and card-by-card statuses: `ROADMAP.md`
- Incremental delivery log: `CHANGELOG.md`
- P5 handover documentation set:
  - `docs/handover/P5-user-guide.md`
  - `docs/handover/P5-api-reference-and-integration-notes.md`
  - `docs/handover/P5-architecture-and-maintenance-guide.md`

## Legacy Branch

The original monolithic version is preserved in the `legacy-v1` branch.
