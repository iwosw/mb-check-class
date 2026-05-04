package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.persistence.SavedDataVersioning;
import com.talhanation.bannermod.war.events.WarSyncDirtyTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class RevoltSavedData extends SavedData {
    private static final String FILE_ID = "bannermodRevolts";
    private static final SavedData.Factory<RevoltSavedData> FACTORY = new SavedData.Factory<>(RevoltSavedData::new, RevoltSavedData::load);

    private static final int CURRENT_VERSION = 1;
    private final RevoltRuntime runtime;

    public RevoltSavedData() {
        this(new RevoltRuntime());
    }

    private RevoltSavedData(RevoltRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::markDirty);
    }

    private void markDirty() {
        setDirty();
        WarSyncDirtyTracker.markDirty();
    }

    public static RevoltSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static RevoltSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.migrate(tag, CURRENT_VERSION, "RevoltSavedData");
        return new RevoltSavedData(RevoltRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.putVersion(tag, CURRENT_VERSION);
        CompoundTag inner = runtime.toTag();
        tag.put("Revolts", inner.getList("Revolts", Tag.TAG_COMPOUND));
        return tag;
    }

    public RevoltRuntime runtime() {
        return runtime;
    }
}
