package com.talhanation.recruits.gametest.support;

import com.talhanation.recruits.entities.AbstractLeaderEntity;
import com.talhanation.recruits.entities.ScoutEntity;
import com.talhanation.recruits.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class RecruitsLeaderScoutGameTestSupport {
    public static final UUID FOREIGN_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000701");
    public static final BlockPos LEADER_POS = new BlockPos(3, 2, 3);
    public static final BlockPos OTHER_LEADER_POS = new BlockPos(6, 2, 3);
    public static final BlockPos SCOUT_POS = new BlockPos(3, 2, 6);
    public static final BlockPos OTHER_SCOUT_POS = new BlockPos(6, 2, 6);
    public static final BlockPos FAR_LEADER_POS = new BlockPos(130, 2, 3);

    private RecruitsLeaderScoutGameTestSupport() {
    }

    public static LeaderScoutScenario spawnScenario(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.moveTo(helper.absolutePos(LEADER_POS).getX() + 0.5D, helper.absolutePos(LEADER_POS).getY(), helper.absolutePos(LEADER_POS).getZ() + 3.5D, 0.0F, 0.0F);

        AbstractLeaderEntity leader = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.PATROL_LEADER.get(), LEADER_POS, "Leader", player.getUUID());
        resetLeaderScenarioState(leader);

        AbstractLeaderEntity otherLeader = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.PATROL_LEADER.get(), OTHER_LEADER_POS, "Other Leader", FOREIGN_OWNER_UUID);
        resetLeaderScenarioState(otherLeader);

        AbstractLeaderEntity farLeader = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.PATROL_LEADER.get(), FAR_LEADER_POS, "Far Leader", player.getUUID());
        resetLeaderScenarioState(farLeader);

        ScoutEntity scout = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.SCOUT.get(), SCOUT_POS, "Scout", player.getUUID());
        resetScoutScenarioState(scout);

        ScoutEntity otherScout = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.SCOUT.get(), OTHER_SCOUT_POS, "Other Scout", FOREIGN_OWNER_UUID);
        resetScoutScenarioState(otherScout);

        return new LeaderScoutScenario(player, leader, otherLeader, farLeader, scout, otherScout);
    }

    private static void resetLeaderScenarioState(AbstractLeaderEntity leader) {
        leader.setNoAi(true);
        leader.setListen(true);
        leader.setIsOwned(leader.getOwnerUUID() != null);
        leader.clearRouteID();
        leader.WAYPOINTS.clear();
        leader.WAYPOINT_ITEMS.clear();
        leader.WAYPOINT_WAIT_SECONDS.clear();
        leader.currentWaypoint = null;
        leader.setPatrolState(AbstractLeaderEntity.State.IDLE);
    }

    public static void assignOwner(AbstractLeaderEntity leader, UUID ownerId) {
        leader.setOwnerUUID(Optional.of(ownerId));
        leader.setIsOwned(true);
    }

    public static void assignOwner(ScoutEntity scout, UUID ownerId) {
        scout.setOwnerUUID(Optional.of(ownerId));
        scout.setIsOwned(true);
    }

    private static void resetScoutScenarioState(ScoutEntity scout) {
        scout.setNoAi(true);
        scout.setListen(true);
        scout.setIsOwned(scout.getOwnerUUID() != null);
        scout.startTask(ScoutEntity.State.IDLE);
    }

    public record LeaderScoutScenario(
            Player player,
            AbstractLeaderEntity leader,
            AbstractLeaderEntity otherLeader,
            AbstractLeaderEntity farLeader,
            ScoutEntity scout,
            ScoutEntity otherScout
    ) {
    }
}
