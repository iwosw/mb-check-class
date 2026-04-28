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

public class RecruitSiegeObjectiveAttackGoal extends Goal {
    private static final double SPEED = 1.0D;
    private static final double ATTACK_RANGE_SQR = 3.0D * 3.0D;
    private static final int DAMAGE_PER_HIT = 3;
    private static final int HIT_INTERVAL_TICKS = 20;

    private final AbstractRecruitEntity recruit;
    private SiegeStandardRecord target;
    private int hitCooldown;

    public RecruitSiegeObjectiveAttackGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(recruit.level() instanceof ServerLevel level)) return false;
        if (recruit.getTarget() != null || recruit.getFleeing()) return false;
        UUID side = ownerSide(level);
        if (side == null) return false;
        Optional<SiegeStandardRecord> nearest = nearestEnemyStandard(level, side);
        if (nearest.isEmpty()) return false;
        target = nearest.get();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!(recruit.level() instanceof ServerLevel level) || target == null || recruit.getFleeing()) return false;
        return WarRuntimeContext.sieges(level).byId(target.id()).isPresent();
    }

    @Override
    public void tick() {
        if (!(recruit.level() instanceof ServerLevel level) || target == null) return;
        BlockPos pos = target.pos();
        recruit.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        if (recruit.distanceToSqr(Vec3.atCenterOf(pos)) > ATTACK_RANGE_SQR) {
            recruit.getNavigation().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, SPEED);
            return;
        }
        recruit.getNavigation().stop();
        if (hitCooldown-- > 0) return;
        hitCooldown = HIT_INTERVAL_TICKS;
        WarRuntimeContext.sieges(level).applyDamage(target.id(), DAMAGE_PER_HIT)
                .ifPresent(outcome -> {
                    if (outcome.destroyed()) {
                        WarRuntimeContext.sieges(level).remove(target.id());
                    }
                });
    }

    @Override
    public void stop() {
        target = null;
        hitCooldown = 0;
        recruit.getNavigation().stop();
    }

    private UUID ownerSide(ServerLevel level) {
        UUID ownerUuid = recruit.getOwnerUUID();
        if (ownerUuid == null) return null;
        for (com.talhanation.bannermod.war.registry.PoliticalEntityRecord record : WarRuntimeContext.registry(level).all()) {
            if (ownerUuid.equals(record.leaderUuid()) || record.coLeaderUuids().contains(ownerUuid)) return record.id();
        }
        return null;
    }

    private Optional<SiegeStandardRecord> nearestEnemyStandard(ServerLevel level, UUID side) {
        SiegeStandardRecord best = null;
        double bestSqr = Double.POSITIVE_INFINITY;
        for (SiegeStandardRecord record : WarRuntimeContext.sieges(level).all()) {
            if (!SiegeObjectivePolicy.canAttackStandard(side, record.sidePoliticalEntityId())) continue;
            double d = recruit.distanceToSqr(Vec3.atCenterOf(record.pos()));
            if (d < bestSqr) {
                bestSqr = d;
                best = record;
            }
        }
        return Optional.ofNullable(best);
    }
}
