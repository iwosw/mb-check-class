package com.talhanation.bannermod.entity.military;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jetbrains.annotations.NotNull;

final class RecruitEquipmentService {
    private RecruitEquipmentService() {
    }

    static void hurtArmor(AbstractRecruitEntity recruit, @NotNull DamageSource damageSource, float damage) {
        if (recruit.level().isClientSide()) return;
        hurtArmorSlot(recruit, damageSource, EquipmentSlot.HEAD, 0, SoundEvents.ITEM_BREAK);
        hurtArmorSlot(recruit, damageSource, EquipmentSlot.CHEST, 1, SoundEvents.ITEM_BREAK);
        hurtArmorSlot(recruit, damageSource, EquipmentSlot.LEGS, 2, SoundEvents.ITEM_BREAK);
        hurtArmorSlot(recruit, damageSource, EquipmentSlot.FEET, 3, SoundEvents.ITEM_BREAK);
    }

    static void damageMainHandItem(AbstractRecruitEntity recruit) {
        if (recruit.level().isClientSide()) return;
        ItemStack handItem = recruit.getItemBySlot(EquipmentSlot.MAINHAND);
        boolean hasHandItem = !handItem.isEmpty();
        recruit.getMainHandItem().hurtAndBreak(1, recruit, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        if (recruit.getMainHandItem().isEmpty() && hasHandItem) {
            recruit.inventory.setItem(5, ItemStack.EMPTY);
            recruit.getInventory().setChanged();
            recruit.playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.8F + recruit.getCommandSenderWorld().random.nextFloat() * 0.4F);
            tryToReequip(recruit, EquipmentSlot.MAINHAND);
        }
    }

    static void tryToReequip(AbstractRecruitEntity recruit, EquipmentSlot equipmentSlot) {
        for (int i = 6; i < 15; i++) {
            ItemStack itemStack = recruit.getInventory().getItem(i);
            if (recruit.canEquipItemToSlot(itemStack, equipmentSlot)) {
                recruit.setItemSlot(equipmentSlot, itemStack);
                recruit.inventory.setItem(recruit.getInventorySlotIndex(equipmentSlot), itemStack);
                recruit.inventory.removeItemNoUpdate(i);
                Equipable equipable = Equipable.get(itemStack);
                if (equipable != null) {
                    recruit.getCommandSenderWorld().playSound(null, recruit.getX(), recruit.getY(), recruit.getZ(), equipable.getEquipSound(), recruit.getSoundSource(), 1.0F, 1.0F);
                }
            }
        }
    }

    static void tryToReequipShield(AbstractRecruitEntity recruit) {
        for (ItemStack itemStack : recruit.getInventory().items) {
            if (itemStack.getItem() instanceof ShieldItem) {
                recruit.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
                recruit.inventory.setItem(recruit.getInventorySlotIndex(EquipmentSlot.OFFHAND), itemStack);
                Equipable equipable = Equipable.get(itemStack);
                if (equipable != null) {
                    recruit.getCommandSenderWorld().playSound(null, recruit.getX(), recruit.getY(), recruit.getZ(), equipable.getEquipSound(), recruit.getSoundSource(), 1.0F, 1.0F);
                }
                itemStack.shrink(1);
            }
        }
    }

    static void hurtCurrentlyUsedShield(AbstractRecruitEntity recruit, float damage) {
        if (recruit.level().isClientSide()) return;
        recruit.getOffhandItem().hurtAndBreak(1, recruit, entity -> entity.broadcastBreakEvent(EquipmentSlot.OFFHAND));
        if (recruit.getOffhandItem().isEmpty()) {
            recruit.inventory.setItem(4, ItemStack.EMPTY);
            recruit.getInventory().setChanged();
            recruit.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + recruit.getCommandSenderWorld().random.nextFloat() * 0.4F);
            tryToReequipShield(recruit);
        }
    }

    private static void hurtArmorSlot(AbstractRecruitEntity recruit, DamageSource damageSource, EquipmentSlot slot, int inventorySlot, net.minecraft.sounds.SoundEvent breakSound) {
        ItemStack armor = recruit.getItemBySlot(slot);
        boolean hadArmor = !armor.isEmpty();
        if ((!(damageSource.is(DamageTypes.IN_FIRE) && damageSource.is(DamageTypes.ON_FIRE)) || !armor.getItem().isFireResistant()) && armor.getItem() instanceof ArmorItem) {
            armor.hurtAndBreak(1, recruit, entity -> entity.broadcastBreakEvent(slot));
        }
        if (recruit.getItemBySlot(slot).isEmpty() && hadArmor) {
            recruit.inventory.setItem(inventorySlot, ItemStack.EMPTY);
            recruit.getInventory().setChanged();
            recruit.playSound(breakSound, 0.8F, 0.8F + recruit.getCommandSenderWorld().random.nextFloat() * 0.4F);
            tryToReequip(recruit, slot);
        }
    }
}
