# Changelog

## Unreleased
- Implemented Card A3 Security Foundation in `pos-server`.
- Added auth schema/migration (`V2__security_foundation.sql`) with `user_account`, role/permission mapping, session, and auth audit tables.
- Added stateless auth endpoints: `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`.
- Added password hashing, failed-login lockout policy, and token/session validation.
- Added protected context endpoint `GET /api/security/me` and auth lifecycle integration tests.
