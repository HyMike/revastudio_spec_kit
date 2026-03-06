# RevaStudio Constitution

## Core Principles

### I. Purpose-First
Every repository artifact must clearly support the RevaStudio product mission: provide a secure, testable, and maintainable media manager API and Angular client that implement the Customer and Employee MVPs described in the README.

### II. Production Parity
Environments should mirror production where practical. Database schema and migrations must be driven by Flyway migrations in `server/src/main/resources/db/migration` and verified against PostgreSQL (RDS in production).

### III. Test-First Quality
Tests are required for new features and bug fixes. Unit tests for logic, integration tests for service boundaries, and migration tests for DB changes. Client tests use Vitest; server tests use JUnit 5.

### IV. Security by Default
Secrets never commit to source. Runtime secrets (JWT keys, DB credentials) must be provided via environment variables or an external secrets manager. Authentication uses JWT and RBAC (`CUSTOMER`, `EMPLOYEE`).

### V. Observability & Simplicity
Log at appropriate levels, prefer structured logs, and keep interfaces minimal and well-documented. Use semantic versioning for public API changes and maintain a changelog.

## Technology & Constraints

### Frontend
- **Framework**: Angular (latest stable) — component-based SPA architecture.
- **Language**: TypeScript.
- **UI library**: Angular Material.
- **HTTP**: Angular `HttpClient` with a JWT interceptor (`client/src/app/interceptors/jwt-interceptor.ts`).
- **Routing & guards**: Angular Router with role-based route guards (`auth-guard`, `employee-guard`, `guest-guard` under `client/src/app/guards/`).
- **Testing**: Vitest (`vitest ^4.0.8`) with `jsdom` as the browser environment — spec files co-located with source as `*.spec.ts`. Jest is **not used**; Vitest is preferred because it is native ESM, significantly faster, and integrates cleanly with the Angular 21 + `@angular/build` toolchain without additional transform configuration.
- **Package manager**: npm; dependency manifest at `client/package.json`.

### Backend
- **Framework**: Spring Boot (Java) — RESTful API server.
- **Language**: Java.
- **Build tool**: Gradle (`server/build.gradle`, root `settings.gradle`).
- **Authentication**: JWT; see `JwtUtil` and `JwtAuthenticationFilter` in `server/src/main/java/com/revature/revastudio`.
- **API base path**: `/api` as configured in `server/src/main/resources/application.properties`.
- **Testing**: JUnit 5; test sources in `server/src/test/java/`, mirroring the package structure of `main`.

### Database
- **Engine**: PostgreSQL — canonical database for CI and production.
- **Hosting**: AWS RDS (production); local PostgreSQL instance for development.
- **Schema management**: Flyway migrations only; all migration scripts live in `server/src/main/resources/db/migration` and must be reviewed with every schema change.
- **SQLite**: permitted for quick local experiments only (per README); never used in CI or production.
- **Connection config**: supplied via environment variables (`DATABASE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`); see `application-rds.properties` for the RDS profile.

## Security Boundaries
The following are hard rules. There are no exceptions without a security review and explicit project-lead sign-off.

1. **No secrets in source control.** Passwords, API keys, JWT signing secrets, database credentials, AWS access keys, and any other sensitive values must never be committed to the repository — not in code, config files, comments, test fixtures, `.env` files, or commit messages.
2. **Environment variables only.** All runtime secrets must be injected via environment variables (e.g., `JWT_SECRET`, `DATABASE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`) or an approved external secrets manager.
3. **Local property files.** `application.properties` and `application-rds.properties` checked into the repo must contain only non-sensitive defaults or placeholders. Production values are set at deploy time.
4. **`.gitignore` enforcement.** Any file that may contain secrets (`.env`, `*.local.properties`, `secrets.*`) must be listed in `.gitignore` before it is created.
5. **Incident response.** If a secret is accidentally committed, it must be rotated immediately, then removed from git history (using `git filter-repo` or equivalent). The incident must be reported to the project lead within 24 hours.

## Project Structure
Source code, tests, and documentation must always live in their designated locations. Deviations require explicit justification and owner approval.

- **Server application code**: `server/src/main/java/` — all production Java source files.
- **Server tests**: `server/src/test/java/` — all JUnit unit and integration tests, mirroring the package structure of `main`.
- **Server resources**: `server/src/main/resources/` — configuration files (`application.properties`, `application-rds.properties`) and Flyway migrations (`db/migration/`).
- **Client application code**: `client/src/app/` — all Angular components, services, guards, interceptors, interfaces, and types.
- **Client tests**: co-located Vitest spec files (`*.spec.ts`) alongside the source files they cover, inside `client/src/app/`.
- **Documentation**: repository root — `README.md` (getting started, architecture overview), `CONTRIBUTING.md` (workflow and conventions), `HELP.md` (troubleshooting). Extended or supplemental docs may live in a `docs/` directory at the repository root.
- **Canonical ERD**: `docs/architecture/erd.md` — machine-parseable data model and relationship map; update this file for every schema-affecting change.
- **Database migrations**: `server/src/main/resources/db/migration/` — Flyway-managed SQL migration files exclusively; no ad-hoc schema scripts elsewhere.
- **Build outputs**: `server/build/` and `client/dist/` — generated artifacts; never commit these to source control.

## Development Workflow & Quality Gates
- Branching: feature branches named `feature/<short-desc>`; bugfix branches `fix/<short-desc>`.
- Pull requests: require at least 1 reviewer. PRs must include a description, linked issue (if applicable), and test coverage for changed behavior.
- CI gating: All PRs must pass server build and tests (`./gradlew build` in `server`) and client tests (`npm test` or `npm run test` in `client`) before merging.
- Schema-change gating: Any PR that changes entities or migrations must update `docs/architecture/erd.md` in the same PR.
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

**Version**: 1.4.0 | **Ratified**: 2026-03-05 | **Last Amended**: 2026-03-06
