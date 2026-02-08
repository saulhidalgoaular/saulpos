# SaulPOS v2: Project Status & Roadmap

## 1. Project Overview
SaulPOS v2 is a **Local-First API** Point of Sale system.
-   **Current Status**: Phase 1 (Foundation) Incomplete.
-   **Architecture**: Monorepo with Maven Modules (`pos-core`, `pos-api`, `pos-server`, `pos-client`).
-   **Legacy Code**: Moved to `saulpos/legacy-v1` branch.
-   **v2 Code**: Located in `saulpos/master` branch.

## 2. Constraints & Principles (REMINDERS)
All future development MUST adhere to these rules:
1.  **Headless First**: The `pos-server` must run without any UI. All logic lives here.
2.  **API Driven**: The `pos-client` (JavaFX) must be a "dumb" terminal communicating ONLY via REST API.
3.  **Testing**:
    -   **Unit Tests**: REQUIRED for every Service and Utility method.
    -   **Integration Tests**: REQUIRED for all API endpoints (`MockMvc` or `TestContainers`).
4.  **Modularity**: Keep domains isolated (e.g., Sales shouldn't know about Printer details directly).
5.  **Database**:
    -   Primary: PostgreSQL.
    -   Alternative: MariaDB / H2.
    -   **Deletion**: Respect `DeletionStrategy` (Soft vs Hard).

## 3. Implementation Checklist (Todo List)

### ✅ Phase 1: Foundation (Completed)
-   [x] Initialize Maven Multi-module project.
-   [x] Configure Spring Boot 3 + Java 21.
-   [x] Setup Database Drivers (Postgres/MariaDB/H2).
-   [x] Implement `DeletionStrategy` (Soft/Hard interface).

### 🚧 Phase 2: Core Domain & Auth (Next Steps)
-   [ ] **Security Setup**:
    -   [ ] Implement Spring Security with JWT/Session.
    -   [ ] Create `User` and `Role` entities in `pos-server`.
    -   [ ] **Test**: Verify protected endpoints.
-   [ ] **Entity Migration** (Careful Review Required):
    -   *Note*: Do not blindly copy from legacy. Review fields for modern POS requirements.
    -   [ ] Migrate `Product` entity (add barcodes, tax group, price).
    -   [ ] Migrate `Category` / `Department`.
    -   [ ] **Test**: Repository tests for CRU (Create, Read, Update).
-   [ ] **API Generation**:
    -   [ ] Define DTOs in `pos-api`.
    -   [ ] Use MapStruct for Entity <-> DTO mapping.
    -   [ ] Create `ProductController` and `UserController`.

### 📅 Phase 3: Business Logic (Sales & Inventory)
-   [ ] **Cart Logic**:
    -   [ ] Implement `CartService` (server-side calculation).
    -   [ ] **Test**: Unit tests for tax/discount calculations.
-   [ ] **Inventory**:
    -   [ ] Implement Stock deduction.
    -   [ ] **Test**: Concurrency tests (two terminals selling same item).
-   [ ] **Sales Transaction**:
    -   [ ] Create `Invoice` and `InvoiceDetail` entities.
    -   [ ] Implement "Checkout" transaction (atomic).

### 📅 Phase 4: Client & Hardware
-   [ ] **JavaFX Client**:
    -   [ ] Create `RestClient` service (using `RestClient` or `WebClient`).
    -   [ ] specialized Login Screen (calls API).
    -   [ ] Main POS Screen.
-   [ ] **Printing**:
    -   [ ] Create `PrinterService` interface in `pos-server`.
    -   [ ] Implement ESC/POS adapter.

## 4. How to Continue (New Context)
1.  **Clone**: `git clone https://github.com/saulhidalgoaular/saulpos.git`
2.  **Checkout**: `git checkout master`
3.  **Read**: Review `pos-server/src/main/resources/application.properties` for DB config.
4.  **Pick a Task**: Select an unchecked item from **Phase 2** above.
5.  **Develop**:
    -   Write Test -> Write Code -> Verify.
    -   Ensure `mvn clean install` passes.
