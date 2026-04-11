# Phase 1: Build Reproducibility Baseline - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md - this log preserves the alternatives considered.

**Date:** 2026-04-05
**Phase:** 1-Build Reproducibility Baseline
**Areas discussed:** Verification entrypoint, Failure stage reporting, Reproducibility strictness, Clean checkout setup contract

---

## Verification entrypoint

### Canonical automation entrypoint

| Option | Description | Selected |
|--------|-------------|----------|
| Use Gradle lifecycle | Keep `./gradlew check` as the standard verification entrypoint and wire later unit/game-test stages under it. | ✓ |
| Add mod-specific task | Create a dedicated task like `verifyMod` or `ci` as the documented entrypoint. | |
| Separate build and verify | Treat build and verification as equally first-class commands instead of one canonical verification entrypoint. | |

**User's choice:** Use Gradle lifecycle

### Documented clean-checkout build command

| Option | Description | Selected |
|--------|-------------|----------|
| `./gradlew build` | Use standard Gradle build for artifact production and keep `check` as the verification umbrella. | ✓ |
| `./gradlew check` | Treat verification as the main documented maintainer command. | |
| Same command for both | Reshape the task graph so one command covers building artifacts and standard verification. | |

**User's choice:** `./gradlew build`
**Notes:** No further questions needed; move to next area.

---

## Failure stage reporting

### Stage separation style

| Option | Description | Selected |
|--------|-------------|----------|
| Separate Gradle stages | Keep build, unit tests, and game tests as distinct tasks/stages under the verification umbrella. | ✓ |
| Single opaque run | Use one top-level verification task without emphasizing internal stage boundaries. | |
| Extra custom reporting | Add an additional report layer beyond normal Gradle task output. | |

**User's choice:** Separate Gradle stages

### Failure flow behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Stop at first failed stage | Fail fast once build, unit tests, or game tests break. | |
| Try all stages anyway | Attempt later stages when possible so maintainers can see multiple failures in one run. | ✓ |
| You decide | Leave this as an implementation choice. | |

**User's choice:** Try all stages anyway
**Notes:** User wants the verification experience to preserve stage visibility while still surfacing more than one broken layer when practical.

---

## Reproducibility strictness

### Default strictness

| Option | Description | Selected |
|--------|-------------|----------|
| Strict by default | Make the documented build path avoid local-only and drifting inputs wherever practical. | ✓ |
| Strict docs, flexible build | Document a reproducible path but leave local escape hatches in the default build. | |
| Allow local overrides | Keep the default build permissive and rely on maintainer discipline. | |

**User's choice:** Strict by default

### Local experimentation path

| Option | Description | Selected |
|--------|-------------|----------|
| Opt-in only | Keep the canonical workflow strict; local overrides must be explicit and outside the default path. | ✓ |
| Built into default tasks | Let the standard workflow automatically consider local overrides. | |
| No local override path | Do not support any local escape hatch at all. | |

**User's choice:** Opt-in only
**Notes:** No further questions needed; move to next area.

---

## Clean checkout setup contract

### Host prerequisites

| Option | Description | Selected |
|--------|-------------|----------|
| Minimal host prerequisites | Assume only normal host basics plus the repo checkout; automate Java/toolchain provisioning where possible. | ✓ |
| Preinstalled Java 17 | Require maintainers to install and manage Java 17 themselves. | |
| Fully provisioned dev environment | Expect a containerized or otherwise pre-provisioned environment. | |

**User's choice:** Minimal host prerequisites

### Documentation location

| Option | Description | Selected |
|--------|-------------|----------|
| README + dedicated build doc | Keep quickstart in `README.md` and the detailed workflow in a dedicated build-focused doc. | ✓ |
| README only | Put the full workflow in the top-level README. | |
| Dedicated doc only | Keep the root README minimal and store the workflow elsewhere. | |

**User's choice:** README + dedicated build doc
**Notes:** No further questions needed; move to next area.

---

## the agent's Discretion

- Exact helper-task names and summary formatting.
- Concrete Gradle implementation details needed to continue into later verification stages when practical.

## Deferred Ideas

None.
