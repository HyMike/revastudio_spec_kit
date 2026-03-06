# API Contract: Employee Support Ticket Management

**Branch**: `001-employee-support-tickets` | **Phase**: 1 | **Date**: 2026-03-06  
**Base path**: `/api` (configured in `application.properties`)  
**Auth**: All endpoints require `Authorization: Bearer <JWT>` header  

---

## 1. Get Employee Tickets

**Status**: Existing — no changes needed

```
GET /api/ticket/employee
```

**Authorization**: `hasRole('EMPLOYEE')`

**Request headers**:
```
Authorization: Bearer <JWT>
```

**Response `200 OK`**:
```json
[
  {
    "ticketId": 1,
    "subject": "Cannot access my playlist",
    "body": "Getting a 403 when trying to open any playlist.",
    "createdAt": "2026-03-01T10:00:00",
    "status": "OPEN",
    "customerId": 12,
    "employeeId": 3
  }
]
```

**Response `403 Forbidden`**: caller has `CUSTOMER` role  
**Response `401 Unauthorized`**: no or invalid JWT

---

## 2. Get Ticket Thread Messages

**Status**: Existing — add `@PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")`

```
GET /api/ticket-threads/{ticketId}
```

**Authorization**: `hasAnyRole('CUSTOMER', 'EMPLOYEE')`

**Path parameters**:
| Param | Type | Description |
|-------|------|-------------|
| `ticketId` | `Integer` | ID of the ticket whose thread messages to retrieve |

**Response `200 OK`**:
```json
[
  {
    "ticketThreadId": 5,
    "ticketId": 1,
    "thread": "Hi, could you describe what URL you're navigating to?",
    "createdAt": "2026-03-02T09:15:00"
  }
]
```

**Response `401 Unauthorized`**: no or invalid JWT

---

## 3. Post Thread Reply

**Status**: Existing — fix `@PreAuthorize` from `hasRole('CUSTOMER')` → `hasAnyRole('CUSTOMER', 'EMPLOYEE')`

```
POST /api/ticket-threads/create
```

**Authorization**: `hasAnyRole('CUSTOMER', 'EMPLOYEE')`

**Request body** (`application/json`):
```json
{
  "ticketId": 1,
  "thread": "Hi, could you describe what URL you're navigating to?"
}
```

**Validation**:
- `ticketId`: required, must reference an existing ticket
- `thread`: required, must not be blank

**Response `200 OK`**:
```json
{
  "ticketThreadId": 6,
  "ticketId": 1,
  "thread": "Hi, could you describe what URL you're navigating to?",
  "createdAt": "2026-03-02T09:15:00"
}
```

**Response `403 Forbidden`**: caller has neither `CUSTOMER` nor `EMPLOYEE` role  
**Response `401 Unauthorized`**: no or invalid JWT  
**Response `404 Not Found`**: `ticketId` does not exist (thrown by `TicketThreadService.createThread()`)

---

## 4. Close Ticket *(NEW)*

```
PATCH /api/ticket/{ticketId}/close
```

**Authorization**: `hasRole('EMPLOYEE')`

**Path parameters**:
| Param | Type | Description |
|-------|------|-------------|
| `ticketId` | `Integer` | ID of the ticket to close |

**Request body**: none

**Business rules**:
1. The authenticated employee's `userId` (from JWT) must match `ticket.employee.user.id`. If not, return `403`.
2. If the ticket's current status is already `RESOLVED`, return `409 Conflict`.
3. Otherwise, set `status = RESOLVED` and persist.

**Response `200 OK`** — returns updated ticket:
```json
{
  "ticketId": 1,
  "subject": "Cannot access my playlist",
  "body": "Getting a 403 when trying to open any playlist.",
  "createdAt": "2026-03-01T10:00:00",
  "status": "RESOLVED",
  "customerId": 12,
  "employeeId": 3
}
```

**Response `403 Forbidden`**: ticket is not assigned to the authenticated employee, or caller has `CUSTOMER` role  
**Response `404 Not Found`**: ticket does not exist  
**Response `409 Conflict`**: ticket is already `RESOLVED`  
**Response `401 Unauthorized`**: no or invalid JWT

---

## Angular Client Service Contracts

### `TicketService` — additions

```typescript
closeTicket(ticketId: number): Observable<TicketResponse> {
  return this.http.patch<TicketResponse>(`${API_BASE_URL}/ticket/${ticketId}/close`, {});
}
```

### `TicketStatus` type — update

```typescript
// client/src/app/interfaces/ticket.ts
export type TicketStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED";  // was "CLOSED"
```

### `TicketThreadModal` — new close integration

- Inject `AuthService` to read `getRole()`
- Expose `isEmployee = this.authService.getRole() === 'EMPLOYEE'`
- Receive `ticketStatus: TicketStatus` via `MAT_DIALOG_DATA` alongside `ticketId`
- On "Close Ticket" click: call `TicketService.closeTicket(ticketId)`, on success close dialog and emit refresh to parent
