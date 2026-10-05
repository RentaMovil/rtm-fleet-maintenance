# rtm-fleet-maintenance

RentaMovil's **Fleet & Maintenance** service: source of truth for the vehicle catalog, branches and
the maintenance lifecycle. Service docs: `rtm-docs/09-microservices/services/03-fleet-maintenance/`.
API contract: `rtm-docs/07-api/contracts/openapi/fleet-maintenance.yaml`.

| | |
|---|---|
| Stack | Java 21 · Spring Boot 4.1 · Maven · hexagonal architecture |
| Local port | 3002 |
| Database | SQL Server `rentamovil`, schema `fleet`, user `fleet_app` |
| Migrations | **not here** — they live in `rtm-fleet-maintenance-db` (Liquibase) |
| Depends on | `iam` (public key at `/jwks` to verify tokens) |
| Calls | `booking-reservation` (`GET /internal/reservations/vehicle-occupancy`) |

## Layout (hexagonal)

```
src/main/java/com/rentamovil/fleetmaintenance/
├── domain/                   Business rules. No Spring, no JPA
│   ├── entity/                 Vehicle, Branch, VehicleModel, VehicleMaintenance, ...
│   ├── enums/                  VehicleStatus, MaintenanceStatus
│   ├── exception/              BusinessRuleException, InvalidDataException, ...
│   └── port/
│       ├── in/                 use cases (RegisterVehicleUseCase, ...)
│       └── out/                what the domain needs (VehicleRepositoryPort, ReservationOccupancyPort, ...)
├── application/usecase/      Use case implementations (*UseCaseImpl)
└── infrastructure/           Adapters
    ├── config/                 BeanConfig wires use cases to adapters
    ├── persistence/            JPA entities (fleet schema), Spring Data repositories, adapters
    ├── security/               JWT verification with iam's JWKS
    ├── client/                 HTTP client to booking-reservation
    ├── event/                  domain event publisher
    └── web/                    controllers, DTOs, @CurrentUser, error handling, request log
```

Same conventions as `rtm-iam`; see `rtm-docs/05-architecture/hexagonal-architecture.md`.

## The service never touches the schema

`spring.jpa.hibernate.ddl-auto=validate`: on startup Hibernate only **checks** that the entities
match the tables created by `rtm-fleet-maintenance-db`, and fails if they don't. A schema change is
a changeset in `rtm-fleet-maintenance-db` first.

## Requirements

- Java 21+
- Maven 3.9+
- SQL Server running, migrated by `rtm-iam-db` and then `rtm-fleet-maintenance-db`
- `rtm-iam` running (only its `/jwks` is used; keys are cached after the first request)

## Local usage

```bash
# 1. Configuration (once)
cp .env.example .env
# DATABASE_PASSWORD = parameter.fleetAppPassword in rtm-fleet-maintenance-db/liquibase.properties

# 2. Tests
mvn test

# 3. Run
mvn spring-boot:run

# 4. Check
curl http://localhost:3002/health
# {"status":"ok","db":"connected"}
```

`/health` answers `503` with `"db":"disconnected"` when the database stops responding.

### With Docker

First `rtm-iam-db` (`docker compose up -d`: `rentamovil` network, SQL Server, iam migrations) and
`rtm-fleet-maintenance-db` (`mvn liquibase:update`). Then, in this repo:

```bash
docker compose up -d --build
```

- Container `rentamovil-fleet-maintenance`, port 3002, alias `fleet-maintenance` on the network
  (the gateway calls `http://fleet-maintenance:3002`).
- Same `.env`; inside Docker the compose file points `DATABASE_URL`, `IAM_URL` and `BOOKING_URL`
  at container names instead of `localhost`.
- Don't run `mvn spring-boot:run` and the container at the same time: both use port 3002.

## Endpoints

