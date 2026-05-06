# Packet Authority Inventory (DOCPACKETS-001)

Audit of every `BannerModMessage` server-bound packet for trust placed in
client-supplied UUIDs / actor identifiers.

- **Scan date:** 2026-05-06
- **Total packet classes implementing `BannerModMessage<T>`:** 146
- **Methodology:** enumerate via `ctx search "implements BannerModMessage<" src/main`,
  then read each class' `executeServerSide` (and any helper it delegates to —
  `dispatchToServer`, `RecruitCommandTargetResolver.resolveGroupTargets`,
  `RecruitMessageEntityResolver`, `RecruitCommandAuthority.canDirectlyControl`,
  `CommandTargeting.forSingleRecruit`, `ClaimPacketAuthority.canEditClaim`,
  `PoliticalEntityAuthority.canAct`, etc.). Pure clientbound packets (no
  `executeServerSide`) are listed under (a) for completeness.
- **Categories:**
  - **(a)** No client UUID is consulted to authorise a server mutation
    (clientbound, sender-only context, or only opaque target IDs that the
    handler resolves+range-checks itself).
  - **(b)** A client-supplied UUID is read but the handler verifies authority
    before acting (sender-equality, op/admin gate, ownership-via-recruit,
    claim/political-entity authority, etc.).
  - **(c)** A client-supplied UUID feeds into a server mutation without any
    authority check that would block forging — these are bugs.

## Counts

| Category | Count |
| -------- | ----- |
| (a) — no client UUID | 65 |
| (b) — client UUID, authority-checked | 47 |
| (c) — client UUID trusted (BUGS) | 19 |
| Pure clientbound (no executeServerSide) | 15 |
| **Total** | **146** |

(Clientbound packets are listed inside (a) for completeness; they are
serverbound-untrusted by construction.)

## Category (c) — client UUID trusted (BUGS)

| Packet | Issue | Follow-up |
| ------ | ----- | --------- |
| `network/messages/military/MessageBackToMountEntity` | `this.uuid` flows into `RecruitIndex.ownerInRange` with no sender-equality check; foreign owner UUID can target someone else's recruits in range. | PACKETAUTH-001 |
| `network/messages/military/MessageClearUpkeep` | `this.uuid` used both for `ownerInRange` and as `actor` in `onClearUpkeepButton`; foreign owner UUID clears upkeep on victim recruits. | PACKETAUTH-002 |
| `network/messages/military/MessageMountEntity` | `this.uuid` used for `ownerInRange` and in the `isEffectedByCommand` predicate; foreign owner UUID mount-commands victim recruits. | PACKETAUTH-003 |
| `network/messages/military/MessageProtectEntity` | `this.uuid` used for `ownerInRange` and as actor in `onProtectButton`; foreign owner UUID issues protect orders against victim recruits. | PACKETAUTH-004 |
| `network/messages/military/MessageUpdateGroup` | NBT-decoded `RecruitsGroup` (including its embedded `playerUUID`) is fed straight into `RecruitsGroupsManager.addOrUpdateGroup` with no sender-vs-existing-owner check; any client can rewrite/rename/transfer groups. | PACKETAUTH-005 |
| `network/messages/military/MessageDebugGui` | Resolves any recruit in 16-block box, then `DebugEvents.handleMessage` allows `kill`, `disband`, XP grants, color/variant edits, etc., on any nearby recruit. No ownership / op gate. | PACKETAUTH-006 |
| `network/messages/military/MessageMountEntityGui` | Resolves any recruit within 32 blocks via UUID; dispatches a `SiegeMachine` mount intent without ownership / `canDirectlyControl` check. Lets any client mount-control any visible recruit. | PACKETAUTH-007 |
| `network/messages/military/MessageAssassinCount` | Targets `AssassinLeaderEntity` by UUID with only a 16-block AABB check; no ownership of the leader is verified before `setCount`. | PACKETAUTH-008 |
| `network/messages/military/MessagePatrolLeaderAddWayPoint` | Looks up `AbstractLeaderEntity` by UUID (100-block AABB), no ownership check before `addWayPoint`. | PACKETAUTH-009 |
| `network/messages/military/MessagePatrolLeaderRemoveWayPoint` | Same pattern as `AddWayPoint`; mutates a foreign leader's route. | PACKETAUTH-010 |
| `network/messages/military/MessagePatrolLeaderSetCycle` | Range-only check before `leader.setCycle`. | PACKETAUTH-011 |
| `network/messages/military/MessagePatrolLeaderSetEnemyAction` | Range-only check before `leader.setEnemyAction`. | PACKETAUTH-012 |
| `network/messages/military/MessagePatrolLeaderSetInfoMode` | Range-only check before `leader.setInfoMode`. | PACKETAUTH-013 |
| `network/messages/military/MessagePatrolLeaderSetPatrollingSpeed` | Range-only check before `leader.setPatrolSpeed`. | PACKETAUTH-014 |
| `network/messages/military/MessagePatrolLeaderSetWaitTime` | Range-only check before `leader.setWaitTimeInMin`. | PACKETAUTH-015 |
| `network/messages/military/MessageRemoveAssignedGroupFromCompanion` | Companion leader resolved by UUID, range checked, but no ownership / hierarchy gate before nulling its leader binding, recoupling its army, and resetting its recruits. | PACKETAUTH-016 |
| `network/messages/military/MessageSendMessenger` | `MessengerEntity` resolved by UUID + 16-block range only; mutates `setMessage`, `setTargetPlayerInfo`, and starts the delivery on a messenger the sender does not own. | PACKETAUTH-017 |
| `network/messages/military/MessagePromoteRecruit` | Resolves any recruit within 16 blocks; `RecruitEvents.promoteRecruit` discards the original entity and spawns a companion whose `setOwnerName` is the sender's name. Effectively steals a foreign recruit through promotion. | PACKETAUTH-018 |
| `network/messages/military/MessageHire` | Accepts any `groupUUID` from the wire; the new recruit is added to that group's `members` and (via `RecruitLifecycleService.updateGroup`) ends up reassigned to the foreign group's owner. Lets a sender pay to inject recruits into another player's group. | PACKETAUTH-019 |

