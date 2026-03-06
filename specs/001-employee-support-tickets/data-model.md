# Data Model: Employee Support Ticket Management

**Branch**: `001-employee-support-tickets` | **Phase**: 1 | **Date**: 2026-03-06

> No new tables or columns are required for this feature. All persistence is handled by the existing `ticket` and `ticket_thread` tables from `V3__ticket.sql`.

---

## Entities

### Ticket

**Table**: `ticket`  
**JPA Entity**: `com.revature.revastudio.entity.Ticket`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `ticket_id` | `SERIAL` (INT) | PK, NOT NULL | Auto-generated |
| `subject` | `VARCHAR(255)` | NOT NULL | Ticket subject line |
| `body` | `TEXT` | NOT NULL | Full ticket description |
| `created_at` | `TIMESTAMP` | NOT NULL | Set at creation time |
| `status` | `VARCHAR(50)` | NOT NULL | One of: `OPEN`, `IN_PROGRESS`, `RESOLVED` |
| `customer_id` | `INT` | FK → `customer.customer_id`, NOT NULL | The customer who opened the ticket |
| `employee_id` | `INT` | FK → `employee.employee_id`, nullable | The employee assigned to the ticket |

**Status State Machine**:

```
OPEN ──────► IN_PROGRESS ──────► RESOLVED
  │                                  ▲
  └──────────────────────────────────┘
  (employee can close from any non-RESOLVED status)
```

- `OPEN` → default when ticket is created by a customer
- `IN_PROGRESS` → set when an employee first replies (future enhancement; not in scope for this feature)
- `RESOLVED` → set when an employee explicitly closes the ticket via `PATCH /api/ticket/{ticketId}/close`
- **Terminal state**: A ticket in `RESOLVED` status cannot be re-opened or closed again

**Validation rules**:
- `subject`: required, max 255 chars
- `body`: required
- `status`: must be a valid `TicketStatus` enum value
- `customer_id`: required (every ticket must have an owner)
- `employee_id`: optional at creation; populated when an employee is assigned

**JPA Relationships**:
- `@ManyToOne` → `Customer` via `customer_id`
- `@ManyToOne(optional=true)` → `Employee` via `employee_id`
- `@OneToMany(mappedBy="ticket")` → `List<TicketThread>`

---

### TicketThread

**Table**: `ticket_thread`  
**JPA Entity**: `com.revature.revastudio.entity.TicketThread`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `ticket_thread_id` | `SERIAL` (INT) | PK, NOT NULL | Auto-generated |
| `ticket_id` | `INT` | FK → `ticket.ticket_id`, NOT NULL, ON DELETE CASCADE | Parent ticket |
| `thread` | `TEXT` | NOT NULL | Message body |
| `created_at` | `TIMESTAMP` | nullable (set via `@PrePersist`) | Auto-set at persist time |

**Validation rules**:
- `thread`: required, must not be blank (enforced at Angular form validation level; backend should also reject via `@NotBlank` or service check)
- `ticket_id`: required; must reference an existing ticket
- `created_at`: automatically populated by `@PrePersist`; should never be set by caller

**JPA Relationships**:
- `@ManyToOne(optional=false)` → `Ticket` via `ticket_id`

---

### Employee (reference — read only for this feature)

**Table**: `employee`  
**JPA Entity**: `com.revature.revastudio.entity.Employee`

Relevant fields used by this feature:

| Field | Used For |
|-------|----------|
| `employeeId` | Ticket assignment FK, included in `TicketResponseDTO` |
| `user` (`@OneToOne` → `User`) | Resolves `employee_id` from the JWT `userId` (`User.id`) via `ticket.getEmployee().getUser().getId()` |
| `tickets` (`@OneToMany`) | Back-reference used by `TicketRepository.findByEmployee_User_Id(UUID)` |

---

### User (reference — read only for this feature)

**Table**: `user` (or `users`)  
**JPA Entity**: `com.revature.revastudio.entity.User`

Used only to link the JWT subject (`UUID`) to the employee record during close-ticket authorization check.

---

## DTO Layer

### TicketResponseDTO (existing — no change)

```java
Integer ticketId
String  subject
String  body
LocalDateTime createdAt
TicketStatus  status       // OPEN | IN_PROGRESS | RESOLVED
Integer       customerId
Integer       employeeId
```

Used by `GET /api/ticket/employee`, `GET /api/ticket/customer`, and the new `PATCH /api/ticket/{ticketId}/close`.

### TicketThreadDTO (existing — no change)

```java
Integer       ticketThreadId
Integer       ticketId
String        thread
LocalDateTime createdAt
```

### TicketThreadRequestDTO (existing — no change)

```java
Integer ticketId
String  thread
```

---

## TicketStatus Enum

**Server** (`com.revature.revastudio.enums.TicketStatus`):
```java
OPEN, IN_PROGRESS, RESOLVED
```

**Client** (`client/src/app/interfaces/ticket.ts`) — **must be updated**:
```typescript
// Before (incorrect):
export type TicketStatus = "OPEN" | "IN_PROGRESS" | "CLOSED";

// After (correct):
export type TicketStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED";
```

---

## No Schema Migration Required

The close-ticket feature writes `RESOLVED` to the existing `status VARCHAR(50)` column. This value is already valid per the server enum. No `Vn__*.sql` file is needed.
