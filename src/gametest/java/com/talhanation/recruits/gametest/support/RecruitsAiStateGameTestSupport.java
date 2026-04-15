package com.talhanation.recruits.gametest.support;

import com.talhanation.recruits.entities.AbstractLeaderEntity;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.MessengerEntity;
import com.talhanation.recruits.entities.ScoutEntity;
import com.talhanation.recruits.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class RecruitsAiStateGameTestSupport {
    public static final BlockPos RECRUIT_POS = new BlockPos(3, 2, 3);
    public static final BlockPos LEADER_POS = new BlockPos(6, 2, 3);
    public static final BlockPos SCOUT_POS = new BlockPos(3, 2, 6);
    public static final BlockPos MESSENGER_POS = new BlockPos(6, 2, 6);

    private RecruitsAiStateGameTestSupport() {
    }

    public static AiStateScenario spawnScenario(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.moveTo(helper.absolutePos(RECRUIT_POS).getX() + 0.5D, helper.absolutePos(RECRUIT_POS).getY(), helper.absolutePos(RECRUIT_POS).getZ() + 5.5D, 0.0F, 0.0F);

        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.RECRUIT.get(), RECRUIT_POS, "Ai Recruit", player.getUUID());
        AbstractLeaderEntity leader = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.PATROL_LEADER.get(), LEADER_POS, "Ai Leader", player.getUUID());
        ScoutEntity scout = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.SCOUT.get(), SCOUT_POS, "Ai Scout", player.getUUID());
        MessengerEntity messenger = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.MESSENGER.get(), MESSENGER_POS, "Ai Messenger", player.getUUID());
        messenger.initialPos = messenger.blockPosition();

        recruit.setHoldPos(Vec3.atCenterOf(recruit.blockPosition()));
        leader.setPatrolState(AbstractLeaderEntity.State.IDLE);
        scout.startTask(ScoutEntity.State.IDLE);
        messenger.setMessengerState(MessengerEntity.MessengerState.IDLE);

        return new AiStateScenario(player, recruit, leader, scout, messenger);
    }

    public static void assignOwner(MessengerEntity messenger, Player player) {
        messenger.setOwnerUUID(Optional.of(player.getUUID()));
        messenger.setIsOwned(true);
    }

    public record AiStateScenario(
            Player player,
            AbstractRecruitEntity recruit,
            AbstractLeaderEntity leader,
            ScoutEntity scout,
            MessengerEntity messenger
    ) {
    }
}
