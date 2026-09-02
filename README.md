# CityPulse 🌎

CityPulse is a portfolio backend project built with [Quarkus](https://quarkus.io) (Java 21). Give it a
city name — full or partial — and it returns current weather, current air quality, and a transparent,
application-specific "CityPulse score" for the best-matching city, in one HTTP call.

> The score is an application heuristic, not medical, health, or official weather/air-quality advice.

```bash
curl "http://localhost:8080/api/v1/pulse?q=berlin"
```

```json
{
  "city": { "name": "Berlin", "country": "Germany", "countryCode": "DE" },
  "score": 90,
  "status": "COMPLETE",
  "weather": { "temperatureCelsius": 21.4, "condition": "CLOUDY" },
  "airQuality": { "pm25": 9.8, "status": "GOOD" },
  "scoreBreakdown": { "weather": 90, "airQuality": 90 },
  "providers": { "weather": "AVAILABLE", "airQuality": "AVAILABLE" }
}
```

## How it works

One Quarkus service, one public endpoint, internally organized into four packages that each own one
responsibility:

```text
Client
  |
  GET /api/v1/pulse?q={text}
  v
com.citypulse.pulse   (the only exposed endpoint: search -> weather -> air quality -> score)
  |
  +--> com.citypulse.city         --> Photon (search "ber" or "Berlin" -> coordinates)
  |
  +--> com.citypulse.weather      --> Open-Meteo Forecast API
  |
  +--> com.citypulse.airquality   --> Open-Meteo Air Quality API
```

`q` doesn't need to be the full city name — [Photon](https://photon.komoot.io) (OpenStreetMap-based,
free, no API key) ranks partial/fuzzy matches, and CityPulse takes the top-ranked result:

```bash
curl "http://localhost:8080/api/v1/pulse?q=ber"       # -> Berlin, same result as ?q=berlin
curl "http://localhost:8080/api/v1/pulse?q=new%20yor"  # -> New York
```

**Why just one endpoint.** Earlier versions of this project exposed `/weather`, `/air-quality`, and
`/cities/search` separately (and, before that, ran as four separate microservices). Both were
deliberately consolidated: nobody calling this API wants to resolve a city, then call weather, then call
air quality, then combine them by hand — they want a score for a city they typed. Internally, the
weather/air-quality/city-search logic still exists as ordinary classes (`WeatherService`,
`AirQualityService`, `CityService`) with the same separation of concerns a multi-endpoint or
multi-service version would have — they're just not separately reachable over HTTP anymore, because
nothing needed them to be.

**Provider isolation.** Every external call (two to Open-Meteo, one to Photon) follows the same shape:
provider JSON → a DTO matching that exact shape → an adapter that translates it (and is the only code
that understands provider-specific details, like Open-Meteo's WMO weather codes or Photon's
`[longitude, latitude]` GeoJSON ordering) → this service's own domain model. If a provider changed its
JSON tomorrow, only its DTO + adapter would change — nothing else in the codebase would notice.

**Partial failure.** A single Open-Meteo outage doesn't fail the whole request — weather and air-quality
are looked up independently, and `status` tells you exactly what happened:

| `status` | Meaning | `score` |
|---|---|---|
| `COMPLETE` | Both weather and air quality available | weighted overall score |
| `PARTIAL` | One of the two unavailable | the one that succeeded |
| `UNAVAILABLE` | Both unavailable | `null` |

Missing data is always `null` — never a fabricated value like `pm25: 0`. Example, air quality down:

```json
{
  "city": { "name": "Berlin", "country": "Germany", "countryCode": "DE" },
  "score": 90,
  "status": "PARTIAL",
  "weather": { "temperatureCelsius": 21.4, "condition": "CLOUDY" },
  "airQuality": null,
  "scoreBreakdown": { "weather": 90, "airQuality": null },
  "providers": { "weather": "AVAILABLE", "airQuality": "UNAVAILABLE" }
}
```

Score formula: `overallScore = weatherScore * 0.60 + airQualityScore * 0.40`. Weather scoring: 18–24°C
scores 100, each degree outside that costs 3 points, then a condition penalty is subtracted (`CLEAR` 0
… `THUNDERSTORM` 35), clamped to `[0, 100]`. Air-quality scoring buckets Open-Meteo's European AQI into
six discrete bands (100/90/75/60/40/20) — deliberately simple, not a health model. Both live in
`ScoreEngine`, a plain Java class with no HTTP/CDI dependency, unit-tested directly.

**Resilience.** Every outbound call (Open-Meteo × 2, Photon) has a 3s connect / 5s read timeout and up
to 2 retries with backoff + jitter — but only for transient failures (network errors, 5xx); a 4xx is
never retried.

