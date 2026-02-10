#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 5 ]]; then
  echo "Usage: $0 <host> <port> <database> <username> <backup-dir>" >&2
  exit 1
fi

host="$1"
port="$2"
database="$3"
username="$4"
backup_dir="$5"

mkdir -p "$backup_dir"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
backup_file="${backup_dir}/saulpos-${database}-${timestamp}.dump"

PGHOST="$host" PGPORT="$port" PGUSER="$username" \
  pg_dump --format=custom --file="$backup_file" "$database"

checksum_file="${backup_file}.sha256"
sha256sum "$backup_file" > "$checksum_file"

echo "Backup created: $backup_file"
echo "Checksum file: $checksum_file"
