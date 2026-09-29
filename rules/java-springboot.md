# Rule: Java 21 + Spring Boot Guidelines

**Scope:** All backend code. These are steering instructions for the AI assistant and humans.
**Applies to:** `backend/` module.

---

## 1. Language & Version
- **Java 21**. Use modern language features where they improve clarity:
  - `record` for DTOs and immutable value objects.
  - `switch` expressions / pattern matching where it reads cleaner.
  - `var` only when the type is obvious from the right-hand side.
  - Text blocks for multi-line strings (SQL, JSON in tests).
- **No Lombok** unless explicitly approved — prefer records and plain constructors to keep the build simple and reviewable.

## 2. Project Structure (layered)
```
controller/  → HTTP only (bind DTOs, choose status codes)
service/     → business logic + transactions (owns state-machine)
repository/  → Spring Data JPA interfaces
domain/      → JPA entities + enums
dto/         → request/response records (never expose entities)
error/       → @RestControllerAdvice + exceptions
config/      → CORS, etc.
```
Package root: `com.support.tickets`.

## 3. Layer Rules
- **Controllers** must not contain business logic. No repository access from controllers.
- **Services** are the only place transactions (`@Transactional`) and business rules live. State-machine enforcement lives in `StatusTransitionValidator` used by the service.
- **Repositories** are Spring Data interfaces; custom queries via derived method names or `@Query`.
- **Never return JPA entities from controllers** — always map to a DTO record. Prevents lazy-loading serialization bugs and keeps the API contract stable.

## 4. DTOs & Validation
- DTOs are `record`s in `dto/`.
- Validate at the boundary with `jakarta.validation` annotations (`@NotBlank`, `@Size`, `@NotNull`) and `@Valid` on controller params.
- Do **not** rely on client-side validation — backend is the source of truth (FR-14).

## 5. Persistence
- Enums stored as `@Enumerated(EnumType.STRING)`.
- Timestamps: `Instant`, set via `@PrePersist`/`@PreUpdate` (or `@CreationTimestamp`/`@UpdateTimestamp`). Never trust client-supplied timestamps.
- Dev DB: H2 file mode (`jdbc:h2:file:./data/tickets`) so data survives restart. Schema is PostgreSQL-compatible.
- `spring.jpa.hibernate.ddl-auto=update` in dev only.

## 6. Error Handling
- One `@RestControllerAdvice` (`GlobalExceptionHandler`) produces the uniform error body (see `rules/api-standards.md`).
- Map:
  - `MethodArgumentNotValidException` / `ConstraintViolationException` → `400` with `fieldErrors`.
  - `NotFoundException` → `404`.
  - `InvalidStatusTransitionException` → `409`.
  - `HttpMessageNotReadableException` (bad enum) → `400`.
- Never leak stack traces to the client.

## 7. Configuration & Secrets
- No secrets in code or committed files. Use `application.yml` for non-secret defaults; override via environment variables.
- `.gitignore` must cover: `target/`, `*.db`, `.env`, IDE folders.

## 8. Naming & Style
- Classes `PascalCase`, methods/fields `camelCase`, constants `UPPER_SNAKE`.
- Method names express intent (`assertCanTransition`, `changeStatus`).
- Keep methods small; one responsibility.

## 9. Dependencies (minimum)
- `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`
- `com.h2database:h2`
- Test: `spring-boot-starter-test` (JUnit 5, MockMvc, AssertJ)

## 10. Definition of "good" backend code here
- Compiles on Java 21, `./mvnw test` green.
- Every FR traceable to a controller+service+test.
- No entity leakage, uniform errors, state-machine enforced server-side.
