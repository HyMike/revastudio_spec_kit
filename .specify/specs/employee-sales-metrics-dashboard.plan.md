# Implementation Plan: Employee Sales Metrics Dashboard

**Branch**: `[main]` | **Date**: 2026-03-05 | **Last Reviewed**: 2026-03-05 | **Spec**: `.specify/specs/employee-sales-metrics-dashboard.md`
**Input**: Feature specification from `.specify/specs/employee-sales-metrics-dashboard.md`

## Summary

Implement an employee-only sales metrics dashboard backed by a Spring Boot REST API with JPA/Hibernate over PostgreSQL and an Angular frontend. The API will expose metrics scoped to the authenticated employee (total sales count, total revenue, customers assisted, and track-level sales details) and the Angular app will render a dashboard view that surfaces these metrics with a clear empty state when no data exists. This plan has been re-evaluated against the current spec and the partially implemented backend artifacts already present in the repository.

## Technical Context

**Language/Version**: Java 21 (Spring Boot), TypeScript (Angular)  
**Primary Dependencies**: Spring Boot Web, Spring Security (JWT filter chain + method security), Spring Data JPA/Hibernate, Angular (HTTPClient, Router)  
**Storage**: PostgreSQL via Flyway-managed schema (see `server/src/main/resources/db/migration`)  
**Testing**: JUnit + Spring Boot test for backend; Angular unit tests (Jasmine/Vitest) for frontend  
**Target Platform**: Backend: JVM on Linux (RDS-backed); Frontend: Browser (SPA)  
**Project Type**: Web service + SPA client  
**Performance Goals**: API responds in ≤2s for typical per-employee data; dashboard renders in ≤3s on a dev machine  
**Constraints**: JWT-based auth with `EMPLOYEE` role; no secrets in source; metrics must be correctly scoped to the employee’s customers only; security behavior must align with the centralized Spring Security filter chain and CORS bean configuration  
**Scale/Scope**: Single-team app; per-employee sales data up to a few thousand invoices and tens of thousands of invoice lines

## Re-evaluation Summary

- The spec is still valid and does not need to be rewritten.
- The current backend implementation already includes DTOs, a metrics service, a controller, and a repository query.
- The plan needed updating to reflect the actual Spring Security setup and to record remaining gaps between the spec and the current code.
- The highest-risk gaps are authorization hardening, alignment with centralized CORS configuration, and completion of tests/frontend work.

## Constitution Check

- Purpose-First: Feature directly supports the employee dashboard requirement from README and adds value for tracking performance. ✅
- Production Parity: Uses existing PostgreSQL/Flyway stack and domain entities (`Employee`, `Customer`, `Invoice`, `InvoiceLine`, `Track`) already modeled. No shadow storage. ✅
- Test-First Quality: Plan includes unit and integration tests around the metrics API and Angular dashboard behavior (with and without data). ✅
- Security by Default: Partially satisfied. JWT authentication exists globally, but the employee metrics endpoint still needs explicit role enforcement and should rely on centralized CORS configuration rather than permissive controller-level CORS. ⚠️
- Observability & Simplicity: Single backend endpoint (or small set) for metrics, simple DTOs, logging of access; no unnecessary services or micro-modules. ✅

## Security & Spring Security Configuration

**Framework**: Spring Security (`SecurityConfig` at `server/src/main/java/com/revature/revastudio/security/SecurityConfig.java`)

**CORS**:
- Configured via `CorsConfig` bean at `server/src/main/java/com/revature/revastudio/security/CorsConfig.java`
- Allows `localhost:4200` (client dev server)
- Credentials enabled for JWT in Authorization header
- Methods allowed: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Plan requirement: feature controllers should not widen CORS beyond this shared configuration unless explicitly approved

**CSRF**: Disabled for stateless JWT authentication (no session cookies)

**HTTP Security**:
- Session creation set to `STATELESS`
- `JwtAuthenticationFilter` (injected into `SecurityConfig`) validates JWT on every request
- Public endpoints: `/user/login` (unauthenticated)
- Protected endpoints: All others require valid JWT
- Role enforcement target: `/api/employee/**` endpoints require `EMPLOYEE` role via `@PreAuthorize` annotation or equivalent method security

