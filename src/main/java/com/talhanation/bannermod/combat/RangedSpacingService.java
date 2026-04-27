package com.talhanation.bannermod.combat;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.IRangedRecruit;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives {@link RangedSpacingPolicy} on a per-recruit basis for {@link IRangedRecruit}
 * mobs (bowmen, crossbowmen, scouts, nomads). The service samples three world inputs
 * per tick:
 * <ul>
 *   <li>{@code distanceToNearestEnemyMelee}: closest non-ranged hostile in
 *       {@link #SCAN_RADIUS}. Hostile classification reuses {@link AbstractRecruitEntity#canAttack}
 *       so the spacing policy stays consistent with the rest of the combat layer.</li>
 *   <li>{@code distanceToOwnMeleeLineFront}: closest same-owner ally that is NOT a ranged
 *       recruit, inside {@link #SCAN_RADIUS}. The ally pool comes from the per-owner
 *       {@link RecruitIndex} bucket — chunk-pruned and recruit-only.</li>
 *   <li>{@code firingLaneBlockedByAlly}: any same-owner ally between the recruit and its
 *       current target inside the firing cone half-angle. We test "ally is closer to target
 *       than recruit AND ally lies along the line-of-sight projection".</li>
 * </ul>
 *
 * <p>The cached {@link RangedAction} is read by {@link com.talhanation.bannermod.ai.military.RecruitRangedSpacingGoal}
 * so it can apply the movement override without re-running the sample on every tick.</p>
 *
 * <p>{@link MoraleState} interaction: this service does not gate on rout — when the recruit
 * is routed the higher-priority {@link com.talhanation.bannermod.ai.military.RecruitMoraleRoutGoal}
 * already overrides movement, so the spacing decision becomes irrelevant until the rout
 * window closes.</p>
 */
public final class RangedSpacingService {

    /** Re-evaluate every 20 ticks (1 second) to keep the scan off the hot path. */
    public static final int EVALUATION_INTERVAL_TICKS = 20;

    /** Radius (blocks) for both ally and hostile scans. */
    public static final double SCAN_RADIUS = 16.0D;

    /** Lateral offset (blocks) the firing-lane check tolerates before flagging "blocked". */
    public static final double FIRING_LANE_HALF_WIDTH = 1.5D;

    private static final Map<UUID, Entry> STATE = new ConcurrentHashMap<>();

    private RangedSpacingService() {
    }

    public static final class Entry {
        public RangedAction lastAction = RangedAction.STAY;
        public long lastEvaluationTick = Long.MIN_VALUE;
    }

    public static Entry stateFor(UUID recruitUuid) {
        return STATE.computeIfAbsent(recruitUuid, k -> new Entry());
    }

    public static RangedAction currentAction(AbstractRecruitEntity recruit) {
        if (recruit == null) return RangedAction.STAY;
        Entry entry = STATE.get(recruit.getUUID());
        return entry == null ? RangedAction.STAY : entry.lastAction;
    }

    public static void invalidate(UUID recruitUuid) {
        if (recruitUuid != null) {
            STATE.remove(recruitUuid);
        }
    }

    public static void resetForTests() {
        STATE.clear();
    }

    /**
     * Run one evaluation step. Throttled to {@link #EVALUATION_INTERVAL_TICKS}; callers can
     * call this every tick from {@code RecruitRuntimeLoop.tick} without adding their own
     * gate.
     */
    public static RangedAction tick(AbstractRecruitEntity recruit, ServerLevel level, long gameTime) {
        if (!(recruit instanceof IRangedRecruit)) {
            return RangedAction.STAY;
        }
        Entry entry = stateFor(recruit.getUUID());
        if (gameTime - entry.lastEvaluationTick < EVALUATION_INTERVAL_TICKS) {
            return entry.lastAction;
        }
        entry.lastEvaluationTick = gameTime;

        UUID ownerUuid = recruit.getOwnerUUID();
        Vec3 selfPos = recruit.position();

        double distToOwnMelee = nearestOwnMeleeDistance(recruit, level, ownerUuid, selfPos);
        double distToEnemyMelee = nearestEnemyMeleeDistance(recruit, level, selfPos);
        boolean laneBlocked = firingLaneBlocked(recruit, level, ownerUuid);

        RangedAction action = RangedSpacingPolicy.decide(distToEnemyMelee, distToOwnMelee, laneBlocked);
        entry.lastAction = action;
        return action;
    }

    private static double nearestOwnMeleeDistance(AbstractRecruitEntity recruit,
                                                  ServerLevel level,
                                                  UUID ownerUuid,
                                                  Vec3 selfPos) {
        if (ownerUuid == null) return Double.POSITIVE_INFINITY;
        List<AbstractRecruitEntity> allies = RecruitIndex.instance()
                .ownerInRange(level, ownerUuid, selfPos, SCAN_RADIUS);
        if (allies == null || allies.isEmpty()) return Double.POSITIVE_INFINITY;
        double best = Double.POSITIVE_INFINITY;
        for (AbstractRecruitEntity ally : allies) {
            if (ally == recruit || !ally.isAlive()) continue;
            if (ally instanceof IRangedRecruit) continue; // Need front-line allies, not other archers.
            double d = recruit.distanceTo(ally);
            if (d < best) best = d;
        }
        return best;
    }

    private static double nearestEnemyMeleeDistance(AbstractRecruitEntity recruit,
                                                    ServerLevel level,
                                                    Vec3 selfPos) {
        AABB box = recruit.getBoundingBox().inflate(SCAN_RADIUS);
        double best = Double.POSITIVE_INFINITY;
        for (LivingEntity hostile : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != recruit && e.isAlive() && recruit.canAttack(e))) {
            // Skip ranged hostile — the policy is about MELEE breakthrough.
            if (RecruitRoleResolver.roleOf(hostile) == CombatRole.RANGED) continue;
            double d = recruit.distanceTo(hostile);
            if (d < best) best = d;
        }
        return best;
    }

    private static boolean firingLaneBlocked(AbstractRecruitEntity recruit,
                                             ServerLevel level,
                                             UUID ownerUuid) {
        LivingEntity target = recruit.getTarget();
        if (target == null || ownerUuid == null) return false;
        Vec3 from = recruit.position();
        Vec3 to = target.position();
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        if (len < 0.001D) return false;
        Vec3 unit = dir.scale(1.0D / len);
        List<AbstractRecruitEntity> allies = RecruitIndex.instance()
                .ownerInRange(level, ownerUuid, from, len);
        if (allies == null) return false;
        for (AbstractRecruitEntity ally : allies) {
            if (ally == recruit || !ally.isAlive()) continue;
            Vec3 rel = ally.position().subtract(from);
            double along = rel.dot(unit);
            if (along <= 0.0D || along >= len) continue;
            // Project ally onto the line; lateral offset is the perpendicular distance.
            Vec3 projection = unit.scale(along);
            Vec3 perpVec = rel.subtract(projection);
            double lateral = perpVec.length();
            if (lateral <= FIRING_LANE_HALF_WIDTH) {
                return true;
            }
        }
        return false;
    }
}
