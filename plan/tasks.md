# Plan / Tasks — Support Ticket Management System

- **Stage:** Plan / Tasks (step 3 of the SDD pipeline)
- **Derived from:** `spec/*`. Each task is small, ordered, and tagged with the requirements it satisfies.
- **Rule:** implement top-to-bottom; do not batch "build everything." Each task → implement → test → review → fix.

---

## Milestone 0 — Project Scaffolding & Hygiene

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 0.1 | Create repo layout: `backend/`, `frontend/`, keep `spec/`, `rules/`, `skills/`, `commands/`, `docs/`. | — | Folders exist. |
| 0.2 | Root `.gitignore` (target/, node_modules/, dist/, *.db, .env, .idea/, .vscode/). | NFR-4 | No build/secret files trackable. |
| 0.3 | Spring Boot 3.x project (Java 21) with deps: web, data-jpa, validation, h2, test. | Tech | `./mvnw -q -DskipTests package` compiles. |
| 0.4 | `application.yml`: H2 file mode, JPA ddl-auto=update, CORS origin via env. | NFR-1 | App boots on 8080. |
| 0.5 | Vite React app in `frontend/` with React Router. | Tech | `npm run dev` serves on 5173. |
| 0.6 | Confirm SpecStory / prompt-history capture set up (`.specstory/history/`). | Process | Prompts recorded. |

---

## Milestone 1 — Domain & Persistence

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 1.1 | Enums `TicketStatus`, `Priority`. | data-model | Values match spec. |
| 1.2 | `Ticket` entity (fields, enums as STRING, timestamps via lifecycle callbacks). | FR-1,13 | Table generated. |
| 1.3 | `Comment` entity + `@ManyToOne` to ticket; `@OneToMany` on ticket, ordered. | FR-8 | FK + cascade work. |
| 1.4 | `TicketRepository`, `CommentRepository` (Spring Data). | FR-2,3 | Basic CRUD works. |
| 1.5 | Search + filter query methods (case-insensitive title/desc; by status). | FR-9,10 | `@DataJpaTest` passes. |

---

## Milestone 2 — DTOs, Validation, Error Handling

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 2.1 | DTO records: Create/Update/ChangeStatus/CreateComment requests; Summary/Detail/Comment responses. | api-contract | Compile. |
| 2.2 | Bean Validation annotations on request DTOs. | FR-14 | Invalid input rejected. |
| 2.3 | `ApiError` + `GlobalExceptionHandler` (@RestControllerAdvice) → uniform body; map 400/404/409. | NFR-3, FR-15 | Errors match contract shape. |
| 2.4 | `NotFoundException`, `InvalidStatusTransitionException`. | FR-3,8,12 | Mapped to 404/409. |

---

## Milestone 3 — State Machine (core, highest scrutiny)

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 3.1 | `StatusTransitionValidator` with immutable ALLOWED map; `assertCanTransition`. | FR-11,12 | Matches `state-machine.md` matrix exactly. |
| 3.2 | Unit tests for validator (all allowed pass, representative rejects throw). | NFR-5 | Green. |

---

## Milestone 4 — Services

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 4.1 | `TicketService`: create (defaults OPEN/MEDIUM), list (all/search/filter), getDetail, updateFields. | FR-1,2,3,4-7,9,10 | Logic + mapping done. |
| 4.2 | `TicketService.changeStatus` → uses validator, saves, returns detail. | FR-11,12 | Invalid → exception. |
| 4.3 | `CommentService.addComment` (ticket exists check, default author). | FR-8 | 404 if missing ticket. |
| 4.4 | Mappers entity↔DTO (no entity leakage). | api-standards | Only DTOs cross boundary. |

---

## Milestone 5 — Controllers (REST API)

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 5.1 | `TicketController`: POST create, GET list (q,status), GET detail, PATCH fields. | FR-1,2,3,4-7,9,10 | Codes 201/200/404/400. |
| 5.2 | `TicketController` PATCH `/status`. | FR-11,12 | 200 / 409 / 404. |
| 5.3 | `CommentController`: POST comment. | FR-8 | 201 / 404 / 400. |
| 5.4 | `CorsConfig` for frontend origin. | api-standards | Frontend can call API. |

---

## Milestone 6 — Backend Tests (Testing stage)

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 6.1 | `StatusTransitionIntegrationTest` (parameterized allowed + rejected). | NFR-5, FR-11,12 | All pass; status unchanged on reject. |
| 6.2 | Validation tests (blank/oversized/bad enum) → 400 + fieldErrors. | FR-14 | Pass. |
| 6.3 | Endpoint tests: create defaults, list newest-first, search, filter, detail 404, comment. | FR-1,2,3,8,9,10 | Pass. |
| 6.4 | Persistence-survives-restart check (automated or documented manual). | NFR-1 | Verified. |

---

## Milestone 7 — Frontend

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 7.1 | `api/client.js`: fetch wrapper, base URL, normalize uniform error → `ApiError`. | FR-15 | Errors parsed. |
| 7.2 | Ticket List page: load, search box (debounced), status filter, badges, row nav. | FR-2,9,10 | Works against API. |
| 7.3 | Create Ticket page: form + inline errors mapped from backend 400. | FR-1,15 | Creates + shows errors. |
| 7.4 | Ticket Detail page: view + edit fields (PATCH), assignee change. | FR-3,4-7 | Saves, shows errors. |
| 7.5 | `StatusActions`: render only allowed transitions; call `/status`; show 409 in banner. | FR-11,12,15 | Correct actions per status. |
| 7.6 | Comments: list + add form. | FR-8 | Appends on success. |
| 7.7 | `ErrorBanner` + inline field errors component. | FR-15 | Human-readable messages. |

---

## Milestone 8 — Review, Fix, Docs

| # | Task | Traces | Done when |
|---|------|--------|-----------|
| 8.1 | Run `commands/review-spec.md` pass over specs; resolve findings. | Process | Spec READY. |
| 8.2 | Run `commands/review-code.md` over backend + frontend; fix findings. | Process | APPROVE. |
| 8.3 | Log AI mistakes caught in `docs/ai-mistakes.md` (≥ a few meaningful ones). | Assignment | Documented + corrected. |
| 8.4 | Root `README.md` per documentation skill. | Docs | Run/test instructions accurate. |
| 8.5 | Security pass: no secrets committed; `.gitignore` verified. | NFR-4 | Clean. |
| 8.6 | Final acceptance-criteria walkthrough (all 15 boxes). | All | All checked. |

---

## Execution Order (critical path)
```
M0 → M1 → M2 → M3 → M4 → M5 → M6 → M7 → M8
             (state machine M3 gated before services that use it)
```

## Requirement Coverage Check
Every `FR-1..15` and `NFR-1..7` appears in at least one task above. `review-spec` verifies this mapping before implementation starts.
