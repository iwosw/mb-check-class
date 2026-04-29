package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsItemFilter;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsNodeRef;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsPriority;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRoute;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeExecutionRecord;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeExecutionRuntime;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeExecutionState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModSeaTradeExecutionGameTests {

    private static final UUID ROUTE_ID = UUID.fromString("00000000-0000-0000-0000-000000002301");
    private static final UUID CARRIER_ID = UUID.fromString("00000000-0000-0000-0000-000000002302");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 100)
    public static void seaTradeMovesFilteredCargoThroughCarrierContainer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        StorageArea sourceStorage = BannerModGameTestSupport.spawnOwnedStorageArea(helper, owner, new BlockPos(2, 2, 2));
        StorageArea destinationStorage = BannerModGameTestSupport.spawnOwnedStorageArea(helper, owner, new BlockPos(8, 2, 2));
        ChestBlockEntity sourceChest = chestAt(level, placeScannableChest(level, sourceStorage));
        ChestBlockEntity destinationChest = chestAt(level, placeScannableChest(level, destinationStorage));
        SimpleContainer carrierCargo = new SimpleContainer(2);
        BannerModSeaTradeExecutionRuntime runtime = newRuntime(sourceStorage, destinationStorage);
        sourceChest.setItem(0, new ItemStack(Items.WHEAT, 16));
        sourceChest.setItem(1, new ItemStack(Items.BREAD, 16));

        BannerModSeaTradeExecutionRecord travelling = runtime.loadFromSource(ROUTE_ID, sourceStorage, carrierCargo);

        helper.assertTrue(travelling.state() == BannerModSeaTradeExecutionState.TRAVELLING,
                "Expected sea route to enter travelling state after loading matching cargo.");
        helper.assertTrue(itemCount(sourceChest, Items.WHEAT) == 0,
                "Expected source storage chest to lose the filtered wheat.");
        helper.assertTrue(itemCount(sourceChest, Items.BREAD) == 16,
                "Expected source storage chest to keep non-matching bread.");
        helper.assertTrue(itemCount(carrierCargo, Items.WHEAT) == 16,
                "Expected carrier cargo container to hold wheat during travel.");
        helper.assertTrue(itemCount(destinationChest, Items.WHEAT) == 0,
                "Expected destination storage to stay empty while route is travelling.");

        runtime.update(travelling.arrivalReady());
        BannerModSeaTradeExecutionRecord complete = runtime.unloadAtDestination(ROUTE_ID, destinationStorage, carrierCargo);

        helper.assertTrue(complete.state() == BannerModSeaTradeExecutionState.COMPLETE,
                "Expected sea route to complete after depositing carrier cargo.");
        helper.assertTrue(itemCount(carrierCargo, Items.WHEAT) == 0,
                "Expected carrier cargo to be empty after unloading.");
        helper.assertTrue(itemCount(destinationChest, Items.WHEAT) == 16,
                "Expected destination storage chest to receive the wheat.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 100)
    public static void seaTradeSourceShortageLeavesPartialCargoSurfaced(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        StorageArea sourceStorage = BannerModGameTestSupport.spawnOwnedStorageArea(helper, owner, new BlockPos(2, 2, 2));
        StorageArea destinationStorage = BannerModGameTestSupport.spawnOwnedStorageArea(helper, owner, new BlockPos(8, 2, 2));
        ChestBlockEntity sourceChest = chestAt(level, placeScannableChest(level, sourceStorage));
        SimpleContainer carrierCargo = new SimpleContainer(1);
        BannerModSeaTradeExecutionRuntime runtime = newRuntime(sourceStorage, destinationStorage);
        sourceChest.setItem(0, new ItemStack(Items.WHEAT, 6));

        BannerModSeaTradeExecutionRecord failed = runtime.loadFromSource(ROUTE_ID, sourceStorage, carrierCargo);

        helper.assertTrue(failed.state() == BannerModSeaTradeExecutionState.FAILED,
                "Expected route to fail when source storage cannot satisfy the requested count.");
        helper.assertTrue(BannerModSeaTradeExecutionRecord.FAILURE_SOURCE_SHORTAGE.equals(failed.failureReason()),
                "Expected source shortage failure reason.");
        helper.assertTrue(failed.cargoCount() == 6 && itemCount(carrierCargo, Items.WHEAT) == 6,
                "Expected the partially loaded goods to remain in carrier cargo, not a parallel store.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", timeoutTicks = 100)
    public static void seaTradeDestinationFullKeepsCargoSurfaced(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        StorageArea sourceStorage = BannerModGameTestSupport.spawnOwnedStorageArea(helper, owner, new BlockPos(2, 2, 2));
        StorageArea destinationStorage = BannerModGameTestSupport.spawnOwnedStorageArea(helper, owner, new BlockPos(8, 2, 2));
        ChestBlockEntity sourceChest = chestAt(level, placeScannableChest(level, sourceStorage));
        ChestBlockEntity destinationChest = chestAt(level, placeScannableChest(level, destinationStorage));
        SimpleContainer carrierCargo = new SimpleContainer(1);
        BannerModSeaTradeExecutionRuntime runtime = newRuntime(sourceStorage, destinationStorage);
        sourceChest.setItem(0, new ItemStack(Items.WHEAT, 16));
        fill(destinationChest, Items.DIRT);
        BannerModSeaTradeExecutionRecord travelling = runtime.loadFromSource(ROUTE_ID, sourceStorage, carrierCargo);
        runtime.update(travelling.arrivalReady());

        BannerModSeaTradeExecutionRecord failed = runtime.unloadAtDestination(ROUTE_ID, destinationStorage, carrierCargo);

        helper.assertTrue(failed.state() == BannerModSeaTradeExecutionState.FAILED,
                "Expected route to fail when destination storage is full.");
        helper.assertTrue(BannerModSeaTradeExecutionRecord.FAILURE_DESTINATION_FULL.equals(failed.failureReason()),
                "Expected destination full failure reason.");
        helper.assertTrue(failed.cargoCount() == 16 && itemCount(carrierCargo, Items.WHEAT) == 16,
                "Expected undeposited goods to stay in carrier cargo.");
        helper.assertTrue(itemCount(destinationChest, Items.WHEAT) == 0,
                "Expected full destination storage not to receive wheat.");
        helper.succeed();
    }

    private static BannerModSeaTradeExecutionRuntime newRuntime(StorageArea sourceStorage, StorageArea destinationStorage) {
        BannerModSeaTradeExecutionRuntime runtime = new BannerModSeaTradeExecutionRuntime();
        runtime.start(new BannerModLogisticsRoute(
                ROUTE_ID,
                new BannerModLogisticsNodeRef(sourceStorage.getUUID()),
                new BannerModLogisticsNodeRef(destinationStorage.getUUID()),
                BannerModLogisticsItemFilter.ofItems(Items.WHEAT),
                16,
                BannerModLogisticsPriority.NORMAL
        ), CARRIER_ID);
        return runtime;
    }

    private static int itemCount(Container container, Item item) {
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static ChestBlockEntity chestAt(ServerLevel level, BlockPos pos) {
        return (ChestBlockEntity) level.getBlockEntity(pos);
    }

    private static void fill(Container container, Item item) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            container.setItem(slot, new ItemStack(item, item.getDefaultMaxStackSize()));
        }
    }

    private static BlockPos placeScannableChest(ServerLevel level, StorageArea storageArea) {
        BlockPos origin = storageArea.getOnPos();
        AABB searchBounds = storageArea.getArea().inflate(2.0D, 0.0D, 2.0D);
        int minX = (int) Math.floor(searchBounds.minX);
        int maxX = (int) Math.floor(searchBounds.maxX);
        int minY = origin.getY() - 1;
        int maxY = origin.getY() + 1;
        int minZ = (int) Math.floor(searchBounds.minZ);
        int maxZ = (int) Math.floor(searchBounds.maxZ);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    level.setBlockAndUpdate(candidate, Blocks.CHEST.defaultBlockState());
                    storageArea.scanStorageBlocks();
                    if (storageArea.storageMap.containsKey(candidate)) {
                        return candidate;
                    }
                    level.setBlockAndUpdate(candidate, Blocks.AIR.defaultBlockState());
                }
            }
        }

        throw new IllegalStateException("Could not place a scannable chest inside the storage footprint");
    }
}
