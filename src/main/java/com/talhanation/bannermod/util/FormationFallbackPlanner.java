package com.talhanation.bannermod.util;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class FormationFallbackPlanner {
    private FormationFallbackPlanner() {
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
