package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ZoneRole;

import javax.annotation.Nullable;
import java.util.List;

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
}
