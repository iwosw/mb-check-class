package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.events.WarSyncDirtyTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class SiegeStandardSavedData extends SavedData {
    private static final String FILE_ID = "bannermodSiegeStandards";

    private final SiegeStandardRuntime runtime;

    public SiegeStandardSavedData() {
        this(new SiegeStandardRuntime());
    }

    private SiegeStandardSavedData(SiegeStandardRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::markDirty);
    }

    private void markDirty() {
        setDirty();
        WarSyncDirtyTracker.markDirty();
    }

    public static SiegeStandardSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                SiegeStandardSavedData::load,
                SiegeStandardSavedData::new,
                FILE_ID
        );
    }

    public static SiegeStandardSavedData load(CompoundTag tag) {
        return new SiegeStandardSavedData(SiegeStandardRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag inner = runtime.toTag();
        tag.put("SiegeStandards", inner.getList("SiegeStandards", Tag.TAG_COMPOUND));
        return tag;
    }

    public SiegeStandardRuntime runtime() {
        return runtime;
    }
}
