---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 13
type: execute
gap_closure: true
closed_gaps:
  - truth: "All UI strings (gui.recruits.*, key.recruits.*, category.recruits, chat.recruits.*, description.recruits.*, subtitles.recruits.*, recruits.*) render translated text instead of raw translation keys"
    test: 7
    outcome: pass
    note: "User confirmed in dev-client: Hire GUI button reads 'Hire' (en_us); Command/Faction screens show localized labels; switching language to ru_ru/de_de switches the labels accordingly. 81 code-referenced lang keys remain absent — all are pre-existing dirt (no source in legacy lang) or out-of-scope (vanilla / external mod keys); enumerated in the audit section below."
completed: 2026-04-15T15:30Z
---

## Summary

Gap closure for UAT test 7 — recruits-side UI keys rendered as raw translation strings because Wave 9 of Phase 21 migrated only entity / item / block lang keys; UI keys (`gui.recruits.*`, `key.recruits.*`, `category.recruits`, `chat.recruits.*`, `description.recruits.*`, `subtitles.recruits.*`, `recruits.*`, `gui.multiLineEditBox.*`) still referenced the legacy `recruits` namespace and code referenced them verbatim.

Mechanically merged each `recruits/src/main/resources/assets/recruits/lang/<locale>.json` into the matching `src/main/resources/assets/bannermod/lang/<locale>.json` for the five legacy locales (en_us, ru_ru, de_de, ja_jp, tr_tr). For es_es (no legacy recruits source) seeded from en_us as a fallback. Skipped any key already present in the target. Skipped `entity.recruits.*` / `item.recruits.*` / `block.recruits.*` entirely (Wave 9 already migrated those to bannermod namespace).

## Per-locale merge stats

| Locale | Kept | Skipped (obsolete entity/item/block) | Skipped (already present) | Inserted |
|--------|------|--------------------------------------|---------------------------|----------|
| en_us  | 521  | 23                                   | 1                         | 520      |
| ru_ru  | 520  | 23                                   | 1                         | 519      |
| de_de  | 520  | 23                                   | 1                         | 519      |
| ja_jp  | 520  | 23                                   | 1                         | 519      |
| tr_tr  | 520  | 23                                   | 1                         | 519      |
| es_es  | 521  | 23                                   | 0                         | 521      |

es_es uses en_us values as a fallback. Future Spanish-translation work is a backlog item, NOT a Phase 21 blocker.

## Verification gates

- All six target lang files re-parse as valid JSON (`python3 -m json.tool`).
- `gui.recruits.hire_gui.text.hire` is present in all six target files.
- `./gradlew --no-daemon processResources`: BUILD SUCCESSFUL.
- `git diff` shows only insertions on existing key entries (the only `-` lines are the trailing `}` and the previously-final entry's missing comma — its value is unchanged).
- Code-vs-lang audit: see "Pre-existing gaps" below.

## Code-vs-lang audit (extra check requested by user)

Ran a sweep of `src/main/java/` for every `Component.translatable("...")`-shaped string and matched against the merged `bannermod/lang/en_us.json`. Of 609 candidate keys, 81 remain absent. All 81 are out of scope or pre-existing dirt:

| Bucket | Count | Disposition |
|--------|-------|-------------|
| `chat.workers.*`     | 43 | Pre-existing — `workers/src/main/resources/assets/workers/lang/en_us.json` does not contain these keys either; never had translations. |
| `gui.workers.*`      | 17 | Pre-existing — same reason. |
| `key.workers.open_command_screen` | 1 | Pre-existing — never in legacy workers lang. |
| `entity.recruits.{recruit,recruit_shieldman,bowman,crossbowman,horseman,nomad}` | 6 | **Real bug** — string comparisons in `MessageWriteSpawnEgg.java:169-179` against entity description IDs that no longer exist (entities now register as `entity.bannermod.*` per Wave 9). The `if/else if` chain therefore never matches; spawn-egg-write logic is silently broken. Out of Phase 21 scope; flagged for follow-up. |
| `gui.recruits.{claim_edit.title, select_faction_screen.title, villager_noble.error_hire_limit_reached}` | 3 | Pre-existing — never in legacy recruits lang either. |
| `commands.team.*`, `argument.player.unknown` | 5 | Vanilla Minecraft keys, present at runtime. |
| `item.musketmod.*` | 6 | External mod keys, present when musketmod is loaded. |

Conclusion: the merge filter captured everything that *could* be carried over from legacy. Remaining gaps require new translation strings or are bugs in code logic, neither of which falls under "Phase 21 source-tree consolidation".

## Commits

- `6f151cb` fix(21-13): merge recruits-side UI lang keys into bannermod namespace

## Unblocked by this fix

- UAT test 7 closes for the UI surface.
- Every recruits-side GUI made reachable by 21-11 (Hire) and 21-12 (Command / Faction / Map) now renders localized labels in en_us / ru_ru / de_de / ja_jp / tr_tr; es_es shows English fallback.

## Pointers

- `src/main/resources/assets/bannermod/lang/{en_us,ru_ru,de_de,ja_jp,tr_tr,es_es}.json` (~520 inserted keys per locale)
- `recruits/src/main/resources/assets/recruits/lang/*.json` (frozen as historical source)
- `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-UAT.md` — test 7 + Gaps section
- Follow-up backlog: `MessageWriteSpawnEgg.java:169-179` `entity.recruits.*` comparison bug (independent of Phase 21).
