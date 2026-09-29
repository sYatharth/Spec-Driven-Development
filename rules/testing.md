# Rule: Testing Guidelines

**Scope:** All tests, backend and frontend. Steering instructions for the AI assistant.

---

## 1. Philosophy
- Tests trace to requirements. Name tests so a reviewer can map them to `FR-*`/`NFR-*`.
- The **state-machine integration tests are non-negotiable** (NFR-5) — they must cover all allowed transitions and representative rejected ones.
- Prefer fast unit tests for logic; use integration tests where persistence or wiring matters.

## 2. Backend Test Levels
| Level | Annotation | Use for |
|-------|-----------|---------|
| Unit | none (plain JUnit 5) | `StatusTransitionValidator`, pure logic |
| Web slice | `@WebMvcTest` | controller status codes, error mapping (mock service) |
| Repo slice | `@DataJpaTest` | search/filter queries (FR-9, FR-10) |
| Integration | `@SpringBootTest` + MockMvc + H2 | end-to-end incl. state machine + persistence |

## 3. Conventions
- JUnit 5 (`org.junit.jupiter`), AssertJ (`assertThat`) for readable assertions.
- Use `@ParameterizedTest` for transition matrices (one test, many cases).
- Test class names: `<ClassUnderTest>Test` (unit/slice), `<Feature>IntegrationTest` (integration).
- Method names: `should_<expected>_when_<condition>` e.g. `should_reject_when_transition_RESOLVED_to_OPEN`.
- Arrange–Act–Assert structure; one logical assertion group per test.
- No test depends on another test's state. Reset DB between integration tests (`@Transactional` rollback or `@DirtiesContext` where needed).

## 4. State-Machine Tests (must exist)
- Parameterized **allowed**: `(OPEN,IN_PROGRESS)`, `(OPEN,CANCELLED)`, `(IN_PROGRESS,RESOLVED)`, `(IN_PROGRESS,CANCELLED)`, `(RESOLVED,CLOSED)` → expect `200` + persisted new status.
- Parameterized **rejected**: `(CLOSED,OPEN)`, `(RESOLVED,OPEN)`, `(CANCELLED,OPEN)`, `(OPEN,RESOLVED)`, `(OPEN,CLOSED)`, `(IN_PROGRESS,CLOSED)`, `(RESOLVED,CANCELLED)`, `(OPEN,OPEN)` → expect `409` + status unchanged.
- To place a ticket in a non-OPEN start state, walk it through valid transitions (also exercises happy path).

## 5. Validation Tests
- Every validation rule (blank title, oversized description, invalid priority enum, blank comment body) has at least one test asserting `400` and the uniform error body with the right `fieldErrors[].field`.

## 6. Persistence Test (NFR-1)
- Automated: create → reload context/read back (H2 file mode) OR document a manual restart step in the acceptance checklist.

## 7. Frontend Tests (optional v1, recommended)
- Vitest + React Testing Library.
- Cover: create-form error rendering (mapped from backend 400), `StatusActions` shows only allowed transitions, `ErrorBanner` renders message + fieldErrors.
- Mock the API client; do not hit a real backend in unit tests.

## 8. What NOT to do
- Don't assert on stack traces or exact framework messages that may change.
- Don't test framework code (getters/setters, Spring wiring itself).
- Don't write flaky time-dependent assertions; compare with tolerance or fixed clock.

## 9. Commands
- All: `./mvnw test`
- State machine only: `./mvnw test -Dtest=StatusTransitionIntegrationTest`
- Frontend: `npm run test`

## 10. Done
- All FR-mapped tests pass; state-machine + validation coverage complete; no flaky tests.
