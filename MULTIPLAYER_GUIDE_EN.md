# BannerMod Multiplayer Guide

BannerMod adds settlements, workers, armies, political states, and wars. This guide is written for regular server players, not for developers.

Main rule: land, settlement, workers, recruits, and political state must agree on ownership. If ownership diverges, the game can stop settlement work or reject an action.

## Three Separate Concepts

**Claim** means protected land on the map. It answers: "whose territory is this?"

**Settlement** means the live base inside a claim: buildings, storage, markets, workers, residents, projects, and work.

**Political state** means the side that owns land, joins wars, has leaders, and can have allies.

Do not treat these as the same thing. A claim can be land owned by a state, a settlement can live on that land, and the state makes political decisions.

## First 10 Minutes

1. Pick a place for your base.
2. Claim the area for your side.
3. Place the main center: starter fort or town hall if your server has it enabled.
4. Mark work sites: farm, storage, market, mine, lumber area, build area.
5. Spawn, hire, or grow workers depending on server rules.
6. Assign workers to clear work places.
7. If you recruit soldiers, keep food, payment, and clear orders available.

If something fails, first check whether it is inside your claim and whether the worker or recruit belongs to your side.

## Promoting A Settlement Into A State

A settlement cannot become a full state without infrastructure. Promotion requires:

- starter fort or town hall;
- storage;
- market.

If promotion is denied with a reason like `infrastructure_insufficient`, build and register the missing object, then try again.

## Workers And Residents

Workers choose jobs through registered buildings and work areas. Storage, markets, farms, mines, and build areas give the settlement different capabilities.

Important checks:

- the work area must be inside a friendly claim;
- the worker must belong to the right side;
- the settlement must see the registered building or work area;
- if a claim is captured, removed, or becomes mismatched, work can stop.

Settlements have internal work orders. Some order state already survives server reloads, including worker claims. Transport orders can store source, destination, resource filter, and item count; actual courier behavior depends on the current server build.

## Storage, Markets, And Trade

Storage helps the settlement understand where resources are. Markets support economy and trade. Port or sea-entry points can affect settlement trade hints.

If the settlement lacks resources, check:

- storage exists;
- market is registered;
- a source of goods exists;
- trade is not blocked by war, ownership, or server configuration.

## Recruits And Armies

Recruits follow their owner. Allies on the same side can help with group commands if the server allows it and they are close enough.

Army stances:

- loose: normal flexible behavior;
- line hold: the unit holds formation more strongly;
- shield wall: stronger frontal defense, slower movement and turning.

Battle tips:

- formation beats a loose crowd;
- shields work best when facing the enemy;
- spears and pikes reach farther than short weapons;
- second-rank spearmen can attack through allies;
- side and back hits are more dangerous;
- pikes are useful against cavalry;
- ranged units need room behind the front line.

## Political States And War Room

War Room shows political sides as records: status, leader, capital, color, region, government form, and wars.

Government form affects authority. Monarchy keeps key decisions leader-only; republic can extend some authority to co-leaders.

## Wars And Sieges

BannerMod tries to make wars organized. A war has sides, state, objective, and battle window. Sieges use siege standards to mark important battle zones.

War Room can show:

- active wars;
- attacker and defender;
- war state;
- war objective;
- allied sides;
- siege standards with side, position, and radius.

If a war or siege action is denied, the usual reasons are:

- you are not leader or operator;
- you are on the wrong side;
- it is not the right battle window;
- the placement position is invalid;
- territory belongs to the wrong side;
- settlement infrastructure is missing;
- the target has a temporary cooldown after defeat or peaceful-status changes.

## Quick Troubleshooting

- Workers do nothing: check claim, worker side, registered building, storage/market, and whether the territory was captured.
- Recruits ignore orders: move closer, check owner/side, and relog after a server restart.
- State promotion fails: add starter fort or town hall, storage, and market inside the settlement.
- Siege placement fails: open War Room, select an active war, and verify you are leader of the correct side.

In short: claim land, build inside your claim, register important buildings, keep workers and armies on the same side, use formations in battle, and check War Room before war.
