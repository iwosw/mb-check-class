package com.talhanation.bannermod.ai.citizen;

import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.settlement.prefab.staffing.PrefabAutoStaffingRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Walks a citizen toward the work-area it has been assigned to but hasn't yet reached.
 *
 * <p>Without this, a citizen with {@code TAG_PENDING_WORKER_PROFESSION} + a bound work-area
 * UUID has no AI goal that actively drives it toward the anchor. The vanilla
 * {@code WaterAvoidingRandomStrollGoal} only picks short random destinations and never
 * pathfinds toward the assignment, so the citizen wanders into nearby walls or strolls
 * away from its zone — the exact "упарываются в стену" symptom the player reported.
 *
 * <p>This goal uses the citizen's {@link com.talhanation.bannermod.ai.pathfinding.AsyncGroundPathNavigation}
 * via {@link net.minecraft.world.entity.ai.navigation.PathNavigation#moveTo(double, double, double, double)}
 * which performs full A* around obstacles. Once the citizen is within the conversion
 * threshold (≤3 blocks of the anchor, mirroring {@link CitizenEntity}'s pending-conversion
 * check), the goal yields so {@code tryConvertIntoPendingWorker} can run on the next tick.
 */
public class CitizenSeekWorkAreaGoal extends Goal {

    /** Square of the conversion-distance threshold from {@code CitizenEntity#tryConvertIntoPendingWorker}. */
    private static final double ARRIVAL_DISTANCE_SQR = 9.0D;

    /** Re-issue moveTo no more often than this — A* is expensive and path stays valid for a while. */
    private static final int REPATH_COOLDOWN_TICKS = 40;

    /** Speed multiplier for the move (1.0 = normal). Citizens are workers-in-waiting, no rush. */
    private static final double SPEED = 0.9D;

    private final CitizenEntity citizen;
    private int repathCooldown;
    private BlockPos lastTarget;

    public CitizenSeekWorkAreaGoal(CitizenEntity citizen) {
        this.citizen = citizen;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(this.citizen.level() instanceof ServerLevel)) return false;
        // Only seek when the auto-staffing pipeline has both bound this citizen to a
        // work area AND queued a pending profession on it. Otherwise we're not in the
        // "walk to the workplace and convert" state and the standard wander goals win.
        if (!this.citizen.getPersistentData().contains(
                PrefabAutoStaffingRuntime.TAG_PENDING_WORKER_PROFESSION, Tag.TAG_STRING)) {
            return false;
        }
        return resolveAnchorPos() != null && distanceToAnchorSqr() > ARRIVAL_DISTANCE_SQR;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
        this.lastTarget = null;
    }

    @Override
    public void stop() {
        this.citizen.getNavigation().stop();
        this.lastTarget = null;
    }

    @Override
    public void tick() {
        BlockPos target = resolveAnchorPos();
        if (target == null) return;

        // Repath only when the cooldown elapses or the anchor moved (e.g. a bound entity
        // tracked vs the static manualAnchor fallback). This avoids resetting the path
        // every tick, which would prevent A* from making forward progress on long routes.
        if (this.repathCooldown > 0 && target.equals(this.lastTarget)) {
            this.repathCooldown--;
            return;
        }

        this.citizen.getNavigation().moveTo(
                target.getX() + 0.5D,
                target.getY(),
                target.getZ() + 0.5D,
                SPEED);
        this.lastTarget = target;
        this.repathCooldown = REPATH_COOLDOWN_TICKS;
    }

    private BlockPos resolveAnchorPos() {
        UUID anchorUuid = this.citizen.getBoundWorkAreaUUID();
        if (anchorUuid == null) return null;

        ServerLevel level = (ServerLevel) this.citizen.level();
        Entity anchor = level.getEntity(anchorUuid);
        if (anchor instanceof AbstractWorkAreaEntity workArea && workArea.isAlive()) {
            return workArea.blockPosition();
        }
        // Fallback: VACANCIES may carry a manual anchor pos for prefab/manual building
        // bindings even if the entity isn't currently loaded. Mirrors the resolution
        // logic in CitizenEntity#tryConvertIntoPendingWorker.
        return PrefabAutoStaffingRuntime.conversionAnchorPosition(anchorUuid);
    }

    private double distanceToAnchorSqr() {
        BlockPos pos = resolveAnchorPos();
        if (pos == null) return Double.POSITIVE_INFINITY;
        double dx = (pos.getX() + 0.5D) - this.citizen.getX();
        double dy = pos.getY() - this.citizen.getY();
        double dz = (pos.getZ() + 0.5D) - this.citizen.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
