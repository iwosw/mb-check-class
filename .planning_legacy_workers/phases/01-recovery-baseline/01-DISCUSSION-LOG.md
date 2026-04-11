# Phase 1: Recovery Baseline - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-05
**Phase:** 1-Recovery Baseline
**Areas discussed:** Baseline proof, Dependency cleanup, Startup success bar, Guardrails now, Degraded startup tolerance

---

## Baseline proof

| Option | Description | Selected |
|--------|-------------|----------|
| Build + both boots | `./gradlew build` passes, client reaches the main menu/world load without startup crash, and dedicated server starts cleanly. | |
| Build + client only | Require a clean build and client startup only. | |
| In-game smoke path | Require build, both boots, plus a tiny world-level smoke path such as spawning a worker item/entity or opening a basic screen. | ✓ |

**User's choice:** In-game smoke path
**Notes:** User wanted Phase 1 to prove more than startup alone.

---

## Smoke path detail

| Option | Description | Selected |
|--------|-------------|----------|
| Spawn-focused | Prove at least one worker-related item/entity can be registered and used without crashing. | ✓ |
| UI-focused | Also prove one existing worker/work-area UI flow opens. | |
| Both spawn and UI | Require one spawn interaction and one UI open path. | |
| You decide | Leave the exact smoke path to implementation. | |

**User's choice:** Spawn-focused
**Notes:** Keep the smoke path minimal and startup-oriented.

---

## Dependency cleanup

| Option | Description | Selected |
|--------|-------------|----------|
| Minimal unblock only | Change only what is needed to get a clean 1.20.1 build/startup baseline. | ✓ |
| Moderate cleanup | Fix direct blockers and also normalize obviously stale nearby metadata. | |
| Full cleanup sweep | Clean all suspicious dependency and metadata issues around the baseline. | |

**User's choice:** Minimal unblock only
**Notes:** Phase 1 should stay tightly scoped.

---

## Compatibility dependency policy

| Option | Description | Selected |
|--------|-------------|----------|
| Disable if safe | Temporarily remove or gate non-core compat dependencies when they are not required for the core recovery baseline. | ✓ |
| Keep all declared deps | Preserve every current dependency and solve around them. | |
| Case by case | Decide per dependency during implementation without locking a policy now. | |

**User's choice:** Disable if safe
**Notes:** Core baseline recovery has priority over non-core integrations.

---

## Startup success bar

| Option | Description | Selected |
|--------|-------------|----------|
| Bootstrap core | Registries, packet channel/message registration, config registration, menu wiring, and client setup hooks must initialize cleanly. | ✓ |
| Bootstrap + area UI | Bootstrap core plus work-area screen opening paths. | |
| Bootstrap + all worker loops | Treat profession AI and full gameplay loops as startup-critical already. | |

**User's choice:** Bootstrap core
**Notes:** Deeper gameplay proof belongs to later phases unless it directly blocks startup.

---

## Guardrails now

| Option | Description | Selected |
|--------|-------------|----------|
| Build/run checks only | Use repeatable build plus manual startup checks only. | |
| One baseline automated check | Add the smallest practical automated protection around baseline or isolated logic. | ✓ |
| Early test harness | Set up broader automated test infrastructure now. | |

**User's choice:** One baseline automated check
**Notes:** Add some protection now without dragging Phase 9 fully forward.

---

## Automated check target

| Option | Description | Selected |
|--------|-------------|----------|
| Pure logic seam | Target validation, serialization, or similarly isolated non-runtime logic. | ✓ |
| Bootstrap wiring | Aim directly at mod bootstrap or registration wiring. | |
| You decide | Leave the exact check target open. | |

**User's choice:** Pure logic seam
**Notes:** Avoid relying on a full runtime test harness in Phase 1.

---

## Degraded startup tolerance

| Option | Description | Selected |
|--------|-------------|----------|
| Core-safe degradation | Allow non-fatal warnings or temporarily disabled non-core compat paths if the core baseline works and the rough edges are documented. | ✓ |
| No known startup warnings | Treat any known startup warning or degraded path as unfinished. | |
| Case by case | Leave this flexible during implementation. | |

**User's choice:** Core-safe degradation
**Notes:** Documentation of tolerated rough edges is required.

---

## the agent's Discretion

- Exact spawn-focused smoke interaction.
- Exact pure-logic automated check target.
- Exact documentation format for tolerated degraded non-core paths.

## Deferred Ideas

None.
