#!/usr/bin/env bash
# End-to-end smoke test against a *running* CityPulse service.
# This is separate from `./mvnw test`: that suite mocks the Open-Meteo and Photon REST
# clients; this script makes real HTTP calls to a real running process, which itself
# calls the real, public Photon and Open-Meteo APIs.
#
# Usage:
#   docker compose up --build -d   # or: cd services/city-pulse-service && ./mvnw quarkus:dev
#   ./tests/api/smoke-test.sh
#   docker compose down

set -uo pipefail

CITY_PULSE_URL="${CITY_PULSE_URL:-http://localhost:8080}"

failures=0

check() {
  local description="$1" expected_status="$2" url="$3"
  local actual_status
  actual_status=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$url")
  if [ "$actual_status" = "$expected_status" ]; then
    echo "PASS  $description (got $actual_status)"
  else
    echo "FAIL  $description (expected $expected_status, got $actual_status) -- $url"
    failures=$((failures + 1))
  fi
}

check_body_contains() {
  local description="$1" expected_substring="$2" url="$3"
  local body
  body=$(curl -s --max-time 5 "$url")
  if [[ "$body" == *"$expected_substring"* ]]; then
    echo "PASS  $description"
  else
    echo "FAIL  $description -- expected body to contain '$expected_substring', got: $body"
    failures=$((failures + 1))
  fi
}

echo "== health =="
check "service is UP" 200 "$CITY_PULSE_URL/q/health/live"

echo "== the one endpoint =="
check "full city name resolves"    200 "$CITY_PULSE_URL/api/v1/pulse?q=berlin"
check_body_contains "resolves to Berlin" '"Berlin"' "$CITY_PULSE_URL/api/v1/pulse?q=berlin"
check "partial city name resolves the same way" 200 "$CITY_PULSE_URL/api/v1/pulse?q=ber"
check_body_contains "partial query also resolves to Berlin" '"Berlin"' "$CITY_PULSE_URL/api/v1/pulse?q=ber"
check "no match is 404"            404 "$CITY_PULSE_URL/api/v1/pulse?q=asdkjhasdkjhasd"
check "missing query is 400"       400 "$CITY_PULSE_URL/api/v1/pulse"

echo
if [ "$failures" -eq 0 ]; then
  echo "All checks passed."
  exit 0
else
  echo "$failures check(s) failed."
  exit 1
fi
