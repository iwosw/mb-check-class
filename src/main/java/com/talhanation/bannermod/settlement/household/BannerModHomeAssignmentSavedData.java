package com.talhanation.bannermod.settlement.household;

import com.talhanation.bannermod.persistence.SavedDataVersioning;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class BannerModHomeAssignmentSavedData extends SavedData {
    private static final String FILE_ID = "bannermodHomeAssignments";
    private static final SavedData.Factory<BannerModHomeAssignmentSavedData> FACTORY = new SavedData.Factory<>(BannerModHomeAssignmentSavedData::new, BannerModHomeAssignmentSavedData::load);

    private static final int CURRENT_VERSION = 1;
    private final BannerModHomeAssignmentRuntime runtime;

    public BannerModHomeAssignmentSavedData() {
        this(new BannerModHomeAssignmentRuntime());
    }

    private BannerModHomeAssignmentSavedData(BannerModHomeAssignmentRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static BannerModHomeAssignmentSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static BannerModHomeAssignmentSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.migrate(tag, CURRENT_VERSION, "BannerModHomeAssignmentSavedData");
        return new BannerModHomeAssignmentSavedData(BannerModHomeAssignmentRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.putVersion(tag, CURRENT_VERSION);
        CompoundTag runtimeTag = this.runtime.toTag();
        tag.put("Assignments", runtimeTag.getList("Assignments", Tag.TAG_COMPOUND));
        return tag;
    }

    public BannerModHomeAssignmentRuntime runtime() {
        return this.runtime;
    }
}
