# Command: review-code

**Purpose:** Systematically review implementation code against the specs and rules before it's accepted.
**Invoke:** during the Review stage of the SDD pipeline, on any new/changed backend or frontend code.

---

## Inputs
- The diff or files under review.
- Steering rules: `rules/java-springboot.md`, `rules/api-standards.md`, `rules/testing.md`.
- Specs: `spec/*.md` (contract, data-model, state-machine).

## Checklist

### Correctness vs Spec
- [ ] Behavior matches `spec/api-contract.md` (paths, methods, status codes, DTO shapes).
- [ ] State transitions match `spec/state-machine.md` exactly (allowed set + rejects). No extra/missing transitions.
- [ ] Validation rules match `spec/data-model.md` (lengths, required, enums).
- [ ] Every touched requirement (`FR-*`) is actually satisfied, not just stubbed.

### Layering & Design (java-springboot rule)
- [ ] Controllers contain no business logic; no repo access from controllers.
- [ ] Business rules + `@Transactional` live in services.
- [ ] No JPA entities returned from controllers — DTOs only.
- [ ] State-machine enforced in service via `StatusTransitionValidator`, unreachable-to-bypass from field update.

### API Standards
- [ ] Uniform error body used everywhere; correct codes (400/404/409).
- [ ] No stack traces / internal names leaked.
- [ ] Status change only via `/status` endpoint; field PATCH cannot change status.

### Security & Hygiene
- [ ] No secrets, credentials, or tokens committed.
- [ ] `.gitignore` covers build output, `*.db`, `.env`, IDE files.
- [ ] No `System.out` debugging left; use logger.

### Quality
- [ ] Java 21 idioms used sensibly (records for DTOs, switch expressions where clearer).
- [ ] Names express intent; methods small and single-purpose.
- [ ] No dead code, no commented-out blocks, no TODOs without an issue reference.
- [ ] Errors handled, not swallowed.

### Tests present
- [ ] New logic has tests; state-machine and validation covered (see `rules/testing.md`).

## Output Format
Produce findings as a list, most-severe first:
```
[SEVERITY: high|med|low] <file:line> — <problem> — <why it violates spec/rule> — <suggested fix>
```
End with: **Verdict** — APPROVE / APPROVE-WITH-NITS / REQUEST-CHANGES, and a one-line summary.

## Rules of engagement
- Cite the exact spec/rule each finding violates.
- Prefer concrete, minimal fixes.
- Do not approve if any state-machine or validation behavior diverges from spec.
