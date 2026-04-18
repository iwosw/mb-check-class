package com.talhanation.bannermod.entity.civilian.workarea;

import com.talhanation.bannermod.client.civilian.gui.StorageAreaScreen;
import com.talhanation.bannermod.entity.civilian.*;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsAuthoringState;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsBlockedReason;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsPriority;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRoute;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
    public static final EntityDataAccessor<String> ROUTE_DESTINATION = SynchedEntityData.defineId(StorageArea.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> ROUTE_FILTER = SynchedEntityData.defineId(StorageArea.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> ROUTE_REQUESTED_COUNT = SynchedEntityData.defineId(StorageArea.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> ROUTE_PRIORITY = SynchedEntityData.defineId(StorageArea.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> ROUTE_BLOCKED_REASON = SynchedEntityData.defineId(StorageArea.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> ROUTE_BLOCKED_MESSAGE = SynchedEntityData.defineId(StorageArea.class, EntityDataSerializers.STRING);
    public Map<BlockPos, Container> storageMap = new HashMap<>();

    public StorageArea(EntityType<?> type, Level level) {
        super(type, level);
    }
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STORAGE_TYPES, 0);
        this.entityData.define(ROUTE_DESTINATION, "");
        this.entityData.define(ROUTE_FILTER, "");
        this.entityData.define(ROUTE_REQUESTED_COUNT, 16);
        this.entityData.define(ROUTE_PRIORITY, BannerModLogisticsPriority.NORMAL.name());
        this.entityData.define(ROUTE_BLOCKED_REASON, "");
        this.entityData.define(ROUTE_BLOCKED_MESSAGE, "");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(STORAGE_TYPES, tag.getInt("StorageTypes"));
        this.entityData.set(ROUTE_DESTINATION, tag.getString("LogisticsRouteDestination"));
        this.entityData.set(ROUTE_FILTER, tag.getString("LogisticsRouteFilter"));
        this.entityData.set(ROUTE_REQUESTED_COUNT, tag.contains("LogisticsRouteRequestedCount") ? Math.max(1, tag.getInt("LogisticsRouteRequestedCount")) : 16);
        this.entityData.set(ROUTE_PRIORITY, tag.contains("LogisticsRoutePriority") ? tag.getString("LogisticsRoutePriority") : BannerModLogisticsPriority.NORMAL.name());
        this.entityData.set(ROUTE_BLOCKED_REASON, tag.getString("LogisticsRouteBlockedReason"));
        this.entityData.set(ROUTE_BLOCKED_MESSAGE, tag.getString("LogisticsRouteBlockedMessage"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("StorageTypes", this.entityData.get(STORAGE_TYPES));
        tag.putString("LogisticsRouteDestination", this.getRouteDestinationText());
        tag.putString("LogisticsRouteFilter", this.getRouteFilterText());
        tag.putInt("LogisticsRouteRequestedCount", this.getRouteRequestedCount());
        tag.putString("LogisticsRoutePriority", this.getRoutePriorityText());
        tag.putString("LogisticsRouteBlockedReason", this.getRouteBlockedReasonToken());
        tag.putString("LogisticsRouteBlockedMessage", this.getRouteBlockedMessage());
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

    public String getRouteDestinationText() {
        return this.entityData.get(ROUTE_DESTINATION);
    }

    public String getRouteFilterText() {
        return this.entityData.get(ROUTE_FILTER);
    }

    public int getRouteRequestedCount() {
        return this.entityData.get(ROUTE_REQUESTED_COUNT);
    }

    public String getRoutePriorityText() {
        return this.entityData.get(ROUTE_PRIORITY);
    }

    public String getRouteBlockedReasonToken() {
        return this.entityData.get(ROUTE_BLOCKED_REASON);
    }

    public String getRouteBlockedMessage() {
        return this.entityData.get(ROUTE_BLOCKED_MESSAGE);
    }

    public void setLogisticsRoute(BannerModLogisticsAuthoringState state) {
        this.entityData.set(ROUTE_DESTINATION, state.destinationText());
        this.entityData.set(ROUTE_FILTER, state.filterText());
        this.entityData.set(ROUTE_REQUESTED_COUNT, state.requestedCount());
        this.entityData.set(ROUTE_PRIORITY, state.priorityText());
    }

    public BannerModLogisticsAuthoringState getLogisticsRouteAuthoringState() {
        return BannerModLogisticsAuthoringState.parse(
                this.getRouteDestinationText(),
                this.getRouteFilterText(),
                Integer.toString(this.getRouteRequestedCount()),
                this.getRoutePriorityText()
        );
    }

    public Optional<BannerModLogisticsRoute> getAuthoredLogisticsRoute() {
        BannerModLogisticsAuthoringState state = this.getLogisticsRouteAuthoringState();
        if (state.destinationStorageAreaId() == null) {
            return Optional.empty();
        }
        return Optional.of(state.toRoute(this.getUUID()));
    }

    public void clearRouteBlockedState() {
        this.entityData.set(ROUTE_BLOCKED_REASON, "");
        this.entityData.set(ROUTE_BLOCKED_MESSAGE, "");
    }

    public void setRouteBlockedState(BannerModLogisticsBlockedReason reason, String message) {
        this.entityData.set(ROUTE_BLOCKED_REASON, reason == null ? "" : reason.reasonToken());
        this.entityData.set(ROUTE_BLOCKED_MESSAGE, message == null ? "" : message);
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
