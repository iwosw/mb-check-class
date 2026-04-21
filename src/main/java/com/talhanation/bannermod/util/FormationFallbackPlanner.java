package com.talhanation.bannermod.util;

import com.talhanation.bannermod.ai.military.CombatStance;
import com.talhanation.bannermod.ai.military.FormationGapFillPolicy;
import com.talhanation.bannermod.ai.military.FormationSlotRegistry;
import com.talhanation.bannermod.ai.military.FormationTargetSelectionController;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class FormationFallbackPlanner {
    private FormationFallbackPlanner() {
    }

    /**
     * Step 1.C: attempt to migrate the recruit into a forward (or adjacent) empty slot
     * whose previous owner has died/been removed. Only runs under LINE_HOLD / SHIELD_WALL.
     *
     * @return true if a migration was applied.
     */
    public static boolean tryFillForwardGap(AbstractRecruitEntity recruit) {
        if (recruit == null || !recruit.isInFormation || recruit.getFollowState() != 3) {
            return false;
        }
        if (recruit.getHoldPos() == null || recruit.formationPos < 0) {
            return false;
        }
        CombatStance stance = recruit.getCombatStance();
        if (!FormationGapFillPolicy.stanceAllowsGapFill(stance)) {
            return false;
        }

        UUID ownerId = recruit.getOwnerUUID();
        UUID groupId = recruit.getGroup();
        if (ownerId == null || groupId == null) {
            return false;
        }
        FormationTargetSelectionController.CohortKey cohortKey = new FormationTargetSelectionController.CohortKey(ownerId, groupId);
        Map<Integer, FormationSlotRegistry.SlotEntry> slotEntries = FormationSlotRegistry.slotsOf(cohortKey);
        if (slotEntries.size() < 2) {
            return false;
        }
        FormationSlotRegistry.SlotEntry selfEntry = slotEntries.get(recruit.formationPos);
        if (selfEntry == null) {
            return false;
        }

        List<AbstractRecruitEntity> cohort = getFormationCohort(recruit);
        Set<UUID> livingOwnerIds = new HashSet<>();
        for (AbstractRecruitEntity member : cohort) {
            if (member.isAlive() && !member.isRemoved()) {
                livingOwnerIds.add(member.getUUID());
            }
        }

        float ownerRotDeg = recruit.ownerRot;
        Vec3 selfHold = recruit.getHoldPos();

        // Build local-frame slots from the registry. A slot is empty when its recorded owner
        // UUID is not present in the living cohort.
        List<FormationGapFillPolicy.LocalSlot> localSlots = new ArrayList<>();
        FormationGapFillPolicy.LocalSlot selfLocal = null;
        for (Map.Entry<Integer, FormationSlotRegistry.SlotEntry> entry : slotEntries.entrySet()) {
            int idx = entry.getKey();
            Vec3 slotPos = entry.getValue().holdPos();
            double dx = slotPos.x - selfHold.x;
            double dz = slotPos.z - selfHold.z;
            double[] local = FormationGapFillPolicy.worldDeltaToLocal(dx, dz, ownerRotDeg);
            boolean occupied = idx == recruit.formationPos
                    || livingOwnerIds.contains(entry.getValue().ownerId());
            FormationGapFillPolicy.LocalSlot ls = new FormationGapFillPolicy.LocalSlot(idx, local[0], local[1], occupied);
            localSlots.add(ls);
            if (idx == recruit.formationPos) {
                selfLocal = ls;
            }
        }
        if (selfLocal == null) {
            return false;
        }

        Optional<Integer> chosen = FormationGapFillPolicy.chooseGapSlot(selfLocal, localSlots);
        if (chosen.isEmpty()) {
            return false;
        }

        int targetSlotIndex = chosen.get();
        FormationSlotRegistry.SlotEntry targetEntry = slotEntries.get(targetSlotIndex);
        if (targetEntry == null) {
            return false;
        }
        Vec3 targetHoldPos = targetEntry.holdPos();

        // Migrate: claim the gap slot. No swap because the previous owner is gone.
        int oldSlot = recruit.formationPos;
        recruit.formationPos = targetSlotIndex;
        recruit.setHoldPos(targetHoldPos);
        recruit.setFollowState(3);
        recruit.isInFormation = true;

        // Keep the registry in sync: the recruit now owns the destination slot; the source
        // slot is left empty (its old owner is the recruit itself, who has moved).
        FormationSlotRegistry.assign(cohortKey, targetSlotIndex, recruit.getUUID(), targetHoldPos, ownerRotDeg);
        FormationSlotRegistry.remove(cohortKey, oldSlot);
        return true;
    }

    public static boolean tryFallbackToNearestFreeSlot(AbstractRecruitEntity blockedRecruit) {
        if (!blockedRecruit.isInFormation || blockedRecruit.getFollowState() != 3 || blockedRecruit.getHoldPos() == null || blockedRecruit.formationPos < 0) {
            return false;
        }

        List<AbstractRecruitEntity> cohort = getFormationCohort(blockedRecruit);
        if (cohort.size() < 2) {
            return false;
        }

        List<FormationFallbackSlot> slots = new ArrayList<>();
        for (AbstractRecruitEntity recruit : cohort) {
            if (recruit.getHoldPos() == null || recruit.formationPos < 0) {
                continue;
            }
            slots.add(new FormationFallbackSlot(
                    recruit.formationPos,
                    recruit.getHoldPos(),
                    isFormationSlotOccupied(recruit.getHoldPos(), cohort, blockedRecruit)
            ));
        }

        Optional<FormationFallbackDecision> decision = chooseNearestFreeFormationSlot(
                blockedRecruit.position(),
                blockedRecruit.formationPos,
                slots
        );
        if (decision.isEmpty()) {
            return false;
        }

        AbstractRecruitEntity targetOwner = findRecruitBySlot(cohort, decision.get().toSlotIndex());
        if (targetOwner == null || targetOwner == blockedRecruit || targetOwner.getHoldPos() == null) {
            return false;
        }

        Vec3 originalHoldPos = blockedRecruit.getHoldPos();
        int originalSlot = blockedRecruit.formationPos;

        blockedRecruit.setHoldPos(targetOwner.getHoldPos());
        blockedRecruit.formationPos = targetOwner.formationPos;
        blockedRecruit.setFollowState(3);
        blockedRecruit.isInFormation = true;

        targetOwner.setHoldPos(originalHoldPos);
        targetOwner.formationPos = originalSlot;
        targetOwner.setFollowState(3);
        targetOwner.isInFormation = true;
        return true;
    }

    private static Optional<FormationFallbackDecision> chooseNearestFreeFormationSlot(
            Vec3 recruitPosition,
            int currentSlotIndex,
            List<FormationFallbackSlot> slots
    ) {
        return slots.stream()
                .filter(slot -> slot.slotIndex() != currentSlotIndex)
                .filter(slot -> !slot.occupied())
                .min(Comparator.comparingDouble(slot -> slot.position().distanceToSqr(recruitPosition)))
                .map(slot -> new FormationFallbackDecision(currentSlotIndex, slot.slotIndex()));
    }

    private static List<AbstractRecruitEntity> getFormationCohort(AbstractRecruitEntity blockedRecruit) {
        UUID ownerId = blockedRecruit.getOwnerUUID();
        UUID groupId = blockedRecruit.getGroup();
        return blockedRecruit.level().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                blockedRecruit.getBoundingBox().inflate(64.0D),
                candidate -> candidate.isAlive()
                        && !candidate.isRemoved()
                        && candidate.isInFormation
                        && candidate.getHoldPos() != null
                        && Objects.equals(candidate.getOwnerUUID(), ownerId)
                        && sameFormationGroup(groupId, candidate.getGroup())
        );
    }

    private static boolean sameFormationGroup(UUID groupId, UUID candidateGroupId) {
        if (groupId == null && candidateGroupId == null) {
            return true;
        }
        return Objects.equals(groupId, candidateGroupId);
    }

    private static boolean isFormationSlotOccupied(Vec3 slotPos, List<AbstractRecruitEntity> cohort, AbstractRecruitEntity blockedRecruit) {
        for (AbstractRecruitEntity recruit : cohort) {
            if (recruit == blockedRecruit) {
                continue;
            }
            if (recruit.distanceToSqr(slotPos) <= 0.75D) {
                return true;
            }
        }
        return false;
    }

    private static AbstractRecruitEntity findRecruitBySlot(List<AbstractRecruitEntity> cohort, int slotIndex) {
        for (AbstractRecruitEntity recruit : cohort) {
            if (recruit.formationPos == slotIndex) {
                return recruit;
            }
        }
        return null;
    }

    private record FormationFallbackSlot(int slotIndex, Vec3 position, boolean occupied) {
    }

    private record FormationFallbackDecision(int fromSlotIndex, int toSlotIndex) {
    }
}
