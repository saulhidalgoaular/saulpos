#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BOOTSTRAP_SQL_FILE="${ROOT_DIR}/ops/scripts/bootstrap-local.sql"
MIGRATIONS_DIR="${ROOT_DIR}/pos-server/src/main/resources/db/migration"

DB_HOST="${SAULPOS_DB_HOST:-saulpos-postgres}"
DB_PORT="${SAULPOS_DB_PORT:-5432}"
DB_NAME="${SAULPOS_DB_NAME:-saulpos_dev}"
DB_USER="${SAULPOS_DB_USERNAME:-saulpos_dev}"
DB_PASSWORD="${SAULPOS_DB_PASSWORD:-saulpos_dev}"
RESET_DB="${SAULPOS_RESET_DB:-true}"
RUN_BOOTSTRAP="${SAULPOS_RUN_BOOTSTRAP:-true}"
SERVER_WAIT_SECONDS="${SAULPOS_SERVER_WAIT_SECONDS:-120}"

SERVER_PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"
SERVER_LOG_DIR="${ROOT_DIR}/tmp"
SERVER_LOG_FILE="${SERVER_LOG_DIR}/saulpos-server.log"

if ! command -v mvn >/dev/null 2>&1; then
  echo "Missing requirement: mvn"
  exit 1
fi

if ! command -v psql >/dev/null 2>&1; then
  echo "Missing requirement: psql"
  exit 1
fi

if [[ "${RUN_BOOTSTRAP}" == "true" ]] && [[ ! -f "${BOOTSTRAP_SQL_FILE}" ]]; then
  echo "Missing bootstrap SQL: ${BOOTSTRAP_SQL_FILE}"
  exit 1
fi

LATEST_FLYWAY_VERSION=""
for migration_file in "${MIGRATIONS_DIR}"/V*__*.sql; do
  [[ -e "${migration_file}" ]] || continue
  migration_name="$(basename "${migration_file}")"
  migration_version="${migration_name%%__*}"
  migration_version="${migration_version#V}"
  if [[ "${migration_version}" =~ ^[0-9]+$ ]]; then
    if [[ -z "${LATEST_FLYWAY_VERSION}" ]] || (( migration_version > LATEST_FLYWAY_VERSION )); then
      LATEST_FLYWAY_VERSION="${migration_version}"
    fi
  fi
done

if [[ -z "${LATEST_FLYWAY_VERSION}" ]]; then
  echo "Unable to determine latest Flyway migration from ${MIGRATIONS_DIR}"
  exit 1
fi

if [[ "${RESET_DB}" == "true" ]]; then
  echo "Resetting PostgreSQL schema on ${DB_HOST}:${DB_PORT}/${DB_NAME}..."
  PGPASSWORD="${DB_PASSWORD}" psql \
    -h "${DB_HOST}" \
    -p "${DB_PORT}" \
    -U "${DB_USER}" \
    -d "${DB_NAME}" \
    -v ON_ERROR_STOP=1 <<'SQL'
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO CURRENT_USER;
SQL
fi

mkdir -p "${SERVER_LOG_DIR}"

echo "Starting SaulPOS server..."
(
  cd "${ROOT_DIR}/pos-server"
  SPRING_PROFILES_ACTIVE="${SERVER_PROFILE}" \
  SPRING_JPA_HIBERNATE_DDL_AUTO=none \
  SAULPOS_DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
  SAULPOS_DB_USERNAME="${DB_USER}" \
  SAULPOS_DB_PASSWORD="${DB_PASSWORD}" \
  mvn -Pit-postgres spring-boot:run
) >"${SERVER_LOG_FILE}" 2>&1 &
SERVER_PID=$!

cleanup() {
  if ps -p "${SERVER_PID}" >/dev/null 2>&1; then
    kill "${SERVER_PID}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT INT TERM

echo "Server PID: ${SERVER_PID}"
echo "Server log: ${SERVER_LOG_FILE}"

echo "Waiting for Flyway migrations/security seed data (target version: ${LATEST_FLYWAY_VERSION})..."
BOOTSTRAP_READY="false"
for ((i = 1; i <= SERVER_WAIT_SECONDS; i++)); do
  if ! ps -p "${SERVER_PID}" >/dev/null 2>&1; then
    echo "Server stopped before initialization completed."
    echo "Check log: ${SERVER_LOG_FILE}"
    exit 1
  fi

  READY_CHECK="$(PGPASSWORD="${DB_PASSWORD}" psql \
    -h "${DB_HOST}" \
    -p "${DB_PORT}" \
    -U "${DB_USER}" \
    -d "${DB_NAME}" \
    -tA \
    -v ON_ERROR_STOP=1 \
    -c "SELECT CASE WHEN EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '${LATEST_FLYWAY_VERSION}' AND success = TRUE) AND EXISTS (SELECT 1 FROM app_role WHERE code = 'MANAGER') THEN 'true' ELSE 'false' END;" 2>/dev/null || true)"

  if [[ "${READY_CHECK}" == "true" ]]; then
    BOOTSTRAP_READY="true"
    break
  fi

  sleep 1
done

if [[ "${BOOTSTRAP_READY}" != "true" ]]; then
  echo "Timed out waiting for Flyway version ${LATEST_FLYWAY_VERSION} and security seed data."
  echo "Check log: ${SERVER_LOG_FILE}"
  exit 1
fi

if [[ "${RUN_BOOTSTRAP}" == "true" ]]; then
  echo "Running bootstrap SQL..."
  PGPASSWORD="${DB_PASSWORD}" psql \
    -h "${DB_HOST}" \
    -p "${DB_PORT}" \
    -U "${DB_USER}" \
    -d "${DB_NAME}" \
    -v ON_ERROR_STOP=1 \
    -f "${BOOTSTRAP_SQL_FILE}"
fi

echo "Waiting for server HTTP startup..."
SERVER_HTTP_READY="false"
for ((i = 1; i <= SERVER_WAIT_SECONDS; i++)); do
  if ! ps -p "${SERVER_PID}" >/dev/null 2>&1; then
    echo "Server stopped before HTTP startup completed."
    echo "Check log: ${SERVER_LOG_FILE}"
    exit 1
  fi

  if grep -q "Tomcat started on port" "${SERVER_LOG_FILE}" \
    && grep -q "Started PosServerApplication" "${SERVER_LOG_FILE}"; then
    SERVER_HTTP_READY="true"
    break
  fi

  sleep 1
done

if [[ "${SERVER_HTTP_READY}" != "true" ]]; then
  echo "Timed out waiting for server HTTP startup."
  echo "Check log: ${SERVER_LOG_FILE}"
  exit 1
fi

echo "Starting SaulPOS client..."
cd "${ROOT_DIR}/pos-client"
mvn javafx:run
