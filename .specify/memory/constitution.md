# RevaStudio Constitution

## Core Principles

### I. Purpose-First
Every repository artifact must clearly support the RevaStudio product mission: provide a secure, testable, and maintainable media manager API and Angular client that implement the Customer and Employee MVPs described in the README.

### II. Production Parity
Environments should mirror production where practical. Database schema and migrations must be driven by Flyway migrations in `server/src/main/resources/db/migration` and verified against PostgreSQL (RDS in production).

### III. Test-First Quality
Tests are required for new features and bug fixes. Unit tests for logic, integration tests for service boundaries, and migration tests for DB changes. Client tests use Vitest/Jasmine; server tests use JUnit.

### IV. Security by Default
Secrets never commit to source. Runtime secrets (JWT keys, DB credentials) must be provided via environment variables or an external secrets manager. Authentication uses JWT and RBAC (`CUSTOMER`, `EMPLOYEE`).

### V. Observability & Simplicity
Log at appropriate levels, prefer structured logs, and keep interfaces minimal and well-documented. Use semantic versioning for public API changes and maintain a changelog.

## Technology & Constraints
- Canonical stack: Angular (client) + Spring Boot (server) + PostgreSQL (Flyway migrations). The README's SQLite guidance is permitted for quick local experiments only; the canonical DB for CI and production is PostgreSQL (RDS).
- API base path: `/api` as implemented in `server/src/main/resources/application.properties`.
- Authentication: JWT; see `JwtUtil` and `JwtAuthenticationFilter` in `server/src/main/java/com/revature/revastudio`.
- Migrations: Managed with Flyway; all migrations live in `server/src/main/resources/db/migration` and must be reviewed with schema changes.

## Development Workflow & Quality Gates
- Branching: feature branches named `feature/<short-desc>`; bugfix branches `fix/<short-desc>`.
- Pull requests: require at least 1 reviewer. PRs must include a description, linked issue (if applicable), and test coverage for changed behavior.
- CI gating: All PRs must pass server build and tests (`./gradlew build` in `server`) and client tests (`npm test` or `npm run test` in `client`) before merging.
- Formatting & linting: Prettier enforced for client; Java code should follow a repository standard (Spotless/Checkstyle recommended) — add enforcement in CI.
- Secrets & config: Use environment variables (e.g., `JWT_SECRET`, `DATABASE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`). Local `application.properties` must not contain production secrets.

## API Contracts & Versioning
- Public API changes that break compatibility must follow semantic versioning and be communicated in the changelog. Maintain contract tests for critical endpoints when possible.

## Responsibilities & Ownership
- Module owners:
	- `client/`: frontend owner(s)
	- `server/`: backend owner(s)
	- `db/migrations`: migration owner(s)
Owners are responsible for reviews in their area, merging, and release coordination.

## Governance
- This Constitution is the authoritative project policy. Amendments must be proposed in a PR that describes the change and migration steps.
- Ratification: project leads approve amendments. A proposal is ratified when a project lead merges the amendment PR.
- Emergency changes (security fixes, critical patches) may be merged by a project lead and must be documented and retroactively reviewed.

**Version**: 1.0.0 | **Ratified**: 2026-03-05 | **Last Amended**: 2026-03-05
