package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.combat.RecruitMoraleService;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * High-priority goal that takes over recruit movement when {@link RecruitMoraleService}
 * has classified the recruit's squad as routed. Drives the recruit away from its nearest
 * hostile and clears any pending combat target, so the recruit visibly disengages.
 *
 * <p>Acceptance link to COMBAT-001: this goal is the "badly outnumbered squads can rout"
 * gameplay outcome. Without it, the morale service would update internal state and chat
 * messages but the recruit would keep fighting — there would be nothing for the player to
 * observe in-world.</p>
 *
 * <p>Goal semantics:
 * <ul>
 *   <li>{@link #canUse()} returns true only while the rout window is open. The window is
 *       set by the morale service and decays over time, so the goal naturally hands control
 *       back to the normal combat goals once the squad recovers.</li>
 *   <li>Re-scans for the nearest hostile within {@link #FLEE_SCAN_RADIUS} every
 *       {@link #FLEE_SCAN_INTERVAL_TICKS} ticks via the leader-pool
 *       {@link CommanderHostileScanCache}, so a routed shield-wall of N recruits sharing
 *       one owner causes <strong>one</strong> underlying world scan per scan-interval
 *       bucket instead of N. Walks away in a straight line for {@link #FLEE_STEP_DISTANCE}
 *       blocks; the pathfinder handles obstacle avoidance.</li>
 *   <li>Combat target is cleared on start so the underlying attack controller stops reacting
 *       to the threat the recruit is fleeing from.</li>
 * </ul>
 *
 * <p>SCANPOOL-002 wiring: {@link CommanderHostileScanCache#findNearestHostile} replaces the
 * previous per-recruit {@code level.getEntitiesOfClass} call. The cache scans with
 * {@code FLEE_SCAN_RADIUS + GROUP_AABB_PADDING} once per (level, owner, scan-bucket) triple
 * and serves the rest of the routed squad from the cached snapshot, while per-recruit
 * filtering still uses the per-recruit radius and {@link AbstractRecruitEntity#canAttack}.
 */
public class RecruitMoraleRoutGoal extends Goal {

    public static final double FLEE_SCAN_RADIUS = 24.0D;
    public static final double FLEE_STEP_DISTANCE = 8.0D;
    public static final double FLEE_SPEED = 1.25D;
    /**
     * Re-scan cadence (ticks) for the rout flee target. Matches
     * {@code UseShield.HOSTILE_SCAN_INTERVAL_TICKS} so routed recruits share buckets with
     * their shield-wall siblings inside {@link CommanderHostileScanCache}.
     */
    public static final int FLEE_SCAN_INTERVAL_TICKS = 10;

    private final AbstractRecruitEntity recruit;
    private LivingEntity threat;

    public RecruitMoraleRoutGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!(recruit.level() instanceof ServerLevel level)) return false;
        return RecruitMoraleService.isRouted(recruit, level.getGameTime());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        super.start();
        recruit.setTarget(null);
    }

    @Override
    public void stop() {
        super.stop();
        threat = null;
        recruit.getNavigation().stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(recruit.level() instanceof ServerLevel)) return;
        if (recruit.tickCount % FLEE_SCAN_INTERVAL_TICKS != 0) {
            return;
        }
        threat = nearestHostile();
        Vec3 awayTarget;
        if (threat != null) {
            Vec3 away = recruit.position().subtract(threat.position());
            if (away.lengthSqr() < 1.0E-4D) {
                away = new Vec3(1.0D, 0.0D, 0.0D);
            }
            Vec3 dir = away.normalize();
            awayTarget = recruit.position().add(dir.scale(FLEE_STEP_DISTANCE));
        } else {
            // No visible threat — keep distancing in the recruit's current facing direction
            // until the window closes. Standing still during a rout would defeat the
            // gameplay intent ("squad disengaged") even if the immediate threat ducked out
            // of the scan radius for a moment.
            float yaw = (float) Math.toRadians(recruit.getYRot());
            Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
            awayTarget = recruit.position().add(forward.scale(FLEE_STEP_DISTANCE));
        }
        recruit.getNavigation().moveTo(awayTarget.x, awayTarget.y, awayTarget.z, FLEE_SPEED);
    }

    /**
     * Pulls the nearest hostile from the leader-pool cache. The cache scans once per
     * {@link #FLEE_SCAN_INTERVAL_TICKS} per (level, owner) group; the per-recruit
     * {@code canAttack} predicate is applied to the cached snapshot so flee-target
     * selection is byte-for-byte equivalent to the previous per-recruit
     * {@code level.getEntitiesOfClass} pick.
     */
    private LivingEntity nearestHostile() {
        return CommanderHostileScanCache.findNearestHostile(
                recruit,
                FLEE_SCAN_RADIUS,
                FLEE_SCAN_INTERVAL_TICKS,
                (r, candidate) -> r.canAttack(candidate),
                CommanderHostileScanCache.LEVEL_SCANNER
        );
    }
}
