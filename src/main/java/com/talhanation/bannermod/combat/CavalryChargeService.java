package com.talhanation.bannermod.combat;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.HorsemanEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives {@link CavalryChargePolicy} on a per-cavalry-recruit basis.
 *
 * <p>The service tracks the {@link CavalryChargeState} for every {@link HorsemanEntity} (or
 * any other recruit that climbs onto a horse) keyed by the recruit's UUID. State transitions:
 * <ul>
 *   <li>NOT_CHARGING → CHARGING when the rider's horse exceeds {@link #CHARGE_SPEED_THRESHOLD}
 *       blocks / tick. The threshold is tuned slightly below a vanilla galloping horse so
 *       intentional charges latch the state, but a slow approach does not.</li>
 *   <li>CHARGING → EXHAUSTED on a successful melee hit (driven by
 *       {@link #onChargeHit(AbstractRecruitEntity)}).</li>
 *   <li>EXHAUSTED → NOT_CHARGING after {@link CavalryChargePolicy#CHARGE_COOLDOWN_TICKS}.</li>
 * </ul>
 *
 * <p>The pure-logic state machine in {@link CavalryChargePolicy#advance} owns every
 * transition; this service only owns the world-side inputs (vehicle speed) and the
 * entity-keyed timeline. State is invalidated through {@link #invalidate(UUID)} on the
 * entity-leave hook so dead/discarded recruits don't leak entries.
 */
public final class CavalryChargeService {

    /**
     * Vehicle speed threshold (blocks per tick) at which the rider is treated as "charging".
     * A vanilla horse galloping at full sprint moves ~0.45 blocks/tick; we latch at 0.20 so
     * a deliberate forward charge enters CHARGING but a casual trot does not.
     */
    public static final double CHARGE_SPEED_THRESHOLD = 0.20D;

    private static final Map<UUID, Entry> STATE = new ConcurrentHashMap<>();

    private CavalryChargeService() {
    }

    /** Per-recruit timeline entry. */
    public static final class Entry {
        public CavalryChargeState state = CavalryChargeState.NOT_CHARGING;
        public long enteredExhaustedTick;
    }

    public static Entry stateFor(UUID recruitUuid) {
        return STATE.computeIfAbsent(recruitUuid, k -> new Entry());
    }

    public static CavalryChargeState currentState(AbstractRecruitEntity recruit) {
        if (recruit == null) {
            return CavalryChargeState.NOT_CHARGING;
        }
        Entry entry = STATE.get(recruit.getUUID());
        return entry == null ? CavalryChargeState.NOT_CHARGING : entry.state;
    }

    /** Drop the recruit's entry. Called from the entity-leave hook. */
    public static void invalidate(UUID recruitUuid) {
        if (recruitUuid != null) {
            STATE.remove(recruitUuid);
        }
    }

    /** Reset everything — used by tests and the dedicated test harness. */
    public static void resetForTests() {
        STATE.clear();
    }

    /**
     * One tick of state evolution. Must be called only on cavalry-eligible recruits — guard
     * with {@link #isChargeEligible(AbstractRecruitEntity)} on the caller. Speed is computed
     * from the horse's delta movement so dismounted horsemen never accumulate charge.
     */
    public static void tick(AbstractRecruitEntity recruit, long gameTime) {
        if (!isChargeEligible(recruit)) {
            return;
        }
        Entry entry = stateFor(recruit.getUUID());
        CavalryChargeState before = entry.state;
        switch (before) {
            case NOT_CHARGING -> {
                if (vehicleSpeed(recruit) >= CHARGE_SPEED_THRESHOLD) {
                    entry.state = CavalryChargeState.CHARGING;
                }
            }
            case CHARGING -> {
                // Charging persists until a hit lands (handled by onChargeHit). If the rider
                // slows to a stop the charge is interpreted as wasted — drop back to
                // NOT_CHARGING so the next gallop re-arms cleanly.
                if (vehicleSpeed(recruit) < CHARGE_SPEED_THRESHOLD * 0.5D) {
                    entry.state = CavalryChargeState.NOT_CHARGING;
                }
            }
            case EXHAUSTED -> {
                long ticksSince = gameTime - entry.enteredExhaustedTick;
                CavalryChargeState advanced = CavalryChargePolicy.advance(
                        before, false, (int) Math.max(0, ticksSince),
                        CavalryChargePolicy.CHARGE_COOLDOWN_TICKS);
                entry.state = advanced;
            }
        }
    }

    /**
     * Notifies the service that the cavalry recruit has just connected a melee hit. Drives
     * the CHARGING → EXHAUSTED transition through {@link CavalryChargePolicy#advance}.
     */
    public static void onChargeHit(AbstractRecruitEntity recruit) {
        if (recruit == null || !isChargeEligible(recruit)) {
            return;
        }
        Entry entry = stateFor(recruit.getUUID());
        CavalryChargeState advanced = CavalryChargePolicy.advance(
                entry.state, true, 0, CavalryChargePolicy.CHARGE_COOLDOWN_TICKS);
        if (advanced == CavalryChargeState.EXHAUSTED && entry.state != CavalryChargeState.EXHAUSTED) {
            entry.enteredExhaustedTick = recruit.level().getGameTime();
        }
        entry.state = advanced;
    }

    /**
     * True if {@code recruit} is eligible to enter / stay in a charge — currently a
     * {@link HorsemanEntity} or any other recruit whose vehicle is an {@link AbstractHorse}.
     */
    public static boolean isChargeEligible(AbstractRecruitEntity recruit) {
        if (recruit == null) return false;
        if (recruit instanceof HorsemanEntity) {
            return recruit.getVehicle() instanceof AbstractHorse;
        }
        return recruit.getVehicle() instanceof AbstractHorse;
    }

    private static double vehicleSpeed(AbstractRecruitEntity recruit) {
        Entity vehicle = recruit.getVehicle();
        if (!(vehicle instanceof LivingEntity living)) return 0.0D;
        return living.getDeltaMovement().horizontalDistance();
    }

    /**
     * Compute the cavalry-charge damage multiplier this {@code attacker} should apply when
     * landing a melee hit on {@code target}. Wraps {@link CavalryChargePolicy#damageMultiplierFor}
     * with the world-side resolution of attacker state, target role, and target brace flag —
     * the call site stays a single line.
     *
     * <p>Returns {@code 1.0} unless the attacker is a charge-eligible recruit, in which case
     * the policy decides:
     * <ul>
     *   <li>CHARGING vs unbraced INFANTRY/RANGED → {@link CavalryChargePolicy#FIRST_HIT_BONUS_MULTIPLIER}.</li>
     *   <li>Anything vs braced PIKE → {@link CavalryChargePolicy#PIKE_BRACE_PENALTY_MULTIPLIER}.</li>
     *   <li>Otherwise (NOT_CHARGING, EXHAUSTED, neutral pairs) → 1.0.</li>
     * </ul>
     */
    public static double computeChargeMultiplierFor(AbstractRecruitEntity attacker, Entity target) {
        if (attacker == null || !isChargeEligible(attacker)) {
            return 1.0D;
        }
        if (!(target instanceof LivingEntity livingTarget)) {
            return 1.0D;
        }
        CavalryChargeState state = currentState(attacker);
        CombatRole targetRole = RecruitRoleResolver.roleOf(livingTarget);
        boolean targetBraced = livingTarget instanceof AbstractRecruitEntity recruitTarget
                && recruitTarget.isBracing;
        return CavalryChargePolicy.damageMultiplierFor(state, targetRole, targetBraced);
    }
}
