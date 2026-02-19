#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

DB_HOST="${SAULPOS_DB_HOST:-saulpos-postgres}"
DB_PORT="${SAULPOS_DB_PORT:-5432}"
DB_NAME="${SAULPOS_DB_NAME:-saulpos_dev}"
DB_USER="${SAULPOS_DB_USERNAME:-saulpos_dev}"
DB_PASSWORD="${SAULPOS_DB_PASSWORD:-saulpos_dev}"
RESET_DB="${SAULPOS_RESET_DB:-true}"

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
echo "Starting SaulPOS client..."
cd "${ROOT_DIR}/pos-client"
mvn javafx:run
