package com.talhanation.recruits.gametest.support;

import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.entities.AbstractLeaderEntity;
import com.talhanation.recruits.init.ModEntityTypes;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsFaction;
import com.talhanation.recruits.world.RecruitsGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.UUID;

public final class RecruitsPersistenceGameTestSupport {
    public static final BlockPos LEADER_POS = new BlockPos(3, 2, 3);
    public static final BlockPos WAYPOINT_ONE = new BlockPos(5, 2, 3);
    public static final BlockPos WAYPOINT_TWO = new BlockPos(7, 2, 3);

    private RecruitsPersistenceGameTestSupport() {
    }

    public static PersistenceScenario spawnLeaderScenario(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.moveTo(helper.absolutePos(LEADER_POS).getX() + 0.5D, helper.absolutePos(LEADER_POS).getY(), helper.absolutePos(LEADER_POS).getZ() + 2.5D, 0.0F, 0.0F);
        AbstractLeaderEntity leader = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.PATROL_LEADER.get(), LEADER_POS, "Persistence Leader", player.getUUID());
        return new PersistenceScenario(helper.getLevel(), player, leader);
    }

    public static void seedJoinSyncBaseline(ServerLevel level, Player player) {
        FactionEvents.recruitsFactionManager.addTeam("phase5-sync", "Phase 5 Sync", player.getUUID(), player.getName().getString(), new net.minecraft.nbt.CompoundTag(), (byte) 2, ChatFormatting.GREEN);
        FactionEvents.recruitsFactionManager.addTeam("neutral-neighbor", "Neutral Neighbor", UUID.randomUUID(), "Neighbor", new net.minecraft.nbt.CompoundTag(), (byte) 4, ChatFormatting.BLUE);

        RecruitsFaction faction = FactionEvents.recruitsFactionManager.getFactionByStringID("phase5-sync");
        if (faction != null) {
            faction.addMember(player.getUUID(), player.getName().getString());
        }

        FactionEvents.recruitsDiplomacyManager.setRelation("phase5-sync", "neutral-neighbor", com.talhanation.recruits.world.RecruitsDiplomacyManager.DiplomacyStatus.ALLY, level, false);
        FactionEvents.recruitsTreatyManager.addTreatyRaw("phase5-sync", "neutral-neighbor", 12345L, level);

        RecruitsClaim claim = new RecruitsClaim("Sync Claim", faction);
        claim.addChunk(new ChunkPos(player.blockPosition()));
        claim.setCenter(new ChunkPos(player.blockPosition()));
        ClaimEvents.recruitsClaimManager.addOrUpdateClaim(level, claim);

        RecruitEvents.recruitsGroupsManager.getPlayerGroups(player);
        RecruitEvents.recruitsPlayerUnitManager.setRecruitCount(player, 2);
    }

    public static void assertLeaderRouteState(AbstractLeaderEntity leader, UUID routeId, List<BlockPos> waypoints, List<Integer> waits) {
        if (!routeId.equals(leader.getRouteID())) {
            throw new IllegalArgumentException("Expected leader route id to match dispatched packet");
        }
        if (leader.WAYPOINTS.size() != waypoints.size() || leader.WAYPOINT_WAIT_SECONDS.size() != waits.size()) {
            throw new IllegalArgumentException("Expected leader waypoint data to be populated");
        }
    }

    public static void assertSavedRouteState(AbstractLeaderEntity leader, UUID routeId, int waypointCount, int waitCount) {
        net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();
        leader.addAdditionalSaveData(nbt);
        if (!nbt.hasUUID("routeId") || !routeId.equals(nbt.getUUID("routeId"))) {
            throw new IllegalArgumentException("Expected saved leader data to include route id");
        }
        if (nbt.getList("Waypoints", 10).size() != waypointCount) {
            throw new IllegalArgumentException("Expected saved leader data to include all waypoints");
        }
        if (nbt.getList("WaypointWaits", 10).size() != waitCount) {
            throw new IllegalArgumentException("Expected saved leader data to include all waypoint waits");
        }
    }

    public record PersistenceScenario(ServerLevel level, Player player, AbstractLeaderEntity leader) {
    }
}
