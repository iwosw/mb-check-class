# BannerMod

BannerMod turns Minecraft into a living multiplayer kingdom sandbox: found a settlement, grow it into a state, raise citizens and soldiers, command formations, trade, defend your land, and fight regulated wars that have real consequences.

It is built from the spirit of Villager Recruits and Workers, but aims higher than “spawn some guards” or “place a few work zones”. The goal is a server mod where towns feel alive, armies are readable, wars are planned events, and players have reasons to build, negotiate, raid, defend, and rebuild.

## What You Do

- Build a starter fort and turn it into a working settlement.
- Bring in citizens and give them jobs.
- Mark farms, storage, markets, workshops, barracks, and other buildings.
- Grow from a small settlement into a real state.
- Recruit and command troops in formations.
- Use shield walls, ranged lines, cavalry, commanders, and siege objectives.
- Declare formal wars instead of starting random 24/7 grief fights.
- Fight during scheduled battle windows so wars become events players can show up for.
- Place siege standards to create battle zones.
- Win tribute, vassalage, occupation, territory, or peace through server-backed outcomes.

## Why It Exists

Old Recruits was great at giving players soldiers. Old Workers was great at giving settlements labor. BannerMod is the merged idea: a multiplayer world where the economy, citizens, armies, claims, and wars all belong to one game loop.

The design target is not a chaotic raid mod. It is a roleplay-friendly war sandbox for servers:

- wars are declared;
- battle times are known;
- neutral players are protected;
- outcomes are recorded;
- settlements matter;
- armies should look and behave like armies.

## Current Player Docs

- `MULTIPLAYER_GUIDE.md` — give this to players.
- `BANNERMOD_BACKLOG.md` — unfinished work and future slices.
- `DEVELOPMENT.md` — short map for developers and agents.

## Project Status

BannerMod is an active merge/stabilization workspace. The root `src/**` tree is the live mod. Old `recruits/` and `workers/` folders are historical reference, not separate live mods.

The mod is playable in pieces, but still under heavy development. The biggest remaining work is player-facing UI, deeper settlement onboarding, polished war outcomes, better siege flow, and more tactical combat behavior.

## For Developers

```bash
./gradlew compileJava
./gradlew test
```

Keep unfinished work in `BANNERMOD_BACKLOG.md`. Do not revive old duplicate faction, diplomacy, or siege systems as parallel gameplay.
