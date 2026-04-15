package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.recruits.util.FormationUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RecruitHoldPosGoal extends Goal {
    private final AbstractRecruitEntity recruit;

    private int timeToRecalcPath;
    private int formationFallbackCooldown;

    public RecruitHoldPosGoal(AbstractRecruitEntity recruit, double within) {
      this.recruit = recruit;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public void start() {
        super.start();
        timeToRecalcPath = 0;
        formationFallbackCooldown = 0;
    }

    public boolean canUse() {
        if (this.recruit.getHoldPos() == null) {
            return false;
        }
        else
            return this.recruit.getShouldHoldPos() && !recruit.getFleeing() && !recruit.needsToGetFood() && !recruit.getShouldMount();
    }

    public boolean canContinueToUse() {
        return canUse();
    }

    public void tick() {
        if (this.formationFallbackCooldown > 0) {
            this.formationFallbackCooldown--;
        }

        Vec3 pos = this.recruit.getHoldPos();
        if (pos != null) {
            double distance = recruit.distanceToSqr(pos);
            if(distance >= 0.3) {
                if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = this.recruit.getVehicle() != null ? this.adjustedTickDelay(5) : this.adjustedTickDelay(10);
                    this.recruit.getNavigation().moveTo(pos.x(), pos.y(), pos.z(), this.recruit.moveSpeed);
                }

                if (recruit.horizontalCollision || recruit.minorHorizontalCollision) {
                    this.recruit.getJumpControl().jump();
                }

                if (this.formationFallbackCooldown <= 0
                        && this.recruit.isInFormation
                        && this.recruit.getFollowState() == 3
                        && (this.recruit.getNavigation().isStuck() || this.recruit.horizontalCollision || this.recruit.minorHorizontalCollision)
                        && FormationUtils.tryFallbackToNearestFreeSlot(this.recruit)) {
                    this.formationFallbackCooldown = this.adjustedTickDelay(20);
                    Vec3 fallbackPos = this.recruit.getHoldPos();
                    if (fallbackPos != null) {
                        this.recruit.getNavigation().moveTo(fallbackPos.x(), fallbackPos.y(), fallbackPos.z(), this.recruit.moveSpeed);
                    }
                }
            } else{
            }
        }
    }
}
