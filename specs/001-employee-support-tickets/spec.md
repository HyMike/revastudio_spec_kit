# Feature Specification: Employee Support Ticket Management

**Feature Branch**: `001-employee-support-tickets`  
**Created**: 2026-03-06  
**Status**: Draft  
**Input**: User description: "Employee feature to view and respond to customer support tickets and close tickets — As an employee, I want to view and respond to customer support tickets so that I can assist customers. As an employee, I want to close support tickets so that I can mark issues as resolved."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Assigned Tickets (Priority: P1)

As an employee, I can navigate to my support tickets section and see a list of all tickets assigned to me (from customers I support), including each ticket's subject, status, and creation date, so that I know what issues need my attention.

**Why this priority**: This is the entry point to the entire feature. Without being able to see tickets, responding and closing are impossible. The backend endpoint `GET /api/ticket/employee` already exists and is secured with `EMPLOYEE` role, but the Angular client-side view for employees needs to be fully wired up.

**Independent Test**: With seeded ticket data linked to an employee account, logging in as that employee and navigating to the tickets section shows the list of assigned tickets. No response or close functionality is required for this story to be testable and useful.

**Acceptance Scenarios**:

1. **Given** I am logged in as an employee with tickets assigned to me, **When** I navigate to my tickets page, **Then** I see a list of my tickets each showing subject, current status (`OPEN`, `IN_PROGRESS`, or `RESOLVED`), and creation date.
2. **Given** I am logged in as an employee with no tickets assigned, **When** I navigate to my tickets page, **Then** I see a clear empty-state message (e.g., "No tickets assigned") instead of an error.
3. **Given** I am logged in as a customer, **When** I attempt to access the employee tickets endpoint (`GET /api/ticket/employee`), **Then** I receive a `403 Forbidden` response.

---

### User Story 2 - Read and Respond to a Ticket Thread (Priority: P2)

As an employee, I can open any of my assigned tickets to view the full conversation thread, and I can post a reply visible to the customer, so that I can communicate directly to help resolve the issue.

**Why this priority**: Responding to tickets is the core communication action of this feature and delivers customer-facing value. This builds on US1 (the ticket list). The thread read endpoint `GET /api/ticket-threads/{ticketId}` exists but lacks role enforcement; the create-thread endpoint `POST /api/ticket-threads/create` currently has `@PreAuthorize("hasRole('CUSTOMER')")` which incorrectly blocks employees and must be corrected to allow both `CUSTOMER` and `EMPLOYEE` roles.

**Independent Test**: With a seeded ticket and thread messages, logging in as the assigned employee, opening the ticket, and submitting a reply — then refreshing — shows the new message in the thread. Close functionality is not required for this story.

**Acceptance Scenarios**:

1. **Given** I am logged in as an employee and have opened one of my assigned tickets, **When** the ticket detail/thread view loads, **Then** I see all existing thread messages in chronological order.
2. **Given** I am viewing a ticket thread, **When** I type a response and submit it, **Then** the new message appears in the thread and is persisted (visible after a page reload).
3. **Given** I am logged in as an employee, **When** I attempt to post a thread message via `POST /api/ticket-threads/create`, **Then** the server accepts the request and returns `200 OK` with the created thread entry (the current CUSTOMER-only guard must be updated to also permit EMPLOYEE).
4. **Given** a ticket thread message is submitted with an empty body, **When** the form is validated, **Then** the system rejects the submission with a user-visible error, and the empty message is not persisted.

---

### User Story 3 - Close a Ticket (Priority: P3)

As an employee, I can mark any of my assigned tickets as closed/resolved so that I can indicate the issue has been addressed and the ticket queue stays current.

**Why this priority**: Closing tickets is a status-management action that depends on US1 and US2. It requires a new backend endpoint (`PATCH /api/ticket/{ticketId}/status` or `PUT /api/ticket/{ticketId}/close`) that does not yet exist, plus the client UI to trigger it.

**Independent Test**: With an open ticket assigned to an employee, clicking "Close Ticket" in the UI (or calling the API directly) changes the ticket status to `RESOLVED` and this is reflected immediately in the ticket list view, without implementing any other new functionality.

**Acceptance Scenarios**:

1. **Given** I am logged in as an employee and viewing one of my assigned tickets with status `OPEN` or `IN_PROGRESS`, **When** I click the "Close Ticket" button and confirm, **Then** the ticket status changes to `RESOLVED` and the UI reflects the new status immediately.
2. **Given** a ticket is already `RESOLVED`, **When** I view that ticket, **Then** the "Close Ticket" action is hidden or disabled so I cannot close it twice.
3. **Given** I am logged in as a customer, **When** I attempt to call the close-ticket API endpoint, **Then** I receive a `403 Forbidden` response (only `EMPLOYEE` role may close tickets).
4. **Given** I try to close a ticket not assigned to me (by ticket ID), **When** the server processes the request, **Then** it returns `403 Forbidden` or `404 Not Found` — the employee may only close their own assigned tickets.

