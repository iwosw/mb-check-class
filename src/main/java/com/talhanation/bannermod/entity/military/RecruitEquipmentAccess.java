package com.talhanation.bannermod.entity.military;

import net.minecraft.world.entity.EquipmentSlot;

interface RecruitEquipmentAccess {
    private AbstractRecruitEntity recruit() {
        return (AbstractRecruitEntity) this;
    }

    default int getBlockCoolDown() {
        return 200;
    }

    default void setEquipment() {
        RecruitEquipmentLoadoutService.applyRandomEquipmentSet(recruit());
    }

    default void damageMainHandItem() {
        RecruitEquipmentService.damageMainHandItem(recruit());
    }

    default void tryToReequip(EquipmentSlot equipmentSlot) {
        RecruitEquipmentService.tryToReequip(recruit(), equipmentSlot);
    }

    default void tryToReequipShield() {
        RecruitEquipmentService.tryToReequipShield(recruit());
    }

    default void disableShield() {
        recruit().blockCoolDown = this.getBlockCoolDown();
        recruit().stopUsingItem();
        recruit().getCommandSenderWorld().broadcastEntityEvent(recruit(), (byte) 30);
    }

    default boolean canBlock() {
        return recruit().blockCoolDown == 0;
    }

    default void updateShield() {
        if (recruit().blockCoolDown > 0) {
            recruit().blockCoolDown--;
        }
    }
}
