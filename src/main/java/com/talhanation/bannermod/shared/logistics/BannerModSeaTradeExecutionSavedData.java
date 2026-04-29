package com.talhanation.bannermod.shared.logistics;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class BannerModSeaTradeExecutionSavedData extends SavedData {
    private static final String FILE_ID = "bannermodSeaTradeExecutions";
    private static final SavedData.Factory<BannerModSeaTradeExecutionSavedData> FACTORY = new SavedData.Factory<>(BannerModSeaTradeExecutionSavedData::new, BannerModSeaTradeExecutionSavedData::load);

    private final BannerModSeaTradeExecutionRuntime runtime;

    public BannerModSeaTradeExecutionSavedData() {
        this(new BannerModSeaTradeExecutionRuntime());
    }

    private BannerModSeaTradeExecutionSavedData(BannerModSeaTradeExecutionRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static BannerModSeaTradeExecutionSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static BannerModSeaTradeExecutionSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new BannerModSeaTradeExecutionSavedData(BannerModSeaTradeExecutionRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag runtimeTag = this.runtime.toTag();
        tag.put("Routes", runtimeTag.getList("Routes", Tag.TAG_COMPOUND));
        return tag;
    }

    public BannerModSeaTradeExecutionRuntime runtime() {
        return this.runtime;
    }
}
