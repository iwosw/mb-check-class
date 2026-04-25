package com.talhanation.bannermod.settlement.dispatch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class BannerModSellerDispatchSavedData extends SavedData {
    private static final String FILE_ID = "bannermodSellerDispatches";

    private final BannerModSellerDispatchRuntime runtime;

    public BannerModSellerDispatchSavedData() {
        this(new BannerModSellerDispatchRuntime());
    }

    private BannerModSellerDispatchSavedData(BannerModSellerDispatchRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static BannerModSellerDispatchSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                BannerModSellerDispatchSavedData::load,
                BannerModSellerDispatchSavedData::new,
                FILE_ID
        );
    }

    public static BannerModSellerDispatchSavedData load(CompoundTag tag) {
        return new BannerModSellerDispatchSavedData(BannerModSellerDispatchRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag runtimeTag = this.runtime.toTag();
        tag.put("Dispatches", runtimeTag.getList("Dispatches", Tag.TAG_COMPOUND));
        return tag;
    }

    public BannerModSellerDispatchRuntime runtime() {
        return this.runtime;
    }
}
