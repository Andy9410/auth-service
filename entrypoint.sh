#!/bin/sh
set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-auth_db}"
DB_USER="${DB_USER:-postgres}"

echo "▶ Esperando PostgreSQL en ${DB_HOST}:${DB_PORT}..."
until pg_isready -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" >/dev/null 2>&1; do
  sleep 2
done

echo "▶ PostgreSQL listo. Iniciando auth-service..."
exec java -Xmx200m -jar /app/app.jar
