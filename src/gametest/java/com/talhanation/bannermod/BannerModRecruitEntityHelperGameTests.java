package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.RecruitEntity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModRecruitEntityHelperGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitEquipmentSlotsAndInventorySurviveSaveLoad(GameTestHelper helper) {
        RecruitEntity source = BannerModGameTestSupport.spawnOwnedRecruit(
                helper,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL),
                net.minecraft.core.BlockPos.ZERO
        );

        source.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        source.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        source.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        source.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        source.getInventory().setItem(6, new ItemStack(Items.BREAD, 3));

        helper.assertTrue(source.getEquipmentSlotForItem(new ItemStack(Items.SHIELD)) == EquipmentSlot.OFFHAND,
                "Expected recruit helper to resolve shield pickup into the offhand slot through current item APIs");
        helper.assertTrue(source.canEquipItemToSlot(new ItemStack(Items.DIAMOND_HELMET), EquipmentSlot.HEAD),
                "Expected recruit helper to use current mob replacement rules for stronger helmet pickup");

        CompoundTag saved = new CompoundTag();
        source.addAdditionalSaveData(saved);

        RecruitEntity loaded = BannerModGameTestSupport.spawnOwnedRecruit(
                helper,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL),
                net.minecraft.core.BlockPos.ZERO.east(2)
        );
        loaded.readAdditionalSaveData(saved);

        helper.assertTrue(loaded.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET),
                "Expected saved recruit helmet to load back into the in-world head equipment slot");
        helper.assertTrue(loaded.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE),
                "Expected saved recruit chest armor to load back into the in-world chest equipment slot");
        helper.assertTrue(loaded.getMainHandItem().is(Items.IRON_SWORD),
                "Expected saved recruit weapon to load back into the in-world main hand slot");
        helper.assertTrue(loaded.getOffhandItem().is(Items.SHIELD),
                "Expected saved recruit shield to load back into the in-world offhand slot");
        helper.assertTrue(loaded.getInventory().getItem(loaded.getInventorySlotIndex(EquipmentSlot.HEAD)).is(Items.IRON_HELMET),
                "Expected recruit inventory mirror to preserve the head equipment slot after load");
        helper.assertTrue(loaded.getInventory().getItem(loaded.getInventorySlotIndex(EquipmentSlot.MAINHAND)).is(Items.IRON_SWORD),
                "Expected recruit inventory mirror to preserve the mainhand equipment slot after load");
        helper.assertTrue(loaded.getInventory().getItem(6).is(Items.BREAD) && loaded.getInventory().getItem(6).getCount() == 3,
                "Expected non-equipment recruit inventory contents to survive save/load unchanged");

        helper.succeed();
    }
}
