package com.talhanation.bannermod.entity.military.perks;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable description of a single perk in the tree.
 *
 * <p>Identifiers are namespaced strings ({@code "bannermod:swordsman/iron_grip"})
 * so they double as localization keys via {@link #localizationKey()}; phase 1
 * defines a small placeholder set so SKILLTREE-003/004 can extend the catalog
 * without redesigning the model.</p>
 */
public record PerkNode(String id,
                       PerkArchetype archetype,
                       int pointCost,
                       List<String> prerequisites,
                       List<PerkBonus> bonuses) {
    public PerkNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(archetype, "archetype");
        if (pointCost < 0) throw new IllegalArgumentException("pointCost must be >= 0");
        prerequisites = List.copyOf(Objects.requireNonNullElse(prerequisites, List.of()));
        bonuses = List.copyOf(Objects.requireNonNullElse(bonuses, List.of()));
    }

    /**
     * Convenience factory for catalog code: no prerequisites, single bonus.
     */
    public static PerkNode leaf(String id, PerkArchetype archetype, int cost, PerkBonus bonus) {
        return new PerkNode(id, archetype, cost, List.of(), List.of(bonus));
    }

    /**
     * Returns true when {@code owned} satisfies every prerequisite in this node.
     */
    public boolean prerequisitesMet(Set<String> owned) {
        if (prerequisites.isEmpty()) return true;
        for (String pre : prerequisites) {
            if (!owned.contains(pre)) return false;
        }
        return true;
    }

    /**
     * Localization key under {@code assets/bannermod/lang/*.json}. Identifiers
     * containing {@code ':'} or {@code '/'} are normalized to {@code '.'} so the
     * resulting key is a flat dotted path.
     */
    public String localizationKey() {
        return "perk.bannermod." + id.replace(':', '.').replace('/', '.');
    }
}
