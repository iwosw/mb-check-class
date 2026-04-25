package com.talhanation.bannermod.client.military.models;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

public final class BetterCombatRecruitPose {
    private BetterCombatRecruitPose() {
    }

    public static void apply(HumanoidModel<AbstractRecruitEntity> model, AbstractRecruitEntity recruit) {
        int ticks = recruit.getBetterCombatAttackTicks();
        int duration = recruit.getBetterCombatAttackDuration();
        if (ticks <= 0 || duration <= 0) {
            return;
        }

        float progress = 1.0F - Mth.clamp((float) ticks / (float) duration, 0.0F, 1.0F);
        float swing = Mth.sin(progress * Mth.PI);
        float windup = Mth.sin(Mth.clamp(progress * 2.0F, 0.0F, 1.0F) * Mth.PI * 0.5F);
        switch (recruit.getBetterCombatAttackShape()) {
            case 2 -> applyHorizontalSweep(model, swing, windup);
            case 3 -> applyVerticalChop(model, swing, windup);
            default -> applyForwardThrust(model, swing, windup);
        }
    }

    private static void applyHorizontalSweep(HumanoidModel<AbstractRecruitEntity> model, float swing, float windup) {
        model.rightArm.xRot = -1.0F + swing * 0.45F;
        model.rightArm.yRot = -0.85F + swing * 1.7F;
        model.rightArm.zRot = 0.25F - windup * 0.5F;
        model.leftArm.xRot *= 0.55F;
    }

    private static void applyVerticalChop(HumanoidModel<AbstractRecruitEntity> model, float swing, float windup) {
        model.rightArm.xRot = -2.6F + swing * 2.0F;
        model.rightArm.yRot = 0.0F;
        model.rightArm.zRot = 0.15F - windup * 0.3F;
        model.leftArm.xRot *= 0.5F;
    }

    private static void applyForwardThrust(HumanoidModel<AbstractRecruitEntity> model, float swing, float windup) {
        model.rightArm.xRot = -1.25F + swing * 0.35F;
        model.rightArm.yRot = -0.15F + windup * 0.3F;
        model.rightArm.zRot = 0.0F;
        model.leftArm.xRot *= 0.7F;
    }
}