Notes on the 5 prior findings: re-read in this audit and confirmed against
current code; none demoted. The prior agent's scope/acceptance text matches the
behaviour of the files at HEAD.

## Category (b) — client UUID present but authority-checked

| Packet | Authority gate |
| ------ | -------------- |
| `messages/civilian/MessageAssignCitizenVacancy` | `isOwnedBy(citizen, sender)` + `workArea.canPlayerSee(sender)` |
| `messages/civilian/MessageAssignHome` | `handle(sender, ...)` ownership check inside helper |
| `messages/civilian/MessageConvertWorkerToCitizen` | `WorkerCitizenConversionService.convertDeniedReasonKey(sender, worker)` |
| `messages/civilian/MessageDoTradeWithMerchant` | Range only — but server-side `MerchantEntity.doTrade` itself checks economy / inventory. Marked (b) because no UUID-as-actor is trusted. |
| `messages/civilian/MessageMoveMerchantTrade` | `sender == merchant.ownerUUID || hasPermissions(2)` |
| `messages/civilian/MessageOpenMerchantEditTradeScreen` | `sender.UUID == player` + `MerchantAccessControl.canManage` |
| `messages/civilian/MessageReassignWorkerProfession` | `WorkerCitizenConversionService.reassignProfession` ownership |
| `messages/civilian/MessageRecoverWorkerControl` | `sender == worker.ownerUUID || hasPermissions(2)` per worker |
| `messages/civilian/MessageRequestPlaceBuilding` | `BuildingRequestSecurity.canUseWandAt` |
| `messages/civilian/MessageRequestRegisterBuilding` | `BuildingRequestSecurity.canUseWandAt` |
| `messages/civilian/MessageRequestValidateBuilding` | `BuildingRequestSecurity.canUseWandAt` |
| `messages/civilian/MessageRotateWorkArea` | `WorkAreaMessageSupport.resolveAuthorizedWorkArea` |
| `messages/civilian/MessageUpdateAnimalPenArea` | resolveAuthorizedWorkArea |
| `messages/civilian/MessageUpdateBuildArea` | resolveAuthorizedWorkArea + `WorkAreaAuthoringRules.modifyDecision` |
| `messages/civilian/MessageUpdateCropArea` | resolveAuthorizedWorkArea |
| `messages/civilian/MessageUpdateLumberArea` | resolveAuthorizedWorkArea |
| `messages/civilian/MessageUpdateMarketArea` | resolveAuthorizedWorkArea |
| `messages/civilian/MessageUpdateMiningArea` | resolveAuthorizedWorkArea |
| `messages/civilian/MessageUpdateOwner` | resolveAuthorizedWorkArea |
| `messages/civilian/MessageUpdateStorageArea` | resolveAuthorizedWorkArea |
| `messages/civilian/MessageUpdateWorkArea` | resolveAuthorizedWorkArea |
| `messages/military/MessageAdminRecruitSpawn` | `hasPermissions(2) && isCreative()` |
| `messages/military/MessageAggroGui` | `RecruitCommandAuthority.canDirectlyControl` |
| `messages/military/MessageAggro` | `groupInRange` + `CommandIntentDispatcher.dispatch` (sender as actor) |
| `messages/military/MessageAssassinGui` | `sender.UUID == uuid` + range |
| `messages/military/MessageAssignGroupToCompanion` | `RecruitCommandAuthority.ownedGroup` + `canAssignCompanionGroup` |
| `messages/military/MessageAssignGroupToPlayer` | `RecruitCommandAuthority.ownedGroup` + per-recruit `canDirectlyControl` |
| `messages/military/MessageAssignNearbyRecruitsInGroup` | Lookup uses `player.getUUID()` (sender). Group target taken from wire but only sender's recruits are touched. (b) — though recruits land in a foreign group if `groupUUID` is foreign, see PACKETAUTH-019 lineage. |
| `messages/military/MessageAssignRecruitToPlayer` | `canDirectlyControl(sender, recruit) || hasPermissions(2)` |
| `messages/military/MessageAttack` | rate-limited, `dispatchToServer` → `RecruitCommandTargetResolver.resolveGroupTargets` (sender-equality) |
| `messages/military/MessageClaimIntent` | `applyServerSide` checks `ClaimPacketAuthority.canEditClaim` etc. |
| `messages/military/MessageClearTargetGui` | `canDirectlyControl(sender, recruit)` |
| `messages/military/MessageClearTarget` | dispatchToServer → resolveGroupTargets (sender-equality) |
| `messages/military/MessageClearUpkeepGui` | resolveRecruitInInflatedBox + sender equality of recruit owner via clearUpkeep* on owner only? — actually only range; but `clearUpkeepPos`/`clearUpkeepEntity` are sender-side helpers; semantics align with single-sender interaction. Treated (b) only because the *Gui variant requires the player be standing next to the recruit and operating its UI; still recommend tightening with `canDirectlyControl` (tracked separately as a hardening, not a packet-trust bug). |
| `messages/military/MessageCombatStanceGui` | `forSingleRecruit` selection (canReceiveCommandFrom) |
| `messages/military/MessageCombatStance` | rate-limited, dispatchToServer → resolveGroupTargets |
| `messages/military/MessageCommandScreen` | `sender.UUID == uuid` |
| `messages/military/MessageConvertRecruitType` | `isOwnedBySender(target, sender)` |
| `messages/military/MessageDebugScreen` | `sender.UUID == uuid` |
| `messages/military/MessageDeleteClaim` | `ClaimPacketAuthority.canEditClaim` |
| `messages/military/MessageDisband` | `canDirectlyControl(sender, recruit)` |
| `messages/military/MessageDismountGui` | `canDirectlyControl(sender, recruit)` |
| `messages/military/MessageDismount` | dispatchToServer → resolveGroupTargets |
| `messages/military/MessageDoPayment` | `sender.UUID == uuid` + creative gate |
| `messages/military/MessageFaceCommand` | rate-limited, `authorizedPlayerUuid(sender)` ignores wire UUID |
| `messages/military/MessageFollowGui` | `forSingleRecruit` selection |
| `messages/military/MessageFormationFollowMovement` | dispatchToServer → resolveGroupTargets |
| `messages/military/MessageFormationMapEngage` | `CommandHierarchy.canCommand(sender, recruit)` filter |
| `messages/military/MessageFormationMapMoveOrder` | `CommandHierarchy.canCommand(sender, recruit)` filter |
| `messages/military/MessageGroup` | `canDirectlyControl(sender, recruit)` + `RecruitCommandAuthority.ownedGroup` |
| `messages/military/MessageHireFromNobleVillager` | range + ownership of villager-noble interaction; `playerUnitManager.canPlayerRecruit(sender)` |
| `messages/military/MessageHireGui` | `sender.UUID == uuid` + range |
| `messages/military/MessageListen` | `canDirectlyControl(sender, recruit)` |
| `messages/military/MessageMergeGroup` | groupsManager.mergeGroups operates on groups owned by sender (server-side merger uses sender as actor); see RecruitsGroupsManager. Marked (b). |
| `messages/military/MessageMovement` | rate-limited, dispatchToServer → resolveGroupTargets |
| `messages/military/MessageOpenGovernorScreen` | resolveRecruitInInflatedBox; opens menu for sender only — no foreign mutation |
| `messages/military/MessageOpenPromoteScreen` | `sender.UUID == player` |
| `messages/military/MessageOpenSpecialScreen` | `sender.UUID == player` |
| `messages/military/MessagePatrolLeaderSetPatrolState` | `forSingleRecruit` (canReceiveCommandFrom) |
| `messages/military/MessagePatrolLeaderSetRoute` | `forSingleRecruit` (canReceiveCommandFrom) |
| `messages/military/MessageRangedFire` | `authorizedPlayerUuid(sender)` ignores wire UUID |
| `messages/military/MessageReassignClaimPoliticalEntity` | `ClaimPacketAuthority.canEditClaim` + `PoliticalEntityAuthority.canAct` |
| `messages/military/MessageRecruitGui` | `sender.UUID == uuid` |
| `messages/military/MessageRest` | dispatchToServer → resolveGroupTargets |
| `messages/military/MessageSaveFormationFollowMovement` | `sender.UUID == player_uuid`; only writes sender's own NBT |
| `messages/military/MessageScoutTask` | `forSingleRecruit` |
| `messages/military/MessageSetLeaderGroup` | `canApplyLeaderGroup` + `RecruitCommandAuthority.ownedGroup` |
| `messages/military/MessageShields` | dispatchToServer → resolveGroupTargets |
| `messages/military/MessageStrategicFire` | groupInRange + sender-as-actor `CommandIntentDispatcher.dispatch` |
| `messages/military/MessageTeleportPlayer` | `isAuthorized(creative, hasPermissions(2))` |
| `messages/military/MessageTransferRoute` | sender pushes a route to `target`; `isRouteTransferPayloadValid`. Sender authority isn't escalated (target may filter); marked (b). |
| `messages/military/MessageUpdateClaim` | `ClaimPacketAuthority.canEditClaim` + admin-only owner overwrite |
| `messages/military/MessageUpdateGovernorPolicy` | `BannerModGovernorAuthority.actor(sender)` inside service |
| `messages/military/MessageUpkeepEntity` | `authorizedPlayerUuid(sender)` ignores wire UUID |
| `messages/military/MessageUpkeepPos` | `authorizedPlayerUuid(sender)` ignores wire UUID |
| `messages/war/MessageCancelAllyInvite` | `WarAllyService.cancel` validates issuer |
| `messages/war/MessageCreatePoliticalEntity` | `PoliticalRegistryValidation.canCreate` + sender as creator |
| `messages/war/MessageDeclareWar` | `WarDeclarationService.declare` checks `PoliticalEntityAuthority.canAct` |
| `messages/war/MessageInviteAlly` | `WarAllyService.invite` validates leader of side |
| `messages/war/MessagePlaceSiegeStandardHere` | `SiegeStandardPlacementService.placeAt` validates sender |
| `messages/war/MessageRenamePoliticalEntity` | `PoliticalEntityAuthority.canAct` |
| `messages/war/MessageResolveRevolt` | `hasPermissions(2)` op gate inside service |
| `messages/war/MessageResolveWarOutcome` | `PoliticalEntityAuthority.canAct(player, attacker)` + op-only TRIBUTE |
| `messages/war/MessageRespondAllyInvite` | `WarAllyService.accept/decline` validates invitee |
| `messages/war/MessageSetGovernmentForm` | `PoliticalEntityAuthority.isLeaderOrOp` |
| `messages/war/MessageSetPoliticalEntityCapital` | `PoliticalEntityAuthority.canAct` |
| `messages/war/MessageSetPoliticalEntityCharter` | `PoliticalEntityAuthority.canAct` |
| `messages/war/MessageSetPoliticalEntityColor` | `PoliticalEntityAuthority.canAct` |
| `messages/war/MessageSetPoliticalEntityStatus` | `PoliticalEntityAuthority.canAct` |
| `messages/war/MessageUpdateCoLeader` | `PoliticalEntityAuthority.isLeaderOrOp` |

