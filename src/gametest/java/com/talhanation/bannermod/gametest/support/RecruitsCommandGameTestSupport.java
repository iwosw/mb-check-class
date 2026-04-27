package com.talhanation.bannermod.gametest.support;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public final class RecruitsCommandGameTestSupport {
    public static final UUID TARGET_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000451");
    public static final UUID OTHER_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000452");
    public static final UUID FOREIGN_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000453");
    public static final BlockPos OTHER_GROUP_POS = new BlockPos(6, 2, 5);
    public static final BlockPos FOREIGN_RECRUIT_POS = new BlockPos(6, 2, 7);
    public static final BlockPos FAR_RECRUIT_POS = new BlockPos(130, 2, 5);

    private RecruitsCommandGameTestSupport() {
    }

    public static CommandScenario spawnCommandScenario(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.moveTo(helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getX() + 0.5D, helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getY(), helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getZ() + 0.5D, -90.0F, 0.0F);
        player.setYRot(-90.0F);

        RecruitsBattleGameTestSupport.BattleSquad targetedSquad = RecruitsBattleGameTestSupport.spawnRecoveryPair(
                helper,
                RecruitsBattleGameTestSupport.SquadAnchor.WEST,
                player.getUUID(),
                "Targeted"
        );
        for (AbstractRecruitEntity recruit : targetedSquad.recruits()) {
            prepareForCommand(recruit, TARGET_GROUP_UUID);
        }

        AbstractRecruitEntity otherGroupRecruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.RECRUIT.get(), OTHER_GROUP_POS, "Other Group", player.getUUID());
        prepareForCommand(otherGroupRecruit, OTHER_GROUP_UUID);

        AbstractRecruitEntity foreignRecruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.RECRUIT.get(), FOREIGN_RECRUIT_POS, "Foreign Recruit", FOREIGN_OWNER_UUID);
        prepareForCommand(foreignRecruit, TARGET_GROUP_UUID);

        AbstractRecruitEntity farRecruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.RECRUIT.get(), FAR_RECRUIT_POS, "Far Recruit", player.getUUID());
        prepareForCommand(farRecruit, TARGET_GROUP_UUID);

        AbstractRecruitEntity enemyRecruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, ModEntityTypes.RECRUIT.get(), RecruitsBattleGameTestSupport.EAST_FRONTLINE_POS, "Enemy Recruit", FOREIGN_OWNER_UUID);
        prepareForCommand(enemyRecruit, null);

        return new CommandScenario(player, targetedSquad.recruits(), otherGroupRecruit, foreignRecruit, farRecruit, enemyRecruit);
    }

    public static void prepareForCommand(AbstractRecruitEntity recruit, UUID groupId) {
        recruit.setGroupUUID(groupId);
        if (recruit.getOwnerUUID() != null) {
            recruit.setIsOwned(true);
        }
        recruit.setListen(true);
        recruit.setFollowState(0);
        recruit.isInFormation = false;
        recruit.setShouldBlock(false);
        recruit.setTarget(null);
        recruit.setHoldPos(Vec3.atCenterOf(recruit.blockPosition()));
    }

    public static void assertUnchanged(AbstractRecruitEntity recruit, int followState, boolean shouldBlock) {
        if (recruit.getFollowState() != followState) {
            throw new IllegalArgumentException("Expected unchanged follow state " + followState + " for " + recruit.getName().getString());
        }
        if (recruit.getShouldBlock() != shouldBlock) {
            throw new IllegalArgumentException("Expected unchanged shield state for " + recruit.getName().getString());
        }
        if (recruit.getTarget() != null) {
            throw new IllegalArgumentException("Expected no combat target for " + recruit.getName().getString());
        }
    }

    public record CommandScenario(
            Player player,
            List<AbstractRecruitEntity> targetedSquad,
            AbstractRecruitEntity otherGroupRecruit,
            AbstractRecruitEntity foreignRecruit,
            AbstractRecruitEntity farRecruit,
            AbstractRecruitEntity enemyRecruit
    ) {
    }
}
