package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.combat.RangedAction;
import com.talhanation.bannermod.combat.RangedSpacingService;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.IRangedRecruit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Movement goal that consumes the per-tick {@link RangedAction} cached by
 * {@link RangedSpacingService} and applies it through {@link AbstractRecruitEntity#getNavigation()}.
 *
 * <p>Activates only when the cached action is {@link RangedAction#FALLBACK} or
 * {@link RangedAction#LATERAL_SHIFT} — for {@link RangedAction#STAY} the goal yields and the
 * existing ranged-attack and target-following goals run unchanged. The goal sits at priority
 * 1, above the regular ranged attack and the move-towards-target goals (priorities 4 and 8 in
 * {@link com.talhanation.bannermod.entity.military.BowmanEntity}), so a fallback decision
 * takes precedence over closing in for a shot.</p>
 *
 * <p>FALLBACK movement: walk a fixed step away from the threat. Threat is the recruit's
 * current target if it exists; otherwise the recruit's facing direction is used so a
 * collapsing front line still produces visible disengagement. LATERAL_SHIFT movement: step
 * sideways relative to the recruit's facing so the firing lane behind the recruit clears
 * while keeping the same distance to the target.</p>
 *
 * <p>Coexistence with {@link RecruitMoraleRoutGoal}: the rout goal sits at priority 0 with
 * the same flag set, so a routed ranged recruit always defers to the rout window before this
 * goal can run. That ordering is intentional — once a squad routs, "fall back" is the rout
 * goal's job, not the spacing policy's.</p>
 */
public class RecruitRangedSpacingGoal extends Goal {

    public static final double FALLBACK_STEP = 4.0D;
    public static final double LATERAL_STEP = 3.0D;
    public static final double FALLBACK_SPEED = 1.10D;
    public static final double LATERAL_SPEED = 1.00D;

    private final AbstractRecruitEntity recruit;

    public RecruitRangedSpacingGoal(AbstractRecruitEntity recruit) {
        if (!(recruit instanceof IRangedRecruit)) {
            throw new IllegalArgumentException("RecruitRangedSpacingGoal requires an IRangedRecruit");
        }
        this.recruit = recruit;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        RangedAction action = RangedSpacingService.currentAction(recruit);
        return action == RangedAction.FALLBACK || action == RangedAction.LATERAL_SHIFT;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        super.tick();
        if (recruit.tickCount % 10 != 0) return;
        RangedAction action = RangedSpacingService.currentAction(recruit);
        if (action == RangedAction.FALLBACK) {
            applyFallback();
        } else if (action == RangedAction.LATERAL_SHIFT) {
            applyLateralShift();
        }
    }

    private void applyFallback() {
        Vec3 self = recruit.position();
        LivingEntity threat = recruit.getTarget();
        Vec3 awayDir;
        if (threat != null) {
            Vec3 v = self.subtract(threat.position());
            awayDir = v.lengthSqr() < 1.0E-4D ? recruit.getLookAngle().reverse() : v.normalize();
        } else {
            awayDir = recruit.getLookAngle().reverse();
        }
        Vec3 destination = self.add(awayDir.x * FALLBACK_STEP, 0.0D, awayDir.z * FALLBACK_STEP);
        recruit.getNavigation().moveTo(destination.x, destination.y, destination.z, FALLBACK_SPEED);
    }

    private void applyLateralShift() {
        Vec3 self = recruit.position();
        Vec3 facing = recruit.getLookAngle().normalize();
        // Right-hand perpendicular in the XZ plane: rotate facing by -90° about Y.
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x).normalize();
        Vec3 destination = self.add(right.x * LATERAL_STEP, 0.0D, right.z * LATERAL_STEP);
        recruit.getNavigation().moveTo(destination.x, destination.y, destination.z, LATERAL_SPEED);
    }
}
