package com.talhanation.workers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.Logger;

@Deprecated(forRemoval = true)
public final class WorkersRuntime {
    public static final String MOD_ID = com.talhanation.bannerlord.compat.workers.WorkersRuntime.MOD_ID;
    public static final String LEGACY_MOD_ID = com.talhanation.bannerlord.compat.workers.WorkersRuntime.LEGACY_MOD_ID;
    public static final String ACTIVE_ASSET_NAMESPACE = com.talhanation.bannerlord.compat.workers.WorkersRuntime.ACTIVE_ASSET_NAMESPACE;

    private WorkersRuntime() {
    }

    public static String modId() {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.modId();
    }

    public static Logger logger() {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.logger();
    }

    public static ResourceLocation id(String path) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.id(path);
    }

    public static ResourceLocation legacyId(String path) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.legacyId(path);
    }

    public static ResourceLocation migrateLegacyId(ResourceLocation id) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.migrateLegacyId(id);
    }

    public static String migrateLegacyId(String rawId) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.migrateLegacyId(rawId);
    }

    public static boolean migrateStructureNbt(CompoundTag structureNbt) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.migrateStructureNbt(structureNbt);
    }

    public static ResourceLocation resolveRegistryId(String rawId) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.resolveRegistryId(rawId);
    }

    public static EntityType<?> resolveEntityType(String rawId) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.resolveEntityType(rawId);
    }

    public static ResourceLocation mergedAssetId(String path) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.mergedAssetId(path);
    }

    public static ResourceLocation mergedGuiTexture(String fileName) {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.mergedGuiTexture(fileName);
    }

    public static ResourceLocation mergedStructureRoot() {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.mergedStructureRoot();
    }

    public static void bindChannel(SimpleChannel channel) {
        com.talhanation.bannerlord.compat.workers.WorkersRuntime.bindChannel(channel);
    }

    public static SimpleChannel channel() {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.channel();
    }

    public static int networkIdOffset() {
        return com.talhanation.bannerlord.compat.workers.WorkersRuntime.networkIdOffset();
    }
}
