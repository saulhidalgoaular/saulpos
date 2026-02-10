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
- `K1` Offline policy definition.
- `K2` Idempotent event ingestion.
- `L1` Sales and returns reports.
- `L2` Inventory reports.
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
- `pos-core`: shared cross-cutting abstractions (for example soft-delete contract).
- `pos-api`: transport contracts (request/response DTOs) shared by server/client.
- `pos-server`: Spring Boot backend with domain logic, persistence, security, and REST APIs.
- `pos-client`: JavaFX client module (currently thin and API-consumer oriented).

Backend source of truth:
- All critical business behavior is implemented in `pos-server`.
- `pos-client` is intentionally "dumb" and should only consume APIs.

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
- Supported filters:
  - `from`, `to`, `storeLocationId`, `terminalDeviceId`, `cashierUserId`, `categoryId`, `taxGroupId`
- Report outputs:
  - summary totals for sales and returns,
  - explicit return breakout (`returnGross`) and discount breakout (`discountGross`),
  - grouped breakdowns by day, store, terminal, cashier, category, and tax group.
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
- Supported filters:
  - `storeLocationId`, `categoryId`, `supplierId`
  - movement report also supports `from` and `to`
  - low-stock report requires `minimumQuantity`
- Report outputs:
  - stock-on-hand tabular rows with quantity on hand, cost snapshots, and stock value,
  - low-stock tabular rows with shortage quantity against requested minimum,
  - movement tabular rows including movement type, source reference, and timestamp.

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
- `management.endpoints.web.exposure.include=health,info,metrics`

## Testing Coverage (Implemented Domains)

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
- Concurrency integration test for parallel checkout attempts on the same cart (single success + conflict for competing request).
- Unit tests for cart quantity policy validation by sale mode and precision.
- Unit tests for tender allocation validation and cash-change calculation.
- Unit tests for payment state transition rules.
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

## Project Planning and Status

- Detailed execution plan and card-by-card statuses: `ROADMAP.md`
- Incremental delivery log: `CHANGELOG.md`

## Legacy Branch

The original monolithic version is preserved in the `legacy-v1` branch.
