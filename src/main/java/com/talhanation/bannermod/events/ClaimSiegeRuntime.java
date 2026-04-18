package com.talhanation.bannermod.events;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.VillagerNobleEntity;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.util.ClaimUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

final class ClaimSiegeRuntime {

    private final MinecraftServer server;
    private final RecruitsClaimManager claimManager;

    ClaimSiegeRuntime(MinecraftServer server, RecruitsClaimManager claimManager) {
        this.server = server;
        this.claimManager = claimManager;
    }

    void tickActiveSieges(ServerLevel level) {
        List<RecruitsClaim> sieges = new ArrayList<>(claimManager.getActiveSieges());

        for (RecruitsClaim claim : sieges) {
            if (claim == null || claim.getOwnerFaction() == null) continue;

            List<LivingEntity> entities = ClaimUtil.getLivingEntitiesInClaim(level, claim, LivingEntity::isAlive);
            List<LivingEntity> attackers = new ArrayList<>();
            List<LivingEntity> defenders = new ArrayList<>();

            classifyEntities(entities, claim, attackers, defenders);

            int attackerSize = attackers.size();
            int defenderSize = defenders.size();

            updateParties(claim, attackers, defenders);

            if (attackerSize < RecruitsServerConfig.SiegeClaimsRecruitsAmount.get()) {
                claim.setUnderSiege(false, level);
                claim.resetHealth();
                claim.setSiegeSpeedPercent(0f);
                claim.attackingParties.clear();
                claim.defendingParties.clear();
                claimManager.removeActiveSiege(claim);
                claimManager.broadcastClaimsToAll(level);
                siegeOverVillagers(level, claim);
                continue;
            }

            float speedPercent = calculateSiegeSpeedPercent(attackerSize, defenderSize);
            claim.setSiegeSpeedPercent(speedPercent);

            int baseDamage = 3;
            SiegeEvent.Tick tickEvent = new SiegeEvent.Tick(claim, level, attackerSize, defenderSize, baseDamage);
            MinecraftForge.EVENT_BUS.post(tickEvent);

            if (!tickEvent.isCanceled()) {
                claim.setHealth(claim.getHealth() - tickEvent.getDamage());
            }

            if (claim.getHealth() <= 0) {
                claim.setSiegeSpeedPercent(0f);
                claim.setSiegeSuccess(level);
                claimManager.removeActiveSiege(claim);
                claimManager.broadcastClaimsToAll(level);
                siegeOverVillagers(level, claim);
                continue;
            }

            List<ServerPlayer> players = attackers.stream().filter(ServerPlayer.class::isInstance).map(ServerPlayer.class::cast).toList();
            claimManager.broadcastClaimUpdateTo(claim, players);
            players = defenders.stream().filter(ServerPlayer.class::isInstance).map(ServerPlayer.class::cast).toList();
            claimManager.broadcastClaimUpdateTo(claim, players);
        }
    }

    void tickDetection(ServerLevel level) {
        for (RecruitsClaim claim : claimManager.getAllClaims()) {
            if (claim == null || claim.getOwnerFaction() == null) continue;
            if (claim.isAdmin) continue;
            if (claimManager.isActiveSiege(claim)) continue;

            List<LivingEntity> entities = ClaimUtil.getLivingEntitiesInClaim(level, claim, LivingEntity::isAlive);
            List<LivingEntity> attackers = new ArrayList<>();
            List<LivingEntity> defenders = new ArrayList<>();

            for (LivingEntity livingEntity : entities) {
                takeOverVillager(level, claim, livingEntity);
            }

            classifyEntities(entities, claim, attackers, defenders);

            int attackerSize = attackers.size();

            updateParties(claim, attackers, defenders);

            if (attackerSize >= RecruitsServerConfig.SiegeClaimsRecruitsAmount.get()) {
                if (isSiegeBlockedOwnerOffline(claim, attackers)) {
                    continue;
                }

                claim.setUnderSiege(true, level);
                claimManager.addActiveSiege(claim);
                claimManager.broadcastClaimsToAll(level);
                sendVillagersHome(level, claim);
            }
        }
    }

