package com.talhanation.bannermod.persistence;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;

public final class SavedDataVersioning {

    public static final String DATA_VERSION_KEY = "DataVersion";

    private static final Logger LOGGER = LogUtils.getLogger();

    private SavedDataVersioning() {
    }

    public static int getVersion(CompoundTag tag) {
        if (tag == null || !tag.contains(DATA_VERSION_KEY, Tag.TAG_INT)) {
            return 0;
        }
        return tag.getInt(DATA_VERSION_KEY);
    }

    public static void putVersion(CompoundTag tag, int currentVersion) {
        if (tag == null) {
            return;
        }
        tag.putInt(DATA_VERSION_KEY, currentVersion);
    }

    public static int migrate(CompoundTag tag, int currentVersion, String savedDataName) {
        int found = getVersion(tag);
        if (found < currentVersion) {
            LOGGER.info("Migrating {} SavedData from v{} to v{}", savedDataName, found, currentVersion);
        } else if (found > currentVersion) {
            LOGGER.warn(
                "{} SavedData on disk is v{} but code only knows v{} — loading anyway",
                savedDataName,
                found,
                currentVersion
            );
        }
        return found;
    }
}
