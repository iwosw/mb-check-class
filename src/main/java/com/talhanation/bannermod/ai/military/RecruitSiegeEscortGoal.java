package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.combat.SiegeObjectivePolicy;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.runtime.SiegeStandardRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Keeps owner-side recruits drifting back toward a same-faction siege standard when they
 * stray outside its radius. Implements the COMBAT-006 acceptance bullet
 * "Defenders hold around own standard" without needing the player to issue a manual
 * hold-pos order.
 *
 * <p>The goal sits at priority 7 — below the explicit move/attack/protect goals so the
 * player's commands and the combat priority chain still win, but above wandering — so an
 * idle defender drifts back toward the standard rather than wandering off.</p>
 *
 * <p>Activation: requires the recruit to have a faction (resolved through
 * {@link WarRuntimeContext#factionOf}), be currently outside the radius of any same-side
 * standard, and not be holding an explicit move-pos / hold-pos / target. The goal uses
 * {@link SiegeObjectivePolicy#shouldEscort} to filter standards so recruits never drift
 * toward an enemy standard, even if it happens to be closer.</p>
 */
public class RecruitSiegeEscortGoal extends Goal {

    public static final double ESCORT_SPEED = 1.00D;
    /** When the recruit is more than {@code radius - SLACK} from the standard centre, head back. */
    public static final double SLACK_BLOCKS = 4.0D;

    private final AbstractRecruitEntity recruit;
    private BlockPos destination;

    public RecruitSiegeEscortGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(recruit.level() instanceof ServerLevel level)) return false;
        if (recruit.getTarget() != null) return false;
        if (recruit.getShouldHoldPos() || recruit.getShouldMovePos()) return false;
        if (recruit.getFleeing()) return false;
        UUID side = ownerSide(level);
        if (side == null) return false;
        Optional<SiegeStandardRecord> escortTarget = nearestSameSideStandard(level, side);
        if (escortTarget.isEmpty()) return false;
        SiegeStandardRecord record = escortTarget.get();
        double distance = Math.sqrt(recruit.distanceToSqr(Vec3.atCenterOf(record.pos())));
        if (distance <= Math.max(0.0D, record.radius() - SLACK_BLOCKS)) {
            return false;
        }
        destination = record.pos();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (destination == null || recruit.getTarget() != null) return false;
        if (recruit.getShouldHoldPos() || recruit.getShouldMovePos() || recruit.getFleeing()) return false;
        return recruit.distanceToSqr(Vec3.atCenterOf(destination)) > 16.0D;
    }

    @Override
    public void start() {
        super.start();
        if (destination != null) {
            recruit.getNavigation().moveTo(destination.getX() + 0.5,
                    destination.getY(), destination.getZ() + 0.5, ESCORT_SPEED);
        }
    }

    @Override
    public void stop() {
        super.stop();
        destination = null;
        recruit.getNavigation().stop();
    }

    private UUID ownerSide(ServerLevel level) {
        UUID ownerUuid = recruit.getOwnerUUID();
        if (ownerUuid == null) return null;
        for (com.talhanation.bannermod.war.registry.PoliticalEntityRecord record :
                WarRuntimeContext.registry(level).all()) {
            if (ownerUuid.equals(record.leaderUuid())) return record.id();
            if (record.coLeaderUuids() != null && record.coLeaderUuids().contains(ownerUuid)) {
                return record.id();
            }
        }
        return null;
    }

    private Optional<SiegeStandardRecord> nearestSameSideStandard(ServerLevel level, UUID side) {
        SiegeStandardRecord best = null;
        double bestSqr = Double.POSITIVE_INFINITY;
        for (SiegeStandardRecord record : WarRuntimeContext.sieges(level).all()) {
            if (!SiegeObjectivePolicy.shouldEscort(side, record.sidePoliticalEntityId())) continue;
            double d = recruit.distanceToSqr(Vec3.atCenterOf(record.pos()));
            if (d < bestSqr) {
                bestSqr = d;
                best = record;
            }
        }
        return Optional.ofNullable(best);
    }
}
