# Research: Employee Support Ticket Management

**Branch**: `001-employee-support-tickets` | **Phase**: 0 | **Date**: 2026-03-06

> All NEEDS CLARIFICATION items from spec.md resolved below.

---

## Decision 1 — TicketStatus Enum Alignment

**Question**: The server `TicketStatus` enum uses `OPEN, IN_PROGRESS, RESOLVED` while the Angular client `TicketStatus` type uses `"OPEN" | "IN_PROGRESS" | "CLOSED"`. Which canonical value should be used for a closed ticket?

**Decision**: Use `RESOLVED` on both server and client.

**Rationale**:
- The server enum (`TicketStatus.java`) and the Flyway migration (`V3__ticket.sql`) already operate on `RESOLVED`. Changing the server would require a Flyway migration to update existing rows.
- `RESOLVED` is more semantically accurate for a support context (the issue is resolved, not just closed).
- The client type needs a one-line change; no data migration is involved on the client side.

**Alternatives considered**:
- Use `CLOSED` on both — rejected because it requires a server enum rename, a new Flyway migration to `UPDATE ticket SET status = 'CLOSED' WHERE status = 'RESOLVED'`, and risks breaking existing data.
- Map at the service layer — rejected as unnecessary complexity; a single source of truth is simpler.

**Change required**: `client/src/app/interfaces/ticket.ts` — replace `"CLOSED"` with `"RESOLVED"`.

---

## Decision 2 — Thread Reply Authorization Fix

**Question**: `POST /api/ticket-threads/create` currently has `@PreAuthorize("hasRole('CUSTOMER')")`, which blocks employees from posting replies. What is the correct fix?

**Decision**: Change the annotation to `@PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")`.

**Rationale**:
- The feature requires employees to respond to tickets. The existing guard is clearly a copy-paste error (the comment even says "Only employees can create thread messages" but the annotation says CUSTOMER).
- `hasAnyRole` is the standard Spring Security expression for multi-role permissions.
- No deeper authorization is needed on this endpoint beyond role check; the ticket association is validated by ticket ID lookup in `TicketThreadService.createThread()`.

**Alternatives considered**:
- Remove the guard entirely — rejected; all sensitive endpoints must carry role checks per the Constitution's security boundary requirements.
- Duplicate the endpoint per role — rejected; unnecessary duplication.

**File**: `server/src/main/java/com/revature/revastudio/controllers/TicketThreadController.java`

---

## Decision 3 — Close Ticket Endpoint Design

**Question**: No close-ticket endpoint exists. Where should it live, what HTTP verb/path, and how should ownership be enforced?

**Decision**: `PATCH /api/ticket/{ticketId}/close` on `TicketController`, `EMPLOYEE` role only, with ownership verification in the service layer.

**Rationale**:
- `PATCH` is semantically correct for a partial status update (not replacing the full resource).
- Housing it in `TicketController` alongside the existing `GET /ticket/employee` keeps ticket operations co-located.
- Ownership enforcement (`ticket.getEmployee().getUser().getId().equals(employeeUserId)`) must happen in `TicketService.closeTicket()` — not in the controller — to respect the project's service-layer business-logic rule (Constitution §IV).
- Returning `403 Forbidden` for unowned tickets (rather than 404) is a deliberate choice to not leak ticket existence to unauthorized employees. **Alternative**: returning 404 is also acceptable and avoids any information disclosure. Decision: use `403` to match the spec's acceptance criteria language.

**New method signature**:
```java
// TicketService
public TicketResponseDTO closeTicket(Integer ticketId, UUID employeeUserId);
```

**Already exists**: `TicketRepository.findByEmployee_User_Id(UUID)` — confirms employee→ticket relationship via JPA derived query.

**Alternatives considered**:
- `PUT /api/ticket/{ticketId}/status` with a body — rejected; the body payload adds unnecessary complexity for a single-state transition.
- `DELETE /api/ticket/{ticketId}` — rejected; tickets should not be deleted, only resolved.

---

## Decision 4 — Thread Read Authorization

**Question**: `GET /api/ticket-threads/{ticketId}` has no `@PreAuthorize` annotation. Should it be locked down?

**Decision**: Add `@PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")` to ensure only authenticated role-bearing users can read threads.

**Rationale**:
- The global `SecurityConfig` already requires authentication for all `/**` endpoints, so unauthenticated access is blocked at the filter level.
- Adding `@PreAuthorize` aligns with the pattern used across all other ticket endpoints and makes authorization explicit/auditable.
- No per-ticket ownership check is added here (either the customer who owns the ticket or the employee assigned can read it). Implementing per-resource authorization would require a lookup; given the scope this is acceptable for MVP — the route is auth-gated and the tickets API already scopes lists per user.

---

## Decision 5 — Angular Employee UI Pattern

**Question**: Should the employee ticket view be a new route/component or reuse the existing `Ticket` component?

**Decision**: Reuse the existing `Ticket` component and `TicketThreadModal` with role-conditional additions.

**Rationale**:
- `TicketService.getAllTickets()` already branches on `CUSTOMER` vs `EMPLOYEE` role to call the correct endpoint. The ticket list component (`ticket.ts`) is already shared.
- Adding a "Close Ticket" button to the `TicketThreadModal` guarded by an `isEmployee` flag is the least-invasive change; no new routing or component scaffolding needed.
- The `AuthService.getRole()` method is already available in the modal's component tree via injection.

**Changes required**:
- `ticket-thread-modal.ts`: inject `AuthService`, expose `isEmployee: boolean`, call close endpoint on button click, refresh ticket list on success.
- `ticket-thread-modal.html`: add "Close Ticket" `<button>` shown only when `isEmployee && ticket.status !== 'RESOLVED'`.
- `ticket-service.ts`: add `closeTicket(ticketId: number): Observable<TicketResponse>` calling `PATCH /api/ticket/{ticketId}/close`.

**Alternatives considered**:
- New `/employee/tickets` route with a dedicated component — rejected for MVP scope; can be refactored post-MVP if employee and customer views diverge significantly.

---

## Decision 6 — No Schema Migration Required

**Question**: Does adding `closeTicket` require a Flyway migration?

**Decision**: No new migration needed.

**Rationale**: Closing a ticket only changes the `status` column value from `OPEN`/`IN_PROGRESS` to `RESOLVED`. The `ticket.status` column already accepts `RESOLVED` (it is a `VARCHAR(50)` storing enum strings). No column adds or renames required.

---

## Summary of All Changes

| Layer | File | Change Type |
|-------|------|-------------|
| Server — Controller | `TicketController.java` | Add `PATCH /{ticketId}/close` endpoint |
| Server — Service | `TicketService.java` | Add `closeTicket(ticketId, employeeUserId)` |
| Server — Controller | `TicketThreadController.java` | Fix `@PreAuthorize` on `POST /create` |
| Client — Interface | `ticket.ts` | Change `"CLOSED"` → `"RESOLVED"` in `TicketStatus` |
| Client — Service | `ticket-service.ts` | Add `closeTicket(ticketId)` method |
| Client — Component | `ticket-thread-modal.ts` | Inject `AuthService`, add close logic |
| Client — Template | `ticket-thread-modal.html` | Add conditional "Close Ticket" button |
