# Command: review-spec

**Purpose:** Review the specification for completeness, consistency, and testability *before* implementation begins.
**Invoke:** at the end of the Specification stage, and whenever a spec file changes.

---

## Inputs
- `spec/requirements.md`, `architecture.md`, `data-model.md`, `api-contract.md`, `state-machine.md`, `ui-flow.md`, `test-strategy.md`.
- The original assignment/requirements.

## Checklist

### Completeness
- [ ] Every application requirement from the assignment maps to at least one `FR-*`.
- [ ] Every acceptance criterion has a corresponding requirement + planned test.
- [ ] All entities, fields, enums, and constraints are defined in `data-model.md`.
- [ ] Every endpoint the UI needs exists in `api-contract.md`.

### Consistency (cross-file)
- [ ] State machine identical across `requirements.md`, `state-machine.md`, `ui-flow.md`, `test-strategy.md`.
- [ ] DTO field names/types match between `api-contract.md` and `data-model.md`.
- [ ] Status/priority enum values identical everywhere.
- [ ] Error shape identical in `api-contract.md` and `rules/api-standards.md`.

### Correctness vs Assignment
- [ ] Forbidden transitions from the assignment (`CLOSED→OPEN`, `RESOLVED→OPEN`, `CANCELLED→OPEN`) are marked rejected — NOT accidentally allowed.
- [ ] Required features all present (create, list, view, update, assignee, comments, search, filter, persistence, validation, UI errors).

### Testability
- [ ] Each `FR-*` has a testable acceptance criterion (observable input→output).
- [ ] State-machine test matrix defined (allowed + rejected).
- [ ] Ambiguities captured as Open Questions with an explicit assumption.

### Risk / Gaps
- [ ] Assumptions documented (e.g., forward-only lifecycle, empty-search behavior).
- [ ] Non-goals stated to bound scope.
- [ ] No requirement left with undefined behavior.

## Output Format
```
[GAP|INCONSISTENCY|AMBIGUITY|OK] <file/section> — <finding> — <suggested resolution>
```
End with: **Verdict** — SPEC READY / SPEC NEEDS WORK, plus the top 3 things to fix.

## Rules of engagement
- Treat the assignment as ground truth; flag any spec that contradicts it (do not silently "improve" forbidden transitions).
- Every ambiguity must become either a resolved decision or a logged Open Question — never left implicit.
