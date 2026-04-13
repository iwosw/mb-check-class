package com.talhanation.bannermod;

import com.mojang.authlib.GameProfile;
import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsClaimManager;
import com.talhanation.recruits.world.RecruitsFaction;
import com.talhanation.recruits.world.RecruitsFactionManager;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import com.talhanation.workers.entities.workarea.AbstractWorkAreaEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Optional;
import java.util.UUID;

public final class BannerModDedicatedServerGameTestSupport {

    private BannerModDedicatedServerGameTestSupport() {
    }

    public static Player createFakeServerPlayer(ServerLevel level, UUID playerId, String name) {
        Player existingPlayer = level.getPlayerByUUID(playerId);
        if (existingPlayer != null) {
            return existingPlayer;
        }

        FakePlayer player = new FakePlayer(level, new GameProfile(playerId, name));
        level.addFreshEntity(player);
        return player;
    }

    public static Player createPositionedFakeServerPlayer(ServerLevel level, UUID playerId, String name, BlockPos pos) {
        Player player = createFakeServerPlayer(level, playerId, name);
        Vec3 spawnPos = Vec3.atBottomCenterOf(pos);
        player.moveTo(spawnPos.x(), spawnPos.y(), spawnPos.z(), 0.0F, 0.0F);
        return player;
    }

    public static PlayerTeam joinTeam(ServerLevel level, String teamName, Entity... members) {
        PlayerTeam team = level.getScoreboard().getPlayerTeam(teamName);
        if (team == null) {
            team = level.getScoreboard().addPlayerTeam(teamName);
        }

        for (Entity member : members) {
            if (member instanceof Player player) {
                level.getScoreboard().addPlayerToTeam(player.getScoreboardName(), team);
                continue;
            }
            level.getScoreboard().addPlayerToTeam(member.getStringUUID(), team);
        }

        return team;
    }

    public static RecruitsFaction ensureFaction(ServerLevel level, String factionId, UUID leaderId, String leaderName) {
        if (FactionEvents.recruitsFactionManager == null) {
            FactionEvents.recruitsFactionManager = new RecruitsFactionManager();
            FactionEvents.recruitsFactionManager.load(level);
        }

        RecruitsFaction faction = FactionEvents.recruitsFactionManager.getFactionByStringID(factionId);
        if (faction == null) {
            FactionEvents.recruitsFactionManager.addTeam(
                    factionId,
                    factionId,
                    leaderId,
                    leaderName,
                    new CompoundTag(),
                    (byte) 0,
                    ChatFormatting.WHITE
            );
            faction = FactionEvents.recruitsFactionManager.getFactionByStringID(factionId);
        }

        return faction;
    }

    public static RecruitsClaim seedClaim(ServerLevel level, BlockPos pos, String factionId, UUID leaderId, String leaderName) {
        ensureClaimManager(level);
        RecruitsFaction faction = ensureFaction(level, factionId, leaderId, leaderName);
        ChunkPos chunkPos = new ChunkPos(pos);
        RecruitsClaim claim = ClaimEvents.recruitsClaimManager.getClaim(chunkPos);
        if (claim == null) {
            claim = new RecruitsClaim(faction);
            claim.setCenter(chunkPos);
        }
        claim.isRemoved = false;
        claim.setOwnerFaction(faction);
        claim.addChunk(chunkPos);
        ClaimEvents.recruitsClaimManager.addOrUpdateClaim(level, claim);
        return claim;
    }

    public static void removeClaim(ServerLevel level, RecruitsClaim claim) {
        ensureClaimManager(level);
        ClaimEvents.recruitsClaimManager.removeClaim(claim);
    }

    public static RecruitsClaim swapClaimFaction(ServerLevel level, RecruitsClaim claim, String factionId, UUID leaderId, String leaderName) {
        ensureClaimManager(level);
        RecruitsFaction faction = ensureFaction(level, factionId, leaderId, leaderName);
        claim.isRemoved = false;
        claim.setOwnerFaction(faction);
        ClaimEvents.recruitsClaimManager.addOrUpdateClaim(level, claim);
        return claim;
    }

    public static void assignDetachedOwnership(AbstractRecruitEntity recruit, UUID ownerId) {
        recruit.setOwnerUUID(Optional.of(ownerId));
        recruit.setIsOwned(true);
    }

    public static void assignDetachedOwnership(AbstractWorkerEntity worker, UUID ownerId) {
        worker.setOwnerUUID(Optional.of(ownerId));
        worker.setIsOwned(true);
    }

    public static void assignDetachedOwnership(AbstractWorkAreaEntity workArea, UUID ownerId, String ownerName) {
        workArea.setPlayerUUID(ownerId);
        workArea.setPlayerName(ownerName);
    }

    public static CompoundTag saveEntity(Entity entity) {
        return entity.saveWithoutId(new CompoundTag());
    }

    public static <T extends Entity> T loadEntity(GameTestHelper helper, EntityType<T> entityType, BlockPos relativePos, CompoundTag savedData) {
        T entity = BannerModGameTestSupport.spawnEntity(helper, entityType, relativePos);
        entity.load(savedData);
        return entity;
    }

    private static void ensureClaimManager(ServerLevel level) {
        if (ClaimEvents.recruitsClaimManager == null) {
            ClaimEvents.recruitsClaimManager = new RecruitsClaimManager();
            ClaimEvents.recruitsClaimManager.load(level);
        }
    }
}
