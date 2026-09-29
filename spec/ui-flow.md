# UI Flow — Support Ticket Management System

- **Stage:** Specification (step 2)
- **Traces:** FR-1,2,3,4-7,8,9,10,11,15

---

## 1. Screens & Routes

| Route | Screen | Requirements |
|-------|--------|--------------|
| `/` | Ticket List | FR-2, FR-9 (search), FR-10 (filter) |
| `/tickets/new` | Create Ticket | FR-1, FR-14/15 (errors) |
| `/tickets/:id` | Ticket Detail | FR-3, FR-4-7, FR-8, FR-11, FR-15 |

---

## 2. Navigation Map

```
        ┌───────────────────────────┐
        │       Ticket List  /       │
        │  [Search box] [Status ▼]   │
        │  [+ New Ticket] ──────────────────▶ /tickets/new
        │  ┌─────────────────────┐   │
        │  │ row → click ────────┼───────────▶ /tickets/:id
        │  └─────────────────────┘   │
        └───────────────────────────┘
                    ▲                          │
                    │ back / after create      │
                    └──────────────────────────┘
```

---

## 3. Ticket List Page (`/`)

**Layout**
```
┌──────────────────────────────────────────────────────────┐
│ Support Tickets                         [ + New Ticket ]   │
│ ┌────────────────────────┐  Status: [ All ▼ ]             │
│ │ 🔍 Search keyword...    │  (OPEN/IN_PROGRESS/RESOLVED/    │
│ └────────────────────────┘   CLOSED/CANCELLED/All)         │
│ ─────────────────────────────────────────────────────────│
│ #  Title                     Status        Priority  Assignee│
│ 1  Login button unresp...    [OPEN]        HIGH      alice   │
│ 2  Export fails on CSV       [IN_PROGRESS] URGENT    bob     │
│ ... (newest first)                                          │
└──────────────────────────────────────────────────────────┘
```

**Behaviour**
- Loads `GET /api/tickets` on mount.
- **Search (FR-9):** typing in the box (debounced) → `GET /api/tickets?q=...`.
- **Filter (FR-10):** status dropdown → `GET /api/tickets?status=...`. Combinable with `q`.
- Status shown as a colored `StatusBadge`.
- Row click → detail page.
- Empty state: "No tickets match your search/filter."
- Error → `ErrorBanner` at top.

---

## 4. Create Ticket Page (`/tickets/new`)

**Form fields**
- Title * (text)
- Description * (textarea)
- Priority (select: LOW/MEDIUM/HIGH/URGENT, default MEDIUM)
- Assignee (text, optional)

**Behaviour**
- Submit → `POST /api/tickets`.
- **Client-side** minimal (required markers), but **backend is source of truth**: on `400`, map `fieldErrors[]` to inline messages under each field + banner (FR-15).
- Success → redirect to `/tickets/:id` of the new ticket.

**Error example rendering**
```
Title  [                         ]
       ⚠ must not be blank
```

---

## 5. Ticket Detail Page (`/tickets/:id`)

**Layout**
```
┌──────────────────────────────────────────────────────────┐
│ ← Back            Ticket #1            [OPEN]              │
│ ─────────────────────────────────────────────────────────│
│ Title       [ Login button unresponsive        ] (editable)│
│ Description [ ....multiline....                  ]         │
│ Priority    [ HIGH ▼ ]     Assignee [ alice        ]      │
│                                     [ Save changes ]       │
│ ─────────────────────────────────────────────────────────│
│ Status actions:  [ Start (→IN_PROGRESS) ] [ Cancel ]       │
│                  (only ALLOWED transitions shown)          │
│ ─────────────────────────────────────────────────────────│
│ Comments (2)                                               │
│  • alice — 2026-09-25 10:25:  Reproduced on Safari...      │
│  • bob   — 2026-09-25 11:02:  Fix deployed to staging.     │
│  ┌───────────────────────────────┐ [ Add comment ]        │
│  │ Write a comment...             │                        │
│  └───────────────────────────────┘                        │
└──────────────────────────────────────────────────────────┘
```

**Behaviour**
- Loads `GET /api/tickets/:id`.
- **Edit fields (FR-4-7):** Save → `PATCH /api/tickets/:id`; validation errors rendered inline.
- **Status actions (FR-11):** `StatusActions` reads `lib/statusMachine.js` and renders **only the allowed next transitions** for the current status (e.g. OPEN → shows "Start" and "Cancel"; CLOSED/CANCELLED → shows none, "This ticket is closed."). Clicking → `PATCH /api/tickets/:id/status`.
  - Even though the UI hides invalid actions, if the backend returns `409` (e.g. stale state), show the message in the banner (FR-15). UI is convenience, backend is authority.
- **Comments (FR-8):** Add → `POST /api/tickets/:id/comments`; new comment appended on success.

---

## 6. Status → Allowed Actions (UI mirror)

| Current | Buttons shown |
|---------|---------------|
| OPEN | "Start" (→IN_PROGRESS), "Cancel" (→CANCELLED) |
| IN_PROGRESS | "Resolve" (→RESOLVED), "Cancel" (→CANCELLED) |
| RESOLVED | "Close" (→CLOSED) |
| CLOSED | (none) — "Ticket closed." |
| CANCELLED | (none) — "Ticket cancelled." |

Mirrors `state-machine.md` §3. This is UX only — never the enforcement point.

---

## 7. Error Handling Pattern (FR-15)

`api/client.js`:
- On non-2xx, parse the uniform error JSON (`message`, `fieldErrors[]`).
- Throw a normalized `ApiError { status, message, fieldErrors }`.

Components:
- **Banner** for general/`409`/`404` messages.
- **Inline field errors** for `400` with `fieldErrors` (matched by `field`).
- Never display raw stack traces or HTTP jargon to the user.

---

## 8. Status Badge Colors (suggested)

| Status | Color |
|--------|-------|
| OPEN | blue |
| IN_PROGRESS | amber |
| RESOLVED | green |
| CLOSED | gray |
| CANCELLED | red |

(Accessible contrast; final palette per implementation.)
