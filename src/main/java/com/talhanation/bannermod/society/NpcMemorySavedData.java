package com.talhanation.bannermod.society;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class NpcMemorySavedData extends SavedData {
    private static final String FILE_ID = "bannermodNpcMemory";
    private static final SavedData.Factory<NpcMemorySavedData> FACTORY =
            new SavedData.Factory<>(NpcMemorySavedData::new, NpcMemorySavedData::load);

    private final NpcMemoryRuntime runtime;

    public NpcMemorySavedData() {
        this(new NpcMemoryRuntime());
    }

    private NpcMemorySavedData(NpcMemoryRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static NpcMemorySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static NpcMemorySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new NpcMemorySavedData(NpcMemoryRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag runtimeTag = this.runtime.toTag();
        tag.put("Memories", runtimeTag.getList("Memories", 10));
        return tag;
    }

    public NpcMemoryRuntime runtime() {
        return this.runtime;
    }
}
