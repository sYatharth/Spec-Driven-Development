# AI Mistakes & Corrections Log

Evidence that AI output was **validated, not blindly accepted** (assignment requirement).
Each entry: what was suggested/produced, why it was wrong or risky, how it was corrected,
and which spec/rule it touched.

---

## 1. "Reopen" request would have violated the assignment's state machine
- **Context:** The user asked to make tickets reopenable. The natural reading of "reopen" is `RESOLVED → OPEN` / `CLOSED → OPEN`.
- **Problem:** The assignment **explicitly** lists `CLOSED → OPEN`, `RESOLVED → OPEN`, and `CANCELLED → OPEN` as invalid (`❌`) and requires the backend to reject them, with graded state-machine tests. Implementing reopen-to-OPEN would have contradicted the ground-truth requirement and failed those tests.
- **Correction:** Instead of implementing it, the conflict was surfaced with safe alternatives (e.g. `RESOLVED → IN_PROGRESS`, which is not on the forbidden list). The user chose to keep the **forward-only** lifecycle exactly as specified.
- **Touches:** `spec/state-machine.md`, `spec/requirements.md` (Q1), FR-11/FR-12.
- **Lesson:** Treat the assignment as ground truth; validate user/AI requests against it rather than silently "improving" the state machine.

---

## 2. Partial-update DTO footgun: title-only update could wipe the assignee
- **Context:** `UpdateTicketRequest` was first documented as "`assignee: null` or `\"\"` clears the assignee."
- **Problem:** A Java `record` cannot distinguish "field omitted" from "field explicitly null." With the original semantics, a client sending only `{"title":"x"}` deserializes `assignee = null` → the assignee would be **silently cleared** on every partial update. Data-loss footgun.
- **Correction:** Changed semantics so `null` = "leave unchanged" for *all* fields, and only an explicit empty string `""` clears the assignee. Updated the DTO Javadoc, the `TicketService.updateFields` logic, and added a test (`partial_update`) that updates priority only and asserts the assignee is retained, then clears it with `""`.
- **Touches:** `dto/UpdateTicketRequest.java`, `service/TicketService.java`, `spec/api-contract.md` §2.4, FR-4..7.
- **Lesson:** Watch for the record null-vs-absent ambiguity in PATCH endpoints; choose the non-destructive default.

---

## 3. Self-authored test was flaky (non-deterministic ordering)
- **Context:** The `list_newest_first` tests (repository + API) asserted a specific order using `ORDER BY createdAt DESC`.
- **Problem:** Multiple tickets created within the same millisecond get identical `createdAt` timestamps, so the tie-break order is **undefined** → intermittent failures. This violated our own `rules/testing.md` ("no flaky time-based assertions").
- **Correction:** Fixed the *root cause*, not the assertion: added `id DESC` as a deterministic secondary sort to every list/search query (`findAllByOrderByCreatedAtDescIdDesc`, `ORDER BY t.createdAt DESC, t.id DESC`). Ordering is now stable regardless of timestamp collisions.
- **Touches:** `repository/TicketRepository.java`, `service/TicketService.java`, tests, FR-2/9/10.
- **Lesson:** When a test is flaky, fix the underlying non-determinism rather than loosening the test.

---

## 4. Ambiguous `doesNotExist()` assertion on a nullable field
- **Context:** `TicketApiIntegrationTest.partial_update` asserted `jsonPath("$.assignee").doesNotExist()` after clearing the assignee.
- **Problem:** Jackson serializes a null field as `"assignee":null` — the key is *present* with a null value, so `doesNotExist()` is ambiguous and the assertion would likely fail even though the behavior is correct.
- **Correction:** Changed to `jsonPath("$.assignee", is(nullValue()))`, which precisely asserts the intended "cleared" state. Caught during the `review-code` self-pass.
- **Touches:** `TicketApiIntegrationTest`, FR-7.
- **Lesson:** Be precise about "absent" vs "present-but-null" when asserting on JSON.

## 5. Missing catch-all broke the uniform error contract on 500s
- **Context:** `GlobalExceptionHandler` mapped 400/404/409 but had no fallback.
- **Problem:** Any unexpected exception would return Spring's default 500 error page/body, violating the uniform `ApiError` shape (NFR-3) that the UI relies on — and risking internal-detail leakage.
- **Correction:** Added an `@ExceptionHandler(Exception.class)` fallback that logs the real error server-side but returns a generic uniform 500 body with no internal details. Caught during the `review-code` self-pass.
- **Touches:** `error/GlobalExceptionHandler.java`, NFR-3, FR-15.
- **Lesson:** A uniform error contract needs a safe catch-all, not just the known cases.

## Template for future entries
### N. <short title>
- **Context:**
- **Problem:**
- **Correction:**
- **Touches:**
- **Lesson:**
