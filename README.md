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

### Shift and Cash Session Lifecycle
- `POST /api/shifts/open`
- `POST /api/shifts/{id}/cash-movements`
- `POST /api/shifts/{id}/close`
- `GET /api/shifts/{id}`
- Business constraints include:
  - one open shift per cashier + terminal,
  - finite-state transition checks,
  - counted-vs-expected reconciliation with variance capture.

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
- Unit tests for pricing resolution with customer-group contexts.

## Project Planning and Status

- Detailed execution plan and card-by-card statuses: `ROADMAP.md`
- Incremental delivery log: `CHANGELOG.md`

## Legacy Branch

The original monolithic version is preserved in the `legacy-v1` branch.
