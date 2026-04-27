package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.events.WarSyncDirtyTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class OccupationSavedData extends SavedData {
    private static final String FILE_ID = "bannermodOccupations";

    private final OccupationRuntime runtime;

    public OccupationSavedData() {
        this(new OccupationRuntime());
    }

    private OccupationSavedData(OccupationRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::markDirty);
    }

    private void markDirty() {
        setDirty();
        WarSyncDirtyTracker.markDirty();
    }

    public static OccupationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                OccupationSavedData::load,
                OccupationSavedData::new,
                FILE_ID
        );
    }

    public static OccupationSavedData load(CompoundTag tag) {
        return new OccupationSavedData(OccupationRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag inner = runtime.toTag();
        tag.put("Occupations", inner.getList("Occupations", Tag.TAG_COMPOUND));
        return tag;
    }

    public OccupationRuntime runtime() {
        return runtime;
    }
}
