# Rule: API Standards

**Scope:** REST API design and responses. Steering instructions for the AI assistant.
**Authoritative contract:** `spec/api-contract.md`.

---

## 1. Style
- RESTful, resource-oriented, JSON in/out (`application/json`).
- Base path: `/api`. Resources plural: `/api/tickets`, `/api/tickets/{id}/comments`.
- Use nouns for resources, HTTP verbs for actions. No verbs in paths (exception: sub-resource `/status` as a constrained field update).

## 2. HTTP Methods
| Method | Use |
|--------|-----|
| GET | read (list, detail, search, filter) — no side effects |
| POST | create (ticket, comment) → `201` |
| PATCH | partial update (fields, status) → `200` |
| (PUT/DELETE) | not used in v1 |

## 3. Status Codes
| Code | When |
|------|------|
| 200 | successful read/update |
| 201 | resource created (include created body) |
| 400 | validation failure / malformed body / bad enum |
| 404 | resource not found |
| 409 | invalid state transition (business conflict) |
| 500 | unexpected (should be rare; logged, generic message) |

## 4. Uniform Error Body (required — NFR-3, FR-15)
```json
{
  "timestamp": "2026-09-28T10:20:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "title", "message": "must not be blank" }
  ]
}
```
- `fieldErrors` is `[]` for non-validation errors.
- `message` is human-readable and safe to show in UI.
- Never return stack traces or internal class names.

## 5. Request/Response Rules
- Requests and responses use DTO records, never entities.
- Timestamps ISO-8601 UTC.
- Enums serialized as their String name (`"IN_PROGRESS"`).
- List responses return an array of **summary** DTOs; detail returns the **full** DTO incl. comments.
- Partial update (PATCH): only provided fields change; omitted fields untouched. Explicit `null` on `assignee` clears it.

## 6. Query Parameters
- List filtering via query params: `?q=<keyword>&status=<STATUS>` (combinable).
- Empty `q` → treated as no filter (returns all). Invalid `status` → `400`.
- No pagination in v1 (documented non-goal).

## 7. Status-Change Endpoint
- Status changes ONLY via `PATCH /api/tickets/{id}/status` with `{ "status": "<TARGET>" }`.
- The generic field-update endpoint (`PATCH /api/tickets/{id}`) must **not** accept or change status. This isolates state-machine enforcement.

## 8. CORS
- Dev: allow only the frontend origin (`http://localhost:5173`), methods `GET,POST,PATCH,OPTIONS`, header `Content-Type`.

## 9. Versioning & Stability
- v1 unversioned under `/api`. If breaking changes are needed later, introduce `/api/v2`.
- Response field additions are backward-compatible; removals/renames are breaking — avoid.

## 10. Documentation
- The contract in `spec/api-contract.md` is the source of truth. Keep it in sync with code. Optionally expose OpenAPI/Swagger UI (nice-to-have).
