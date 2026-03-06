# Tasks: Employee Support Ticket Management

**Branch**: `001-employee-support-tickets`  
**Input**: Design documents from `specs/001-employee-support-tickets/`  
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/ ✅ quickstart.md ✅

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no shared dependencies)
- **[Story]**: User story this task belongs to (US1 / US2 / US3)
- Exact file paths are included in every task description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: One-time corrections that unblock all three user stories.  
These are cross-cutting fixes that must land before any story work begins.

- [x] T001 Fix `TicketStatus` type in `client/src/app/interfaces/ticket.ts` — change `"CLOSED"` to `"RESOLVED"` (research Decision 1; data-model.md §TicketStatus Enum)
- [x] T002 Fix `@PreAuthorize` annotation on `POST /api/ticket-threads/create` in `server/src/main/java/com/revature/revastudio/controllers/TicketThreadController.java` — change `hasRole('CUSTOMER')` to `hasAnyRole('CUSTOMER', 'EMPLOYEE')` (research Decision 2; contracts §3)
- [x] T003 [P] Add `@PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")` to `GET /api/ticket-threads/{ticketId}` in `server/src/main/java/com/revature/revastudio/controllers/TicketThreadController.java` (research Decision 4; contracts §2)

**Checkpoint**: Enum is aligned, thread endpoints accept employee role. US1, US2, US3 work can now begin.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: No user-story-specific code — these are shared infrastructure tasks that all three stories depend on.

- [x] T004 Verify `employee` route guard is applied to the tickets route — confirm `path: 'ticket'` in `client/src/app/app.routes.ts` uses `employeeGuard` (or `authGuard` + role-check) so only employees reach the ticket page when navigating as an employee (plan.md §Source Code Changes; spec.md FR-012)
- [x] T005 [P] Confirm `RetrieveUser` utility is injected and available in `TicketController` — verify `server/src/main/java/com/revature/revastudio/util/RetrieveUser.java` is already wired (needed by T009 for close-ticket ownership check)

**Checkpoint**: Routing guard confirmed, `RetrieveUser` verified. All story phases can proceed.

---

## Phase 3: User Story 1 — View Assigned Tickets (Priority: P1) 🎯 MVP

**Goal**: Employee logs in, navigates to `/ticket`, and sees their assigned ticket list with subject, status badge, and creation date. Empty state shown when no tickets assigned.

**Independent Test** (quickstart.md Scenario 1 + Scenario 5 steps 1–3):  
`GET /api/ticket/employee` with a valid employee JWT returns an array of tickets. In the UI, the `/ticket` page renders each ticket card with subject, status, and date. No response or close functionality needed.

### Implementation for User Story 1

- [x] T006 [US1] Verify `TicketService.getAllTickets()` in `client/src/app/services/ticket-service.ts` calls `GET /api/ticket/employee` when role is `EMPLOYEE` — confirm branch logic is correct and no changes needed, or fix if role check is wrong (contracts §1)
- [x] T007 [US1] Update empty-state message in `client/src/app/components/ticket/ticket.html` — change `"No tickets yet."` to `"No tickets assigned."` to match spec.md US1 acceptance scenario 2
- [x] T008 [US1] Verify `client/src/app/app.routes.ts` — the `/ticket` route must use `employeeGuard` (not just `authGuard`) so customers cannot reach the employee ticket list; update `canActivate` if needed (spec.md FR-012; employee-guard.ts already exists)

**Checkpoint**: Employee can log in, navigate to `/ticket`, and see their ticket list or an empty state. US1 is independently testable.

---

## Phase 4: User Story 2 — Read and Respond to a Ticket Thread (Priority: P2)

**Goal**: Employee opens a ticket from the list, reads the full thread in the modal, types a reply, submits it, and sees the new message appear in the thread.

**Independent Test** (quickstart.md Scenarios 2 & 3):  
`GET /api/ticket-threads/{ticketId}` returns thread messages. `POST /api/ticket-threads/create` with an employee JWT returns `200 OK` with the new `TicketThreadDTO`. In the UI, the modal loads messages and "Send Message" submits successfully without a 403 error.

### Implementation for User Story 2

