---

description: "Tasks for implementing Employee Sales Metrics Dashboard"
---

# Tasks: Employee Sales Metrics Dashboard

**Input**: Design documents from `.specify/specs/employee-sales-metrics-dashboard*`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Format: `[ID] [P?] [Story] Description`

---

## Phase 1: Foundational (Shared Backend/Frontend)

- [ ] T001 [P] [ALL] Confirm PostgreSQL config and Flyway migrations for invoices/invoice_lines are applied (server/src/main/resources/db/migration).
- [ ] T002 [P] [ALL] Verify JWT auth and `EMPLOYEE` role mapping for users is working end-to-end.
- [x] T003 [ALL] Reconcile current backend implementation with centralized Spring Security configuration in `SecurityConfig` and shared CORS policy in `CorsConfig`.
- [x] T004 [ALL] Confirm the employee metrics endpoint follows the server `/api` base-path convention and document any route adjustments needed.

---

## Phase 2: User Story 1 – View overall sales metrics (P1)

**Goal**: Employee sees total sales count and total revenue for their sales.

**Independent Test**: With seeded data for a single employee, hitting the metrics endpoint and loading the dashboard shows correct totals without relying on customer/track lists.

### Backend – US1

- [x] T010 [P] [US1] Add metrics DTOs in `server/src/main/java/com/revature/revastudio/dto/` (summary + metrics root DTO).
- [x] T011 [P] [US1] Extend repositories (e.g., `InvoiceRepository`) to fetch invoices for customers where `Customer.supportRep` is the authenticated employee.
- [x] T012 [US1] Implement `EmployeeMetricsService` in `server/src/main/java/com/revature/revastudio/services/` to compute total sales count and revenue for the employee.
- [x] T013 [US1] Harden `EmployeeMetricsController` for `GET /api/employee/sales-metrics` by enforcing `EMPLOYEE` role access explicitly, aligning mapping with `/api`, and removing permissive controller-level CORS if covered by shared config.
- [x] T014 [US1] Add backend tests (unit + integration) to verify correct totals, no-linked-employee empty state, and access control for the metrics endpoint.

### Frontend – US1

- [x] T015 [P] [US1] Create `employee-metrics.service.ts` in `client/src/app/services/` to call `/api/employee/sales-metrics` and map the summary fields.
- [x] T016 [US1] Create or extend an `employee-dashboard` component under `client/src/app/components/` to display total sales count and total revenue.
- [x] T017 [US1] Wire a route (e.g., `/employee/dashboard`) in `client/src/app/app.routes.ts` (or equivalent) and ensure only employees can access it.
- [x] T018 [US1] Add frontend tests for service and component covering summary display and "no data" state.

---

## Phase 3: User Story 2 – See customers I assisted with sales (P2)

**Goal**: Employee sees a list of customers they have assisted with at least one sale.

**Independent Test**: With multiple customers linked to an employee via invoices, the dashboard shows a unique list of those customers even without track-level details.

### Backend – US2

- [x] T020 [P] [US2] Extend metrics DTOs to include a `customers[]` collection (e.g., `EmployeeCustomerDTO`).
- [x] T021 [US2] Update `EmployeeMetricsService` to compute unique customers from employee-scoped invoices and populate the customers list.
- [ ] T022 [US2] Validate and test the metrics endpoint response for `customers[]`, including uniqueness and empty-state behavior.

### Frontend – US2

- [x] T023 [P] [US2] Extend `employee-metrics.service.ts` types to include `customers[]`.
- [x] T024 [US2] Update `employee-dashboard` component template to render a customers list section.
- [x] T025 [US2] Add/extend frontend tests to verify customers list rendering and empty state.

---

## Phase 4: User Story 3 – See tracks purchased and billing amounts (P3)

**Goal**: Employee sees which tracks were purchased and how much customers were billed for sales they assisted.

**Independent Test**: With seeded invoice/invoiceLine/track data, the metrics endpoint and dashboard show a sales table with track, customer, date, and billed amount for the employee’s sales.

### Backend – US3

- [x] T030 [P] [US3] Extend metrics DTOs to include `sales[]` entries (e.g., `EmployeeSaleDetailDTO`).
- [x] T031 [US3] Upgrade sales detail generation from invoice-level rows to track/invoice-line detail rows scoped to the employee.
- [x] T032 [US3] Update metrics endpoint tests to cover sales details and ensure no leakage of other employees’ sales.

### Frontend – US3

- [x] T033 [P] [US3] Extend `employee-metrics.service.ts` types to include `sales[]`.
- [x] T034 [US3] Update `employee-dashboard` component to render a table of sales details (track name, customer name, invoice date, billed amount).
- [x] T035 [US3] Add/extend frontend tests to verify the sales table behavior (data present, no data).

---

## Phase 5: Cross-Cutting & Polish

- [ ] T040 [P] [ALL] Add logging around metrics calculations and endpoint access for observability.
- [ ] T041 [ALL] Review security configuration to confirm only `EMPLOYEE` role can access metrics, shared CORS rules are respected, and data is strictly scoped.
- [ ] T042 [P] [ALL] Update `.specify/specs/employee-sales-metrics-dashboard.quickstart.md` if implementation details (paths, fields) changed.
- [ ] T043 [ALL] Run full backend and frontend test suites and validate against Success Criteria (SC-001–SC-005).
