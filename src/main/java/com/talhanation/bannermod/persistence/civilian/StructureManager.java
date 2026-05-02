package com.talhanation.bannermod.persistence.civilian;

import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.entity.civilian.workarea.WorkAreaIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StructureManager {

    public static CompoundTag scanStructure(Level level, BuildArea buildArea, String name) {
        CompoundTag root = new CompoundTag();
        ListTag blockList = new ListTag();

        Direction facing = buildArea.getFacing();
        Direction right  = facing.getClockWise();
        BlockPos  origin = buildArea.getOriginPos();
        int width  = buildArea.getWidthSize();
        int height = buildArea.getHeightSize();
        int depth  = buildArea.getDepthSize();

        for (int y = 0; y < height; y++) {
            for (int dz = 0; dz < depth; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    BlockPos offset = origin.relative(facing, dz).relative(right, width - 1 - dx).above(y);
                    BlockState state = level.getBlockState(offset);
                    if (!state.isAir() && !(state.getBlock() instanceof BushBlock)) {
                        CompoundTag blockTag = new CompoundTag();
                        blockTag.putInt("x", dx);
                        blockTag.putInt("y", y);
                        blockTag.putInt("z", dz);
                        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                        blockTag.putString("block", id.toString());
                        blockTag.put("state", NbtUtils.writeBlockState(state));
                        blockList.add(blockTag);
                    }
                }
            }
        }

        ListTag entityList = new ListTag();
        net.minecraft.world.phys.AABB buildBox = buildArea.getArea();
        double queryRadius = Math.sqrt(buildBox.getXsize() * buildBox.getXsize()
                + buildBox.getYsize() * buildBox.getYsize()
                + buildBox.getZsize() * buildBox.getZsize()) / 2.0D;
        List<AbstractWorkAreaEntity> workAreas = WorkAreaIndex.instance().queryInRange(
                level,
                buildBox.getCenter(),
                queryRadius,
                AbstractWorkAreaEntity.class
        );
        RuntimeProfilingCounters.add("structure_scan.work_areas_seen", workAreas.size());
        for (AbstractWorkAreaEntity wa : workAreas) {
            ResourceLocation typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(wa.getType());
            if (typeKey == null || wa instanceof BuildArea) continue;
            BlockPos delta = wa.getOnPos().subtract(origin);
            int relZ = dotHorizontal(delta, facing);
            int relX = width - 1 - dotHorizontal(delta, right);
            int relY = delta.getY();
            if (relX < 0 || relX >= width || relZ < 0 || relZ >= depth || relY < 0 || relY >= height) continue;
            CompoundTag entityTag = new CompoundTag();
            entityTag.putString("entity_type", typeKey.toString());
            entityTag.putInt("x", relX);
            entityTag.putInt("y", relY);
            entityTag.putInt("z", relZ);
            entityTag.putInt("facing", wa.getFacing().get2DDataValue());
            // Store dimensions so they can be restored (with rotation correction) on placement
            entityTag.putInt("wa_width",  wa.getWidthSize());
            entityTag.putInt("wa_height", wa.getHeightSize());
            entityTag.putInt("wa_depth",  wa.getDepthSize());
            entityList.add(entityTag);
        }

        root.putString("name", name);
        root.putInt("width", width);
        root.putInt("height", height);
        root.putInt("depth", depth);
        root.putString("facing", facing.getName());
        root.put("blocks", blockList);
        root.put("entities", entityList);
        return root;
    }

    private static int dotHorizontal(BlockPos pos, Direction dir) {
        return pos.getX() * dir.getStepX() + pos.getZ() * dir.getStepZ();
    }

    public static void saveStructureToFile(String filename, CompoundTag root) {
        // Always store scans as lowercase to stay consistent with ResourceManager requirements
        filename = filename.toLowerCase(java.util.Locale.ROOT);

        File dir = new File(Minecraft.getInstance().gameDirectory, "workers/scan");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, filename.endsWith(".nbt") ? filename : filename + ".nbt");
        root.putString("name", filename);
        try {
            NbtIo.writeCompressed(root, file.toPath());
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Scan saved to: " + file.getPath()), true);
        } catch (IOException e) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Error saving scan: " + e.getMessage()), true);
            e.printStackTrace();
        }
    }
    public static CompoundTag loadScanNbt(String scanRelativePath) {
        String[] parts = scanRelativePath.replace('\\', '/').split("/");
        Path base = Path.of(Minecraft.getInstance().gameDirectory.getAbsolutePath(), "workers", "scan");
        for (int i = 0; i < parts.length - 1; i++) base = base.resolve(parts[i]);
        Path scanFile = base.resolve(parts[parts.length - 1]);
        return StructureTemplateLoader.loadTemplate(scanFile);
    }

    public static void copyDefaultStructuresIfMissing() {
        Path scanRoot = Path.of(
                Minecraft.getInstance().gameDirectory.getAbsolutePath(), "workers", "scan");

        if (!Files.exists(scanRoot)) {
            try { Files.createDirectories(scanRoot); } catch (IOException e) { e.printStackTrace(); return; }
        }

        if (containsStructureTemplates(scanRoot)) return;

        String folderPrefix = WorkersRuntime.mergedStructureRoot().getPath();
        Map<ResourceLocation, Resource> resources = Minecraft.getInstance().getResourceManager()
                .listResources(folderPrefix,
                        rl -> rl.getNamespace().equals(WorkersRuntime.ACTIVE_ASSET_NAMESPACE) && rl.getPath().endsWith(".nbt"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            String relPath = entry.getKey().getPath();
            if (relPath.startsWith(folderPrefix + "/"))
                relPath = relPath.substring(folderPrefix.length() + 1);

            relPath = relPath.toLowerCase(java.util.Locale.ROOT);

            Path destFile = scanRoot.resolve(relPath.replace('/', File.separatorChar));
            try {
                Files.createDirectories(destFile.getParent());
                if (!Files.exists(destFile)) {
                    try (InputStream in = entry.getValue().open()) {
                        Files.copy(in, destFile);
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private static boolean containsStructureTemplates(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    if (containsStructureTemplates(entry)) return true;
                } else if (StructureTemplateLoader.supportedExtensions.stream()
                        .anyMatch(extension -> entry.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(extension))) {
                    return true;
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

    public static BlockState resolveBlockState(CompoundTag blockTag) {
        if (blockTag.contains("state", Tag.TAG_COMPOUND) && !blockTag.getCompound("state").isEmpty()) {
            return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), blockTag.getCompound("state"));
        }

        ResourceLocation blockId = ResourceLocation.tryParse(blockTag.getString("block"));
        if (blockId == null) {
            return null;
        }
        return BuiltInRegistries.BLOCK.getOptional(blockId).map(Block::defaultBlockState).orElse(null);
    }

    public static List<ScannedBlock> parseStructureFromNBT(CompoundTag root) {
        List<ScannedBlock> result = new ArrayList<>();
        WorkersRuntime.migrateStructureNbt(root);
        Direction scanFacing = Direction.byName(root.getString("facing"));
        ListTag blockList = root.getList("blocks", Tag.TAG_COMPOUND);
        for (Tag tag : blockList) {
            CompoundTag blockTag = (CompoundTag) tag;
            BlockState state = resolveBlockState(blockTag);
            if (state == null) {
                continue;
            }
            BlockPos relPos = new BlockPos(blockTag.getInt("x"), blockTag.getInt("y"), blockTag.getInt("z"));
            CompoundTag be = blockTag.contains("blockEntity") ? blockTag.getCompound("blockEntity") : null;
            result.add(new ScannedBlock(state, scanFacing, be, relPos));
        }
        return result;
    }
}
