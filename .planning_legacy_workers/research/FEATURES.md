# Feature Landscape

**Domain:** Minecraft Forge villager-worker automation mod revival
**Researched:** 2026-04-05

## Table Stakes

Features players will treat as baseline for a 2025 villager-worker automation mod, especially when reviving an existing Workers-style mod rather than inventing a new one.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Reliable worker hiring, ownership, and reassignment | The current Villager Workers listings promise hireable workers; MineColonies also treats worker assignment and management as core UI, not an optional extra. | Low | Must work with existing worker entities and owner/team data. Reassignment should not wipe worker state. |
| Server-authoritative work areas with visible bounds | Players expect to define where a worker operates, edit it later, and trust it on dedicated servers. This already exists architecturally via work-area entities and update packets. | Med | Table stakes for all profession flows. Needs overlap/permission validation and clean client sync. |
| Per-worker home/base storage binding | Players expect each worker to know where to return, restock, and deposit. The mod’s own listing explicitly advertises inventories and chest deposit. | Med | Existing code already implies worker inventory, upkeep, deposit thresholds, and storage goals. Finish the loop rather than redesign it. |
| Predictable inventory loop: take tools/materials -> work -> keep essentials -> deposit surplus | 2024-2025 releases for Villager Workers focused heavily on deposit fixes, chest access, and forced upkeep/deposit, which signals this is core player pain. | High | Must cover double chests, missing storage, sleeping/deposit edge cases, and no dupes/loss. |
| Farmer loop: till/plant/harvest/replant within assigned area | This is the minimum expectation for any worker-farming mod. The current public mod description already documents 9x9 farming and seed management. | Med | Replanting and reserve-seed behavior are mandatory. Bone meal/fertilizer support is nice but secondary. |
| Miner loop: clear assigned pattern safely and consistently | The public mod page already advertises 8 mining modes and command-assigned start blocks, so “miner actually completes the ordered dig area” is table stakes. | High | Must not destroy protected/unbreakable blocks, strand itself, or stop permanently after chunk reload. Torch placement and stair/fill logic strongly affect usability. |
| Builder/build-area completion of intended existing mechanics | Because this is a recovery project and the codebase already contains build areas, templates, scans, and required-item handling, players will expect builders to use those systems, not leave them half-functional. | High | Recovery scope means: finish scanning/loading, required-resource display, placement progression, and resume behavior; do not expand into full MineColonies-style colony construction. |
| Animal work loops with target-count behavior | The public mod body describes shepherd/chicken/cattle/swineherd workers breeding or slaughtering to maintain counts. Users will expect this to be stable if those professions ship. | Med | Needs assigned area, breeding item access, reserve logic, and clear target settings. |
| Clear “missing item / blocked” feedback | MineColonies’ request system and resource UIs set the expectation that workers tell players what they lack instead of silently idling. | Med | For recovery scope, a minimal but accurate status/reason display is enough; do not build a giant colony-wide logistics dashboard. |
| Recall/unstick behavior | Modern worker mods expose some way to recall or reset workers because pathing failures are common and tolerated only if recoverable. | Low | Existing GUI and entity command patterns suggest this should be straightforward. |
| Dedicated-server and multiplayer-safe permissions | Players expect owner/team/admin gating on work-area edits, inventory access, and worker commands. MineColonies explicitly treats permissions as core colony UX, and this codebase already has ownership/team checks. | Med | Must prevent non-owners from hijacking workers or editing areas; must work without client-side trust. |
| Save/reload persistence for assignments, areas, inventories, and progress | Automation mods are judged by whether loops survive relog, restart, and chunk unload. | High | NBT persistence exists in architecture; the recovery work is making all core loops resume cleanly. |
| Reasonable pathing and failure recovery | Users will forgive imperfect AI, but not workers getting stuck forever on fences, double chests, boats, or chunk borders. The mod’s recent changelogs repeatedly fixed these exact issues. | High | Pathing polish is table stakes because broken pathing equals broken automation. |

## Differentiators

