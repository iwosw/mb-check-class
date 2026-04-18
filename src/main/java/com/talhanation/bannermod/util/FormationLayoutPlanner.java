package com.talhanation.bannermod.util;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.CaptainEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

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
        }
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
