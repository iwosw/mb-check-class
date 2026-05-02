package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class SurveyorModeGuidance {
    private SurveyorModeGuidance() {
    }

    public static List<ZoneRole> requiredRoles(SurveyorMode mode) {
        return switch (mode == null ? SurveyorMode.BOOTSTRAP_FORT : mode) {
            case BOOTSTRAP_FORT -> List.of(ZoneRole.AUTHORITY_POINT, ZoneRole.INTERIOR);
            case HOUSE -> List.of(ZoneRole.INTERIOR, ZoneRole.SLEEPING);
            case FARM, MINE, LUMBER_CAMP -> List.of(ZoneRole.WORK_ZONE);
            case SMITHY, ARCHITECT_BUILDER -> List.of(ZoneRole.INTERIOR, ZoneRole.WORK_ZONE);
            case STORAGE -> List.of(ZoneRole.STORAGE);
            case BARRACKS -> List.of(ZoneRole.INTERIOR, ZoneRole.SLEEPING, ZoneRole.STORAGE);
            case INSPECT_EXISTING -> List.of();
        };
    }

    public static ZoneRole defaultRole(SurveyorMode mode) {
        List<ZoneRole> roles = requiredRoles(mode);
        return roles.isEmpty() ? ZoneRole.INTERIOR : roles.get(0);
    }

    public static @Nullable ZoneRole nextMissingRole(SurveyorMode mode, @Nullable ValidationSession session) {
        if (session == null) {
            List<ZoneRole> roles = requiredRoles(mode);
            return roles.isEmpty() ? null : roles.get(0);
        }
        for (ZoneRole role : requiredRoles(mode)) {
            boolean captured = session.selections().stream().anyMatch(selection -> selection.role() == role);
            if (!captured) {
                return role;
            }
        }
        return null;
    }

    public static ValidationSession normalizeSession(@Nullable ValidationSession session) {
        if (session == null) {
            return null;
        }
        List<ZoneSelection> normalized = normalizeSelections(session.mode(), session.selections());
        return normalized.equals(session.selections()) ? session : session.withSelections(normalized);
    }

    public static List<ZoneSelection> normalizeSelections(SurveyorMode mode, List<ZoneSelection> selections) {
        if (selections == null || selections.isEmpty()) {
            return List.of();
        }
        return selections.stream().map(selection -> normalizeSelection(mode, selection)).collect(Collectors.toList());
    }

    public static ZoneSelection normalizeSelection(SurveyorMode mode, @Nullable ZoneSelection selection) {
        if (selection == null) {
            return null;
        }
        if (selection.role() != ZoneRole.INTERIOR || !usesWalkableInterior(mode)) {
            return selection;
        }
        int minX = Math.min(selection.min().getX(), selection.max().getX());
        int minY = Math.min(selection.min().getY(), selection.max().getY());
        int minZ = Math.min(selection.min().getZ(), selection.max().getZ());
        int maxX = Math.max(selection.min().getX(), selection.max().getX());
        int maxY = Math.max(selection.min().getY(), selection.max().getY());
        int maxZ = Math.max(selection.min().getZ(), selection.max().getZ());
        if (maxY - minY >= 2) {
            return selection;
        }
        ZoneSelection normalized = new ZoneSelection(
                selection.role(),
                new BlockPos(minX, minY, minZ),
                new BlockPos(maxX, minY + 2, maxZ),
                selection.marker()
        );
        return normalized;
    }

    public static Component rolePurpose(ZoneRole role) {
        ZoneRole resolvedRole = role == null ? ZoneRole.INTERIOR : role;
        return Component.translatable("bannermod.surveyor.role_purpose." + resolvedRole.name().toLowerCase(Locale.ROOT));
    }

    public static Component roleBuildHint(SurveyorMode mode, ZoneRole role) {
        return Component.translatable(roleBuildHintKey(mode, role));
    }

    private static boolean usesWalkableInterior(SurveyorMode mode) {
        return switch (mode == null ? SurveyorMode.BOOTSTRAP_FORT : mode) {
            case BOOTSTRAP_FORT, HOUSE, SMITHY, ARCHITECT_BUILDER, BARRACKS -> true;
            default -> false;
        };
    }

    private static String roleBuildHintKey(SurveyorMode mode, ZoneRole role) {
        SurveyorMode resolvedMode = mode == null ? SurveyorMode.BOOTSTRAP_FORT : mode;
        ZoneRole resolvedRole = role == null ? ZoneRole.INTERIOR : role;
        if (resolvedRole == ZoneRole.WORK_ZONE) {
            return switch (resolvedMode) {
                case FARM -> "bannermod.surveyor.role_blocks.farm.work_zone";
                case MINE -> "bannermod.surveyor.role_blocks.mine.work_zone";
                case LUMBER_CAMP -> "bannermod.surveyor.role_blocks.lumber_camp.work_zone";
                case SMITHY -> "bannermod.surveyor.role_blocks.smithy.work_zone";
                case ARCHITECT_BUILDER -> "bannermod.surveyor.role_blocks.architect_builder.work_zone";
                default -> "bannermod.surveyor.role_blocks.work_zone";
            };
        }
        if (resolvedMode == SurveyorMode.BOOTSTRAP_FORT && resolvedRole == ZoneRole.INTERIOR) {
            return "bannermod.surveyor.role_blocks.bootstrap_fort.interior";
        }
        return switch (resolvedRole) {
            case AUTHORITY_POINT -> "bannermod.surveyor.role_blocks.authority_point";
            case INTERIOR -> "bannermod.surveyor.role_blocks.interior";
            case SLEEPING -> "bannermod.surveyor.role_blocks.sleeping";
            case FORT_PERIMETER -> "bannermod.surveyor.role_blocks.fort_perimeter";
            case ENTRANCE -> "bannermod.surveyor.role_blocks.entrance";
            case STORAGE -> "bannermod.surveyor.role_blocks.storage";
            case PREFAB_FOOTPRINT -> "bannermod.surveyor.role_blocks.prefab_footprint";
            case WORK_ZONE -> "bannermod.surveyor.role_blocks.work_zone";
        };
    }
}
