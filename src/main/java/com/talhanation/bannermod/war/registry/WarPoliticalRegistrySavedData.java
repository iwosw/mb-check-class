package com.talhanation.bannermod.war.registry;

import com.talhanation.bannermod.war.events.WarSyncDirtyTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WarPoliticalRegistrySavedData extends SavedData {
    private static final String FILE_ID = "bannermodPoliticalRegistry";

    private final PoliticalRegistryRuntime runtime;

    public WarPoliticalRegistrySavedData() {
        this(new PoliticalRegistryRuntime());
    }

    private WarPoliticalRegistrySavedData(PoliticalRegistryRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::markDirty);
    }

    private void markDirty() {
        setDirty();
        WarSyncDirtyTracker.markDirty();
    }

    public static WarPoliticalRegistrySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                WarPoliticalRegistrySavedData::load,
                WarPoliticalRegistrySavedData::new,
                FILE_ID
        );
    }

    public static WarPoliticalRegistrySavedData load(CompoundTag tag) {
        return new WarPoliticalRegistrySavedData(PoliticalRegistryRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag runtimeTag = this.runtime.toTag();
        tag.put("PoliticalEntities", runtimeTag.getList("PoliticalEntities", Tag.TAG_COMPOUND));
        return tag;
    }

    public PoliticalRegistryRuntime runtime() {
        return runtime;
    }
}
