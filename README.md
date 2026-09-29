# Support Ticket Management System

A full-stack support ticket app built with **Spec-Driven Development (SDD)** using AI
assistants (Cursor / GitHub Copilot). The application matters, but the primary artefact is
**how it was built**: requirements → specification → plan → implementation → testing → review → fix.

> New here? Start with [`spec/requirements.md`](spec/requirements.md) and
> [`docs/prompt-history.md`](docs/prompt-history.md).

---

## Tech Stack
- **Backend:** Java 21, Spring Boot 3.3, Spring Data JPA, Bean Validation
- **Database:** H2 in **file mode** (durable across restarts; PostgreSQL-compatible SQL, swappable to Postgres via env)
- **Frontend:** React 18 + Vite, React Router
- **API:** REST / JSON

## Prerequisites
- **JDK 21** (required — Spring Boot 3 will not run on older JDKs)
- **Maven** (or use the `mvnw` wrapper if generated) — `brew install maven`
- **Node 18+** and npm

---

## Run the Backend
```bash
cd backend
mvn spring-boot:run          # starts on http://localhost:8080
```
- API base: `http://localhost:8080/api`
- H2 console (dev): `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/tickets`)
- Data is written to `backend/data/tickets.mv.db` → **survives restart**.

## Run the Frontend
```bash
cd frontend
cp .env.example .env          # optional; defaults to localhost:8080
npm install
npm run dev                   # starts on http://localhost:5173
```

## Run the Tests
```bash
cd backend
mvn test                                          # all backend tests
mvn test -Dtest=StatusTransitionIntegrationTest   # state-machine tests only (NFR-5)
```
```bash
cd frontend
npm run build                 # verifies the frontend compiles
```

---

## API Summary
Full contract: [`spec/api-contract.md`](spec/api-contract.md).

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/tickets` | create |
| GET | `/api/tickets?q=&status=` | list / search / filter |
| GET | `/api/tickets/{id}` | detail (with comments) |
| PATCH | `/api/tickets/{id}` | update fields (not status) |
| PATCH | `/api/tickets/{id}/status` | change status (state machine) |
| POST | `/api/tickets/{id}/comments` | add comment |

All errors use a uniform JSON body (`timestamp, status, error, message, fieldErrors[]`).

## State Machine
Enforced by the backend (authoritative: [`spec/state-machine.md`](spec/state-machine.md)):
```
OPEN → IN_PROGRESS → RESOLVED → CLOSED
OPEN → CANCELLED
IN_PROGRESS → CANCELLED
```
Forward-only: `CLOSED` and `CANCELLED` are terminal. Invalid transitions (e.g. `CLOSED→OPEN`,
`RESOLVED→OPEN`) return **409 Conflict** and leave the ticket unchanged.

---

## Configuration & Secrets
No secrets are committed (NFR-4). All config has safe defaults and is overridable via env:

| Env var | Purpose | Default |
|---------|---------|---------|
| `SPRING_DATASOURCE_URL` | DB URL (use for PostgreSQL) | `jdbc:h2:file:./data/tickets` |
| `APP_CORS_ORIGIN` | allowed frontend origin | `http://localhost:5173` |
| `VITE_API_BASE` | frontend → API base URL | `http://localhost:8080/api` |

`.gitignore` excludes `*.db`, `data/`, `node_modules/`, `dist/`, `target/`, and `.env`.

> `npm audit` reports advisories in the Vite/esbuild **dev** toolchain (build-time only, not shipped
> in the app). Left as-is to avoid breaking upgrades; revisit before any production deploy.

---

## Project Structure
```
spec/           Specification (requirements, architecture, data-model, api-contract,
                state-machine, ui-flow, test-strategy)
rules/          Steering: java-springboot, testing, api-standards guidelines (reusable AI context)
skills/         documentation skill
commands/       review-code, review-spec, generate-tests (reusable AI commands)
plan/           tasks.md — milestone plan (M0–M8), each task tagged to FR/NFR
docs/           prompt-history.md, ai-mistakes.md, acceptance-checklist.md
backend/        Spring Boot app (controller/service/repository/domain/dto/error/config) + tests
frontend/       React + Vite SPA (pages/components/api/lib)
```

## Spec-Driven Development Artefacts
- **Specs first:** everything in `spec/` was written before code; requirement IDs (`FR-*`/`NFR-*`)
  trace from spec → `plan/tasks.md` → code → tests.
- **Prompt history:** [`docs/prompt-history.md`](docs/prompt-history.md) (and `.specstory/history/` when
  captured by the IDE extension).
- **AI validation:** [`docs/ai-mistakes.md`](docs/ai-mistakes.md) records meaningful AI/requirement
  mistakes that were caught and corrected — evidence of using AI as an assistant, not an oracle.
- **Acceptance:** [`docs/acceptance-checklist.md`](docs/acceptance-checklist.md) maps all 15 criteria to tests.
