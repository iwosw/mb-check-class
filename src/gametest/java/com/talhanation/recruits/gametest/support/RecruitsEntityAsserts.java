package com.talhanation.recruits.gametest.support;

import com.talhanation.recruits.entities.RecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public final class RecruitsEntityAsserts {

    private RecruitsEntityAsserts() {
    }

    public static void assertRecruitPresentAt(GameTestHelper helper, RecruitEntity recruit, BlockPos relativePos) {
        if (recruit == null) {
            throw new IllegalArgumentException("Expected recruit to exist");
        }

        BlockPos expectedPos = helper.absolutePos(relativePos);
        BlockPos actualPos = recruit.blockPosition();
        boolean sameColumn = expectedPos.getX() == actualPos.getX() && expectedPos.getZ() == actualPos.getZ();
        boolean sameOrGroundedY = expectedPos.getY() == actualPos.getY() || expectedPos.getY() + 1 == actualPos.getY();
        if (!sameColumn || !sameOrGroundedY) {
            throw new IllegalArgumentException("Expected recruit at " + expectedPos + " but was at " + actualPos);
        }
    }

    public static void assertRecruitAlive(RecruitEntity recruit) {
        if (recruit == null || !recruit.isAlive()) {
            throw new IllegalArgumentException("Expected recruit to be alive");
        }
    }

    public static void assertRecruitCustomName(RecruitEntity recruit, String expectedName) {
        if (recruit.getCustomName() == null) {
            throw new IllegalArgumentException("Expected recruit custom name to be set");
        }

        String actualName = recruit.getCustomName().getString();
        if (!expectedName.equals(actualName)) {
            throw new IllegalArgumentException("Expected recruit custom name '" + expectedName + "' but was '" + actualName + "'");
        }
    }
}
