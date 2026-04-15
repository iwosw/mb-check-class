package com.talhanation.bannerlord.entity.civilian.workarea;

import com.talhanation.bannerlord.shared.logistics.BannerModLogisticsRoute;
import com.talhanation.bannerlord.shared.logistics.BannerModLogisticsService;
import com.talhanation.bannerlord.client.civilian.gui.StorageAreaScreen;
import com.talhanation.bannerlord.entity.civilian.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public class StorageArea extends AbstractWorkAreaEntity {

    public static final EntityDataAccessor<Integer> STORAGE_TYPES = SynchedEntityData.defineId(StorageArea.class, EntityDataSerializers.INT);
    public Map<BlockPos, Container> storageMap = new HashMap<>();
    private final List<BannerModLogisticsRoute> logisticsRoutes = new ArrayList<>();

    public StorageArea(EntityType<?> type, Level level) {
        super(type, level);
    }
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STORAGE_TYPES, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(STORAGE_TYPES, tag.getInt("StorageTypes"));
        this.readRoutes(tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("StorageTypes", this.entityData.get(STORAGE_TYPES));
        this.writeRoutes(tag);
    }

    public Item getRenderItem(){
        return Items.CHEST;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen getScreen(Player player) {
        return new StorageAreaScreen(this, player);
    }
    public void scanStorageBlocks(){
        if(area == null) area = this.getArea();

        storageMap.clear();

        BlockPos.betweenClosedStream(area).forEach(pos -> {
            BlockState stateAbove = this.getCommandSenderWorld().getBlockState(pos.above());

            if(stateAbove.isAir()){
                Container container = getContainer(pos);

                if(container != null && !storageMap.containsValue(container)){
                    storageMap.put(pos.immutable(), container);
                }
            }
        });
    }
    public Container getContainer(BlockPos chestPos) {
        BlockEntity entity = this.getCommandSenderWorld().getBlockEntity(chestPos);
        BlockState blockState = this.getCommandSenderWorld().getBlockState(chestPos);
        if (blockState.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, blockState, this.getCommandSenderWorld(), chestPos, false);
        } else if (entity instanceof Container containerEntity) {
            return containerEntity;
        }
        return null;
    }

    public int getStorageMask(EnumSet<StorageType> types){
        int mask = 0;
        for (StorageType type : types) {
            mask |= (1 << type.getIndex());
        }

        return mask;
    }
    public void setStorageTypes(int mask) {
        this.entityData.set(STORAGE_TYPES, mask);
    }
    public EnumSet<StorageType> getStorageTypes() {
        int mask = this.entityData.get(STORAGE_TYPES);
        EnumSet<StorageType> set = EnumSet.noneOf(StorageType.class);

        for (StorageType type : StorageType.values()) {
            if ((mask & (1 << type.getIndex())) != 0) {
                set.add(type);
            }
        }
        return set;
    }

    public List<BannerModLogisticsRoute> getLogisticsRoutes() {
        return Collections.unmodifiableList(this.logisticsRoutes);
    }

    public void setLogisticsRoutes(List<BannerModLogisticsRoute> routes) {
        this.logisticsRoutes.clear();
        if (routes != null) {
            this.logisticsRoutes.addAll(routes);
        }
    }

    public int countItemsForRoute(BannerModLogisticsRoute route) {
        this.scanStorageBlocks();
        Map<String, Integer> counts = new HashMap<>();
        for (Container container : this.storageMap.values()) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.isEmpty() || !route.matchesItem(stack)) {
                    continue;
                }
                String itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                counts.merge(itemId, stack.getCount(), Integer::sum);
            }
        }
        if (route.itemFilterId() != null) {
            return counts.getOrDefault(route.itemFilterId(), 0);
        }
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public BannerModLogisticsService.ItemStock findBestRouteItem(BannerModLogisticsRoute route) {
        this.scanStorageBlocks();
        Map<String, Integer> counts = new HashMap<>();
        for (Container container : this.storageMap.values()) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.isEmpty() || !route.matchesItem(stack)) {
                    continue;
                }
                String itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                counts.merge(itemId, stack.getCount(), Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> new BannerModLogisticsService.ItemStock(entry.getKey(), entry.getValue()))
                .orElse(null);
    }

    public BlockPos findNearestContainerWithItem(BlockPos origin, String itemId) {
        this.scanStorageBlocks();
        return this.storageMap.entrySet().stream()
                .filter(entry -> containerHasItem(entry.getValue(), itemId))
                .min(Comparator.comparingDouble(entry -> entry.getKey().distSqr(origin)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private boolean containerHasItem(Container container, String itemId) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (Objects.equals(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(), itemId)) {
                return true;
            }
        }
        return false;
    }

    private void writeRoutes(CompoundTag tag) {
        ListTag routes = new ListTag();
        for (BannerModLogisticsRoute route : this.logisticsRoutes) {
            routes.add(route.toTag());
        }
        tag.put("LogisticsRoutes", routes);
    }

    private void readRoutes(CompoundTag tag) {
        this.logisticsRoutes.clear();
        if (!tag.contains("LogisticsRoutes", Tag.TAG_LIST)) {
            return;
        }
        ListTag routes = tag.getList("LogisticsRoutes", Tag.TAG_COMPOUND);
        for (int i = 0; i < routes.size(); i++) {
            this.logisticsRoutes.add(BannerModLogisticsRoute.fromTag(routes.getCompound(i)));
        }
    }

    public enum StorageType {
        MINERS(0),
        LUMBERS(1),
        BUILDERS(2),
        FARMERS(3),
        MERCHANTS(4),
        FISHERMAN(5),
        ANIMAL_FARMERS(6);

        private final int index;
        StorageType(int index){
            this.index = index;
        }
        public int getIndex(){
            return this.index;
        }

        public static StorageType fromIndex(int index) {
            for (StorageType messengerState : StorageType.values()) {
                if (messengerState.getIndex() == index) {
                    return messengerState;
                }
            }
            throw new IllegalArgumentException("Invalid State index: " + index);
        }
    }

    public boolean canWorkHere(AbstractWorkerEntity worker){
        EnumSet<StorageType> types = this.getStorageTypes();
        if(super.canWorkHere(worker)){
            if(worker instanceof FarmerEntity){
                return types.contains(StorageType.FARMERS);
            }
            else if( worker instanceof LumberjackEntity){
                return types.contains(StorageType.LUMBERS);
            }
            else if( worker instanceof MinerEntity){
                return types.contains(StorageType.MINERS);
            }
            else if( worker instanceof BuilderEntity){
                return types.contains(StorageType.BUILDERS);
            }
            else if( worker instanceof MerchantEntity){
                return types.contains(StorageType.MERCHANTS);
            }
            else if( worker instanceof FishermanEntity){
                return types.contains(StorageType.FISHERMAN);
            }
            else if( worker instanceof AnimalFarmerEntity){
                return this.getStorageTypes().contains(StorageType.ANIMAL_FARMERS);
            }

        }
        return false;
    }
}
