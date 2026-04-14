package com.talhanation.bannerlord.compat.workers;

import com.talhanation.bannerlord.bootstrap.BannerlordMain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public final class WorkersRuntime {
    public static final String MOD_ID = BannerlordMain.MOD_ID;
    public static final String LEGACY_MOD_ID = "workers";
    public static final String ACTIVE_ASSET_NAMESPACE = "bannermod";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final int ROOT_NETWORK_ID_OFFSET = 104;
    private static SimpleChannel simpleChannel;

    private WorkersRuntime() {
    }

    public static String modId() {
        return MOD_ID;
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static ResourceLocation legacyId(String path) {
        return new ResourceLocation(LEGACY_MOD_ID, path);
    }

    public static ResourceLocation migrateLegacyId(ResourceLocation id) {
        if (!LEGACY_MOD_ID.equals(id.getNamespace())) {
            return id;
        }
        return new ResourceLocation(MOD_ID, id.getPath());
    }

    public static String migrateLegacyId(String rawId) {
        ResourceLocation id = ResourceLocation.tryParse(rawId);
        return id == null ? rawId : migrateLegacyId(id).toString();
    }

    public static boolean migrateStructureNbt(CompoundTag structureNbt) {
        boolean migrated = false;

        if (structureNbt.contains("blocks", Tag.TAG_LIST)) {
            ListTag blockList = structureNbt.getList("blocks", Tag.TAG_COMPOUND);
            for (Tag tag : blockList) {
                CompoundTag blockTag = (CompoundTag) tag;
                migrated |= migrateStringField(blockTag, "block");

                if (blockTag.contains("state", Tag.TAG_COMPOUND)) {
                    CompoundTag stateTag = blockTag.getCompound("state");
                    migrated |= migrateStringField(stateTag, "Name");
                }
            }
        }

        if (structureNbt.contains("entities", Tag.TAG_LIST)) {
            ListTag entityList = structureNbt.getList("entities", Tag.TAG_COMPOUND);
            for (Tag tag : entityList) {
                migrated |= migrateStringField((CompoundTag) tag, "entity_type");
            }
        }

        return migrated;
    }

    public static ResourceLocation resolveRegistryId(String rawId) {
        ResourceLocation id = new ResourceLocation(rawId);
        if (ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
            return id;
        }
        if (LEGACY_MOD_ID.equals(id.getNamespace())) {
            ResourceLocation migratedId = migrateLegacyId(id);
            if (ForgeRegistries.ENTITY_TYPES.containsKey(migratedId)) {
                return migratedId;
            }
        }
        return id;
    }

    public static EntityType<?> resolveEntityType(String rawId) {
        return ForgeRegistries.ENTITY_TYPES.getValue(resolveRegistryId(rawId));
    }

    public static ResourceLocation mergedAssetId(String path) {
        return new ResourceLocation(ACTIVE_ASSET_NAMESPACE, path);
    }

    public static ResourceLocation mergedGuiTexture(String fileName) {
        return mergedAssetId("textures/gui/workers/" + fileName);
    }

    public static ResourceLocation mergedStructureRoot() {
        return mergedAssetId("structures/workers");
    }

    public static void bindChannel(SimpleChannel channel) {
        simpleChannel = channel;
    }

    public static SimpleChannel channel() {
        return Objects.requireNonNull(simpleChannel, "Workers shared channel is not initialized yet");
    }

    public static int networkIdOffset() {
        return ROOT_NETWORK_ID_OFFSET;
    }

    private static boolean migrateStringField(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_STRING)) {
            return false;
        }

        String currentValue = tag.getString(key);
        String migratedValue = migrateLegacyId(currentValue);
        if (currentValue.equals(migratedValue)) {
            return false;
        }

        tag.putString(key, migratedValue);
        return true;
    }
}
