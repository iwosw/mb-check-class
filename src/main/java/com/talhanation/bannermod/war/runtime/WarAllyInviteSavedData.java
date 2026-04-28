package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.events.WarSyncDirtyTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WarAllyInviteSavedData extends SavedData {
    private static final String FILE_ID = "bannermodWarAllyInvites";
    private static final SavedData.Factory<WarAllyInviteSavedData> FACTORY = new SavedData.Factory<>(WarAllyInviteSavedData::new, WarAllyInviteSavedData::load);

    private final WarAllyInviteRuntime runtime;

    public WarAllyInviteSavedData() {
        this(new WarAllyInviteRuntime());
    }

    private WarAllyInviteSavedData(WarAllyInviteRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::markDirty);
    }

    private void markDirty() {
        setDirty();
        WarSyncDirtyTracker.markDirty();
    }

    public static WarAllyInviteSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static WarAllyInviteSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new WarAllyInviteSavedData(WarAllyInviteRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag inner = runtime.toTag();
        tag.put("Invites", inner.getList("Invites", Tag.TAG_COMPOUND));
        return tag;
    }

    public WarAllyInviteRuntime runtime() {
        return runtime;
    }
}