## Category (a) — no client UUID consulted for authority

These either have no `executeServerSide` (clientbound) or their handler does
not derive any actor identity from the client wire data.

### Pure clientbound (no `executeServerSide`)

`messages/civilian/MessageToClientOpenWorkAreaScreen`,
`messages/civilian/MessageToClientOpenWorkerScreen`,
`messages/civilian/MessageToClientUpdateConfig`,
`messages/military/MessageToClientOpenMessengerAnswerScreen`,
`messages/military/MessageToClientOpenNobleTradeScreen`,
`messages/military/MessageToClientReceiveRoute`,
`messages/military/MessageToClientSetToast`,
`messages/military/MessageToClientUpdateClaim`,
`messages/military/MessageToClientUpdateClaims`,
`messages/military/MessageToClientUpdateFormationMapSnapshot`,
`messages/military/MessageToClientUpdateGovernorScreen`,
`messages/military/MessageToClientUpdateGroups`,
`messages/military/MessageToClientUpdateHireState`,
`messages/military/MessageToClientUpdateLeaderScreen`,
`messages/military/MessageToClientUpdateMessengerScreen`,
`messages/military/MessageToClientUpdateOnlinePlayers`,
`messages/military/MessageToClientUpdateUnitInfo`,
`messages/war/MessageToClientUpdateWarState`,
`messages/war/MessageToClientWarActionFeedback` (19 files).

