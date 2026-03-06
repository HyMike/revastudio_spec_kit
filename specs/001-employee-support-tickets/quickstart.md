# Quickstart: Employee Support Ticket Management

**Branch**: `001-employee-support-tickets` | **Phase**: 1 | **Date**: 2026-03-06

End-to-end integration scenarios that must pass once the feature is implemented. Use these as manual smoke tests or as the basis for automated integration tests.

---

## Prerequisites

- Server running on `http://localhost:8080`
- Angular client running on `http://localhost:4200`
- PostgreSQL with Chinook + Flyway migrations applied (including `V3__ticket.sql`)
- Seed data: at least one employee account, one customer account, and one ticket assigned to that employee

---

## Scenario 1 — Employee Views Assigned Tickets (US1)

**Goal**: Employee can fetch their ticket list.

```bash
# 1. Login as employee
TOKEN=$(curl -s -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"employee@test.com","password":"password"}' \
  | jq -r '.token')

# 2. Fetch employee tickets
curl -s http://localhost:8080/api/ticket/employee \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**Expected**: `200 OK` with an array of ticket objects where each ticket's `employeeId` matches the authenticated employee. Status values are one of `OPEN`, `IN_PROGRESS`, `RESOLVED`.

**Empty state**: if employee has no assigned tickets, returns `200 OK` with `[]`.

---

## Scenario 2 — Employee Reads Ticket Thread (US2)

**Goal**: Employee can read all thread messages for one of their assigned tickets.

```bash
# Using TOKEN from Scenario 1; replace 1 with a real ticketId
curl -s http://localhost:8080/api/ticket-threads/1 \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**Expected**: `200 OK` with array of `TicketThreadDTO` objects ordered by `createdAt`. Returns `[]` if no messages exist yet.

---

## Scenario 3 — Employee Posts Reply (US2)

**Goal**: Employee can add a thread message to a ticket.

```bash
curl -s -X POST http://localhost:8080/api/ticket-threads/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ticketId":1,"thread":"Hi, I can help — could you describe the issue?"}' | jq .
```

**Expected**: `200 OK` with the new `TicketThreadDTO`. Re-fetching `GET /api/ticket-threads/1` includes the new message.

**Blocked before fix**: returns `403 Forbidden` because `@PreAuthorize("hasRole('CUSTOMER')")` blocks employees. After fix, returns `200 OK`.

---

## Scenario 4 — Close Ticket (US3)

**Goal**: Employee closes one of their assigned tickets.

```bash
curl -s -X PATCH http://localhost:8080/api/ticket/1/close \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**Expected**: `200 OK` with the updated `TicketResponseDTO` where `status` is `"RESOLVED"`. Re-fetching ticket list confirms the status change.

**Error cases**:
```bash
# a) Already resolved — expect 409
curl -s -X PATCH http://localhost:8080/api/ticket/1/close \
  -H "Authorization: Bearer $TOKEN" | jq .

# b) Different employee's ticket (ticketId=99 owned by other employee) — expect 403
curl -s -X PATCH http://localhost:8080/api/ticket/99/close \
  -H "Authorization: Bearer $TOKEN" | jq .

# c) Customer tries to close — expect 403
CUSTOMER_TOKEN=$(curl -s -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer@test.com","password":"password"}' \
  | jq -r '.token')

curl -s -X PATCH http://localhost:8080/api/ticket/1/close \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | jq .
```

---

## Scenario 5 — Angular UI Smoke Test

1. Log in as an employee at `http://localhost:4200/login`
2. Navigate to `http://localhost:4200/ticket`
3. Verify: ticket list shows assigned tickets with subject, status badge, and date
4. Click a ticket card — dialog opens showing thread messages
5. Type a reply in the textarea and click **Send Message** — message appears in the thread list
6. Verify: **Close Ticket** button is visible (because role = EMPLOYEE)
7. Click **Close Ticket** — dialog closes; ticket list refreshes; the ticket's status badge now shows **RESOLVED**
8. Re-open the same ticket — verify **Close Ticket** button is hidden/disabled

---

## Scenario 6 — TicketStatus Enum Alignment Smoke Test

After changing `client/src/app/interfaces/ticket.ts` to use `"RESOLVED"` instead of `"CLOSED"`:

1. Open the Angular app as a customer with a resolved ticket
2. Verify: the status badge renders `RESOLVED` without TypeScript errors in the console
3. Verify: no `Cannot read property` or type mismatch errors in the browser console
