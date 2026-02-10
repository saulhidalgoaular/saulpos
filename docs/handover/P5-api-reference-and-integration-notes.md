# P5 API Reference and Integration Notes

Card ID: P5  
Title: Documentation and Handover  
Audience: Integrators and Client Engineers

## 1. API Foundation

- Base path: `/api`
- Auth pattern: bearer token from `POST /api/auth/login`
- Error model: RFC7807-compatible payloads with stable `code` values (`POS-xxxx`)
- Correlation: propagate `X-Correlation-ID` for traceability
- Idempotency: provide `Idempotency-Key` where required (checkout and payment transitions)

## 2. Core Endpoint Groups

### Authentication and Security
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/security/me`
- `GET /api/security/permissions/current`

### Sales and Checkout
- `POST /api/sales/carts`
- `POST /api/sales/carts/{id}/lines`
- `POST /api/sales/carts/{id}/recalculate`
- `POST /api/sales/checkout`
- `GET /api/refunds/lookup`
- `POST /api/refunds/submit`

### Inventory and Suppliers
- `GET /api/inventory/balances`
- `POST /api/inventory/adjustments`
- `POST /api/inventory/stocktakes`
- `POST /api/inventory/transfers`
- `POST /api/inventory/purchase-orders`
- `POST /api/inventory/supplier-returns`

### Reporting and Operations
- `GET /api/reports/sales`
- `GET /api/reports/inventory/movements`
- `GET /api/reports/cash/shifts`
- `GET /api/reports/exceptions`
- `POST /api/receipts/print`
- `POST /api/receipts/reprint`

## 3. Integration Notes

1. Prefer server-authoritative recalculation over client-side total calculation.
2. Use explicit reason codes for sensitive actions (discounts, voids, overrides, returns).
3. Treat `409` conflicts as expected concurrency/idempotency outcomes and implement retry-safe UX.
4. For fiscal deployments, honor `app.fiscal.*` policy flags and required customer fields.

## 4. DTO and Contract Location

- Shared transport DTOs live in `pos-api/src/main/java/com/saulpos/api/**`.
- Server endpoint behavior lives in `pos-server/src/main/java/com/saulpos/server/**/web`.
- Client API bindings live in `pos-client/src/main/java/com/saulpos/client/api`.

## 5. Verification Commands

```bash
mvn -pl pos-server test -Dtest=P1EndToEndUatIntegrationTest,P2PerformanceReliabilityIntegrationTest,P3SecurityComplianceIntegrationTest,P4OperationsReadinessTest
```

```bash
mvn clean verify
```
