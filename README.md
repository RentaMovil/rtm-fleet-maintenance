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

Errors follow `ErrorResponse` from `_shared.yaml`: `{"error": "CODE", "message": "...", "details": [...]}`.

Endpoints with Bearer receive the caller through `@CurrentUser AuthenticatedUser`: the token is
**verified again here** with iam's public key (RS256, `/jwks`), never trusted from the gateway's
`X-User-*` headers, and each use case checks the permission it needs (`07-api/authentication.md`).

## Branches

`feat/`, `fix/`, `chore/` → PR to `develop` (Squash and Merge). See
`rtm-docs/00-governance/git-conventions.md`.
