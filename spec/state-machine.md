# State Machine — Ticket Status Lifecycle

- **Stage:** Specification (step 2)
- **Authoritative source** for FR-11 / FR-12. Backend enforces; UI mirrors for UX only.

---

## 1. States

| State | Meaning | Terminal? |
|-------|---------|-----------|
| `OPEN` | Newly created, not yet worked on | No |
| `IN_PROGRESS` | An agent is actively working it | No |
| `RESOLVED` | Fix applied / answer given, awaiting closure | No |
| `CLOSED` | Verified done | **Yes** |
| `CANCELLED` | Abandoned / invalid / duplicate | **Yes** |

Initial state on create: **`OPEN`**.

---

## 2. Transition Diagram

```
                 ┌─────────────┐
                 │    OPEN     │
                 └──┬───────┬──┘
        IN_PROGRESS │       │ CANCELLED
                    ▼       ▼
            ┌─────────────┐ │
            │ IN_PROGRESS │ │
            └──┬───────┬──┘ │
       RESOLVED│       │CANCELLED
               ▼       └──────┐
        ┌─────────────┐       ▼
        │  RESOLVED   │  ┌───────────┐
        └──────┬──────┘  │ CANCELLED │  (terminal)
         CLOSED│         └───────────┘
               ▼
        ┌─────────────┐
        │   CLOSED    │  (terminal)
        └─────────────┘
```

---

## 3. Allowed Transitions (authoritative list)

| From | To | Allowed |
|------|----|---------|
| OPEN | IN_PROGRESS | ✅ |
| OPEN | CANCELLED | ✅ |
| IN_PROGRESS | RESOLVED | ✅ |
| IN_PROGRESS | CANCELLED | ✅ |
| RESOLVED | CLOSED | ✅ |
| *any other* | *any other* | ❌ |

Design stance: **forward-only lifecycle** (no re-open, no backward moves). See Requirements Q1.

---

## 4. Full Transition Matrix

Rows = current state, Columns = target state. ✅ allowed, ❌ rejected (`409`), — = no-op/self (rejected as invalid transition).

| from \ to | OPEN | IN_PROGRESS | RESOLVED | CLOSED | CANCELLED |
|-----------|------|-------------|----------|--------|-----------|
| **OPEN** | — | ✅ | ❌ | ❌ | ✅ |
| **IN_PROGRESS** | ❌ | — | ✅ | ❌ | ✅ |
| **RESOLVED** | ❌ | ❌ | — | ✅ | ❌ |
| **CLOSED** | ❌ | ❌ | ❌ | — | ❌ |
| **CANCELLED** | ❌ | ❌ | ❌ | ❌ | — |

Self-transitions (`X → X`) are treated as invalid (rejected). If a no-op should be idempotent instead, that is a future decision — v1 rejects.

---

## 5. Rejected Examples (must be tested — NFR-5)

```
CLOSED    → OPEN          ❌ 409
RESOLVED  → OPEN          ❌ 409
CANCELLED → OPEN          ❌ 409
OPEN      → RESOLVED      ❌ 409  (skips IN_PROGRESS)
OPEN      → CLOSED        ❌ 409
IN_PROGRESS → CLOSED      ❌ 409  (skips RESOLVED)
RESOLVED  → CANCELLED     ❌ 409
CLOSED    → CANCELLED     ❌ 409
OPEN      → OPEN          ❌ 409  (self)
```

---

## 6. Enforcement Design

`StatusTransitionValidator` (service layer) holds an immutable map:

```java
private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = Map.of(
    OPEN,        EnumSet.of(IN_PROGRESS, CANCELLED),
    IN_PROGRESS, EnumSet.of(RESOLVED, CANCELLED),
    RESOLVED,    EnumSet.of(CLOSED),
    CLOSED,      EnumSet.noneOf(TicketStatus.class),
    CANCELLED,   EnumSet.noneOf(TicketStatus.class)
);

public void assertCanTransition(TicketStatus from, TicketStatus to) {
    if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
        throw new InvalidStatusTransitionException(from, to);
    }
}
```

- `InvalidStatusTransitionException` → mapped to `409 Conflict` by `GlobalExceptionHandler`.
- Enforced **only** in the service, reached via `PATCH /tickets/{id}/status`. Field-update endpoint cannot alter status, so the rule can't be bypassed.

---

## 7. Test Matrix (drives integration tests)

- 5 allowed transitions → expect `200` and persisted new status.
- Representative rejected transitions (§5) → expect `409`, status unchanged.
- Terminal states (`CLOSED`, `CANCELLED`) → every target rejected.
- Unknown id → `404`.
- Invalid enum in body → `400`.

See `spec/test-strategy.md` §State-Machine Tests.
