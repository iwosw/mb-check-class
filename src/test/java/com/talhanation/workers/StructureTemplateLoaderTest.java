package com.talhanation.workers;

import com.talhanation.bannerlord.persistence.civilian.StructureTemplateLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StructureTemplateLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void nbtTemplatesStillLoadThroughCurrentContract() throws Exception {
        Path file = tempDir.resolve("house.nbt");
        CompoundTag root = new CompoundTag();
        root.putString("name", "house");
        root.putInt("width", 1);
        root.putInt("height", 1);
        root.putInt("depth", 1);
        root.putString("facing", "south");
        root.put("blocks", new net.minecraft.nbt.ListTag());
        root.put("entities", new net.minecraft.nbt.ListTag());
        try (OutputStream output = Files.newOutputStream(file)) {
            NbtIo.writeCompressed(root, output);
        }

        CompoundTag loaded = StructureTemplateLoader.loadTemplate(file, tag -> {});

        assertNotNull(loaded);
        assertEquals("house", loaded.getString("name"));
        assertEquals(1, loaded.getInt("width"));
    }

    @Test
    void schemAndSchematicFilesConvertIntoBuildAreaShape() throws Exception {
        assertConverted(tempDir.resolve("ore.schem"));
        assertConverted(tempDir.resolve("ore.schematic"));
    }

    private void assertConverted(Path file) throws Exception {
        CompoundTag schematic = new CompoundTag();
        schematic.putInt("Width", 1);
        schematic.putInt("Height", 1);
        schematic.putInt("Length", 1);
        CompoundTag palette = new CompoundTag();
        palette.putInt("minecraft:stone", 0);
        schematic.put("Palette", palette);
        schematic.putByteArray("BlockData", new byte[]{0});
        try (OutputStream output = Files.newOutputStream(file)) {
            NbtIo.writeCompressed(schematic, output);
        }

        CompoundTag loaded = StructureTemplateLoader.loadTemplate(file, tag -> {});

        assertNotNull(loaded);
        assertEquals(1, loaded.getInt("width"));
        assertEquals(1, loaded.getInt("height"));
        assertEquals(1, loaded.getInt("depth"));
        CompoundTag blockTag = loaded.getList("blocks", 10).getCompound(0);
        assertEquals("minecraft:stone", blockTag.getString("block"));
        assertEquals("minecraft:stone", blockTag.getString("block"));
    }
}
