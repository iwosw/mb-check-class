package com.talhanation.bannermod.util;

import com.talhanation.bannermod.ai.military.FormationSlotRegistry;
import com.talhanation.bannermod.ai.military.FormationTargetSelectionController;
import com.talhanation.bannermod.combat.CombatRole;
import com.talhanation.bannermod.combat.FormationPlanner;
import com.talhanation.bannermod.combat.RecruitRoleResolver;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.CaptainEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class FormationLayoutPlanner {
    private FormationLayoutPlanner() {
    }

    static double scaleSpacingForShipCaptains(List<AbstractRecruitEntity> recruits, double spacing) {
        for (AbstractRecruitEntity recruit : recruits) {
            if (recruit instanceof CaptainEntity captain
                    && captain.smallShipsController.ship != null
                    && captain.smallShipsController.ship.isCaptainDriver()) {
                return spacing * 10;
            }
        }
        return spacing;
    }

    static void assignAndApplySlots(List<AbstractRecruitEntity> recruits, List<Vec3> slotPositions, Float ownerRot, boolean preserveHorizontalPosition) {
        List<FormationSlot> slots = new ArrayList<>(slotPositions.size());
        for (Vec3 slotPosition : slotPositions) {
            slots.add(new FormationSlot(slotPosition));
        }

        for (AbstractRecruitEntity recruit : recruits) {
            FormationSlot slot = claimSlot(recruit, slots);
            if (slot == null) {
                continue;
            }

            BlockPos blockPos = FormationUtils.getPositionOrSurface(
                    recruit.getCommandSenderWorld(),
                    new BlockPos((int) slot.position.x, (int) slot.position.y, (int) slot.position.z)
            );

            Vec3 holdPos = preserveHorizontalPosition
                    ? new Vec3(slot.position.x, blockPos.getY(), slot.position.z)
                    : new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            recruit.setHoldPos(holdPos);
            if (ownerRot != null) {
                recruit.ownerRot = ownerRot;
            }
            recruit.setFollowState(3);
            recruit.isInFormation = true;
            registerSlotOwnership(recruit, recruit.formationPos, holdPos);
        }
    }

    static void applySequentialSlots(List<AbstractRecruitEntity> recruits, List<Vec3> slotPositions, float ownerRot, boolean preserveHorizontalPosition) {
        for (int i = 0; i < recruits.size() && i < slotPositions.size(); i++) {
            AbstractRecruitEntity recruit = recruits.get(i);
            Vec3 slotPosition = slotPositions.get(i);

            BlockPos blockPos = FormationUtils.getPositionOrSurface(
                    recruit.getCommandSenderWorld(),
                    new BlockPos((int) slotPosition.x, (int) slotPosition.y, (int) slotPosition.z)
            );

            Vec3 holdPos = preserveHorizontalPosition
                    ? new Vec3(slotPosition.x, blockPos.getY(), slotPosition.z)
                    : new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            recruit.setHoldPos(holdPos);
            recruit.ownerRot = ownerRot;
            recruit.setFollowState(3);
            recruit.isInFormation = true;
            recruit.formationPos = i;
            registerSlotOwnership(recruit, i, holdPos);
        }
    }

    static void applyRoleAwareLineSlots(List<AbstractRecruitEntity> recruits, Vec3 targetPos, float ownerRot, double spacing) {
        Map<com.talhanation.bannermod.combat.FormationSlot, List<AbstractRecruitEntity>> bySlot = new EnumMap<>(com.talhanation.bannermod.combat.FormationSlot.class);
        for (AbstractRecruitEntity recruit : recruits) {
            CombatRole role = RecruitRoleResolver.roleOf(recruit);
            bySlot.computeIfAbsent(FormationPlanner.slotFor(role), ignored -> new ArrayList<>()).add(recruit);
        }

        Vec3 forward = FormationPatternBuilder.forwardFromYaw(ownerRot).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();
        int slotIndex = 0;
        slotIndex = applyRoleRow(bySlot.get(com.talhanation.bannermod.combat.FormationSlot.FRONT_RANK), targetPos, right, forward, 0.0D, spacing, ownerRot, slotIndex);
        slotIndex = applyRoleRow(bySlot.get(com.talhanation.bannermod.combat.FormationSlot.SUPPORT_RANK), targetPos, right, forward, -spacing, spacing, ownerRot, slotIndex);
        slotIndex = applyRoleRow(bySlot.get(com.talhanation.bannermod.combat.FormationSlot.REAR_RANK), targetPos, right, forward, -spacing * 2.0D, spacing * 1.5D, ownerRot, slotIndex);
        List<AbstractRecruitEntity> flank = bySlot.get(com.talhanation.bannermod.combat.FormationSlot.FLANK);
        if (flank != null) {
            double wingOffset = Math.max(spacing * 2.0D, (recruits.size() / 2.0D + 1.0D) * spacing);
            for (int i = 0; i < flank.size(); i++) {
                double side = i % 2 == 0 ? -1.0D : 1.0D;
                double depth = -spacing * (i / 2);
                Vec3 pos = targetPos.add(right.scale(side * wingOffset)).add(forward.scale(depth));
                applySpecificSlot(flank.get(i), pos, ownerRot, slotIndex++);
            }
        }
    }

    private static int applyRoleRow(List<AbstractRecruitEntity> recruits, Vec3 targetPos, Vec3 right, Vec3 forward, double depth, double spacing, float ownerRot, int slotIndex) {
        if (recruits == null || recruits.isEmpty()) {
            return slotIndex;
        }
        double center = (recruits.size() - 1) / 2.0D;
        for (int i = 0; i < recruits.size(); i++) {
            Vec3 pos = targetPos.add(right.scale((i - center) * spacing)).add(forward.scale(depth));
            applySpecificSlot(recruits.get(i), pos, ownerRot, slotIndex++);
        }
        return slotIndex;
    }

    private static void applySpecificSlot(AbstractRecruitEntity recruit, Vec3 slotPosition, float ownerRot, int slotIndex) {
        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                recruit.getCommandSenderWorld(),
                new BlockPos((int) slotPosition.x, (int) slotPosition.y, (int) slotPosition.z)
        );
        Vec3 holdPos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        recruit.setHoldPos(holdPos);
        recruit.ownerRot = ownerRot;
        recruit.setFollowState(3);
        recruit.isInFormation = true;
        recruit.formationPos = slotIndex;
        registerSlotOwnership(recruit, slotIndex, holdPos);
    }

    private static void registerSlotOwnership(AbstractRecruitEntity recruit, int slotIndex, Vec3 holdPos) {
        UUID ownerId = recruit.getOwnerUUID();
        UUID groupId = recruit.getGroup();
        if (ownerId == null || groupId == null || slotIndex < 0) {
            return;
        }
        FormationSlotRegistry.assign(
                new FormationTargetSelectionController.CohortKey(ownerId, groupId),
                slotIndex,
                recruit.getUUID(),
                holdPos,
                recruit.ownerRot
        );
    }

    private static FormationSlot claimSlot(AbstractRecruitEntity recruit, List<FormationSlot> slots) {
        if (recruit.formationPos >= 0 && recruit.formationPos < slots.size()) {
            FormationSlot preferredSlot = slots.get(recruit.formationPos);
            if (preferredSlot.isFree) {
                preferredSlot.isFree = false;
                return preferredSlot;
            }
        }

        for (int i = 0; i < slots.size(); i++) {
            FormationSlot slot = slots.get(i);
            if (!slot.isFree) {
                continue;
            }
            recruit.formationPos = i;
            slot.isFree = false;
            return slot;
        }

        return null;
    }

    private static final class FormationSlot {
        private final Vec3 position;
        private boolean isFree = true;

        private FormationSlot(Vec3 position) {
            this.position = position;
        }
    }
}
