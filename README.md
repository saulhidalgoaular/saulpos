# SaulPOS v2 (Local-First Architecture)

This is the rewritten version of SaulPOS, designed for production environments with a **Local-First API** approach.

## 🏗️ Architecture
The project is split into Maven modules:
-   **`pos-core`**: Shared logic, base entities, utilities.
-   **`pos-api`**: Shared DTOs and API interfaces.
-   **`pos-server`**: Spring Boot application. Handles database, auth, printing, and business logic.
-   **`pos-client`**: JavaFX application. A "dumb" client that consumes the API.

## 🚀 Getting Started

### Prerequisites
-   Java 21
-   Maven 3.8+
-   PostgreSQL or MariaDB (optional, defaults to H2 for quick start)

### Build
```bash
mvn clean install
```

### Run Server
```bash
cd pos-server
mvn spring-boot:run
```

### Run Client
```bash
cd pos-client
mvn javafx:run
```

## 🗄️ Database Strategy
-   **Soft Delete**: Enabled by default (audit trail). Configurable to HARD delete via `app.deletion-strategy=hard`.
-   **Migrations**: Managed by Flyway (`pos-server/src/main/resources/db/migration`).

## 🔐 Security Foundation (A3)
-   Auth endpoints: `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`.
-   Protected endpoint example: `GET /api/security/me`.
-   Login security includes BCrypt password hashing and configurable brute-force lockout.
-   Login success/failure and logout are persisted in `auth_audit_event`.

## 🛡️ Permission Matrix (B2)
-   Permission introspection: `GET /api/security/permissions/current`.
-   Permission catalog: `GET /api/security/permissions/catalog`.
-   Role management: `GET /api/security/roles`, `POST /api/security/roles`, `PUT /api/security/roles/{id}/permissions`.
-   Sensitive domains are deny-by-default and require explicit permissions:
    - `SALES_PROCESS` for `/api/sales/**`
    - `REFUND_PROCESS` for `/api/refunds/**`
    - `INVENTORY_ADJUST` for `/api/inventory/**`
    - `REPORT_VIEW` for `/api/reports/**`
    - `CONFIGURATION_MANAGE` for `/api/identity/**` and role/configuration APIs

## 💵 Shift and Cash Session Lifecycle (B3)
-   Open shift: `POST /api/shifts/open`
-   Register paid-in/paid-out cash movements: `POST /api/shifts/{id}/cash-movements`
-   Close shift with counted cash and variance capture: `POST /api/shifts/{id}/close`
-   Retrieve shift reconciliation totals: `GET /api/shifts/{id}`
-   Shift endpoints are permission-protected under `SALES_PROCESS`.

## 🛣️ Roadmap
See [ROADMAP.md](ROADMAP.md) for the detailed implementation plan and checking project status.

## 📜 Legacy Code
The original monolithic code has been moved to the `legacy-v1` branch.
