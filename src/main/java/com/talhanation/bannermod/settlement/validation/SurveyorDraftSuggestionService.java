package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SurveyorDraftSuggestionService {
    private SurveyorDraftSuggestionService() {
    }

    public static DraftSuggestionResult suggest(BlockGetter level, ValidationSession session) {
        return suggest(session, new LevelBlockAccess(level));
    }

    static DraftSuggestionResult suggest(ValidationSession session, SampledBlockAccess blocks) {
        if (session == null || !supportsDraftSuggestion(session.mode())) {
            return DraftSuggestionResult.unsupported(session);
        }
        if (session.anchorPos().equals(BlockPos.ZERO)) {
            return DraftSuggestionResult.noAnchor(session);
        }

        List<ZoneSelection> updatedSelections = new ArrayList<>(session.selections());
        int added = switch (session.mode()) {
            case HOUSE -> suggestHouse(blocks, session.anchorPos(), updatedSelections);
            case FARM -> suggestFarm(blocks, session.anchorPos(), updatedSelections);
            case MINE -> suggestMine(blocks, session.anchorPos(), updatedSelections);
            case LUMBER_CAMP -> suggestLumberCamp(blocks, session.anchorPos(), updatedSelections);
            case SMITHY -> suggestSmithy(blocks, session.anchorPos(), updatedSelections);
            case STORAGE -> suggestStorage(blocks, session.anchorPos(), updatedSelections);
            case ARCHITECT_BUILDER -> suggestArchitectBuilder(blocks, session.anchorPos(), updatedSelections);
            case BARRACKS -> suggestBarracks(blocks, session.anchorPos(), updatedSelections);
            default -> 0;
        };
        if (added <= 0) {
            return DraftSuggestionResult.noMatches(session);
        }
        return DraftSuggestionResult.applied(session.withSelections(updatedSelections), added);
    }

    public static boolean supportsDraftSuggestion(@Nullable SurveyorMode mode) {
        return switch (mode == null ? SurveyorMode.BOOTSTRAP_FORT : mode) {
            case HOUSE, FARM, MINE, LUMBER_CAMP, SMITHY, STORAGE, ARCHITECT_BUILDER, BARRACKS -> true;
            default -> false;
        };
    }

    private static int suggestHouse(SampledBlockAccess blocks, BlockPos anchor, List<ZoneSelection> selections) {
        int added = 0;
        ZoneSelection sleeping = selectionForRole(selections, ZoneRole.SLEEPING);
        if (sleeping == null) {
            sleeping = fromCluster(ZoneRole.SLEEPING, anchor, cluster(anchor, collect(blocks, anchor, 14, 6, pos -> isBed(blocks.blockId(pos))), 4), 1, 0, 1);
            added += addIfMissing(selections, sleeping);
        }
        if (selectionForRole(selections, ZoneRole.INTERIOR) == null && sleeping != null) {
            added += addIfMissing(selections, expandSelection(ZoneRole.INTERIOR, sleeping, 2, 2, 2));
        }
        return added;
    }

    private static int suggestFarm(SampledBlockAccess blocks, BlockPos anchor, List<ZoneSelection> selections) {
        return addIfMissing(selections, fromCluster(ZoneRole.WORK_ZONE, anchor,
                cluster(anchor, collect(blocks, anchor, 18, 4, pos -> isFarmBlock(blocks.blockId(pos))), 4), 1, 0, 1));
    }

    private static int suggestMine(SampledBlockAccess blocks, BlockPos anchor, List<ZoneSelection> selections) {
        return addIfMissing(selections, fromCluster(ZoneRole.WORK_ZONE, anchor,
                cluster(anchor, collect(blocks, anchor, 20, 8, pos -> isMineFace(blocks, pos)), 3), 1, 1, 1));
    }

    private static int suggestLumberCamp(SampledBlockAccess blocks, BlockPos anchor, List<ZoneSelection> selections) {
        return addIfMissing(selections, fromCluster(ZoneRole.WORK_ZONE, anchor,
                cluster(anchor, collect(blocks, anchor, 18, 6, pos -> isLumberBlock(blocks.blockId(pos))), 4), 1, 0, 1));
    }

    private static int suggestSmithy(SampledBlockAccess blocks, BlockPos anchor, List<ZoneSelection> selections) {
        int added = 0;
        ZoneSelection workZone = selectionForRole(selections, ZoneRole.WORK_ZONE);
        if (workZone == null) {
            workZone = fromCluster(ZoneRole.WORK_ZONE, anchor,
                    cluster(anchor, collect(blocks, anchor, 12, 5, pos -> isSmithyWorkBlock(blocks.blockId(pos))), 4), 1, 1, 1);
            added += addIfMissing(selections, workZone);
        }
        if (selectionForRole(selections, ZoneRole.INTERIOR) == null && workZone != null) {
            added += addIfMissing(selections, expandSelection(ZoneRole.INTERIOR, workZone, 2, 2, 2));
        }
        return added;
    }

    private static int suggestStorage(SampledBlockAccess blocks, BlockPos anchor, List<ZoneSelection> selections) {
        return addIfMissing(selections, fromCluster(ZoneRole.STORAGE, anchor,
                cluster(anchor, collect(blocks, anchor, 12, 5, pos -> isStorageBlock(blocks.blockId(pos))), 4), 1, 1, 1));
    }

    private static int suggestArchitectBuilder(SampledBlockAccess blocks, BlockPos anchor, List<ZoneSelection> selections) {
        int added = 0;
        ZoneSelection workZone = selectionForRole(selections, ZoneRole.WORK_ZONE);
        if (workZone == null) {
            workZone = fromCluster(ZoneRole.WORK_ZONE, anchor,
                    cluster(anchor, collect(blocks, anchor, 12, 5, pos -> isBuilderWorkBlock(blocks.blockId(pos))), 4), 1, 1, 1);
            added += addIfMissing(selections, workZone);
        }
        if (selectionForRole(selections, ZoneRole.INTERIOR) == null && workZone != null) {
            added += addIfMissing(selections, expandSelection(ZoneRole.INTERIOR, workZone, 2, 2, 2));
        }
        return added;
    }

    private static int suggestBarracks(SampledBlockAccess blocks, BlockPos anchor, List<ZoneSelection> selections) {
        int added = 0;
        ZoneSelection sleeping = selectionForRole(selections, ZoneRole.SLEEPING);
        if (sleeping == null) {
            sleeping = fromCluster(ZoneRole.SLEEPING, anchor, cluster(anchor, collect(blocks, anchor, 16, 6, pos -> isBed(blocks.blockId(pos))), 4), 1, 0, 1);
            added += addIfMissing(selections, sleeping);
        }
        if (selectionForRole(selections, ZoneRole.STORAGE) == null) {
            added += addIfMissing(selections, fromCluster(ZoneRole.STORAGE, anchor,
                    cluster(anchor, collect(blocks, anchor, 16, 6, pos -> isStorageBlock(blocks.blockId(pos))), 4), 1, 1, 1));
        }
        if (selectionForRole(selections, ZoneRole.INTERIOR) == null && sleeping != null) {
            added += addIfMissing(selections, expandSelection(ZoneRole.INTERIOR, sleeping, 3, 2, 3));
        }
        return added;
    }

    private static int addIfMissing(List<ZoneSelection> selections, @Nullable ZoneSelection suggestion) {
        if (suggestion == null || selectionForRole(selections, suggestion.role()) != null) {
            return 0;
        }
        selections.add(suggestion);
        return 1;
    }

    private static @Nullable ZoneSelection selectionForRole(List<ZoneSelection> selections, ZoneRole role) {
        for (ZoneSelection selection : selections) {
            if (selection.role() == role) {
                return selection;
            }
        }
        return null;
    }

    private static @Nullable ZoneSelection fromCluster(ZoneRole role, BlockPos anchor, List<BlockPos> cluster, int padX, int padY, int padZ) {
        if (cluster.isEmpty()) {
            return null;
        }
        int minX = anchor.getX();
        int minY = anchor.getY();
        int minZ = anchor.getZ();
        int maxX = anchor.getX();
        int maxY = anchor.getY();
        int maxZ = anchor.getZ();
        BlockPos marker = anchor;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : cluster) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
            double distance = pos.distSqr(anchor);
            if (distance < bestDistance) {
                bestDistance = distance;
                marker = pos;
            }
        }
        return new ZoneSelection(role,
                new BlockPos(minX - padX, minY - padY, minZ - padZ),
                new BlockPos(maxX + padX, maxY + padY, maxZ + padZ),
                marker);
    }

    private static ZoneSelection expandSelection(ZoneRole role, ZoneSelection selection, int padX, int padY, int padZ) {
        int minX = Math.min(selection.min().getX(), selection.max().getX()) - padX;
        int minY = Math.min(selection.min().getY(), selection.max().getY()) - padY;
        int minZ = Math.min(selection.min().getZ(), selection.max().getZ()) - padZ;
        int maxX = Math.max(selection.min().getX(), selection.max().getX()) + padX;
        int maxY = Math.max(selection.min().getY(), selection.max().getY()) + padY;
        int maxZ = Math.max(selection.min().getZ(), selection.max().getZ()) + padZ;
        return new ZoneSelection(role, new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), selection.marker());
    }

    private static List<BlockPos> collect(SampledBlockAccess blocks, BlockPos anchor, int horizontalRadius, int verticalRadius, PositionPredicate predicate) {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = anchor.getX() - horizontalRadius; x <= anchor.getX() + horizontalRadius; x++) {
            for (int y = anchor.getY() - verticalRadius; y <= anchor.getY() + verticalRadius; y++) {
                for (int z = anchor.getZ() - horizontalRadius; z <= anchor.getZ() + horizontalRadius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (predicate.test(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    private static List<BlockPos> cluster(BlockPos anchor, List<BlockPos> candidates, int gap) {
        if (candidates.isEmpty()) {
            return List.of();
        }
        BlockPos seed = candidates.getFirst();
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : candidates) {
            double distance = candidate.distSqr(anchor);
            if (distance < bestDistance) {
                bestDistance = distance;
                seed = candidate;
            }
        }

        List<BlockPos> selected = new ArrayList<>();
        Set<BlockPos> remaining = new HashSet<>(candidates);
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);
        remaining.remove(seed);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            selected.add(current);
            List<BlockPos> newlyConnected = new ArrayList<>();
            for (BlockPos candidate : remaining) {
                if (Math.abs(candidate.getX() - current.getX()) <= gap
                        && Math.abs(candidate.getY() - current.getY()) <= gap
                        && Math.abs(candidate.getZ() - current.getZ()) <= gap) {
                    newlyConnected.add(candidate);
                }
            }
            for (BlockPos candidate : newlyConnected) {
                remaining.remove(candidate);
                queue.addLast(candidate);
            }
        }
        return selected;
    }

    private static boolean isBed(String blockId) {
        return blockId.contains("_bed");
    }

    private static boolean isFarmBlock(String blockId) {
        return containsAny(blockId, "farmland", "wheat", "carrots", "potatoes", "beetroots", "melon_stem", "pumpkin_stem", "water");
    }

    private static boolean isMineFace(SampledBlockAccess blocks, BlockPos pos) {
        if (!isMineMaterial(blocks.blockId(pos))) {
            return false;
        }
        return blocks.isAir(pos.north()) || blocks.isAir(pos.south()) || blocks.isAir(pos.east()) || blocks.isAir(pos.west());
    }

    private static boolean isMineMaterial(String blockId) {
        return containsAny(blockId, "stone", "deepslate", "cobblestone", "cobbled_deepslate", "granite", "diorite", "andesite", "ore");
    }

    private static boolean isLumberBlock(String blockId) {
        return containsAny(blockId, "_log", "_wood", "_sapling");
    }

    private static boolean isSmithyWorkBlock(String blockId) {
        return containsAny(blockId, "furnace", "anvil", "smithing_table");
    }

    private static boolean isBuilderWorkBlock(String blockId) {
        return isSmithyWorkBlock(blockId) || blockId.contains("crafting_table");
    }

    private static boolean isStorageBlock(String blockId) {
        return containsAny(blockId, "chest", "barrel");
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private record LevelBlockAccess(BlockGetter level) implements SampledBlockAccess {
        @Override
        public String blockId(BlockPos pos) {
            String descriptionId = level.getBlockState(pos).getBlock().getDescriptionId();
            return descriptionId == null ? "" : descriptionId.toLowerCase(Locale.ROOT);
        }

        @Override
        public boolean isAir(BlockPos pos) {
            return level.getBlockState(pos).isAir();
        }
    }

    interface SampledBlockAccess {
        String blockId(BlockPos pos);

        boolean isAir(BlockPos pos);
    }

    @FunctionalInterface
    private interface PositionPredicate {
        boolean test(BlockPos pos);
    }

    public record DraftSuggestionResult(Status status, ValidationSession session, int addedZones) {
        private static DraftSuggestionResult unsupported(ValidationSession session) {
            return new DraftSuggestionResult(Status.UNSUPPORTED_MODE, session, 0);
        }

        private static DraftSuggestionResult noAnchor(ValidationSession session) {
            return new DraftSuggestionResult(Status.MISSING_ANCHOR, session, 0);
        }

        private static DraftSuggestionResult noMatches(ValidationSession session) {
            return new DraftSuggestionResult(Status.NO_MATCHES, session, 0);
        }

        private static DraftSuggestionResult applied(ValidationSession session, int addedZones) {
            return new DraftSuggestionResult(Status.APPLIED, session, addedZones);
        }
    }

    public enum Status {
        APPLIED,
        NO_MATCHES,
        MISSING_ANCHOR,
        UNSUPPORTED_MODE
    }
}
