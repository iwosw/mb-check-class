package com.talhanation.bannermod.combat;

import java.util.Collection;
import java.util.UUID;

/**
 * Pure radius + faction check that decides whether any commander's aura is active for a
 * given squad. Output feeds {@link MoraleSnapshot#commanderPresent()} in the upcoming combat
 * AI hookup.
 *
 * <p>Two rules:
 * <ul>
 *   <li>The commander's political entity must equal the squad's political entity. The aura
 *       does not project morale to enemies or to neutral squads with a different (or no)
 *       affiliation. Both ids must be non-null and equal.</li>
 *   <li>The squared 3D distance between commander and squad must be {@code <=} the squared
 *       configured aura radius. Squared comparison avoids the sqrt cost on every tick.</li>
 * </ul>
 *
 * <p>The radius constant is a public {@code static final double} so a future Forge-config
 * layer can override it without touching the decision tree.
 */
public final class CommanderAuraPolicy {

    /**
     * Default aura radius in blocks. A captain's discipline reaches out to roughly the same
     * distance as a typical mob's follow range, so squads in tight formation behind a
     * captain stay reliably under the aura while skirmishers wandering off lose the relief.
     */
    public static final double DEFAULT_AURA_RADIUS_BLOCKS = 16.0D;

    private CommanderAuraPolicy() {
    }

    /**
     * Returns {@code true} if any commander in {@code commanders} projects a same-faction
     * aura to a squad at ({@code squadX}, {@code squadY}, {@code squadZ}) under the default
     * radius.
     */
    public static boolean isAuraActive(Collection<CommanderAura> commanders,
                                       UUID squadPoliticalEntityId,
                                       double squadX,
                                       double squadY,
                                       double squadZ) {
        return isAuraActive(commanders, squadPoliticalEntityId,
                squadX, squadY, squadZ, DEFAULT_AURA_RADIUS_BLOCKS);
    }

    /** Variant that takes an explicit radius so tests can exercise edge cases without globals. */
    public static boolean isAuraActive(Collection<CommanderAura> commanders,
                                       UUID squadPoliticalEntityId,
                                       double squadX,
                                       double squadY,
                                       double squadZ,
                                       double radiusBlocks) {
        if (commanders == null || commanders.isEmpty() || squadPoliticalEntityId == null) {
            return false;
        }
        double radius = Math.max(0.0D, radiusBlocks);
        double radiusSq = radius * radius;
        for (CommanderAura aura : commanders) {
            if (aura == null) continue;
            if (!squadPoliticalEntityId.equals(aura.politicalEntityId())) {
                continue;
            }
            double dx = aura.x() - squadX;
            double dy = aura.y() - squadY;
            double dz = aura.z() - squadZ;
            if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code commander} projects an aura to a squad at the given
     * coordinates under {@code radiusBlocks}. Convenience for a single-commander scenario;
     * the multi-commander variant is the production path.
     */
    public static boolean isAuraActive(CommanderAura commander,
                                       UUID squadPoliticalEntityId,
                                       double squadX,
                                       double squadY,
                                       double squadZ,
                                       double radiusBlocks) {
        if (commander == null || squadPoliticalEntityId == null) {
            return false;
        }
        if (!squadPoliticalEntityId.equals(commander.politicalEntityId())) {
            return false;
        }
        double radius = Math.max(0.0D, radiusBlocks);
        double radiusSq = radius * radius;
        double dx = commander.x() - squadX;
        double dy = commander.y() - squadY;
        double dz = commander.z() - squadZ;
        return dx * dx + dy * dy + dz * dz <= radiusSq;
    }
}
