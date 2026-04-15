package com.talhanation.recruits.gametest.support;

import com.talhanation.recruits.entities.RecruitEntity;
import com.talhanation.recruits.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class RecruitsGameTestSupport {
    public static final BlockPos PRIMARY_RECRUIT_POS = new BlockPos(1, 2, 1);
    public static final BlockPos SECONDARY_RECRUIT_POS = new BlockPos(3, 2, 1);

    private RecruitsGameTestSupport() {
    }

    public static RecruitEntity spawnRecruit(GameTestHelper helper, BlockPos relativePos) {
        ServerLevel level = helper.getLevel();
        RecruitEntity recruit = ModEntityTypes.RECRUIT.get().create(level);

        if (recruit == null) {
            throw new IllegalArgumentException("Failed to create recruit test entity");
        }

        Vec3 spawnCenter = spawnCenter(helper, relativePos);
        recruit.moveTo(spawnCenter.x(), spawnCenter.y(), spawnCenter.z(), 0.0F, 0.0F);
        recruit.initSpawn();

        if (!level.addFreshEntity(recruit)) {
            throw new IllegalArgumentException("Failed to insert recruit test entity into GameTest level");
        }

        return recruit;
    }

    private static Vec3 spawnCenter(GameTestHelper helper, BlockPos relativePos) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        return new Vec3(absolutePos.getX() + 0.5D, absolutePos.getY() + 1.0D, absolutePos.getZ() + 0.5D);
    }
}
