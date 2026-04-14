package com.talhanation.bannerlord.persistence.civilian;

import com.talhanation.bannerlord.compat.workers.WorkersRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class StructureTemplateLoader {

    public static final List<String> supportedExtensions = List.of(".nbt", ".schem", ".schematic");

    public static CompoundTag loadTemplate(Path templatePath) {
        return loadTemplate(templatePath, WorkersRuntime::migrateStructureNbt);
    }

    public static CompoundTag loadTemplate(Path templatePath, Consumer<CompoundTag> migrator) {
        String fileName = templatePath.getFileName().toString().toLowerCase(Locale.ROOT);
        try {
            CompoundTag root;
            if (fileName.endsWith(".nbt")) {
                try (InputStream input = Files.newInputStream(templatePath)) {
                    root = NbtIo.readCompressed(input);
                }
            } else if (fileName.endsWith(".schem") || fileName.endsWith(".schematic")) {
                try (InputStream input = Files.newInputStream(templatePath)) {
                    root = convertSchematic(NbtIo.readCompressed(input), stripExtension(fileName));
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
        Map<Integer, String> paletteById = new TreeMap<>();
        for (String blockName : palette.getAllKeys()) {
            paletteById.put(palette.getInt(blockName), blockName);
        }

        ListTag blocks = new ListTag();
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    if (index >= blockData.length) {
                        break;
                    }
                    String blockName = paletteById.get(Byte.toUnsignedInt(blockData[index++]));
                    if (blockName == null || blockName.endsWith(":air")) {
                        continue;
                    }
                    CompoundTag blockTag = new CompoundTag();
                    blockTag.putInt("x", x);
                    blockTag.putInt("y", y);
                    blockTag.putInt("z", z);
                    blockTag.putString("block", blockName);
                    blockTag.put("state", new CompoundTag());
                    blocks.add(blockTag);
                }
            }
        }
        root.put("blocks", blocks);
        return root;
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
}
