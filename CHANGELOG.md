# Changelog

## Unreleased
- Implemented Card A3 Security Foundation in `pos-server`.
- Added auth schema/migration (`V2__security_foundation.sql`) with `user_account`, role/permission mapping, session, and auth audit tables.
- Added stateless auth endpoints: `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`.
- Added password hashing, failed-login lockout policy, and token/session validation.
- Added protected context endpoint `GET /api/security/me` and auth lifecycle integration tests.
- Implemented Card B2 Permission Matrix and Enforcement in `pos-server` and `pos-api`.
- Added migration `V4__permission_matrix_and_enforcement.sql` with baseline permission catalog and role-permission mappings.
- Added role and permission APIs: `GET /api/security/permissions/current`, `GET /api/security/permissions/catalog`, `GET/POST /api/security/roles`, and `PUT /api/security/roles/{id}/permissions`.
- Added explicit permission enforcement for sales, refunds, inventory adjustments, reports, and configuration operations with stable `POS-4030` denial contract.
- Added permission matrix integration tests and permission evaluator unit tests.
