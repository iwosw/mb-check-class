package com.talhanation.bannermod.settlement.project;

import com.talhanation.bannermod.persistence.SavedDataVersioning;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class BannerModSettlementProjectSavedData extends SavedData {
    private static final String FILE_ID = "bannermodSettlementProjects";
    private static final SavedData.Factory<BannerModSettlementProjectSavedData> FACTORY = new SavedData.Factory<>(BannerModSettlementProjectSavedData::new, BannerModSettlementProjectSavedData::load);

    private static final int CURRENT_VERSION = 1;
    private final BannerModSettlementProjectRuntime runtime;

    public BannerModSettlementProjectSavedData() {
        this(new BannerModSettlementProjectRuntime(
                BannerModSettlementProjectScheduler.detached(),
                new BannerModBuildAreaProjectBridge()
        ));
    }

    private BannerModSettlementProjectSavedData(BannerModSettlementProjectRuntime runtime) {
        this.runtime = runtime;
        this.runtime.scheduler().setDirtyListener(this::setDirty);
    }

    public static BannerModSettlementProjectSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static BannerModSettlementProjectSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.migrate(tag, CURRENT_VERSION, "BannerModSettlementProjectSavedData");
        return new BannerModSettlementProjectSavedData(new BannerModSettlementProjectRuntime(
                BannerModSettlementProjectScheduler.fromTag(tag),
                new BannerModBuildAreaProjectBridge()
        ));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.putVersion(tag, CURRENT_VERSION);
        CompoundTag runtimeTag = this.runtime.scheduler().toTag();
        tag.put("Queues", runtimeTag.getList("Queues", Tag.TAG_COMPOUND));
        tag.put("Cancellations", runtimeTag.getList("Cancellations", Tag.TAG_COMPOUND));
        return tag;
    }

    public BannerModSettlementProjectRuntime runtime() {
        return this.runtime;
    }
}
