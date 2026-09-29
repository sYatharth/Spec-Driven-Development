# Requirements — Support Ticket Management System

- **Status:** Draft v1
- **Date:** 2026-09-25
- **Author:** (you) with AI assistant
- **Stage in SDD pipeline:** Requirement (step 1 of Requirement → Specification → Plan → Implementation → Testing → Review → Fix)

---

## 1. Purpose & Scope

Build a **Support Ticket Management System** that lets a support team create, track, update, and resolve customer support tickets through a well-defined lifecycle. The system is a full-stack web application with a REST backend and a browser UI.

**In scope**
- Ticket CRUD (create, list, view, update fields).
- Commenting on tickets.
- Keyword search and status filtering.
- Enforced ticket status lifecycle (state machine) on the backend.
- Backend input validation and meaningful UI error display.
- Persistent storage that survives application restart.

**Out of scope (v1)**
- Authentication / authorization / multi-tenancy.
- File attachments on tickets/comments.
- Email/SLA notifications, reporting dashboards.
- Pagination beyond a simple limit (may be added later).
- Soft-delete / audit history of field changes (only status transitions are constrained).

---

## 2. Actors

| Actor | Description |
|-------|-------------|
| **Support Agent** | Primary user. Creates tickets, updates fields, changes status, assigns tickets, comments. |
| **System (Backend)** | Enforces validation and the status state machine; persists data. |

> v1 has no login; all users act as a single implicit "agent". Assignee is a free-text/identifier field, not a managed user account.

---

## 3. Functional Requirements

Each requirement has an ID (`FR-n`) and a testable acceptance criterion. IDs are referenced by the spec, tasks, and tests for traceability.

| ID | Requirement | Acceptance Criterion |
|----|-------------|----------------------|
| **FR-1** | Create a ticket | Given valid title + description (+ optional priority/assignee), a ticket is persisted with status `OPEN` and a generated id, createdAt, updatedAt. Returns `201`. |
| **FR-2** | List tickets | Returns all tickets (newest first), each with id, title, status, priority, assignee, updatedAt. Returns `200`. |
| **FR-3** | View ticket details | Given a valid id, returns the full ticket including description and its comments. Unknown id → `404`. |
| **FR-4** | Update title | Title can be changed; blank/too-long title rejected with `400`. |
| **FR-5** | Update description | Description can be changed; validation applied. |
| **FR-6** | Update priority | Priority can be set to one of `LOW`, `MEDIUM`, `HIGH`, `URGENT`. Invalid value → `400`. |
| **FR-7** | Update assignee | Assignee (string identifier) can be set or cleared. |
| **FR-8** | Add comment | A comment with non-blank body can be added to an existing ticket; returns the created comment. Ticket must exist (`404` otherwise). |
| **FR-9** | Search tickets by keyword | Case-insensitive match against title and description; returns matching tickets. Empty keyword → returns all (or `400`, see Open Questions). |
| **FR-10** | Filter tickets by status | Returns only tickets with the given status. Invalid status → `400`. |
| **FR-11** | Change status (state machine) | Status changes only along allowed transitions; the transition is validated on the backend. |
| **FR-12** | Reject invalid transitions | Disallowed transitions return `409 Conflict` (or `422`) with a clear message; state is unchanged. |
| **FR-13** | Persist data | All tickets/comments survive an application restart (durable DB, not in-memory only). |
| **FR-14** | Backend validation | All create/update inputs validated server-side regardless of client. Violations → `400` with field-level detail. |
| **FR-15** | Meaningful UI errors | UI surfaces backend validation and transition errors as human-readable messages, not raw stack traces. |

### 3.1 Combined query (nice-to-have)
Search + status filter may be combinable (`?status=OPEN&q=login`). v1 minimum: each works independently.

---

## 4. Ticket Status State Machine (authoritative)

