package com.talhanation.bannermod.persistence.civilian;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureTemplateLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadTemplateAcceptsLitematicAndKeepsSparseAuthoredBlocks() throws IOException {
        Path file = tempDir.resolve("watchtower.litematic");
        NbtIo.writeCompressed(sampleLitematic(), file);

        CompoundTag root = StructureTemplateLoader.loadTemplate(file);

        assertNotNull(root);
        assertTrue(StructureTemplateLoader.supportedExtensions.contains(".litematic"));
        assertEquals("watchtower", root.getString("name"));
        assertEquals(4, root.getInt("width"));
        assertEquals(1, root.getInt("height"));
        assertEquals(1, root.getInt("depth"));

        ListTag blocks = root.getList("blocks", Tag.TAG_COMPOUND);
        assertEquals(2, blocks.size());

        Map<Integer, CompoundTag> byX = new HashMap<>();
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag block = blocks.getCompound(i);
            byX.put(block.getInt("x"), block);
        }

        assertEquals("minecraft:stone", byX.get(0).getCompound("state").getString("Name"));
        assertEquals("minecraft:oak_stairs", byX.get(3).getCompound("state").getString("Name"));
        assertEquals("east", byX.get(3).getCompound("state").getCompound("Properties").getString("facing"));
    }

    @Test
    void loadTemplateParsesSchematicPalettePropertiesIntoStateTag() throws IOException {
        Path file = tempDir.resolve("stairs.schem");
        NbtIo.writeCompressed(sampleSchematic(), file);

        CompoundTag root = StructureTemplateLoader.loadTemplate(file);

        assertNotNull(root);
        ListTag blocks = root.getList("blocks", Tag.TAG_COMPOUND);
        assertEquals(1, blocks.size());
        CompoundTag state = blocks.getCompound(0).getCompound("state");
        assertEquals("minecraft:oak_stairs", state.getString("Name"));
        assertEquals("east", state.getCompound("Properties").getString("facing"));
        assertEquals("bottom", state.getCompound("Properties").getString("half"));
    }

    private static CompoundTag sampleLitematic() {
        CompoundTag root = new CompoundTag();
        CompoundTag metadata = new CompoundTag();
        metadata.putString("Name", "watchtower");
        root.put("Metadata", metadata);

        CompoundTag regions = new CompoundTag();
        regions.put("main", region(new int[]{0, 0, 0}, new int[]{1, 1, 1},
                new CompoundTag[]{state("minecraft:air"), state("minecraft:stone")}, new int[]{1}));
        regions.put("roof", region(new int[]{3, 0, 0}, new int[]{1, 1, 1},
                new CompoundTag[]{state("minecraft:air"), state("minecraft:oak_stairs", "facing", "east", "half", "bottom", "shape", "straight", "waterlogged", "false")}, new int[]{1}));
        root.put("Regions", regions);
        return root;
    }

    private static CompoundTag sampleSchematic() {
        CompoundTag root = new CompoundTag();
        root.putInt("Width", 1);
        root.putInt("Height", 1);
        root.putInt("Length", 1);
        CompoundTag palette = new CompoundTag();
        palette.putInt("minecraft:air", 0);
        palette.putInt("minecraft:oak_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]", 1);
        root.put("Palette", palette);
        root.putByteArray("BlockData", new byte[]{1});
        return root;
    }

    private static CompoundTag region(int[] position, int[] size, CompoundTag[] paletteEntries, int[] paletteIndices) {
        CompoundTag region = new CompoundTag();
        region.putIntArray("Position", position);
        region.putIntArray("Size", size);
        ListTag palette = new ListTag();
        for (CompoundTag paletteEntry : paletteEntries) {
            palette.add(paletteEntry);
        }
        region.put("BlockStatePalette", palette);
        int bitsPerBlock = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(0, paletteEntries.length - 1)));
        region.putLongArray("BlockStates", packBlockStates(bitsPerBlock, paletteIndices));
        return region;
    }

    private static CompoundTag state(String name, String... properties) {
        CompoundTag state = new CompoundTag();
        state.putString("Name", name);
        if (properties.length > 0) {
            CompoundTag props = new CompoundTag();
            for (int i = 0; i < properties.length; i += 2) {
                props.putString(properties[i], properties[i + 1]);
            }
            state.put("Properties", props);
        }
        return state;
    }

    private static long[] packBlockStates(int bitsPerBlock, int[] paletteIndices) {
        long mask = bitsPerBlock >= Long.SIZE ? -1L : (1L << bitsPerBlock) - 1L;
        long[] packed = new long[(int) Math.ceil((double) paletteIndices.length * bitsPerBlock / Long.SIZE)];
        for (int index = 0; index < paletteIndices.length; index++) {
            long value = paletteIndices[index] & mask;
            long bitIndex = (long) index * bitsPerBlock;
            int startLong = (int) (bitIndex >>> 6);
            int startOffset = (int) (bitIndex & 63L);
            packed[startLong] |= value << startOffset;
            int overflow = startOffset + bitsPerBlock - Long.SIZE;
            if (overflow > 0) {
                packed[startLong + 1] |= value >>> (bitsPerBlock - overflow);
            }
        }
        return packed;
    }
}
