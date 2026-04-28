package com.talhanation.bannermod.war.runtime;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class DemilitarizationSavedData extends SavedData {
    private static final String FILE_ID = "bannermodDemilitarizations";
    private static final SavedData.Factory<DemilitarizationSavedData> FACTORY = new SavedData.Factory<>(DemilitarizationSavedData::new, DemilitarizationSavedData::load);

    private final DemilitarizationRuntime runtime;

    public DemilitarizationSavedData() {
        this(new DemilitarizationRuntime());
    }

    private DemilitarizationSavedData(DemilitarizationRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static DemilitarizationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static DemilitarizationSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new DemilitarizationSavedData(DemilitarizationRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag inner = runtime.toTag();
        tag.put("Demilitarizations", inner.getList("Demilitarizations", Tag.TAG_COMPOUND));
        return tag;
    }

    public DemilitarizationRuntime runtime() {
        return runtime;
    }
}