Statuses: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`.

**Allowed transitions**
```
OPEN         → IN_PROGRESS
IN_PROGRESS  → RESOLVED
RESOLVED     → CLOSED
OPEN         → CANCELLED
IN_PROGRESS  → CANCELLED
```

**Terminal states:** `CLOSED`, `CANCELLED` (no outgoing transitions).

**Explicitly rejected examples (non-exhaustive):**
```
CLOSED    → OPEN        ❌
RESOLVED  → OPEN        ❌
CANCELLED → OPEN        ❌
OPEN      → RESOLVED    ❌  (must pass through IN_PROGRESS)
OPEN      → CLOSED      ❌
```

Full transition table lives in `spec/state-machine.md`. The backend is the single source of truth; the UI may hide invalid actions but must not be relied upon for enforcement.

> **Design note / assumption:** The requirement lists forward transitions only. We treat the lifecycle as **forward-only** (no re-open, no backward moves). Any backward transition is rejected. This is an explicit decision — see Open Questions Q1.

---

## 5. Data Requirements

**Ticket**
- `id` (generated, immutable)
- `title` — required, 1–150 chars
- `description` — required, 1–5000 chars
- `status` — enum, defaults to `OPEN`
- `priority` — enum (`LOW|MEDIUM|HIGH|URGENT`), defaults to `MEDIUM`
- `assignee` — optional string, ≤100 chars
- `createdAt`, `updatedAt` — timestamps, server-managed

**Comment**
- `id` (generated)
- `ticketId` (FK)
- `author` — optional string (defaults to `"agent"` in v1), ≤100 chars
- `body` — required, 1–2000 chars
- `createdAt` — timestamp

Detail in `spec/data-model.md`.

---

## 6. Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| **NFR-1 (Persistence)** | Data stored in a relational DB (H2 file-mode for dev, PostgreSQL-compatible schema). Survives restart. |
| **NFR-2 (Validation)** | Bean Validation (`jakarta.validation`) on DTOs; centralized error handling. |
| **NFR-3 (Error contract)** | Consistent JSON error shape: `{ timestamp, status, error, message, fieldErrors[] }`. |
| **NFR-4 (Security hygiene)** | No secrets committed. Config via env vars / `.env` (git-ignored). `.gitignore` covers build output, IDE, env. |
| **NFR-5 (Testability)** | State-machine transitions covered by integration tests (all valid + representative invalid). |
| **NFR-6 (Portability)** | Runs locally with a single documented command per side (backend/frontend). |
| **NFR-7 (Observability)** | Meaningful HTTP status codes and log messages for failures. |

---

## 7. Core Acceptance Criteria (assignment checklist)

The solution is complete when:

- [ ] Ticket can be created from UI — *FR-1*
- [ ] Tickets can be listed — *FR-2*
- [ ] Ticket details can be viewed — *FR-3*
- [ ] Ticket fields can be updated — *FR-4..6*
- [ ] Assignee can be changed — *FR-7*
- [ ] Comments can be added — *FR-8*
- [ ] Search works — *FR-9*
- [ ] Status filter works — *FR-10*
- [ ] Valid status transitions work — *FR-11*
- [ ] Invalid status transitions are rejected by backend — *FR-12*
- [ ] Data survives application restart — *FR-13 / NFR-1*
- [ ] Backend validation works — *FR-14 / NFR-2*
- [ ] UI shows meaningful errors — *FR-15 / NFR-3*
- [ ] State-machine integration tests pass — *NFR-5*
- [ ] No secrets are committed — *NFR-4*

---

## 8. Technology Constraints (from assignment)

- **Language:** Java 21
- **Backend:** Spring Boot (3.x)
- **DB:** PostgreSQL or H2 → **decision: H2 in file mode** for dev (zero external setup, durable, PostgreSQL-compatible SQL). Swappable to Postgres via config.
- **API:** REST (JSON)
- **Frontend:** React (Vite) — "React/Next.js or equivalent"
- **Tooling:** Cursor / GitHub Copilot with Spec-Driven Development

---

## 9. Open Questions & Assumptions

| # | Question | Assumption taken (v1) |
|---|----------|------------------------|
| Q1 | Can a ticket be re-opened (e.g. `RESOLVED → IN_PROGRESS`)? | **No.** Lifecycle is forward-only per the given diagram. Documented as a deliberate constraint. |
| Q2 | Should search on empty keyword error or return all? | Return all tickets (treat empty `q` as "no filter"). |
| Q3 | Is `author` on comments user-provided? | Optional; defaults to `"agent"` since there is no auth in v1. |
| Q4 | Is status changed via the generic update endpoint or a dedicated one? | **Dedicated** `PATCH /tickets/{id}/status` so transition rules are isolated and testable; field updates use `PATCH /tickets/{id}`. |
| Q5 | Pagination? | Out of scope v1; return full list (optionally capped). |

---

## 10. Traceability

Requirement IDs (`FR-*`, `NFR-*`) flow through:
`spec/*` → `plan/tasks.md` → code (referenced in commit messages / comments) → tests (named per FR). This lets a reviewer confirm every requirement is designed, built, and tested.
