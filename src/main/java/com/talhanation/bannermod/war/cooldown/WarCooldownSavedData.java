package com.talhanation.bannermod.war.cooldown;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WarCooldownSavedData extends SavedData {

    private static final String FILE_ID = "bannermodWarCooldowns";
    private static final SavedData.Factory<WarCooldownSavedData> FACTORY = new SavedData.Factory<>(WarCooldownSavedData::new, WarCooldownSavedData::load);

    private final WarCooldownRuntime runtime;

    public WarCooldownSavedData() {
        this(new WarCooldownRuntime());
    }

    private WarCooldownSavedData(WarCooldownRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static WarCooldownSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static WarCooldownSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new WarCooldownSavedData(WarCooldownRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag inner = runtime.toTag();
        tag.put("WarCooldowns", inner.getList("WarCooldowns", Tag.TAG_COMPOUND));
        return tag;
    }

    public WarCooldownRuntime runtime() {
        return runtime;
    }
}
