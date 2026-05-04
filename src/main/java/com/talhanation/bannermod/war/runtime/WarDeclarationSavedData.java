package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.persistence.SavedDataVersioning;
import com.talhanation.bannermod.war.events.WarSyncDirtyTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WarDeclarationSavedData extends SavedData {
    private static final String FILE_ID = "bannermodWarDeclarations";
    private static final SavedData.Factory<WarDeclarationSavedData> FACTORY = new SavedData.Factory<>(WarDeclarationSavedData::new, WarDeclarationSavedData::load);

    private static final int CURRENT_VERSION = 1;
    private final WarDeclarationRuntime runtime;

    public WarDeclarationSavedData() {
        this(new WarDeclarationRuntime());
    }

    private WarDeclarationSavedData(WarDeclarationRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::markDirty);
    }

    private void markDirty() {
        setDirty();
        WarSyncDirtyTracker.markDirty();
    }

    public static WarDeclarationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static WarDeclarationSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.migrate(tag, CURRENT_VERSION, "WarDeclarationSavedData");
        return new WarDeclarationSavedData(WarDeclarationRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.putVersion(tag, CURRENT_VERSION);
        CompoundTag runtimeTag = runtime.toTag();
        tag.put("Wars", runtimeTag.getList("Wars", Tag.TAG_COMPOUND));
        return tag;
    }

    public WarDeclarationRuntime runtime() {
        return runtime;
    }
}
