package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.BannerModGameTestSupport;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import com.talhanation.bannermod.shared.logistics.BannerModCourierTask;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsAuthoringState;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;
import java.util.List;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModWorkerTransportInspectionGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 100)
    public static void mountedCourierInspectionReportsMountedTransport(GameTestHelper helper) {
        BannerModLogisticsRuntime.resetForTests();
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        MerchantEntity courier = BannerModGameTestSupport.spawnOwnedMerchant(helper, owner, new BlockPos(2, 2, 4));
        Horse horse = BannerModGameTestSupport.spawnEntity(helper, EntityType.HORSE, new BlockPos(3, 2, 4));
        BannerModCourierTask task = assignSupportedCourierRoute(helper, level, owner, courier);
        courier.getInventory().addItem(new ItemStack(Items.OAK_PLANKS));
        courier.setActiveCourierTask(task);

        helper.succeedWhen(() -> {
            helper.assertTrue(courier.getVehicle() == horse, "Expected courier to mount the approved nearby horse.");
            assertTranslationKey(helper, courier.transportService().inspectionMessage(), "chat.bannermod.workers.transport.mounted");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 100)
    public static void unmountedCourierInspectionReportsFootFallback(GameTestHelper helper) {
        BannerModLogisticsRuntime.resetForTests();
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        MerchantEntity courier = BannerModGameTestSupport.spawnOwnedMerchant(helper, owner, new BlockPos(2, 2, 4));
        BannerModCourierTask task = assignSupportedCourierRoute(helper, level, owner, courier);
        courier.getInventory().addItem(new ItemStack(Items.OAK_PLANKS));
        courier.setActiveCourierTask(task);

        helper.runAfterDelay(20, () -> {
            helper.assertFalse(courier.isPassenger(), "Expected courier to remain on foot without an approved nearby mount.");
            assertTranslationKey(helper, courier.transportService().inspectionMessage(), "chat.bannermod.workers.transport.fallback_no_mount");
            helper.succeed();
        });
    }

    private static BannerModCourierTask assignSupportedCourierRoute(GameTestHelper helper, ServerLevel level, Player owner, MerchantEntity courier) {
        StorageArea sourceStorage = BannerModGameTestSupport.spawnOwnedStorageArea(helper, owner, new BlockPos(2, 2, 2));
        StorageArea destinationStorage = BannerModGameTestSupport.spawnOwnedStorageArea(helper, owner, new BlockPos(24, 2, 2));
        int merchantMask = sourceStorage.getStorageMask(EnumSet.of(StorageArea.StorageType.MERCHANTS));
        sourceStorage.setStorageTypes(merchantMask);
        destinationStorage.setStorageTypes(merchantMask);
        sourceStorage.setLogisticsRoute(BannerModLogisticsAuthoringState.parse(destinationStorage.getUUID().toString(), "minecraft:oak_planks", "1", "NORMAL"));
        placeScannableChest(level, sourceStorage);
        placeScannableChest(level, destinationStorage);
        return BannerModLogisticsRuntime.service()
                .claimNextTask(courier.getUUID(), List.of(sourceStorage.getAuthoredLogisticsRoute().orElseThrow()), route -> true, level.getGameTime(), 200L)
                .orElseThrow();
    }

    private static BlockPos placeScannableChest(ServerLevel level, StorageArea storageArea) {
        AABB area = storageArea.getArea().inflate(-0.25D);
        BlockPos chestPos = new BlockPos(
                (int) Math.floor(area.minX),
                storageArea.getOnPos().getY(),
                (int) Math.floor(area.minZ)
        );
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        storageArea.scanStorageBlocks();
        ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
        if (chest != null) {
            chest.setItem(0, ItemStack.EMPTY);
        }
        return chestPos;
    }

    private static void assertTranslationKey(GameTestHelper helper, Component component, String expectedKey) {
        helper.assertTrue(component.getContents() instanceof TranslatableContents, "Expected a translatable inspection message.");
        TranslatableContents contents = (TranslatableContents) component.getContents();
        helper.assertTrue(expectedKey.equals(contents.getKey()), "Expected inspection key " + expectedKey + ", got " + contents.getKey());
    }
}
