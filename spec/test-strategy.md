# Test Strategy — Support Ticket Management System

- **Stage:** Specification (step 2)
- **Traces:** every FR/NFR; NFR-5 (state-machine integration tests) is the graded centerpiece.

---

## 1. Test Levels

| Level | Tooling | Scope |
|-------|---------|-------|
| **Unit** | JUnit 5 | `StatusTransitionValidator`, service logic, validation edge cases (no Spring context). |
| **Slice** | `@WebMvcTest` | Controllers: request binding, status codes, error mapping (mocked services). |
| **Integration** | `@SpringBootTest` + MockMvc + H2 | Full stack incl. persistence + state machine (**required by assignment**). |
| **Repository** | `@DataJpaTest` | Search/filter queries (FR-9, FR-10). |
| **Frontend** | Vitest + React Testing Library (optional v1) | Form validation display, allowed-action rendering. |
| **Manual/E2E** | checklist | Acceptance criteria walk-through, restart-persistence. |

---

## 2. Backend Test Plan by Requirement

| FR | Test(s) |
|----|---------|
| FR-1 create | POST valid → 201 + persisted, defaults (OPEN/MEDIUM) applied. |
| FR-2 list | GET returns all, newest first. |
| FR-3 detail | GET by id → full body + comments; unknown id → 404. |
| FR-4/5/6/7 update | PATCH each field; blank title → 400; invalid priority → 400; assignee cleared with null. |
| FR-8 comment | POST comment → 201; blank body → 400; unknown ticket → 404. |
| FR-9 search | seed data; `?q=login` returns only matching (case-insensitive, title+desc). |
| FR-10 filter | `?status=OPEN` returns only OPEN; invalid status → 400. |
| FR-11 valid transition | each of the 5 allowed → 200 + new status persisted. |
| FR-12 invalid transition | representative rejected set → 409, status unchanged. |
| FR-14 validation | oversized fields, blanks, bad enums → 400 with fieldErrors. |
| NFR-1 persistence | see §4. |
| NFR-3 error shape | error responses contain timestamp/status/error/message/fieldErrors. |

---

## 3. State-Machine Integration Tests (NFR-5 — must pass)

`StatusTransitionIntegrationTest` (`@SpringBootTest`, MockMvc, real H2):

**Parameterized allowed transitions**
```
(OPEN, IN_PROGRESS)
(OPEN, CANCELLED)
(IN_PROGRESS, RESOLVED)
(IN_PROGRESS, CANCELLED)
(RESOLVED, CLOSED)
```
For each: create/seed ticket in `from`, `PATCH /status {to}`, assert `200` and GET shows `to`.

**Parameterized rejected transitions** (from `state-machine.md` §5)
```
(CLOSED, OPEN) (RESOLVED, OPEN) (CANCELLED, OPEN)
(OPEN, RESOLVED) (OPEN, CLOSED) (IN_PROGRESS, CLOSED)
(RESOLVED, CANCELLED) (CLOSED, CANCELLED) (OPEN, OPEN)
```
For each: assert `409` and status **unchanged** in DB.

**Edge**: unknown id → 404; malformed enum → 400.

> To seed a ticket directly in a non-OPEN state (e.g. RESOLVED) for rejected-transition tests, walk it there through valid transitions, or insert via repository in the test setup. Prefer walking valid transitions to also exercise the happy path.

---

## 4. Persistence-Survives-Restart Test (NFR-1 / acceptance)

Two complementary approaches:
1. **Automated:** H2 file mode; a test creates a ticket, then a second context/`@DirtiesContext` reload reads it back. (Or documented manual step.)
2. **Manual (in acceptance checklist):** create ticket → stop backend → start backend → GET list still shows it.

---

## 5. Frontend Tests (optional but recommended)

- Create form: submit empty → shows inline "must not be blank" from mapped backend error (mock 400).
- StatusActions: given status=RESOLVED, only "Close" renders; given CLOSED, none.
- ErrorBanner: renders `message` and `fieldErrors`.

---

## 6. Test Data & Fixtures

- Builder/helper `TicketTestFactory` for creating tickets in a given status.
- Seed dataset for search/filter: mix of statuses and keywords ("login", "export", "safari").

---

## 7. Coverage Goals (v1)

- 100% of state-machine transitions (allowed + representative rejected).
- All validation rules hit at least once.
- All endpoints have at least one slice or integration test.
- No coverage % gate enforced in v1, but state-machine + validation are non-negotiable.

---

## 8. Commands (see `commands/generate-tests.md`)

- `./mvnw test` — run all backend tests.
- `./mvnw test -Dtest=StatusTransitionIntegrationTest` — state machine only.
- Frontend: `npm run test`.

---

## 9. Definition of Done (testing)

- [ ] All FR-mapped tests green.
- [ ] State-machine integration tests pass (allowed + rejected).
- [ ] Validation returns uniform error shape.
- [ ] Restart-persistence verified (automated or documented manual).
