package com.talhanation.bannermod.war.runtime;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WarDeclarationSavedData extends SavedData {
    private static final String FILE_ID = "bannermodWarDeclarations";

    private final WarDeclarationRuntime runtime;

    public WarDeclarationSavedData() {
        this(new WarDeclarationRuntime());
    }

    private WarDeclarationSavedData(WarDeclarationRuntime runtime) {
        this.runtime = runtime;
        this.runtime.setDirtyListener(this::setDirty);
    }

    public static WarDeclarationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                WarDeclarationSavedData::load,
                WarDeclarationSavedData::new,
                FILE_ID
        );
    }

    public static WarDeclarationSavedData load(CompoundTag tag) {
        return new WarDeclarationSavedData(WarDeclarationRuntime.fromTag(tag));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag runtimeTag = runtime.toTag();
        tag.put("Wars", runtimeTag.getList("Wars", Tag.TAG_COMPOUND));
        return tag;
    }

    public WarDeclarationRuntime runtime() {
        return runtime;
    }
}
