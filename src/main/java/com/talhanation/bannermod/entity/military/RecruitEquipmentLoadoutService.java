package com.talhanation.bannermod.entity.military;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

final class RecruitEquipmentLoadoutService {
    private RecruitEquipmentLoadoutService() {
    }

    static void applyRandomEquipmentSet(AbstractRecruitEntity recruit) {
        List<List<String>> equipmentSets = recruit.getEquipment();
        if (equipmentSets == null || equipmentSets.isEmpty()) {
            return;
        }

        int selectedIndex = recruit.getRandom().nextInt(0, equipmentSets.size());
        List<String> equipmentSet = equipmentSets.get(selectedIndex);
        while (equipmentSet.size() < 6) {
            equipmentSet.add("");
        }

        equipSlot(recruit, EquipmentSlot.HEAD, equipmentSet.get(5));
        equipSlot(recruit, EquipmentSlot.CHEST, equipmentSet.get(4));
        equipSlot(recruit, EquipmentSlot.LEGS, equipmentSet.get(3));
        equipSlot(recruit, EquipmentSlot.FEET, equipmentSet.get(2));
        equipSlot(recruit, EquipmentSlot.MAINHAND, equipmentSet.get(0));
        equipSlot(recruit, EquipmentSlot.OFFHAND, equipmentSet.get(1));
    }

    private static void equipSlot(AbstractRecruitEntity recruit, EquipmentSlot slot, String itemId) {
        Optional<Holder<Item>> itemHolder = ForgeRegistries.ITEMS.getHolder(ResourceLocation.tryParse(itemId));
        itemHolder.ifPresent(holder -> recruit.setItemSlot(slot, holder.value().getDefaultInstance()));
    }
}
