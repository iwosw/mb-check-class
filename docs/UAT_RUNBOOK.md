# BannerMod Warfare-RP UAT Runbook

Reproducible smoke flow for a dev or operator to verify the warfare slice end-to-end on a fresh server. Target time: 5–10 minutes. Every step lists the expected result and the failure signal so a green run is unambiguous.

Prerequisites:
- `compileJava` is green on the target build.
- A dev world with two op-level players (or one op + one client). Player A hosts state X, player B hosts state Y. Names below: `playerA`, `playerB`.

## 1. State creation (POL-001 / UI-001)

1. `playerA` opens the Political Entities screen via War Room → "Political Entities".
2. Click **Create**, name `Aurelia`. Confirm dialog accepts.
3. Click on the new row → click **Capital here** while standing on a 1-block claimed-by-A area.
4. Toggle government form to `REPUBLIC`, then back to `MONARCHY`.

Expected: state appears in the list with `MONARCHY` badge, capital position visible in detail panel, color picker accepts a `RRGGBB` hex; charter accepts text up to 256 chars.
Failure signal: `infrastructure_insufficient` reason on capital change (precondition not met — set up town hall + storage + market first), or denial because the player is not leader (auth path); chat shows the denial token.

## 2. Settlement infrastructure (SETTLEMENT-003 gate)

1. Place a starter fort with `BuildingPlacementWandItem`.
2. Place storage area + market area inside the same claim.
3. Run `/bannermod state promote Aurelia STATE`.

Expected: promotion succeeds; the political entity now shows `STATE` status.
Failure signal: `infrastructure_insufficient` — confirm all three buildings registered through the surveyor, not just placed.

## 3. War declaration (WAR-001 / WAR-005)

1. `playerA` declares war on `Beorna` (state hosted by `playerB`): `/bannermod war declare Aurelia Beorna`.
2. `playerA` invites an ally: War Room → Allies → Invite Attacker → pick state `Caldar` (third state). The third leader accepts via `/bannermod war ally accept <id>` or right-click decline on the row.
3. War status becomes `PRE_ACTIVE` then `ACTIVE` after configured wait.

Expected: audit log includes `DECLARED`, `ALLY_INVITED`, `ALLY_JOINED`. PvP gate now allows attacker-side hits on defender-side; same-side friendly fire still blocked.
Failure signal: `WAR_NOT_PRE_ACTIVE` on accept — invitation expired because war advanced. `INVITEE_ON_OPPOSING_SIDE` — pick a different ally.

## 4. Battle window + siege standard (UI-002 / WAR-006)

1. Wait until the configured `BattleWindowSchedule` opens. War Room top banner reads `Battle window: OPEN ...`.
2. `playerA` stands inside `Beorna`'s claim, opens War Room, clicks **Place siege here** for the active war.

Expected: a `siege_standard` block spawns at the player's position with the political-color cap rendered above it. War zone overlay banner appears on screen when standing inside the radius.
Failure signal: button disabled — `playerA` is not the leader of either side; or war already RESOLVED. Block doesn't render colored cap — political color is unset on the placing side.

## 5. PvP gate verification (WAR-005)

1. With battle window OPEN, an attacker-side recruit / player attacks a defender-side recruit / player.
2. With battle window OPEN, an attacker-side player attacks a defender-side ally player.

Expected: both hits land. `applyPvpGate` returns ALLOW.
Failure signal: hit is cancelled with denial token in chat. Verify the hitter is in the right scoreboard team (`/team list`).

## 6. Outcome + occupation tax (WAR-001 / WAR-002 / WAR-004)

1. Resolve the war: `/bannermod war occupy <warId> 1` while standing in defender's center chunk.
2. Wait `OccupationTaxIntervalDays` (default 1 day; set to a few hundred ticks via config for a smoke run).
3. `/bannermod treasury show Aurelia` → expect attacker ledger increased by ~tax.
4. `/bannermod treasury show Beorna` → expect defender ledger decreased or `OCCUPATION_TAX_DEFAULTED` audit if defender ran dry.
5. Try declaring a new war on the freshly occupied `Beorna` immediately — expect `LOST_TERRITORY_IMMUNITY` denial.

Expected: occupation record placed; war RESOLVED; per-second tax ticker writes `OCCUPATION_TAX_PAID` audit on success and advances `lastTaxedAt` even on default; immunity denial fires within `LostTerritoryImmunityDays`.
Failure signal: tax doesn't tick — `OccupationTaxAmountPerChunk` or `OccupationTaxIntervalDays` set to 0 (disabled). Treasury didn't move — defender has no claim ledger. Immunity didn't trigger — `WarCooldownRuntime` not initialized for this level.

## 7. Revolt path (WAR-003, objective-presence driven)

1. Defender or third-party calls `/bannermod war revolt <occupationId>`.
2. Wait until the revolt is due and the battle window is open.
3. Put only rebel-side recruits/players at the first occupied objective chunk.
4. Repeat with defender/occupier presence at the objective.
5. Repeat with the objective empty.

Expected: rebel-only presence resolves the revolt as SUCCESS and removes the occupation; any defender/occupier presence resolves it as FAILED; an empty objective stays PENDING for the next open-window evaluation instead of resolving by timer alone.

## Cleanup

1. `/bannermod war annul <warId>` (or wait for natural expiry).
2. `/bannermod state demote Aurelia SETTLEMENT` to revert promotion.
3. Remove siege standard with vanilla pickaxe (op only) or wait for war end.

## Known limitations to record on each run

- WAR-003 revolt is timer-only — empty revolt may auto-win today.
- Occupied-claim **player-facing** UI panel is not yet shipped (audit + treasury are observable to ops via slash commands; War Room panel is on the WAR-002 follow-up list).
- Two pre-existing flake tests (see backlog `FLAKE-001`/`FLAKE-002`) are unrelated to this flow but may surface during full `verifyGameTestStage` runs.