- [x] T009 [US2] Pass `ticketStatus` and `ticketSubject` into the modal data object in `client/src/app/components/ticket/ticket.ts` — update `openThread()` call to `this.dialog.open(TicketThreadModal, { data: { ticketId, ticketStatus: ticket.status, ticketSubject: ticket.subject }, width: '500px' })` (needed by US3 T013; contracts §Angular Client Service Contracts)
- [x] T010 [US2] Inject `AuthService` into `TicketThreadModal` in `client/src/app/components/ticket-thread-modal/ticket-thread-modal.ts` and expose `isEmployee = this.authService.getRole() === 'EMPLOYEE'` as a component property (research Decision 5; contracts §Angular)
- [x] T011 [US2] Update `MAT_DIALOG_DATA` type in `client/src/app/components/ticket-thread-modal/ticket-thread-modal.ts` — change `data: {ticketId: number}` to `data: {ticketId: number, ticketStatus: TicketStatus, ticketSubject: string}` and import `TicketStatus` from `../../interfaces/ticket` (data-model.md §TicketStatus; contracts §Angular)
- [x] T012 [US2] Add empty-body guard to `createThreadMessage()` in `client/src/app/components/ticket-thread-modal/ticket-thread-modal.ts` — show a user-visible error message (e.g., set an `errorMessage` string property rendered in the template) when `newThreadMessage.trim()` is empty, instead of silently returning (spec.md US2 acceptance scenario 4)
- [x] T012b [P] [US2] Add error message display to `client/src/app/components/ticket-thread-modal/ticket-thread-modal.html` — render `<p class="thread-error">{{ errorMessage }}</p>` below the textarea when `errorMessage` is set; clear it on successful send (spec.md FR-010)

**Checkpoint**: Employee can open any assigned ticket, read the thread, and post a reply. The new message is visible after posting. US2 is independently testable alongside US1.

---

## Phase 5: User Story 3 — Close a Ticket (Priority: P3)

**Goal**: Employee clicks "Close Ticket" in the ticket modal. The ticket's status changes to `RESOLVED` in the database and the ticket list updates immediately. Button is hidden when ticket is already `RESOLVED`.

**Independent Test** (quickstart.md Scenario 4):  
`PATCH /api/ticket/{ticketId}/close` with an employee JWT returns `200 OK` with the updated ticket showing `status: "RESOLVED"`. Returns `403` for a customer JWT. Returns `409` if already `RESOLVED`. In the UI, clicking "Close Ticket" closes the dialog and the ticket list refreshes showing the updated status.

### Implementation for User Story 3 — Backend

- [x] T013 [US3] Add `closeTicket(Integer ticketId, UUID employeeUserId)` method to `server/src/main/java/com/revature/revastudio/services/TicketService.java`:
  - Fetch ticket by `ticketId` — throw `NoSuchElementException` (→ 404) if not found
  - Verify `ticket.getEmployee().getUser().getId().equals(employeeUserId)` — throw `AccessDeniedException` (→ 403) if mismatch
  - If `ticket.getStatus() == TicketStatus.RESOLVED` — throw `IllegalStateException` (→ 409) if already resolved
  - Set `ticket.setStatus(TicketStatus.RESOLVED)`, save, return `TicketResponseDTO`
  (research Decision 3; contracts §4; data-model.md §State Machine)
- [x] T014 [US3] Add `PATCH /{ticketId}/close` endpoint to `server/src/main/java/com/revature/revastudio/controllers/TicketController.java`:
  - Annotate with `@PreAuthorize("hasRole('EMPLOYEE')")` and `@PatchMapping("{ticketId}/close")`
  - Call `retrieveUser.getUser()` to get `employeeUserId`, delegate to `ticketService.closeTicket(ticketId, employeeUserId)`
  - Return `ResponseEntity.ok(result)`
  (contracts §4; plan.md §Source Code Changes)
- [x] T015 [US3] Add exception mappings to `server/src/main/java/com/revature/revastudio/exception/GlobalExceptionHandler.java` (or inline `@ExceptionHandler` in `TicketController` if simpler) for:
  - `NoSuchElementException` → `404 Not Found`
  - `AccessDeniedException` → `403 Forbidden`
  - `IllegalStateException` → `409 Conflict`
  (contracts §4 error codes)

### Implementation for User Story 3 — Frontend

