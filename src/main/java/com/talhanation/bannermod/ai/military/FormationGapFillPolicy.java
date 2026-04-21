package com.talhanation.bannermod.ai.military;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Pure helper for Step 1.C: choose a formation slot to migrate into when a
 * neighbour dies and leaves a forward gap.
 *
 * <p>Works in the formation's local (owner-rot) frame so "forward" means toward
 * the enemy. Kept framework-free so unit tests do not need a Minecraft runtime.
 */
public final class FormationGapFillPolicy {

    /** How many ranks of lateral distance count as "adjacent" for fallback fills. */
    public static final int ADJACENT_LATERAL_RANK_DISTANCE = 1;

    private FormationGapFillPolicy() {
    }

    /** Gap-fill respects stance: only LINE_HOLD and SHIELD_WALL migrate to fill gaps. */
    public static boolean stanceAllowsGapFill(CombatStance stance) {
        return stance == CombatStance.LINE_HOLD || stance == CombatStance.SHIELD_WALL;
    }

    /**
     * Local-frame slot snapshot.
     *
     * @param slotIndex formation index (matches recruit.formationPos).
     * @param localX    rightward offset from the selfSlot in the formation frame.
     * @param localY    forward offset from the selfSlot in the formation frame
     *                  (positive = further from enemy, negative = closer to enemy).
     *                  This matches the existing formation patterns where higher rank
     *                  index is further back (behind the line).
     * @param occupied  true if some living recruit currently owns this slot.
     */
    public record LocalSlot(int slotIndex, double localX, double localY, boolean occupied) {
    }

    /**
     * Given a set of slots in the formation's local frame, choose the best empty
     * slot for the self-slot to migrate into.
     *
     * <p>Preference order:
     * <ol>
     *   <li>Strictly forward gap (localY strictly less than self's localY), nearest
     *       to self measured in local frame.</li>
     *   <li>Adjacent lateral gap within {@link #ADJACENT_LATERAL_RANK_DISTANCE} ranks,
     *       nearest to self.</li>
     * </ol>
     *
     * @return the chosen slot's index, if any.
     */
    public static Optional<Integer> chooseGapSlot(LocalSlot self, List<LocalSlot> slots) {
        if (self == null || slots == null || slots.isEmpty()) {
            return Optional.empty();
        }

        Optional<LocalSlot> forwardGap = slots.stream()
                .filter(s -> s.slotIndex() != self.slotIndex())
                .filter(s -> !s.occupied())
                .filter(s -> s.localY() < self.localY() - 1e-6)
                .min(Comparator.comparingDouble(s -> distanceSqr(self, s)));
        if (forwardGap.isPresent()) {
            return Optional.of(forwardGap.get().slotIndex());
        }

        Optional<LocalSlot> adjacentGap = slots.stream()
                .filter(s -> s.slotIndex() != self.slotIndex())
                .filter(s -> !s.occupied())
                .filter(s -> Math.abs(s.localY() - self.localY()) <= ADJACENT_LATERAL_RANK_DISTANCE + 1e-6)
                .min(Comparator.comparingDouble(s -> distanceSqr(self, s)));
        return adjacentGap.map(LocalSlot::slotIndex);
    }

    private static double distanceSqr(LocalSlot a, LocalSlot b) {
        double dx = a.localX() - b.localX();
        double dy = a.localY() - b.localY();
        return dx * dx + dy * dy;
    }

    /**
     * Project a world-space delta onto the formation's local (x=right, y=forward-along-rank) frame
     * given the owner yaw in degrees. Positive Y goes "backward" (away from the enemy) — this
     * matches the pattern builders, which subtract {@code forward} as rank index grows.
     */
    public static double[] worldDeltaToLocal(double worldDx, double worldDz, float ownerRotDeg) {
        // Formation forward vector (toward enemy) matches FormationPatternBuilder.forwardFromYaw:
        //   forward = (-sin(yaw), 0, cos(yaw))
        double rad = Math.toRadians(ownerRotDeg);
        double forwardX = -Math.sin(rad);
        double forwardZ = Math.cos(rad);
        // Left vector (leftOf) = (-forward.z, 0, forward.x) => "right" = (forward.z, 0, -forward.x).
        double rightX = forwardZ;
        double rightZ = -forwardX;

        double localRight = worldDx * rightX + worldDz * rightZ;
        double localForward = worldDx * forwardX + worldDz * forwardZ;
        // Pattern builders subtract `forward * rank * spacing` as rank grows, so higher rank
        // index -> smaller (more negative) worldDelta·forward. Flip sign so localY is "back".
        return new double[]{localRight, -localForward};
    }
}
