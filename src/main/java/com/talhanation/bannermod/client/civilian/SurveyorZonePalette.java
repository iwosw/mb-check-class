package com.talhanation.bannermod.client.civilian;

import com.talhanation.bannermod.settlement.building.ZoneRole;

public final class SurveyorZonePalette {
    private SurveyorZonePalette() {
    }

    public static int color(ZoneRole role) {
        ZoneRole resolvedRole = role == null ? ZoneRole.INTERIOR : role;
        return switch (resolvedRole) {
            case AUTHORITY_POINT -> 0xFFFFB13B;
            case INTERIOR -> 0xFF7FD7FF;
            case SLEEPING -> 0xFFB07B46;
            case WORK_ZONE -> 0xFF83D36B;
            case FORT_PERIMETER -> 0xFFD8A55A;
            case ENTRANCE -> 0xFFFFE2A6;
            case STORAGE -> 0xFFC58A54;
            case PREFAB_FOOTPRINT -> 0xFFE5E5E5;
        };
    }

    public static float[] rgb(ZoneRole role) {
        return rgb(color(role));
    }

    public static float[] rgb(int argb) {
        return new float[]{((argb >> 16) & 0xFF) / 255.0F, ((argb >> 8) & 0xFF) / 255.0F, (argb & 0xFF) / 255.0F};
    }
}
