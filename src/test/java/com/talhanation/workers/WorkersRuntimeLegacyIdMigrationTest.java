package com.talhanation.workers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkersRuntimeLegacyIdMigrationTest {

    @Test
    void migrateLegacyIdOnlyRewritesWorkersNamespace() {
        assertEquals("bannermod:farmer", WorkersRuntime.migrateLegacyId("workers:farmer"));
        assertEquals("minecraft:stone", WorkersRuntime.migrateLegacyId("minecraft:stone"));
    }

    @Test
    void migrateStructureNbtRewritesLegacyWorkerEntriesInKnownSchema() {
        CompoundTag root = new CompoundTag();

        CompoundTag blockTag = new CompoundTag();
        blockTag.putString("block", "workers:builder_block");
        CompoundTag stateTag = new CompoundTag();
        stateTag.putString("Name", "workers:builder_block");
        blockTag.put("state", stateTag);
        ListTag blocks = new ListTag();
        blocks.add(blockTag);
        root.put("blocks", blocks);

        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("entity_type", "workers:builder");
        ListTag entities = new ListTag();
        entities.add(entityTag);
        root.put("entities", entities);

        assertTrue(WorkersRuntime.migrateStructureNbt(root));
        assertEquals("bannermod:builder_block", blockTag.getString("block"));
        assertEquals("bannermod:builder_block", stateTag.getString("Name"));
        assertEquals("bannermod:builder", entityTag.getString("entity_type"));
        assertFalse(WorkersRuntime.migrateStructureNbt(root));
    }
}