## API

### `GET /api/v1/pulse?q={text}`

| Status | Body | When |
|---|---|---|
| 200 | `CityPulseResponse` (above) | A city matched — check `status` for data completeness |
| 400 | `{ "code": "INVALID_QUERY", "message": "..." }` | `q` missing or blank |
| 404 | `{ "code": "CITY_NOT_FOUND", "message": "..." }` | No city matched the query at all |

`condition` (weather): `CLEAR`, `PARTLY_CLOUDY`, `CLOUDY`, `FOG`, `DRIZZLE`, `RAIN`, `SHOWERS`, `SNOW`,
`THUNDERSTORM`, `UNKNOWN`. `status` (air quality): `GOOD`, `FAIR`, `MODERATE`, `POOR`, `VERY_POOR`,
`EXTREMELY_POOR` (Open-Meteo's official European AQI bands).

### Health

```text
GET /q/health/live    liveness  — is the process alive? (never depends on Open-Meteo or Photon)
GET /q/health/ready   readiness — is it ready to accept traffic?
```

### OpenAPI / Swagger UI

```text
GET /q/openapi
GET /q/swagger-ui
```

Exported spec checked into [`openapi/citypulse.yaml`](openapi/citypulse.yaml).

## Running locally

```bash
cd services/city-pulse-service
./mvnw quarkus:dev
```

One command, one process, one port (`:8080`). Live reload on every request; Dev UI at
`http://localhost:8080/q/dev`. No local Maven needed — the service ships its own wrapper (`./mvnw`).

### Docker Compose

```bash
docker compose up --build
```

Multi-stage build (a JDK stage compiles with the Maven Wrapper, then a small non-root UBI9 JRE image
runs it) — no local Maven or pre-built jar needed.

## Testing

```bash
cd services/city-pulse-service
./mvnw verify
```

The three external REST clients (Open-Meteo weather, Open-Meteo air quality, Photon) are mocked — no
test depends on a live network call. Everything else (the full search → weather → air-quality → score
chain) runs for real in tests, since it's plain CDI beans in one process. `ScoreEngine` and each
adapter's mapping logic (weather-code → condition, AQI → status, GeoJSON → coordinates) are also covered
by plain unit tests with no Quarkus context at all.

For an end-to-end check against a real running instance (real Photon/Open-Meteo calls):

```bash
docker compose up --build -d
./tests/api/smoke-test.sh
docker compose down
```

## Technologies

```text
Java 21
Quarkus 3.39.1
REST (RESTEasy Reactive / quarkus-rest-jackson)
MicroProfile REST Client (quarkus-rest-client-jackson) — Open-Meteo + Photon integrations
MicroProfile Fault Tolerance (quarkus-smallrye-fault-tolerance) — retries
MicroProfile Health (quarkus-smallrye-health) — liveness/readiness
MicroProfile OpenAPI (quarkus-smallrye-openapi) — Swagger UI
Docker / Docker Compose (multi-stage build)
JUnit 5, REST Assured, Mockito
GitHub Actions
```

Every `io.quarkus:*` and MicroProfile artifact version is pinned by the Quarkus BOM
(`quarkus.platform.version=3.39.1`) — no per-extension version management needed.

## Deployment

Current target: local Docker Compose (above). Intended (not yet deployed) cloud target: **Azure
Container Apps**, image built and pushed to **Azure Container Registry** by GitHub Actions:

```text
GitHub -> GitHub Actions (build, test, push image) -> Azure Container Registry -> Azure Container Apps
```

Deploying to Azure is out of scope for this iteration — this section documents the intended target, not
a claim of what's live.

## Security

No API key, password, or token is required anywhere — Open-Meteo's and Photon's public endpoints need
none. No `.env` file exists; `.gitignore` excludes one if it ever does. The Docker image's runtime stage
runs as a non-root user (`USER 185`, inherited from the UBI9 OpenJDK runtime image) on a minimal,
maintained base; the build stage (with source, `pom.xml`, `mvnw`) never ships in the final image
(multi-stage build discards it). Every error response is a predictable `{ "code", "message" }` shape —
no stack traces, no internal class names. No authentication layer exists by design: there are no user
accounts, no write operations, and every dependency is a public read-only data source.

## Data providers

- Weather and air-quality data: [Open-Meteo](https://open-meteo.com/), free non-commercial terms, no API
  key. Air-quality data includes CAMS as an underlying source per Open-Meteo's own documentation.
- City search/geocoding: [Photon](https://photon.komoot.io) (Komoot, built on OpenStreetMap data), free
  public instance, no API key.

CityPulse does not own, endorse, or guarantee the accuracy of this data.

## License

[MIT](LICENSE)
