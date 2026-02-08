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

## 🛣️ Roadmap
See [ROADMAP.md](ROADMAP.md) for the detailed implementation plan and checking project status.

## 📜 Legacy Code
The original monolithic code has been moved to the `legacy-v1` branch.
