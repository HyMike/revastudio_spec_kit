# Research: Employee Sales Metrics Dashboard

## Decisions

### D1: Use existing PostgreSQL + JPA domain model
- **Decision**: Reuse existing entities (`Employee`, `Customer`, `Invoice`, `InvoiceLine`, `Track`) and relationships to derive metrics; no new sales tables.
- **Rationale**: Aligns with Production Parity and avoids duplicating data models. The Chinook schema already supports invoices and invoice lines.
- **Alternatives considered**:
  - Add a separate aggregated metrics table updated via batch jobs → rejected as premature optimization and extra complexity.
  - Use a reporting replica or external analytics DB → rejected as out of scope for current MVP.

### D2: Single metrics endpoint per employee
- **Decision**: Expose a single REST endpoint (e.g., `GET /api/employee/sales-metrics`) that returns a composite metrics payload (summary + customers + sales details).
- **Rationale**: Simpler client integration and fewer round trips; easier to secure and test. Keeps API surface small.
- **Alternatives considered**:
  - Multiple endpoints for summary/customers/details → more flexible but higher coordination overhead and complexity for this MVP.

### D3: Scope by authenticated `User` → `Employee` → `Customer`
- **Decision**: Metrics queries derive the employee from the authenticated `User` (via JWT), then join to customers and invoices.
- **Rationale**: Centralizes scoping logic and reduces risk of cross-employee data leakage.
- **Alternatives considered**:
  - Accept employee ID as a path parameter → rejected as it increases risk of unauthorized access to another employee’s data.

### D4: Simple DTOs for metrics
- **Decision**: Use DTOs for metrics summary and sales rows (e.g., `totalSalesCount`, `totalRevenue`, `customers[]`, `sales[]`).
- **Rationale**: Decouples API from internal entities and avoids overexposing domain model; makes the Angular integration cleaner.
- **Alternatives considered**:
  - Return entities directly via JSON → rejected per good API design practice and to avoid leaking extra fields.

### D5: Angular dashboard as a single page/route
- **Decision**: Implement the employee metrics view as a dedicated Angular component/route (e.g., `/employee/dashboard`) with a service that calls the metrics endpoint.
- **Rationale**: Keeps employee-specific UI concerns localized; fits existing Angular routing/component patterns.
- **Alternatives considered**:
  - Integrate into an existing generic dashboard component → acceptable but might blur responsibilities; can be refactored later.

## Open Questions (NEEDS CLARIFICATION)

- Time range for metrics (e.g., all-time vs last 30/90 days)?
- Sorting and pagination requirements for sales table (limit 20? client-side vs server-side paging?).
- Any export/reporting requirements (CSV, PDF)?
