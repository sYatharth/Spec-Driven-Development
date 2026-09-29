# Command: generate-tests

**Purpose:** Generate tests that trace to requirements, with emphasis on the state machine and validation.
**Invoke:** during the Testing stage, or when adding a feature that needs coverage.

---

## Inputs
- Code under test (controller/service/repository).
- `spec/test-strategy.md` (the plan), `spec/state-machine.md`, `spec/api-contract.md`, `spec/data-model.md`.
- `rules/testing.md` (conventions).

## What to generate

### 1. State-machine integration tests (priority — NFR-5)
Create `StatusTransitionIntegrationTest` (`@SpringBootTest` + MockMvc + H2):
- `@ParameterizedTest` over **allowed** transitions → assert `200` + GET shows new status.
  `(OPEN,IN_PROGRESS)(OPEN,CANCELLED)(IN_PROGRESS,RESOLVED)(IN_PROGRESS,CANCELLED)(RESOLVED,CLOSED)`
- `@ParameterizedTest` over **rejected** transitions → assert `409` + status unchanged.
  `(CLOSED,OPEN)(RESOLVED,OPEN)(CANCELLED,OPEN)(OPEN,RESOLVED)(OPEN,CLOSED)(IN_PROGRESS,CLOSED)(RESOLVED,CANCELLED)(OPEN,OPEN)`
- Helper to move a ticket to a start state by walking valid transitions.

### 2. Validation tests
- Blank title → 400 + fieldError `title`.
- Oversized description (>5000) → 400.
- Invalid priority enum → 400.
- Blank comment body → 400.
- Assert uniform error body shape each time.

### 3. Endpoint/behavior tests
- Create → 201 with defaults OPEN/MEDIUM.
- List newest-first; search `?q=` case-insensitive on title+desc; filter `?status=`.
- Detail unknown id → 404; add comment to unknown ticket → 404.

### 4. Repository slice (`@DataJpaTest`)
- Search and status-filter queries return the right rows.

## Conventions (from rules/testing.md)
- JUnit 5 + AssertJ; `@ParameterizedTest` with `@MethodSource`/`@CsvSource` for matrices.
- Method names: `should_<expected>_when_<condition>`.
- Independent tests; roll back or reset DB between integration tests.
- No flaky/time-based assertions.

## Output
- Compilable test classes in `src/test/java/...`.
- A short note mapping each generated test to its `FR-*`/`NFR-*`.

## Commands to run after generating
```
./mvnw test                                   # all
./mvnw test -Dtest=StatusTransitionIntegrationTest   # state machine only
```

## Rules of engagement
- Do not weaken assertions to make tests pass — if a test fails, the code or spec is wrong; flag it.
- Every FR touched by new code must gain or already have a test.
