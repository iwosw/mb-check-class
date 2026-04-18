package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.entity.LivingEntity;

public class RecruitProtectHurtByTargetGoal extends AbstractRecruitObservedThreatGoal {

    public RecruitProtectHurtByTargetGoal(AbstractRecruitEntity recruit) {
        super(recruit);
    }

    @Override
    protected LivingEntity getObservedObserver() {
        return this.recruit.getShouldProtect() ? this.recruit.getProtectingMob() : null;
    }

    @Override
    protected LivingEntity getObservedTarget(LivingEntity observer) {
        return observer.getLastHurtByMob();
    }

    @Override
    protected int getObservedTimestamp(LivingEntity observer) {
        return observer.getLastHurtByMobTimestamp();
    }

    @Override
    protected void afterTargetAssigned(LivingEntity target) {
        this.mob.setLastHurtMob(target);
    }
}
