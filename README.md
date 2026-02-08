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
  - `GET /api/catalog/prices/resolve?storeLocationId={id}&productId={id}&at={isoDateTime}`
- Resolution precedence:
  - store override,
  - active price book item (effective window),
  - product base price fallback.
- Deterministic and auditable resolution is returned with source metadata (`STORE_OVERRIDE`, `PRICE_BOOK`, `BASE_PRICE`).

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
- Concurrency coverage for open-shift race conditions.
- Repository and validator tests for catalog and category constraints.
- Error-handling integration tests for stable error contracts.
- Integration tests for `POST /api/tax/preview` covering deterministic line/total breakdown and missing-rule validation.

## Project Planning and Status

- Detailed execution plan and card-by-card statuses: `ROADMAP.md`
- Incremental delivery log: `CHANGELOG.md`

## Legacy Branch

The original monolithic version is preserved in the `legacy-v1` branch.
