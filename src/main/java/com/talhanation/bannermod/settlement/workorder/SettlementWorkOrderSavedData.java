package com.talhanation.bannermod.settlement.workorder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class SettlementWorkOrderSavedData extends SavedData {
    private static final String FILE_ID = "bannermodSettlementWorkOrders";

    private final SettlementWorkOrderRuntime runtime;

    public SettlementWorkOrderSavedData() {
        this(new SettlementWorkOrderRuntime());
    }

    private SettlementWorkOrderSavedData(SettlementWorkOrderRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static SettlementWorkOrderSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                SettlementWorkOrderSavedData::load,
                SettlementWorkOrderSavedData::new,
                FILE_ID
        );
    }

    public static SettlementWorkOrderSavedData load(CompoundTag tag) {
        return new SettlementWorkOrderSavedData(SettlementWorkOrderRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag runtimeTag = this.runtime.toTag();
        tag.put("Orders", runtimeTag.getList("Orders", Tag.TAG_COMPOUND));
        return tag;
    }

    public SettlementWorkOrderRuntime runtime() {
        return this.runtime;
    }
}
