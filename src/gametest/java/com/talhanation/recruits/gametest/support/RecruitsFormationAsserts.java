package com.talhanation.recruits.gametest.support;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RecruitsFormationAsserts {

    private RecruitsFormationAsserts() {
    }

    public static void assertHoldPositionIntent(AbstractRecruitEntity recruit, Vec3 expectedHoldPos, double tolerance) {
        if (recruit.getFollowState() != 2) {
            throw new IllegalArgumentException("Expected recruit to hold position but follow state was " + recruit.getFollowState());
        }

        assertHoldPositionNear(recruit, expectedHoldPos, tolerance);
    }

    public static void assertReturnToPositionIntent(AbstractRecruitEntity recruit, Vec3 expectedHoldPos, double tolerance) {
        if (recruit.getFollowState() != 3) {
            throw new IllegalArgumentException("Expected recruit to return to position but follow state was " + recruit.getFollowState());
        }

        assertHoldPositionNear(recruit, expectedHoldPos, tolerance);
    }

    public static void assertStableSpacing(List<? extends AbstractRecruitEntity> recruits, double minimumSpacing, double maximumAnchorDrift, Vec3 anchor) {
        for (int i = 0; i < recruits.size(); i++) {
            AbstractRecruitEntity recruit = recruits.get(i);
            if (recruit.position().distanceTo(anchor) > maximumAnchorDrift) {
                throw new IllegalArgumentException("Expected recruit to stay near anchor " + anchor + " but was at " + recruit.position());
            }

            for (int j = i + 1; j < recruits.size(); j++) {
                AbstractRecruitEntity otherRecruit = recruits.get(j);
                if (recruit.position().distanceTo(otherRecruit.position()) < minimumSpacing) {
                    throw new IllegalArgumentException("Expected stable formation spacing of at least " + minimumSpacing + " but recruits were too close together");
                }
            }
        }
    }

    public static void assertFormationAnchorIntent(GameTestHelper helper, RecruitsBattleGameTestSupport.BattleSquad squad, RecruitsBattleGameTestSupport.SquadAnchor expectedAnchor, double tolerance) {
        Vec3 expected = RecruitsBattleGameTestSupport.formationAnchor(helper, expectedAnchor);
        Vec3 actual = Vec3.atCenterOf(helper.absolutePos(squad.relativeAnchor()));
        if (actual.distanceTo(expected) > tolerance) {
            throw new IllegalArgumentException("Expected formation anchor near " + expected + " but was " + actual);
        }
    }

    private static void assertHoldPositionNear(AbstractRecruitEntity recruit, Vec3 expectedHoldPos, double tolerance) {
        Vec3 holdPos = recruit.getHoldPos();
        if (holdPos == null) {
            throw new IllegalArgumentException("Expected recruit hold position to be set");
        }

        if (holdPos.distanceTo(expectedHoldPos) > tolerance) {
            throw new IllegalArgumentException("Expected hold position near " + expectedHoldPos + " but was " + holdPos);
        }
    }
}
