# Acceptance Criteria Checklist

Manual + automated verification of the 15 assignment acceptance criteria. Automated
coverage is noted per row; check the box after verifying on a JDK 21 machine.

| # | Criterion | How verified | Automated test | Status |
|---|-----------|--------------|----------------|--------|
| 1 | Ticket can be created from UI | UI create form → POST | `TicketApiIntegrationTest.create_applies_defaults` | ☐ |
| 2 | Tickets can be listed | List page loads | `...list_newest_first` | ☐ |
| 3 | Ticket details can be viewed | Detail page | `...unknown_detail_returns_404` + create | ☐ |
| 4-6 | Ticket fields can be updated | Detail edit → PATCH | `...partial_update` | ☐ |
| 7 | Assignee can be changed | Detail edit | `...partial_update` (set + clear) | ☐ |
| 8 | Comments can be added | Detail comment form | `...add_comment` | ☐ |
| 9 | Search works | List search box | `...search_keyword`, repo `search_*` | ☐ |
| 10 | Status filter works | List status dropdown | `...filter_by_status` | ☐ |
| 11 | Valid status transitions work | Detail status actions | `StatusTransitionIntegrationTest.allowed_transition_succeeds` | ☐ |
| 12 | Invalid transitions rejected by backend | direct API/UI 409 | `...rejected_transition_returns_409` | ☐ |
| 13 | Data survives application restart | **manual, see below** | (file-mode config) | ☐ |
| 14 | Backend validation works | invalid inputs | `...blank_title_returns_400`, `...invalid_priority` | ☐ |
| 15 | UI shows meaningful errors | trigger 400/404/409 in UI | frontend `ErrorBanner` | ☐ |
| — | State-machine integration tests pass | `./mvnw test` | `StatusTransitionIntegrationTest` (NFR-5) | ☐ |
| — | No secrets committed | `git ls-files` review + `.gitignore` | n/a | ☐ |

## Manual: Data survives restart (Criterion 13, NFR-1)

The dev DB is H2 in **file mode** (`jdbc:h2:file:./data/tickets`), so data is written to disk.

1. Start backend: `./mvnw spring-boot:run`
2. Create a ticket (UI or `curl -X POST http://localhost:8080/api/tickets -H 'Content-Type: application/json' -d '{"title":"persist test","description":"should survive"}'`).
3. Stop the backend (Ctrl+C).
4. Start it again: `./mvnw spring-boot:run`.
5. `GET http://localhost:8080/api/tickets` → the ticket is still present. ✅

> The automated integration tests use in-memory H2 (test profile) for isolation and speed;
> durability is a property of the file-mode dev datasource and is verified by the manual step above.
