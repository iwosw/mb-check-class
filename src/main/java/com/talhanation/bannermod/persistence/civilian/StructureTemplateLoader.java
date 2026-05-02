package com.talhanation.bannermod.persistence.civilian;

import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class StructureTemplateLoader {

    public static final List<String> supportedExtensions = List.of(
            ".nbt",
            ".schem",
            ".schematic",
            ".litematic",
            ".forgematica",
            ".forgematic");

    public static CompoundTag loadTemplate(Path templatePath) {
        return loadTemplate(templatePath, WorkersRuntime::migrateStructureNbt);
    }

    public static CompoundTag loadTemplate(Path templatePath, Consumer<CompoundTag> migrator) {
        String fileName = templatePath.getFileName().toString().toLowerCase(Locale.ROOT);
        try {
            CompoundTag root;
            if (fileName.endsWith(".nbt")) {
                try (InputStream input = Files.newInputStream(templatePath)) {
                    root = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
                }
            } else if (fileName.endsWith(".litematic") || fileName.endsWith(".forgematica") || fileName.endsWith(".forgematic")) {
                try (InputStream input = Files.newInputStream(templatePath)) {
                    root = convertLitematic(NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap()), stripExtension(fileName));
                }
            } else if (fileName.endsWith(".schem") || fileName.endsWith(".schematic")) {
                try (InputStream input = Files.newInputStream(templatePath)) {
                    root = convertSchematic(NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap()), stripExtension(fileName));
                }
            } else {
                return null;
            }
            if (root != null) {
                migrator.accept(root);
            }
            return root;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static CompoundTag convertSchematic(CompoundTag schematic, String fallbackName) {
        CompoundTag root = new CompoundTag();
        int width = readDimension(schematic, "Width", "width");
        int height = readDimension(schematic, "Height", "height");
        int depth = readDimension(schematic, "Length", "depth");
        root.putString("name", schematic.contains("Metadata") ? schematic.getCompound("Metadata").getString("Name") : fallbackName);
        root.putInt("width", width);
        root.putInt("height", height);
        root.putInt("depth", depth);
        root.putString("facing", Direction.SOUTH.getName());
        root.put("entities", new ListTag());

        CompoundTag palette = schematic.getCompound("Palette");
        byte[] blockData = schematic.getByteArray("BlockData");
        Map<Integer, CompoundTag> paletteById = new TreeMap<>();
        for (String blockName : palette.getAllKeys()) {
            paletteById.put(palette.getInt(blockName), parseBlockStateString(blockName));
        }

        ListTag blocks = new ListTag();
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    if (index >= blockData.length) {
                        break;
                    }
                    CompoundTag stateTag = paletteById.get(Byte.toUnsignedInt(blockData[index++]));
                    if (stateTag == null || shouldSkipStructureBlock(stateTag.getString("Name"))) {
                        continue;
                    }
                    CompoundTag blockTag = new CompoundTag();
                    blockTag.putInt("x", x);
                    blockTag.putInt("y", y);
                    blockTag.putInt("z", z);
                    blockTag.putString("block", stateTag.getString("Name"));
                    blockTag.put("state", stateTag.copy());
                    blocks.add(blockTag);
                }
            }
        }
        root.put("blocks", blocks);
        return root;
    }

    private static CompoundTag convertLitematic(CompoundTag litematic, String fallbackName) {
        CompoundTag regions = litematic.getCompound("Regions");
        if (regions.isEmpty()) {
            return null;
        }

        List<ConvertedRegion> convertedRegions = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (String regionName : regions.getAllKeys()) {
            ConvertedRegion region = convertLitematicRegion(regions.getCompound(regionName));
            if (region == null || region.blocks().isEmpty()) {
                if (region != null) {
                    minX = Math.min(minX, region.minX());
                    minY = Math.min(minY, region.minY());
                    minZ = Math.min(minZ, region.minZ());
                    maxX = Math.max(maxX, region.maxX());
                    maxY = Math.max(maxY, region.maxY());
                    maxZ = Math.max(maxZ, region.maxZ());
                }
                continue;
            }
            convertedRegions.add(region);
            minX = Math.min(minX, region.minX());
            minY = Math.min(minY, region.minY());
            minZ = Math.min(minZ, region.minZ());
            maxX = Math.max(maxX, region.maxX());
            maxY = Math.max(maxY, region.maxY());
            maxZ = Math.max(maxZ, region.maxZ());
        }

        if (minX == Integer.MAX_VALUE) {
            minX = minY = minZ = 0;
            maxX = maxY = maxZ = 0;
        }

        CompoundTag metadata = litematic.getCompound("Metadata");
        CompoundTag root = new CompoundTag();
        root.putString("name", metadata.contains("Name") ? metadata.getString("Name") : fallbackName);
        root.putInt("width", Math.max(1, maxX - minX + 1));
        root.putInt("height", Math.max(1, maxY - minY + 1));
        root.putInt("depth", Math.max(1, maxZ - minZ + 1));
        root.putString("facing", Direction.SOUTH.getName());
        root.put("entities", new ListTag());

        ListTag blocks = new ListTag();
        for (ConvertedRegion region : convertedRegions) {
            for (CompoundTag blockTag : region.blocks()) {
                CompoundTag normalized = blockTag.copy();
                normalized.putInt("x", normalized.getInt("x") - minX);
                normalized.putInt("y", normalized.getInt("y") - minY);
                normalized.putInt("z", normalized.getInt("z") - minZ);
                blocks.add(normalized);
            }
        }
        root.put("blocks", blocks);
        return root;
    }

    private static ConvertedRegion convertLitematicRegion(CompoundTag region) {
        int[] position = readVector(region, "Position");
        int[] size = readVector(region, "Size");
        int sizeX = Math.abs(size[0]);
        int sizeY = Math.abs(size[1]);
        int sizeZ = Math.abs(size[2]);
        if (sizeX == 0 || sizeY == 0 || sizeZ == 0) {
            return null;
        }

        int minX = size[0] >= 0 ? position[0] : position[0] + size[0] + 1;
        int minY = size[1] >= 0 ? position[1] : position[1] + size[1] + 1;
        int minZ = size[2] >= 0 ? position[2] : position[2] + size[2] + 1;
        int maxX = minX + sizeX - 1;
        int maxY = minY + sizeY - 1;
        int maxZ = minZ + sizeZ - 1;

        ListTag palette = region.getList("BlockStatePalette", Tag.TAG_COMPOUND);
        long[] blockStates = region.getLongArray("BlockStates");
        int totalBlocks = sizeX * sizeY * sizeZ;
        if (palette.isEmpty() || blockStates.length == 0) {
            return new ConvertedRegion(minX, minY, minZ, maxX, maxY, maxZ, List.of());
        }

        int bitsPerBlock = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(0, palette.size() - 1)));
        long paletteMask = bitsPerBlock >= Long.SIZE ? -1L : (1L << bitsPerBlock) - 1L;
        List<CompoundTag> blocks = new ArrayList<>();
        for (int index = 0; index < totalBlocks; index++) {
            int paletteIndex = readPackedValue(blockStates, index, bitsPerBlock, paletteMask);
            if (paletteIndex < 0 || paletteIndex >= palette.size()) {
                continue;
            }
            CompoundTag stateTag = palette.getCompound(paletteIndex).copy();
            String blockName = stateTag.getString("Name");
            if (shouldSkipStructureBlock(blockName)) {
                continue;
            }

            int localX = index % sizeX;
            int remainder = index / sizeX;
            int localZ = remainder % sizeZ;
            int localY = remainder / sizeZ;

            int worldX = size[0] >= 0 ? position[0] + localX : position[0] - localX;
            int worldY = size[1] >= 0 ? position[1] + localY : position[1] - localY;
            int worldZ = size[2] >= 0 ? position[2] + localZ : position[2] - localZ;

            CompoundTag blockTag = new CompoundTag();
            blockTag.putInt("x", worldX);
            blockTag.putInt("y", worldY);
            blockTag.putInt("z", worldZ);
            blockTag.putString("block", blockName);
            blockTag.put("state", stateTag);
            blocks.add(blockTag);
        }
        return new ConvertedRegion(minX, minY, minZ, maxX, maxY, maxZ, blocks);
    }

    private static int readPackedValue(long[] blockStates, int index, int bitsPerBlock, long mask) {
        long bitIndex = (long) index * bitsPerBlock;
        int startLong = (int) (bitIndex >>> 6);
        int startOffset = (int) (bitIndex & 63L);
        if (startLong >= blockStates.length) {
            return -1;
        }

        long value = blockStates[startLong] >>> startOffset;
        int bitsRead = Long.SIZE - startOffset;
        if (bitsRead < bitsPerBlock && startLong + 1 < blockStates.length) {
            value |= blockStates[startLong + 1] << bitsRead;
        }
        return (int) (value & mask);
    }

    private static int[] readVector(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_INT_ARRAY)) {
            int[] values = tag.getIntArray(key);
            if (values.length >= 3) {
                return new int[]{values[0], values[1], values[2]};
            }
        }
        CompoundTag vector = tag.getCompound(key);
        return new int[]{vector.getInt("x"), vector.getInt("y"), vector.getInt("z")};
    }

    private static CompoundTag parseBlockStateString(String rawState) {
        String stateString = rawState.trim();
        String blockName = stateString;
        CompoundTag properties = new CompoundTag();
        int propertiesStart = stateString.indexOf('[');
        if (propertiesStart >= 0 && stateString.endsWith("]")) {
            blockName = stateString.substring(0, propertiesStart);
            String propertiesBody = stateString.substring(propertiesStart + 1, stateString.length() - 1);
            if (!propertiesBody.isEmpty()) {
                for (String entry : propertiesBody.split(",")) {
                    int separator = entry.indexOf('=');
                    if (separator <= 0 || separator >= entry.length() - 1) {
                        continue;
                    }
                    properties.putString(entry.substring(0, separator), entry.substring(separator + 1));
                }
            }
        }

        CompoundTag stateTag = new CompoundTag();
        stateTag.putString("Name", blockName);
        if (!properties.isEmpty()) {
            stateTag.put("Properties", properties);
        }
        return stateTag;
    }

    private static boolean shouldSkipStructureBlock(String blockName) {
        return blockName.endsWith(":air") || "minecraft:structure_void".equals(blockName);
    }

    private static int readDimension(CompoundTag tag, String primary, String fallback) {
        if (tag.contains(primary)) {
            return tag.getInt(primary);
        }
        return tag.getInt(fallback);
    }

    private static String stripExtension(String name) {
        for (String extension : supportedExtensions) {
            if (name.endsWith(extension)) {
                return name.substring(0, name.length() - extension.length());
            }
        }
        return name;
    }

    private record ConvertedRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                   List<CompoundTag> blocks) {
    }
}
