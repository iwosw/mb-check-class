package com.talhanation.bannermod.ai.military;

import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal cohort-scoped registry of formation slot ownership for Step 1.C gap-fill.
 *
 * <p>Formation layout does not otherwise track per-slot ownership explicitly. When a
 * layout is applied the planner calls {@link #assign(FormationTargetSelectionController.CohortKey,
 * int, UUID, Vec3, float)}; later, when a cohort member scans for a forward gap, it can
 * consult this registry to learn which slots exist in its cohort and which UUIDs once
 * owned them. A slot whose recorded owner is no longer living in the cohort is "empty".
 *
 * <p>This is the authoritative state for gap-fill. When a migration succeeds the registry
 * is updated so subsequent scans see the new owner.
 */
public final class FormationSlotRegistry {

    public record SlotEntry(UUID ownerId, Vec3 holdPos, float ownerRotDeg) {
    }

    private static final Map<FormationTargetSelectionController.CohortKey, Map<Integer, SlotEntry>> REGISTRY =
            new ConcurrentHashMap<>();

    private FormationSlotRegistry() {
    }

    /** Replace / set the owner of the given slot within the cohort. */
    public static void assign(FormationTargetSelectionController.CohortKey cohort, int slotIndex, UUID ownerId, Vec3 holdPos, float ownerRotDeg) {
        if (cohort == null || ownerId == null || holdPos == null || slotIndex < 0) {
            return;
        }
        REGISTRY.computeIfAbsent(cohort, k -> new ConcurrentHashMap<>())
                .put(slotIndex, new SlotEntry(ownerId, holdPos, ownerRotDeg));
    }

    /** Snapshot of the cohort's slot ownership, or an empty map if no entry. */
    public static Map<Integer, SlotEntry> slotsOf(FormationTargetSelectionController.CohortKey cohort) {
        if (cohort == null) {
            return Map.of();
        }
        Map<Integer, SlotEntry> map = REGISTRY.get(cohort);
        return map == null ? Map.of() : Map.copyOf(map);
    }

    /** Remove a specific slot from the cohort's registry. */
    public static void remove(FormationTargetSelectionController.CohortKey cohort, int slotIndex) {
        if (cohort == null) {
            return;
        }
        Map<Integer, SlotEntry> map = REGISTRY.get(cohort);
        if (map != null) {
            map.remove(slotIndex);
        }
    }

    /** Drop the entire cohort entry. */
    public static void clear(FormationTargetSelectionController.CohortKey cohort) {
        if (cohort == null) {
            return;
        }
        REGISTRY.remove(cohort);
    }

    /** Testing / dev-only: wipe everything. */
    public static void clearAll() {
        REGISTRY.clear();
    }
}