**HTTPS**: Enforced in production (application-rds.properties); local dev uses HTTP

**Implementation Details**:
- Endpoint target: `GET /api/employee/sales-metrics` protected by JWT + `EMPLOYEE` role
- Current controller artifact: `EmployeeMetricsController` at `server/src/main/java/com/revature/revastudio/controllers/EmployeeMetricsController.java`
- Current service artifact: `EmployeeMetricsService` at `server/src/main/java/com/revature/revastudio/services/EmployeeMetricsService.java`
- Current repository artifact: `InvoiceRepository.findByCustomer_SupportRep_EmployeeId(Integer employeeId)`
- Current implementation resolves the current user from `SecurityContextHolder` and maps user → employee → invoices
- Remaining hardening: add explicit role guard, align class mapping with `/api` convention, and remove permissive controller-specific CORS if redundant with `CorsConfig`

## Current Implementation Snapshot

### Backend completed or partially completed

- DTOs created for summary, customer list, sales detail, and aggregate metrics response
- Repository query added to scope invoices by employee through `Customer.supportRep`
- Service computes revenue totals, invoice count, customer aggregation, and per-invoice details
- Controller exposes a metrics endpoint for the authenticated principal

### Still pending to satisfy the spec fully

- Enforce `EMPLOYEE` role explicitly at controller/service level
- Add backend unit/integration tests for happy path, empty state, and unauthorized/forbidden access
- Confirm endpoint path and response contract stay consistent with `/api` base-path conventions
- Implement Angular metrics service and employee dashboard UI
- Add endpoint access logging for observability requirement `FR-008`

## Project Structure

### Documentation (this feature)

```text
.specify/specs/
├── employee-sales-metrics-dashboard.md        # Feature spec
├── employee-sales-metrics-dashboard.plan.md   # This plan
├── employee-sales-metrics-dashboard.research.md
├── employee-sales-metrics-dashboard.data-model.md
├── employee-sales-metrics-dashboard.quickstart.md
└── employee-sales-metrics-dashboard.contracts/
    └── sales-metrics-api.yaml (or .md)
```

### Source Code (repository root)

```text
server/
├── src/main/java/com/revature/revastudio/
│   ├── controllers/        # + Employee metrics controller
│   ├── services/           # + Employee metrics service
│   ├── dto/                # + Sales metrics DTOs
│   └── repositories/       # + metrics-related queries via existing entities
└── src/test/java/com/revature/revastudio/
    └── ...                 # + integration/unit tests for metrics API

client/
├── src/app/
│   ├── components/
│   │   └── employee-dashboard/   # new or extended dashboard component
│   ├── services/
│   │   └── employee-metrics.service.ts
│   └── app.routes.ts             # route for employee dashboard (if not present)
└── src/app/**/*.spec.ts          # unit tests for component + service
```

**Structure Decision**: Use existing `server` and `client` modules. Add a focused metrics service/controller + DTOs on the backend and a dedicated Angular component + service for the employee dashboard on the frontend. Tests live alongside existing test structure.

## Delivery Phases

### Phase A — Backend hardening
- Confirm entity imports/package names are consistent with the existing domain model
- Add explicit `EMPLOYEE` authorization check
- Normalize endpoint mapping to the `/api` convention used by the server
- Remove or narrow controller-level `@CrossOrigin` if shared CORS configuration already covers the route

### Phase B — Backend validation
- Add service tests for employee with sales, employee with no sales, and user with no linked employee
- Add controller/integration tests for 200, 401, and 403 behaviors
- Validate response payload shape against the contract document

### Phase C — Frontend completion
- Add Angular service for the metrics endpoint
- Add/extend employee dashboard component
- Render summary, customer list, and sales details with empty/error states
- Add client tests for success, empty state, and error handling

## Complexity Tracking

No architecture-level constitution violations are required for delivery. Current implementation has two items to reconcile with the constitution before completion: explicit role enforcement and alignment with centralized CORS/security configuration.
