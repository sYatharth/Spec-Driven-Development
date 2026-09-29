# API Contract — Support Ticket Management System

- **Stage:** Specification (step 2)
- **Base URL:** `http://localhost:8080/api`
- **Content-Type:** `application/json`
- **Traces:** FR-1..15, NFR-3

---

## 1. Conventions

- All request/response bodies are JSON.
- Timestamps are ISO-8601 UTC (`2026-09-25T10:15:30Z`).
- Errors follow the uniform shape in §9.
- Status codes: `200` OK, `201` Created, `400` Bad Request (validation), `404` Not Found, `409` Conflict (invalid state transition).

---

## 2. Resource: Ticket

### 2.1 Create ticket — `POST /api/tickets`  (FR-1)
**Request**
```json
{
  "title": "Login button unresponsive",
  "description": "Clicking login does nothing on Safari 17.",
  "priority": "HIGH",          // optional, default MEDIUM
  "assignee": "alice"          // optional
}
```
**Response `201 Created`**
```json
{
  "id": 1,
  "title": "Login button unresponsive",
  "description": "Clicking login does nothing on Safari 17.",
  "status": "OPEN",
  "priority": "HIGH",
  "assignee": "alice",
  "createdAt": "2026-09-25T10:15:30Z",
  "updatedAt": "2026-09-25T10:15:30Z",
  "comments": []
}
```
Errors: `400` if title/description blank or too long, or priority invalid.

---

### 2.2 List tickets — `GET /api/tickets`  (FR-2, FR-9, FR-10)
**Query params (all optional, combinable):**
| Param | Meaning |
|-------|---------|
| `q` | keyword; case-insensitive match on title OR description (FR-9) |
| `status` | one of the enum values; filters by status (FR-10) |

**Examples**
- `GET /api/tickets` → all, newest first
- `GET /api/tickets?status=OPEN`
- `GET /api/tickets?q=login`
- `GET /api/tickets?status=IN_PROGRESS&q=safari`

**Response `200`**
```json
[
  {
    "id": 1,
    "title": "Login button unresponsive",
    "status": "OPEN",
    "priority": "HIGH",
    "assignee": "alice",
    "updatedAt": "2026-09-25T10:15:30Z"
  }
]
```
Errors: `400` if `status` is not a valid enum value.
Note: empty `q` is treated as "no keyword filter" (returns all) — Requirements Q2.

---

### 2.3 Get ticket detail — `GET /api/tickets/{id}`  (FR-3)
**Response `200`** — full ticket incl. `description` and `comments[]` (see §3.2).
Errors: `404` if id unknown.

---

### 2.4 Update ticket fields — `PATCH /api/tickets/{id}`  (FR-4,5,6,7)
Partial update; only provided fields change. **Does not change status.**
**Request (any subset)**
```json
{
  "title": "Login button unresponsive on Safari",
  "description": "Updated repro steps...",
  "priority": "URGENT",
  "assignee": "bob"        // null or "" clears assignee
}
```
**Response `200`** — updated `TicketDetailResponse`.
Errors: `404` unknown id; `400` validation (blank title, oversized fields, invalid priority).

> Status is intentionally **not** updatable here — use §2.5 so state-machine rules stay isolated (Requirements Q4).

---

### 2.5 Change status — `PATCH /api/tickets/{id}/status`  (FR-11, FR-12)
**Request**
```json
{ "status": "IN_PROGRESS" }
```
**Response `200`** — updated `TicketDetailResponse` with new status.

**Errors**
- `404` unknown id.
- `400` if `status` missing/not a valid enum value.
- `409 Conflict` if the transition is not allowed by the state machine:
```json
{
  "timestamp": "2026-09-25T10:20:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot transition ticket from RESOLVED to OPEN",
  "fieldErrors": []
}
```

Allowed transitions (authoritative in `state-machine.md`):
`OPEN→IN_PROGRESS`, `IN_PROGRESS→RESOLVED`, `RESOLVED→CLOSED`, `OPEN→CANCELLED`, `IN_PROGRESS→CANCELLED`.

---

## 3. Resource: Comment

### 3.1 Add comment — `POST /api/tickets/{id}/comments`  (FR-8)
**Request**
```json
{ "body": "Reproduced on Safari 17.1, investigating.", "author": "alice" }
```
`author` optional (defaults to `"agent"`).
**Response `201`**
```json
{
  "id": 10,
  "ticketId": 1,
  "author": "alice",
  "body": "Reproduced on Safari 17.1, investigating.",
  "createdAt": "2026-09-25T10:25:00Z"
}
```
Errors: `404` if ticket missing; `400` if body blank/too long.

### 3.2 Comments in ticket detail
`GET /api/tickets/{id}` includes:
```json
"comments": [
  { "id": 10, "ticketId": 1, "author": "alice", "body": "...", "createdAt": "..." }
]
```
Ordered by `createdAt` ascending.

---

## 4. Endpoint Summary

| Method | Path | Purpose | Success | Key errors |
|--------|------|---------|---------|-----------|
| POST | `/api/tickets` | create | 201 | 400 |
| GET | `/api/tickets` | list/search/filter | 200 | 400 (bad status) |
| GET | `/api/tickets/{id}` | detail | 200 | 404 |
| PATCH | `/api/tickets/{id}` | update fields | 200 | 400, 404 |
| PATCH | `/api/tickets/{id}/status` | change status | 200 | 400, 404, 409 |
| POST | `/api/tickets/{id}/comments` | add comment | 201 | 400, 404 |

---

## 5. DTO Field Reference

**CreateTicketRequest**: `title*`, `description*`, `priority?`, `assignee?`
**UpdateTicketRequest**: `title?`, `description?`, `priority?`, `assignee?` (all optional; at least one recommended)
**ChangeStatusRequest**: `status*`
**CreateCommentRequest**: `body*`, `author?`
**TicketSummaryResponse**: `id, title, status, priority, assignee, updatedAt`
**TicketDetailResponse**: `id, title, description, status, priority, assignee, createdAt, updatedAt, comments[]`
**CommentResponse**: `id, ticketId, author, body, createdAt`

`*` required, `?` optional.

---

## 6. Validation Rules (server-side, FR-14)

| Field | Rule | On violation |
|-------|------|--------------|
| title | not blank, ≤150 | 400 + fieldError |
| description | not blank, ≤5000 | 400 + fieldError |
| priority | must be enum value | 400 |
| assignee | ≤100 | 400 |
| comment.body | not blank, ≤2000 | 400 |
| status (change) | valid enum + allowed transition | 400 / 409 |

---

## 7. CORS
Dev: allow origin `http://localhost:5173`, methods `GET,POST,PATCH,OPTIONS`, headers `Content-Type`.

---

## 8. Non-goals
No auth headers, pagination, sorting params (beyond default newest-first), or bulk endpoints in v1.

---

## 9. Uniform Error Shape (NFR-3, FR-15)
```json
{
  "timestamp": "2026-09-25T10:20:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "title", "message": "must not be blank" }
  ]
}
```
`fieldErrors` is `[]` for non-validation errors (404/409). The UI's `ErrorBanner` renders `message` and lists `fieldErrors`.