| Method | Path | Auth | Story | Responses |
|---|---|---|---|---|
| GET | `/health` | — | — | 200 / 503 |
| GET | `/brands`, `/categories`, `/engine-types`, `/vehicle-models`, `/maintenance-types` | — | — | 200 |
| GET | `/branches` | — | HU-FLEET-007 | 200 (with the 7 days of operating hours and optional `latitude`/`longitude` for the map) |
| POST | `/branches` | Bearer + `branches:manage` | HU-FLEET-007 | 201 · 400 `VALIDATION_ERROR` · 403 |
| PUT | `/branches/{id}` | Bearer + `branches:manage` | HU-FLEET-007 | 200 · 400 · 403 · 404 |
| DELETE | `/branches/{id}` | Bearer + `branches:manage` | HU-FLEET-007 | 204 · 403 · 404 · 409 `BRANCH_HAS_VEHICLES` / `RESOURCE_IN_USE` |
| GET | `/vehicles?dateFrom&dateTo&branchId&categoryId&brandId&engineTypeId&minPrice&maxPrice&page&limit` | — | HU-FLEET-001, 003 | 200 (paged, AVAILABLE only) · 400 · 503 `BOOKING_UNAVAILABLE` |
| GET | `/vehicles/{id}` | — | HU-FLEET-001, 002 | 200 · 404 |
| GET | `/vehicles/{id}/availability?from&to` | — | HU-FLEET-002 | 200 `{"available": ...}` · 400 · 404 · 503 |
| POST | `/vehicles` | Bearer + `vehicles:create` | HU-FLEET-004 | 201 (always `AVAILABLE`) · 400 · 403 · 409 `PLATE_ALREADY_EXISTS` |
| PUT | `/vehicles/{id}` | Bearer + `vehicles:update` | HU-FLEET-005 | 200 · 400 · 403 · 404 · 409 `PLATE_ALREADY_EXISTS` / `MILEAGE_CANNOT_DECREASE` / `VEHICLE_RETIRED` |
| PATCH | `/vehicles/{id}/status` | Bearer + `maintenance:create` (MAINTENANCE) / `vehicles:update` (RETIRED) | HU-FLEET-005, 006 | 200 · 400 · 403 · 404 · 409 `VEHICLE_RENTED` / `VEHICLE_RETIRED` / `INVALID_STATUS_TRANSITION` / `VEHICLE_HAS_FUTURE_RESERVATIONS` · 503 |
| POST | `/vehicles/{id}/maintenance/complete` | Bearer + `maintenance:update` | HU-FLEET-006 | 200 · 403 · 404 · 409 `NO_MAINTENANCE_IN_PROGRESS` |
| GET | `/vehicles/inventory?status=&branchId=` | Bearer + `vehicles:update` | Admin inventory | 200 (every status, not paged) · 403 |
| GET | `/vehicles/stats` | Bearer + `vehicles:update` | HU-FLEET-006 | 200 (count per status) · 403 |
| GET | `/maintenances?vehicleId=&status=` | Bearer + `maintenance:update` | Admin history | 200 (newest first, with plate, vehicle name and type) · 403 |
| POST | `/internal/events` | — (internal network only) | — | 204 · 400 · 404 · 409 `INVALID_STATUS_TRANSITION` |

Errors follow `ErrorResponse` from `_shared.yaml`: `{"error": "CODE", "message": "...", "details": [...]}`.

Endpoints with Bearer receive the caller through `@CurrentUser AuthenticatedUser`: the token is
**verified again here** with iam's public key (RS256, `/jwks`), never trusted from the gateway's
`X-User-*` headers, and each use case checks the permission it needs (`07-api/authentication.md`).

Branches always carry the 7 weekdays (`1` = Monday … `7` = Sunday). A closed day is
`{"dayOfWeek": 7, "closed": true}` and has no row in `fleet.branch_operating_hour`.

**Availability** is never stored here: reservations belong to `booking-reservation`, which is
asked synchronously for every vehicle (`/internal/reservations/vehicle-occupancy`, no JWT, internal
network). If it's down, date-filtered searches, availability checks and retiring answer `503`
instead of guessing (`decisions.md`).

**Status rules:** `AVAILABLE` is never a manual target. A vehicle is created `AVAILABLE`, goes back
to it when a maintenance is completed or a rental ends, can't go to maintenance or be retired while
`RENTED`, and `RETIRED` is terminal (rows are never deleted).

**Events published:** `VehicleSentToMaintenance`, `MaintenanceCompleted`. For now
`LogEventPublisherAdapter` writes them to the service log (look for `[EVENT]`) with the
`02-domain/domain-events.md` envelope; only that adapter changes once `booking-reservation` exists.

**Events consumed:** `RentalStarted` (→ `RENTED`) and `RentalCompleted` (→ `AVAILABLE`) through
`POST /internal/events`. Idempotent: the same event twice leaves the same status. The gateway
doesn't route `/internal/**`, so only services on the Docker network can call it.

Every change is recorded in `shared.audit` (`BRANCH_CREATED`, `VEHICLE_REGISTERED`,
`VEHICLE_SENT_TO_MAINTENANCE`, ...).

## Branches

`feat/`, `fix/`, `chore/` → PR to `develop` (Squash and Merge). See
`rtm-docs/00-governance/git-conventions.md`.
