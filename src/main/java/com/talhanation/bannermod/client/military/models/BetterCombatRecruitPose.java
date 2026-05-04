package com.talhanation.bannermod.client.military.models;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public final class BetterCombatRecruitPose {
    private BetterCombatRecruitPose() {
    }

    public static void apply(HumanoidModel<AbstractRecruitEntity> model, AbstractRecruitEntity recruit) {
        int ticks = recruit.getBetterCombatAttackTicks();
        int duration = recruit.getBetterCombatAttackDuration();
        if (ticks <= 0 || duration <= 0) {
            return;
        }

        ModelPart attackArm = recruit.getMainArm() == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
        ModelPart supportArm = recruit.getMainArm() == HumanoidArm.LEFT ? model.rightArm : model.leftArm;
        float baseAttackX = attackArm.xRot;
        float baseAttackY = attackArm.yRot;
        float baseAttackZ = attackArm.zRot;
        float baseSupportX = supportArm.xRot;
        float baseSupportY = supportArm.yRot;
        float baseSupportZ = supportArm.zRot;

        int elapsed = duration - ticks;
        int upswing = Mth.clamp(recruit.getBetterCombatAttackUpswing(), 1, duration);
        float progress = 1.0F - Mth.clamp((float) ticks / (float) duration, 0.0F, 1.0F);
        float windupProgress = Mth.clamp((float) elapsed / (float) upswing, 0.0F, 1.0F);
        float recoveryProgress = duration > upswing
                ? Mth.clamp((float) (elapsed - upswing) / (float) (duration - upswing), 0.0F, 1.0F)
                : 0.0F;
        float attackWeight = 1.0F - recoveryProgress;
        float swing = Mth.sin(progress * Mth.PI) * attackWeight;
        float windup = Mth.sin(windupProgress * Mth.PI * 0.5F) * attackWeight;
        switch (recruit.getBetterCombatAttackShape()) {
            case 2 -> applyHorizontalSweep(attackArm, supportArm, swing, windup);
            case 3 -> applyVerticalChop(attackArm, supportArm, swing, windup);
            default -> applyForwardThrust(attackArm, supportArm, swing, windup);
        }

        attackArm.xRot = Mth.lerp(attackWeight, baseAttackX, attackArm.xRot);
        attackArm.yRot = Mth.lerp(attackWeight, baseAttackY, attackArm.yRot);
        attackArm.zRot = Mth.lerp(attackWeight, baseAttackZ, attackArm.zRot);
        supportArm.xRot = Mth.lerp(attackWeight, baseSupportX, supportArm.xRot);
        supportArm.yRot = Mth.lerp(attackWeight, baseSupportY, supportArm.yRot);
        supportArm.zRot = Mth.lerp(attackWeight, baseSupportZ, supportArm.zRot);
    }

    private static void applyHorizontalSweep(ModelPart attackArm, ModelPart supportArm, float swing, float windup) {
        attackArm.xRot = -1.0F + swing * 0.45F;
        attackArm.yRot = -0.85F + swing * 1.7F;
        attackArm.zRot = 0.25F - windup * 0.5F;
        supportArm.xRot *= 0.55F;
    }

    private static void applyVerticalChop(ModelPart attackArm, ModelPart supportArm, float swing, float windup) {
        attackArm.xRot = -2.6F + swing * 2.0F;
        attackArm.yRot = 0.0F;
        attackArm.zRot = 0.15F - windup * 0.3F;
        supportArm.xRot *= 0.5F;
    }

    private static void applyForwardThrust(ModelPart attackArm, ModelPart supportArm, float swing, float windup) {
        attackArm.xRot = -1.25F + swing * 0.35F;
        attackArm.yRot = -0.15F + windup * 0.3F;
        attackArm.zRot = 0.0F;
        supportArm.xRot *= 0.7F;
    }
}
