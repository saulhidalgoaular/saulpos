#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 6 ]]; then
  echo "Usage: $0 <host> <port> <database> <username> <backup-file> <checksum-file>" >&2
  exit 1
fi

host="$1"
port="$2"
database="$3"
username="$4"
backup_file="$5"
checksum_file="$6"

sha256sum --check "$checksum_file"

PGHOST="$host" PGPORT="$port" PGUSER="$username" \
  pg_restore --clean --if-exists --no-owner --no-privileges --dbname="$database" "$backup_file"

echo "Restore completed for database ${database}"