    private boolean isSiegeBlockedOwnerOffline(RecruitsClaim claim, List<LivingEntity> attackers) {
        if (!RecruitsServerConfig.SiegeRequiresOwnerOnline.get()) {
            return false;
        }

        RecruitsPlayerInfo ownerInfo = claim.getPlayerInfo();
        if (ownerInfo == null) {
            return false;
        }

        ServerPlayer onlineOwner = server.getPlayerList().getPlayer(ownerInfo.getUUID());
        if (onlineOwner != null) {
            return false;
        }

        Component msg = Component.translatable(
                "chat.bannermod.text.siegeBlockedOwnerOffline",
                claim.getName(),
                ownerInfo.getName()
        ).withStyle(ChatFormatting.RED);
        for (LivingEntity attacker : attackers) {
            if (attacker instanceof ServerPlayer player) {
                player.sendSystemMessage(msg);
            }
        }
        return true;
    }

    private void classifyEntities(List<LivingEntity> entities, RecruitsClaim claim, List<LivingEntity> attackers, List<LivingEntity> defenders) {
        for (LivingEntity livingEntity : entities) {
            if (!livingEntity.isAlive() || livingEntity.getTeam() == null) continue;

            String teamName = livingEntity.getTeam().getName();
            RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);

            if (recruitsFaction == null) continue;

            RecruitsDiplomacyManager.DiplomacyStatus relation = FactionEvents.recruitsDiplomacyManager.getRelation(teamName, claim.getOwnerFactionStringID());

            if (recruitsFaction.getStringID().equals(claim.getOwnerFactionStringID()) || relation == RecruitsDiplomacyManager.DiplomacyStatus.ALLY) {
                defenders.add(livingEntity);
            } else if (relation == RecruitsDiplomacyManager.DiplomacyStatus.ENEMY) {
                attackers.add(livingEntity);
            }
        }
    }

    private void updateParties(RecruitsClaim claim, List<LivingEntity> attackers, List<LivingEntity> defenders) {
        for (LivingEntity livingEntity : defenders) {
            RecruitsFaction recruitsFaction = getFaction(livingEntity);
            if (recruitsFaction == null) continue;
            if (!claim.getOwnerFaction().equalsFaction(recruitsFaction)) {
                claim.addParty(claim.defendingParties, recruitsFaction);
            }
        }

        for (LivingEntity livingEntity : attackers) {
            RecruitsFaction recruitsFaction = getFaction(livingEntity);
            if (recruitsFaction == null) continue;
            claim.addParty(claim.attackingParties, recruitsFaction);
        }
    }

    private RecruitsFaction getFaction(LivingEntity livingEntity) {
        if (livingEntity.getTeam() == null) {
            return null;
        }
        return FactionEvents.recruitsFactionManager.getFactionByStringID(livingEntity.getTeam().getName());
    }

    private void takeOverVillager(ServerLevel level, RecruitsClaim claim, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Villager || livingEntity instanceof VillagerNobleEntity)) return;
        if (!livingEntity.isAlive()) return;

        String teamName = claim.getOwnerFaction().getStringID();
        PlayerTeam team = level.getScoreboard().getPlayerTeam(teamName);
        if (team == null) return;

        if (livingEntity.getTeam() == null || !livingEntity.getTeam().getName().equals(team.getName())) {
            level.getScoreboard().addPlayerToTeam(livingEntity.getStringUUID(), team);
        }

        if (livingEntity instanceof VillagerNobleEntity noble) {
            noble.updateTeam();
        }
    }

    static float calculateSiegeSpeedPercent(int attackerCount, int defenderCount) {
        if (attackerCount <= 0) return 0f;
        if (defenderCount <= 0) return 2.0f;
        return (float) attackerCount / (float) defenderCount;
    }

    static void sendVillagersHome(ServerLevel level, RecruitsClaim claim) {
        List<LivingEntity> list = ClaimUtil.getLivingEntitiesInClaim(level, claim, livingEntity -> livingEntity instanceof Villager);

        for (LivingEntity living : list) {
            if (living instanceof Villager villager) {
                Brain<?> brain = villager.getBrain();
                brain.setActiveActivityIfPossible(Activity.HIDE);
                brain.setMemory(MemoryModuleType.IS_PANICKING, true);
                brain.setMemory(MemoryModuleType.HEARD_BELL_TIME, level.getGameTime());
            }
        }
    }

    static void siegeOverVillagers(ServerLevel level, RecruitsClaim claim) {
        List<LivingEntity> list = ClaimUtil.getLivingEntitiesInClaim(level, claim, livingEntity -> livingEntity instanceof Villager);

        for (LivingEntity living : list) {
            if (living instanceof Villager villager) {
                Brain<?> brain = villager.getBrain();
                brain.setActiveActivityIfPossible(Activity.MEET);
                brain.eraseMemory(MemoryModuleType.IS_PANICKING);
                brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
            }
        }
    }
}
