# Architecture — Support Ticket Management System

- **Stage:** Specification (step 2)
- **Traces:** all FR/NFR in `requirements.md`

---

## 1. System Overview

A classic two-tier full-stack app:

```
┌────────────────────┐        HTTP/JSON (REST)        ┌───────────────────────────┐
│  React (Vite) SPA   │  ───────────────────────────▶ │   Spring Boot REST API     │
│  - Ticket list/view │  ◀─────────────────────────── │   (Java 21)                │
│  - Forms + errors   │        JSON responses          │                            │
└────────────────────┘                                 │   Controller → Service →   │
                                                        │   Repository (JPA)         │
                                                        └────────────┬──────────────┘
                                                                     │ JDBC
                                                              ┌──────▼───────┐
                                                              │  H2 (file)   │
                                                              │  ~/data/*.db │
                                                              └──────────────┘
```

- Frontend and backend are separate deployables; the frontend calls the API base URL (configurable, default `http://localhost:8080/api`).
- CORS enabled for the dev frontend origin only.

---

## 2. Backend Layered Architecture

```
com.support.tickets
├── TicketApplication.java          // Spring Boot entry point
├── config/
│   ├── CorsConfig.java             // allow frontend origin
│   └── (OpenApi/DataInit if used)
├── controller/
│   ├── TicketController.java       // /api/tickets ...
│   └── CommentController.java      // /api/tickets/{id}/comments
├── service/
│   ├── TicketService.java          // business logic
│   ├── CommentService.java
│   └── StatusTransitionValidator.java  // state machine (single source of truth)
├── domain/                         // JPA entities + enums
│   ├── Ticket.java
│   ├── Comment.java
│   ├── TicketStatus.java           // enum
│   └── Priority.java               // enum
├── repository/                     // Spring Data JPA
│   ├── TicketRepository.java
│   └── CommentRepository.java
├── dto/                            // request/response records
│   ├── CreateTicketRequest.java
│   ├── UpdateTicketRequest.java
│   ├── ChangeStatusRequest.java
│   ├── CreateCommentRequest.java
│   ├── TicketSummaryResponse.java
│   ├── TicketDetailResponse.java
│   └── CommentResponse.java
└── error/
    ├── GlobalExceptionHandler.java // @RestControllerAdvice → NFR-3 error shape
    ├── ApiError.java
    ├── NotFoundException.java
    └── InvalidStatusTransitionException.java
```

**Responsibilities**
- **Controller** — HTTP concerns only: bind/validate DTOs, map to/from service, choose status codes.
- **Service** — business rules, transactions, orchestration. Owns status-transition enforcement.
- **Repository** — persistence via Spring Data JPA.
- **DTOs** — never expose entities directly; keeps API contract stable (API standard).
- **Error layer** — one `@RestControllerAdvice` produces the uniform error body.

---

## 3. Frontend Architecture (React + Vite)

```
src/
├── main.jsx / App.jsx
├── api/client.js            // fetch wrapper, base URL, error normalization
├── pages/
│   ├── TicketListPage.jsx   // list + search + status filter (FR-2,9,10)
│   ├── TicketDetailPage.jsx // view + edit fields + status + comments
│   └── CreateTicketPage.jsx // create form (FR-1)
├── components/
│   ├── TicketForm.jsx
│   ├── StatusBadge.jsx
│   ├── StatusActions.jsx    // only shows allowed transitions
│   ├── CommentList.jsx / CommentForm.jsx
│   └── ErrorBanner.jsx      // renders backend messages (FR-15)
└── lib/statusMachine.js     // client mirror of allowed transitions (UX only)
```

- **State:** local component state + a small data layer; no heavy state library needed for v1.
- **Routing:** React Router (`/`, `/tickets/new`, `/tickets/:id`).
- **Error handling:** `api/client.js` parses the standard error JSON and throws a normalized error consumed by `ErrorBanner`.

---

## 4. Key Cross-Cutting Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| DB | H2 file mode (`jdbc:h2:file:./data/tickets`) | Durable across restart (NFR-1) with zero setup; PostgreSQL-compatible mode so it can be swapped. |
| Status change endpoint | Dedicated `PATCH /tickets/{id}/status` | Isolates state-machine rules → cleanly testable (NFR-5). |
| Validation | Bean Validation on DTO records | Declarative, server-side (NFR-2). |
| Error shape | Single `@RestControllerAdvice` | Consistent contract (NFR-3), meaningful UI errors (FR-15). |
| Entity exposure | DTOs only | Stable API, no lazy-loading leaks. |
| IDs | `Long` auto-increment (or UUID) | Simple; UUID optional. Decision: **Long** for v1. |
| Timestamps | `Instant`, server-set via `@PrePersist/@PreUpdate` | Client cannot forge; consistent ordering. |

---

## 5. Request Lifecycle (status change example)

```
PATCH /api/tickets/42/status  { "status": "RESOLVED" }
   │
   ▼
TicketController.changeStatus()
   │  (validate body shape)
   ▼
TicketService.changeStatus(42, RESOLVED)
   │  load ticket (404 if missing)
   │  StatusTransitionValidator.assertCanTransition(current, target)
   │      └─ throws InvalidStatusTransitionException (→ 409) if not allowed
   │  ticket.setStatus(target); save
   ▼
TicketDetailResponse (200)
```

Invalid transition → `GlobalExceptionHandler` → `409` with body:
```json
{ "status":409, "error":"Conflict",
  "message":"Cannot transition ticket from RESOLVED to OPEN" }
```

---

## 6. Environments & Config

| Concern | Dev default | Override |
|---------|-------------|----------|
| DB URL | `jdbc:h2:file:./data/tickets` | `SPRING_DATASOURCE_URL` env |
| Frontend origin (CORS) | `http://localhost:5173` | `APP_CORS_ORIGIN` env |
| API base (frontend) | `http://localhost:8080/api` | `VITE_API_BASE` env |
| Secrets | none required in v1 | env vars only; never committed (NFR-4) |

---

## 7. Build & Run (summary; detail in README later)

- **Backend:** `./mvnw spring-boot:run` (port 8080)
- **Frontend:** `npm install && npm run dev` (port 5173)
- **Tests:** `./mvnw test` (includes state-machine integration tests)
