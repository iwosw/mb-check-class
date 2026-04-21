package com.talhanation.bannermod.ai.military;

import java.util.List;
import java.util.function.Predicate;

/**
 * Stage 3.B: "friendly-allowed" line of sight for reach-weapon attackers.
 *
 * <p>A spearman in rank 2 with a reach-3 spear should be able to attack a target
 * engaged by rank 1 without leaving his slot. Vanilla LOS fails as soon as an
 * ally stands between attacker and target. This helper widens the check: a
 * blocking ALLIED entity is permitted, but any intervening BLOCK or non-allied
 * entity still breaks LOS.
 *
 * <p>Kept framework-free so unit tests can inject raycast results via a small
 * functional interface; the real goal wires it to {@code level().clip(...)} and
 * the normal entity collision lookup.
 */
public final class FriendlyLineOfSight {

    /** Simple bag of what a world raycast returned along the attacker→target segment. */
    public static final class SightProbe<E> {
        /** True if a block column (stone/wall/dirt) blocks the segment. */
        public final boolean blockedByWorld;
        /** Entities (other than attacker / target) the segment intersects, in nearest-first order. */
        public final List<E> entitiesOnPath;

        public SightProbe(boolean blockedByWorld, List<E> entitiesOnPath) {
            this.blockedByWorld = blockedByWorld;
            this.entitiesOnPath = entitiesOnPath == null ? List.of() : entitiesOnPath;
        }
    }

    @FunctionalInterface
    public interface WorldRaycastFn<E> {
        SightProbe<E> probe();
    }

    private FriendlyLineOfSight() {
    }

    /**
     * Returns true if the attacker can see / reach the target through a gauntlet
     * of intervening entities, treating allied entities as transparent.
     *
     * <p>Semantics:
     * <ul>
     *   <li>If the block raycast hits a solid block → return false.</li>
     *   <li>If every entity on the path satisfies {@code alliedPredicate} →
     *       return true (spearman pokes through rank 1).</li>
     *   <li>The first non-allied entity on the path → return false.</li>
     * </ul>
     *
     * <p>Callers that already have a passing vanilla {@code hasLineOfSight} can
     * skip this helper; this routine exists for the fallback path where vanilla
     * LOS failed precisely because an ally stood in the way.
     *
     * @param probeFn          lazy world probe; only invoked if needed.
     * @param alliedPredicate  returns true if the given entity should be treated
     *                         as transparent (same team / recruit / etc.).
     */
    public static <E> boolean canReachThroughAllies(WorldRaycastFn<E> probeFn,
                                                    Predicate<E> alliedPredicate) {
        if (probeFn == null || alliedPredicate == null) {
            return false;
        }
        SightProbe<E> probe = probeFn.probe();
        if (probe == null) {
            return false;
        }
        if (probe.blockedByWorld) {
            return false;
        }
        for (E entity : probe.entitiesOnPath) {
            if (!alliedPredicate.test(entity)) {
                return false;
            }
        }
        return true;
    }
}