---

### Edge Cases

- What happens when an employee opens a ticket that has no thread messages yet? The thread view should show an empty state and still allow posting the first reply.
- What happens if the ticket the employee is viewing is deleted or no longer assigned to them between page loads? The API should return `404` and the UI should display a friendly "ticket not found" message.
- What if two employees are assigned to the same ticket? The system should show the ticket to both, and both should be able to respond. (Current schema links one employee per ticket; if the data model enforces this, the spec assumes one-to-one assignment.)
- What happens if the server is temporarily unavailable when an employee submits a reply or closes a ticket? The client should show an error notification and not lose the typed response.
- How should status inconsistency between the client (`CLOSED`) and server (`RESOLVED`) be handled? The `TicketStatus` enum and the client `TicketStatus` type must be reconciled to use the same value (recommend aligning to `RESOLVED` on both sides, or mapping in the service layer).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide an authenticated `GET /api/ticket/employee` endpoint protected by `EMPLOYEE` role that returns all tickets assigned to the authenticated employee.
- **FR-002**: System MUST provide an authenticated `GET /api/ticket-threads/{ticketId}` endpoint that returns all thread messages for a given ticket in chronological order; access must be restricted to the employee assigned to the ticket or the customer who owns it.
- **FR-003**: System MUST allow both `EMPLOYEE` and `CUSTOMER` roles to post thread messages via `POST /api/ticket-threads/create`; the current `@PreAuthorize("hasRole('CUSTOMER')")` guard on that endpoint must be updated to `hasAnyRole('CUSTOMER', 'EMPLOYEE')`.
- **FR-004**: System MUST provide a new endpoint (e.g., `PATCH /api/ticket/{ticketId}/close`) protected by the `EMPLOYEE` role that transitions a ticket's status to `RESOLVED`; the endpoint must verify the ticket is assigned to the authenticated employee before applying the change.
- **FR-005**: System MUST reject close-ticket requests from customers or from employees not assigned to the given ticket with `403 Forbidden`.
- **FR-006**: Client MUST render an employee-specific tickets list view that consumes `GET /api/ticket/employee` and displays each ticket's subject, status badge, and creation date.
- **FR-007**: Client MUST render a ticket detail/thread view that displays all thread messages for the selected ticket and includes a text input plus submit button for posting a reply.
- **FR-008**: Client MUST send reply messages via `POST /api/ticket-threads/create` using the existing `TicketThreadService.createThreadMessage()` method and reflect the new message in the thread view upon success.
- **FR-009**: Client MUST provide a "Close Ticket" action on the ticket detail view that calls the close endpoint; the action must be hidden or disabled when the ticket status is already `RESOLVED`.
- **FR-010**: Client MUST handle error responses (4xx/5xx) from all ticket API calls gracefully, displaying a user-visible notification without crashing the view.
- **FR-011**: System MUST reconcile the `TicketStatus` enum discrepancy: the server enum uses `RESOLVED` while the client interface uses `CLOSED`; both must agree on the canonical value.
- **FR-012**: System MUST enforce JWT authentication on all ticket and ticket-thread endpoints; unauthenticated requests must receive `401 Unauthorized`.

### Key Entities *(include if feature involves data)*

- **Ticket**: Represents a customer support request. Key attributes: `ticketId`, `subject`, `body`, `status` (`OPEN` | `IN_PROGRESS` | `RESOLVED`), `createdAt`, linked `customerId`, linked `employeeId`. An employee can only manage tickets where `employeeId` matches their own ID.
- **TicketThread**: Represents a single message in the conversation on a ticket. Key attributes: `ticketThreadId`, `ticketId` (FK), `thread` (message body), `createdAt`. Both customers and employees contribute messages to the same thread.
- **Employee**: Represents a support staff member. Linked to tickets via `employeeId`. The authenticated employee's identity is resolved from the JWT via `RetrieveUser.getUser()`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An authenticated employee can load their ticket list and see all assigned tickets within 2 seconds for a typical data set (up to 50 tickets).
- **SC-002**: The system returns only tickets assigned to the authenticated employee in 100% of tested scenarios (no data leakage across employees).
- **SC-003**: An authenticated employee can post a reply to a ticket thread, and the message is visible in the thread on the next load in 100% of tested scenarios.
- **SC-004**: Calling the close-ticket endpoint as a `CUSTOMER` returns `403 Forbidden` in 100% of tested scenarios.
- **SC-005**: After an employee closes a ticket, its status is `RESOLVED` in the database and the client reflects this status without requiring a manual page refresh.
- **SC-006**: At least one automated integration test exists per new or modified endpoint covering: happy path, unauthorized (401), and forbidden (403) access scenarios.
- **SC-007**: The `TicketStatus` enum is consistent between the server (`RESOLVED`) and the client TypeScript type; no value mismatch errors appear in E2E or integration tests.