- [x] T016 [P] [US3] Add `closeTicket(ticketId: number): Observable<TicketResponse>` method to `client/src/app/services/ticket-service.ts` — call `this.http.patch<TicketResponse>(\`${API_BASE_URL}/ticket/${ticketId}/close\`, {})` (contracts §Angular Client Service Contracts)
- [x] T017 [US3] Add `closeTicket()` method to `client/src/app/components/ticket-thread-modal/ticket-thread-modal.ts`:
  - Inject `TicketService`
  - On click: call `ticketService.closeTicket(data.ticketId)`, on success close dialog with result `{ closed: true }`, on error set `errorMessage`
  (research Decision 5; contracts §Angular)
- [x] T018 [US3] Add "Close Ticket" button to `client/src/app/components/ticket-thread-modal/ticket-thread-modal.html`:
  - Render only when `isEmployee && data.ticketStatus !== 'RESOLVED'`
  - Button: `<button type="button" class="close-ticket-btn" (click)="closeTicket()">Close Ticket</button>`
  (spec.md FR-009; contracts §Angular)
- [x] T019 [US3] Handle dialog close result in `client/src/app/components/ticket/ticket.ts` — subscribe to `dialogRef.afterClosed()`, if result is `{ closed: true }` refresh `allTickets` by re-calling `this.ticketService.getAllTickets()` so the status update appears immediately in the list (spec.md SC-005)

**Checkpoint**: Employee can close any assigned open ticket from the modal. Status shows `RESOLVED` in the list immediately. Close button is hidden for already-resolved tickets. US3 is independently testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Error handling, UX consistency, and observability across all user stories.

- [x] T020 [P] Add API error handling to `client/src/app/components/ticket/ticket.ts` — in `ngOnInit`, handle errors from `getAllTickets()` observable by setting a component-level `errorMessage` string and rendering it in `ticket.html` (spec.md FR-010)
- [x] T021 [P] Add loading state to `client/src/app/components/ticket/ticket.html` — show a Material spinner or "Loading..." text while the `allTickets` observable is pending (UX — spec.md SC-001)
- [x] T022 [P] Add modal header title to `client/src/app/components/ticket-thread-modal/ticket-thread-modal.html` — display `data.ticketSubject` in the `<h2>Ticket thread</h2>` so employees know which ticket they have open (set in T009)
- [x] T023 Remove stray `ç` character from `client/src/app/components/ticket-thread-modal/ticket-thread-modal.html` line 15 — typo inside the `</ul>` block

---

## Dependencies

```
T001 (enum fix) ──────────────────────────────► T006, T011, T016, T018
T002 (auth guard fix) ────────────────────────► T012 (US2 posting works)
T003 (thread read guard) ─────────────────────► T010 (modal loads threads)
T004, T005 (foundation) ──────────────────────► T006–T023

T006, T007, T008 (US1) ──────────────────────► T009 (pass status to modal)
T009 ─────────────────────────────────────────► T011, T018, T022
T010, T011 (modal setup) ─────────────────────► T012, T012b, T017, T018
T013, T014, T015 (backend close) ────────────► T016, T017, T018, T019
```

**Story completion order**: US1 → US2 → US3 (each is independently deployable)

## Parallel Execution Opportunities

| Group | Tasks | Why Safe to Parallelize |
|-------|-------|------------------------|
| Setup fixes | T001, T002, T003 | Different files — enum type, controller annotation ×2 |
| US1 client | T006, T007, T008 | All different files/concerns in client |
| US2 backend (already done) + US2 client | T010, T011, T012b, T016 | No shared dependencies once T001–T005 complete |
| US3 backend + US3 frontend service | T013+T014+T015 alongside T016 | Backend and frontend service are independent |
| Polish | T020, T021, T022, T023 | All different files/lines |

## Implementation Strategy

**MVP = Phase 1 + Phase 2 + Phase 3 (US1 only)**  
After T001–T008, an employee can log in, see their ticket list, and the status values render correctly. Shippable without US2 or US3.

**Next increment = + Phase 4 (US2)**  
Adds thread reading and replying. Requires T002 (auth fix) which is in Phase 1.

**Full feature = + Phase 5 (US3)**  
Adds close-ticket. Requires the new backend endpoint (T013–T015) and frontend wiring (T016–T019).

**Total tasks: 23**  
- Phase 1 (Setup): 3 tasks  
- Phase 2 (Foundation): 2 tasks  
- Phase 3 (US1): 3 tasks  
- Phase 4 (US2): 5 tasks  
- Phase 5 (US3): 7 tasks  
- Phase 6 (Polish): 4 tasks  