Valuable features, but safe to defer during recovery if they threaten the 1.21.1 port or core-loop stability.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Colony-wide logistics/request system with courier workers | Powerful automation upgrade; aligns with what MineColonies players recognize as advanced logistics. | High | The public Villager Workers page says “Courier Update” is still coming soon, so do not treat this as required for parity. Simple per-worker chest loops are enough for baseline. |
| Global request board / clipboard / postbox-style UX | Makes large settlements manageable and reduces running between workers. | High | Strong differentiator, but outside recovery scope unless partial infrastructure already exists. |
| Advanced builder resource planner and live required-resources UI polish | Great for large builds and highly visible to players. | Med | Finish existing required-item/build progress screens first; extra planning features can wait. |
| Merchant waypoint economy and route scheduling | Distinctive flavor feature already advertised by the public mod, especially with boats/ships. | High | Worth keeping if existing code is close, but should not block core farm/mine/build/storage recovery. |
| Multi-warehouse / networked logistics | Helps large multiplayer towns scale. | High | Avoid unless current code already nearly supports it. Single-storage-area correctness matters more. |
| Worker leveling, stats, or specialized tool quality systems | Adds long-term progression and player attachment. | Med | Nice-to-have only. Public page still lists leveling as “coming soon,” so parity does not require inventing it now. |
| Automated fertilizer/bone-meal optimizers, stock rules, or craft chains | Improves throughput and reduces micromanagement. | Med | Defer until basic loops are stable and test-covered. |
| Small Ships / boat-heavy merchant or fishing integrations | Cool ecosystem synergy and good marketing feature. | High | Integration risk is high for a port/revival; keep optional. |
| Chunk-loading or long-range autonomous routing features | Helps persistent SMP use. | High | Easy source of exploits/performance bugs. Only keep if already present and well-bounded. |

## Anti-Features

Features or directions that are tempting but wrong for this recovery milestone.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| Rebuilding the mod into a full MineColonies-style colony sim | Explodes scope and conflicts with the project goal of recovering existing mechanics. | Finish per-worker work-area automation and limited logistics already implied by the codebase. |
| Adding brand-new professions just because the ecosystem has them | New professions multiply AI, UI, storage, and test scope. | Stabilize the professions already implemented or publicly promised. |
| Designing a complex global logistics economy before chest deposit works | Players notice broken local loops first. | Make single-worker storage/inventory loops airtight, then revisit courier/global logistics later. |
| Heavy client-only control logic | Multiplayer and dedicated servers will desync or become exploitable. | Keep the server authoritative; let screens only send validated commands. |
| Hiding blocked states behind silent idle AI | Feels buggy and makes recovery testing much harder. | Expose explicit status such as missing tool, no storage, area invalid, no path, or waiting for items. |
| Over-configuring every edge case before baseline stability | Large config surfaces slow testing and porting. | Ship a few robust defaults and only preserve configs already central to existing behavior. |
| Pathfinding “smartness” rewrites during the 1.21.1 port | High risk, hard to validate, easy to regress all professions. | Fix common stuck cases, recall behavior, and retry logic first. |
| Cross-chunk always-on automation without limits | Common source of lag, dupes, and server complaints. | Prefer “works while loaded” plus clean resume on reload unless current design explicitly supports more. |

## Feature Dependencies

```text
Worker hire/ownership -> assignment UI -> work-area editing -> profession work loop
Storage binding -> inventory reserve/deposit logic -> stable farm/mine/build automation
Server-authoritative packets -> multiplayer safety -> dedicated-server usability
NBT persistence -> chunk unload/relog resume -> trustworthy automation
Builder template loading/scanning -> required-items tracking -> usable build flow
Unstick/recall + status feedback -> acceptable pathing failures -> multiplayer supportability
```

## MVP Recommendation

Prioritize:
1. **Work-area assignment and editing that persists correctly**
2. **Storage/inventory loops that never lose, dupe, or strand items**
3. **Profession-complete farm, mine, and build flows using the mechanics already present in code**

Defer: **Courier/global logistics system**: high value, but not essential to a recovery release if per-worker chest/storage loops are solid.

## 2025 Recovery Guidance

- Treat **farm, mine, build, storage, and multiplayer correctness** as the acceptance bar.
- Treat **merchant/courier polish, leveling, and broad colony-management UX** as later-phase differentiators.
- If a feature is publicly advertised by the existing mod page but only partially implemented, prefer **minimal honest completion** over ambitious redesign.

## Sources

- HIGH: Project context and codebase architecture in `/home/kaiserroman/workers/.planning/PROJECT.md` and `/home/kaiserroman/workers/.planning/codebase/ARCHITECTURE.md`
- HIGH: Villager Workers Modrinth project page and 2024-2025 version changelogs: `https://api.modrinth.com/v2/project/Pqlv7VM3`, `https://api.modrinth.com/v2/project/Pqlv7VM3/version`
- HIGH: MineColonies 2026 wiki pages for current worker expectations and multiplayer/admin UX:
  - `https://minecolonies.com/wiki/buildings/builder/`
  - `https://minecolonies.com/wiki/buildings/farmer/`
  - `https://minecolonies.com/wiki/buildings/miner/`
  - `https://minecolonies.com/wiki/buildings/warehouse/`
  - `https://minecolonies.com/wiki/buildings/deliveryman/`
  - `https://minecolonies.com/wiki/systems/worker/`
  - `https://minecolonies.com/wiki/systems/request/`
  - `https://minecolonies.com/wiki/systems/protection/`
- MEDIUM: Modrinth ecosystem search for related/current projects and support mods: `https://api.modrinth.com/v2/search?query=villager%20worker&limit=10&index=relevance`
