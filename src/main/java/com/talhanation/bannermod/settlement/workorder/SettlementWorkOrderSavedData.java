package com.talhanation.bannermod.settlement.workorder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class SettlementWorkOrderSavedData extends SavedData {
    private static final String FILE_ID = "bannermodSettlementWorkOrders";
    private static final SavedData.Factory<SettlementWorkOrderSavedData> FACTORY = new SavedData.Factory<>(SettlementWorkOrderSavedData::new, SettlementWorkOrderSavedData::load);

    private final SettlementWorkOrderRuntime runtime;

    public SettlementWorkOrderSavedData() {
        this(new SettlementWorkOrderRuntime());
    }

    private SettlementWorkOrderSavedData(SettlementWorkOrderRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static SettlementWorkOrderSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static SettlementWorkOrderSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new SettlementWorkOrderSavedData(SettlementWorkOrderRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag runtimeTag = this.runtime.toTag();
        tag.put("Orders", runtimeTag.getList("Orders", Tag.TAG_COMPOUND));
        return tag;
    }

    public SettlementWorkOrderRuntime runtime() {
        return this.runtime;
    }
}
