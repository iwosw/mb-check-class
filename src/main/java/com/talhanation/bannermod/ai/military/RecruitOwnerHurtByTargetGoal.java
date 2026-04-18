package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.entity.LivingEntity;

public class RecruitOwnerHurtByTargetGoal extends AbstractRecruitObservedThreatGoal {

    public RecruitOwnerHurtByTargetGoal(AbstractRecruitEntity recruit) {
        super(recruit);
    }

    @Override
    protected LivingEntity getObservedObserver() {
        return this.recruit.isOwned() ? this.recruit.getOwner() : null;
    }

    @Override
    protected LivingEntity getObservedTarget(LivingEntity observer) {
        return observer.getLastHurtByMob();
    }

    @Override
    protected int getObservedTimestamp(LivingEntity observer) {
        return observer.getLastHurtByMobTimestamp();
    }
}
