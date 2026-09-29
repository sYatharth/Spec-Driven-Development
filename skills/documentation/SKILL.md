---
name: documentation
description: How to write and maintain project documentation (README, API docs, spec updates, ADRs) for the Support Ticket Management System. Invoke when creating or updating any docs.
---

# Documentation Skill

Reusable instructions for producing consistent, useful documentation across this project.

## When to use
- Writing/updating the root `README.md`.
- Keeping `spec/*` in sync when a decision changes.
- Recording a decision (ADR) or an AI mistake caught.
- Documenting how to run, test, and use the API.

## Documentation Types & Where They Live
| Type | Location | Purpose |
|------|----------|---------|
| Getting started / run | `README.md` | clone → run backend + frontend → test |
| Specification | `spec/*.md` | authoritative design (requirements, api, state machine…) |
| API reference | `spec/api-contract.md` (+ optional Swagger) | endpoints, DTOs, errors |
| Decisions (ADR) | `docs/decisions/` | one file per notable decision |
| AI mistakes caught | `docs/ai-mistakes.md` | evidence of validating AI output |
| Prompt history | `docs/prompt-history.md` + `.specstory/history/` | SDD process trail |

## Principles
1. **Traceable** — reference requirement IDs (`FR-*`, `NFR-*`) so docs link to spec and tests.
2. **Runnable** — every doc that describes a command must give the exact command and expected result.
3. **Truthful** — document what the code actually does; if code and spec diverge, fix one and note it. Never document aspirational behavior as done.
4. **Concise** — tables and short sections over prose walls. One concept per heading.
5. **Copy-pasteable** — commands in fenced blocks, no placeholders left unexplained.

## README Required Sections
1. Title + one-line description.
2. Tech stack (Java 21, Spring Boot, H2, React/Vite).
3. Prerequisites (JDK 21, Node).
4. Run backend (`./mvnw spring-boot:run`, port 8080).
5. Run frontend (`npm install && npm run dev`, port 5173).
6. Run tests (`./mvnw test`).
7. API summary (link to `spec/api-contract.md`).
8. State machine summary (link to `spec/state-machine.md`).
9. Configuration & env vars (no secrets committed — NFR-4).
10. Project structure map.

## ADR Format (`docs/decisions/NNNN-title.md`)
```
# NNNN. <decision title>
Date: YYYY-MM-DD
Status: Accepted | Superseded
## Context
## Decision
## Consequences
```

## AI Mistakes Log Format (`docs/ai-mistakes.md`)
For each entry: **What the AI suggested**, **Why it was wrong/risky**, **How it was corrected**, **Requirement/rule it violated**. This is graded evidence — keep it specific and honest.

## Style
- Markdown, GitHub-flavored. Fenced code blocks with language hints.
- Present tense, active voice. US spelling.
- Keep line-of-sight from requirement → design → code → test in mind when writing.
