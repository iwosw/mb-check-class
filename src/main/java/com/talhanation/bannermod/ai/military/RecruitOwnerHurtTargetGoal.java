package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.entity.LivingEntity;

public class RecruitOwnerHurtTargetGoal extends AbstractRecruitObservedThreatGoal {

    public RecruitOwnerHurtTargetGoal(AbstractRecruitEntity recruit) {
        super(recruit);
    }

    @Override
    protected boolean requiresActiveCombatState() {
        return false;
    }

    @Override
    protected LivingEntity getObservedObserver() {
        return this.recruit.isOwned() ? this.recruit.getOwner() : null;
    }

    @Override
    protected LivingEntity getObservedTarget(LivingEntity observer) {
        return observer.getLastHurtMob();
    }

    @Override
    protected int getObservedTimestamp(LivingEntity observer) {
        return observer.getLastHurtMobTimestamp();
    }
}
