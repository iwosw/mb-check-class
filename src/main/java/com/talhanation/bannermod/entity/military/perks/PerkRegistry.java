package com.talhanation.bannermod.entity.military.perks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory perk catalog. Phase 1 (SKILLTREE-002) seeds a tiny placeholder set
 * so persistence + skill-point grant can be exercised end-to-end; SKILLTREE-003
 * authors the per-archetype catalogs and SKILLTREE-004 adds the player-facing
 * general-stat tree on top of the same lookup API.
 *
 * <p>Server-authoritative: every {@link PerkNode} lives in the JVM, never on
 * the wire. Identifiers are deliberately stable strings so save data written
 * before SKILLTREE-003 stays valid once richer trees are registered.</p>
 */
public final class PerkRegistry {
    private static final Map<String, PerkNode> BY_ID = new HashMap<>();
    private static final Map<PerkArchetype, List<PerkNode>> BY_ARCHETYPE = new EnumMap<>(PerkArchetype.class);

    static {
        for (PerkArchetype archetype : PerkArchetype.values()) {
            BY_ARCHETYPE.put(archetype, new ArrayList<>());
        }
        seedPlaceholderCatalog();
    }

    private PerkRegistry() {
    }

    /**
     * Registers a perk; later phases call this from their catalog initializers.
     * Idempotent on identical re-registration to keep gametest reload safe;
     * conflicting redefinitions throw so author errors surface immediately.
     */
    public static synchronized void register(PerkNode node) {
        PerkNode existing = BY_ID.get(node.id());
        if (existing != null) {
            if (existing.equals(node)) return;
            throw new IllegalStateException("Perk id already registered with different payload: " + node.id());
        }
        BY_ID.put(node.id(), node);
        BY_ARCHETYPE.get(node.archetype()).add(node);
    }

    public static Optional<PerkNode> get(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static List<PerkNode> byArchetype(PerkArchetype archetype) {
        return Collections.unmodifiableList(BY_ARCHETYPE.get(archetype));
    }

    /**
     * Returns true when the id was registered. Persistence uses this on load
     * to silently drop perks removed by a later mod update.
     */
    public static boolean isKnown(String id) {
        return BY_ID.containsKey(id);
    }

    /**
     * Seeds one placeholder perk per archetype + one universal perk so the
     * registry is non-empty and downstream tests have observable ids to query.
     * Catalog authoring proper happens in SKILLTREE-003/004.
     */
    private static void seedPlaceholderCatalog() {
        registerInternal(PerkNode.leaf("universal/toughness_i", PerkArchetype.UNIVERSAL, 1,
                new PerkBonus(PerkStat.MAX_HEALTH, 2.0D)));
        registerInternal(PerkNode.leaf("swordsman/iron_grip_i", PerkArchetype.SWORDSMAN, 1,
                new PerkBonus(PerkStat.ATTACK_DAMAGE, 0.5D)));
        registerInternal(PerkNode.leaf("bowman/steady_aim_i", PerkArchetype.BOWMAN, 1,
                new PerkBonus(PerkStat.RANGED_ACCURACY, 0.05D)));
        registerInternal(PerkNode.leaf("crossbowman/heavy_bolts_i", PerkArchetype.CROSSBOWMAN, 1,
                new PerkBonus(PerkStat.RANGED_VELOCITY, 0.1D)));
        registerInternal(PerkNode.leaf("pikeman/braced_stance_i", PerkArchetype.PIKEMAN, 1,
                new PerkBonus(PerkStat.KNOCKBACK_RESIST, 0.1D)));
        registerInternal(PerkNode.leaf("cavalry/swift_charge_i", PerkArchetype.CAVALRY, 1,
                new PerkBonus(PerkStat.MOVEMENT_SPEED, 0.01D)));
    }

    private static void registerInternal(PerkNode node) {
        BY_ID.put(node.id(), node);
        BY_ARCHETYPE.get(node.archetype()).add(node);
    }
}