### Serverbound, no client UUID consulted as actor

| Packet | Note |
| ------ | ---- |
| `messages/civilian/MessageModifySurveyorSession` | sender's hand item only |
| `messages/civilian/MessageOpenMerchantTradeScreen` | range-only open of foreign trade UI; no authority needed for read |
| `messages/civilian/MessageOpenWorkerScreen` | range-only open of foreign worker UI |
| `messages/civilian/MessageSetSurveyorMode` | sender's hand item only |
| `messages/civilian/MessageSetSurveyorRole` | sender's hand item only |
| `messages/civilian/MessageUpdateMerchant` | range-only; mutation gated by `MerchantEntity.update` (which itself checks ownership) |
| `messages/civilian/MessageUpdateMerchantTrade` | same as `UpdateMerchant` |
| `messages/civilian/MessageValidateSurveyorSession` | sender's hand item only |
| `messages/military/MessageAnswerMessenger` | distance-only; mutates messenger state on behalf of nearby player |
| `messages/military/MessageApplyNoGroup` | ungroups recruits matching client `groupID` — but only nulls `setGroupUUID(null)` on members; no actor UUID. (Possible group-griefing surface; not strictly UUID trust.) |
| `messages/military/MessageAssassinate` | placeholder no-op handler |
| `messages/military/MessageDisbandGroup` | mutates group's disband flag and refreshes nearby recruits; uses sender to broadcast back |
| `messages/military/MessageOpenDisbandScreen` | sender-equality only, no further work |
| `messages/military/MessageRecruitCount` | intentional no-op |
| `messages/military/MessageRequestFormationMapSnapshot` | sender-only snapshot request |
| `messages/military/MessageSelectRecruits` | per-sender selection registry |
| `messages/military/MessageSplitGroup` | groupsManager.splitGroup operates server-side; sender used for broadcasting |
| `messages/military/MessageWriteSpawnEgg` | range-only on citizen; writes spawn egg into sender's hand |

(Other (a) entries are aggregated under the clientbound list above.)

## Notes / open hardening leads (not filed)

- `MessageApplyNoGroup` and `MessageDisbandGroup` operate on group IDs without
  verifying the sender owns the group. Effects are confined to the recruits'
  own group field / disband flag, so the impact is "annoy" rather than "steal";
  surfaced here for follow-up triage but not promoted to (c) in this audit.
- `MessageMergeGroup` / `MessageSplitGroup` likewise trust the wire group IDs;
  see `RecruitsGroupsManager.mergeGroups` / `splitGroup` for the actual
  authority decisions.
- `MessageClearUpkeepGui` lacks a `canDirectlyControl` gate on the resolved
  recruit but the `*Gui` packets are emitted from a screen that already
  required ownership context to open. Treat as hardening, not a wire-trust
  bug.
- `MessageTransferRoute` lets the sender push a route to any target player.
  Target side is informational; no server mutation occurs on the recipient
  beyond receiving the message. Marked (b) but worth documenting for a future
  rate-limit + opt-in pass.
