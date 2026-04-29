package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.RecruitEntity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
        helper.assertTrue(source.getEquipmentSlotForItem(new ItemStack(Items.IRON_HELMET)) == EquipmentSlot.HEAD,
                "Expected recruit helper to resolve helmet pickup into the head slot through current item APIs");

        CompoundTag saved = new CompoundTag();
        source.addAdditionalSaveData(saved);
        helper.assertTrue(saved.contains("RecruitItems"),
                "Expected recruit save data to use the dedicated RecruitItems inventory tag");

        ListTag recruitItems = saved.getList("RecruitItems", 10);
        helper.assertTrue(recruitItems.size() == 5,
                "Expected recruit save data to preserve four equipment slots plus one inventory slot");
        helper.assertTrue(ItemStack.parseOptional(source.registryAccess(), recruitItems.getCompound(0)).is(Items.IRON_HELMET),
                "Expected recruit helper to serialize the head equipment mirror as a current item stack");
        helper.assertTrue(ItemStack.parseOptional(source.registryAccess(), recruitItems.getCompound(3)).is(Items.IRON_SWORD),
                "Expected recruit helper to serialize the mainhand equipment mirror as a current item stack");
        ItemStack bread = ItemStack.parseOptional(source.registryAccess(), recruitItems.getCompound(4));
        helper.assertTrue(bread.is(Items.BREAD) && bread.getCount() == 3,
                "Expected recruit helper to serialize non-equipment inventory contents as a current item stack");

        helper.succeed();
    }
}
