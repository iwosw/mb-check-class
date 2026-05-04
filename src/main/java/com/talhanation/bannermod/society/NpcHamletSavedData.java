package com.talhanation.bannermod.society;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class NpcHamletSavedData extends SavedData {
    private static final String FILE_ID = "bannermodNpcHamlets";
    private static final SavedData.Factory<NpcHamletSavedData> FACTORY =
            new SavedData.Factory<>(NpcHamletSavedData::new, NpcHamletSavedData::load);

    private final NpcHamletRuntime runtime;

    public NpcHamletSavedData() {
        this(new NpcHamletRuntime());
    }

    private NpcHamletSavedData(NpcHamletRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static NpcHamletSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static NpcHamletSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new NpcHamletSavedData(NpcHamletRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag runtimeTag = this.runtime.toTag();
        tag.put("Hamlets", runtimeTag.getList("Hamlets", 10));
        return tag;
    }

    public NpcHamletRuntime runtime() {
        return this.runtime;
    }
}
