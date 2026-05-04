package com.talhanation.bannermod.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedDataVersioningTest {

    @Test
    void getVersionReturnsZeroForLegacyTagsWithoutVersionField() {
        CompoundTag legacy = new CompoundTag();

        assertEquals(0, SavedDataVersioning.getVersion(legacy));
    }

    @Test
    void getVersionReturnsZeroForNullTag() {
        assertEquals(0, SavedDataVersioning.getVersion(null));
    }

    @Test
    void putVersionWritesIntegerUnderDataVersionKey() {
        CompoundTag tag = new CompoundTag();

        SavedDataVersioning.putVersion(tag, 4);

        assertTrue(tag.contains(SavedDataVersioning.DATA_VERSION_KEY, Tag.TAG_INT));
        assertEquals(4, tag.getInt(SavedDataVersioning.DATA_VERSION_KEY));
    }

    @Test
    void putVersionToleratesNullTag() {
        SavedDataVersioning.putVersion(null, 1);
    }

    @Test
    void migrateReportsDiscoveredVersionAndDoesNotMutateTag() {
        CompoundTag tag = new CompoundTag();
        SavedDataVersioning.putVersion(tag, 3);

        int found = SavedDataVersioning.migrate(tag, 5, "ExampleSavedData");

        assertEquals(3, found);
        assertEquals(3, tag.getInt(SavedDataVersioning.DATA_VERSION_KEY));
    }

    @Test
    void migrateReportsZeroForLegacyTag() {
        CompoundTag legacy = new CompoundTag();
        legacy.putString("Other", "value");

        int found = SavedDataVersioning.migrate(legacy, 1, "ExampleSavedData");

        assertEquals(0, found);
        assertFalse(legacy.contains(SavedDataVersioning.DATA_VERSION_KEY));
    }
}
