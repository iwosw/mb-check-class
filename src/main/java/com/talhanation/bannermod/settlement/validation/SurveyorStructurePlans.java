package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ZoneRole;

import java.util.List;

public final class SurveyorStructurePlans {
    private static final int WOOD = 0xFFD8A55A;
    private static final int DARK_WOOD = 0xFFB8793C;
    private static final int STONE = 0xFF95A1B3;
    private static final int FIELD = 0xFF83D36B;
    private static final int WATER = 0xFF7FD7FF;
    private static final int METAL = 0xFFF0C06A;
    private static final int STORAGE = 0xFFCDA86B;
    private static final int SHAFT = 0xFF6F7A88;

    private static final SurveyorPlan HOUSE = new SurveyorPlan(
            List.of(
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, -3, 3, 3, -3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, 3, -1, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, 3, 3, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, -3, -3, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(3, 1, -3, 3, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 4, -3, 3, 4, 3), DARK_WOOD, 0.66F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-1, 3, 3, 1, 3, 3), METAL, 0.82F)
            ),
            List.of(
                    new GuideBox(ZoneRole.INTERIOR, new StarterFortPlan.RelativeBox(-2, 1, -2, 2, 2, 2), 0.38F),
                    new GuideBox(ZoneRole.SLEEPING, new StarterFortPlan.RelativeBox(-1, 1, -2, 1, 1, -1), 0.44F)
            )
    );

    private static final SurveyorPlan FARM = new SurveyorPlan(
            List.of(
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, -2, 2, 3, -2), WOOD, 0.70F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, 1, -1, 3, 1), WOOD, 0.70F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, 1, 2, 3, 1), WOOD, 0.70F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, -2, -2, 3, 1), WOOD, 0.70F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(2, 1, -2, 2, 3, 1), WOOD, 0.70F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 4, -2, 2, 4, 1), DARK_WOOD, 0.66F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-5, 1, 3, 5, 1, 11), FIELD, 0.58F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-1, 0, 6, 1, 0, 8), WATER, 0.56F)
            ),
            List.of(
                    new GuideBox(ZoneRole.WORK_ZONE, new StarterFortPlan.RelativeBox(-4, 0, 4, 4, 1, 10), 0.36F)
            )
    );

    private static final SurveyorPlan MINE = new SurveyorPlan(
            List.of(
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, 0, 2, 3, 0), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, 4, -1, 3, 4), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, 4, 2, 3, 4), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, 0, -2, 3, 4), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(2, 1, 0, 2, 3, 4), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 4, 0, 2, 4, 4), DARK_WOOD, 0.66F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-1, 1, -1, -1, 3, 0), METAL, 0.82F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, -1, 1, 3, 0), METAL, 0.82F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-1, 3, -1, 1, 3, -1), METAL, 0.82F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 0, -7, 3, 3, -3), SHAFT, 0.50F)
            ),
            List.of(
                    new GuideBox(ZoneRole.WORK_ZONE, new StarterFortPlan.RelativeBox(-3, 0, -7, 3, 3, -3), 0.38F)
            )
    );

    private static final SurveyorPlan LUMBER_CAMP = new SurveyorPlan(
            List.of(
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, -2, 3, 3, 2), WOOD, 0.70F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 4, -2, 3, 4, 2), DARK_WOOD, 0.64F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, 4, 2, 2, 6), STORAGE, 0.68F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-5, 0, 4, 5, 4, 14), FIELD, 0.42F)
            ),
            List.of(
                    new GuideBox(ZoneRole.WORK_ZONE, new StarterFortPlan.RelativeBox(-5, 0, 4, 5, 4, 14), 0.34F)
            )
    );

    private static final SurveyorPlan SMITHY = new SurveyorPlan(
            List.of(
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, -3, 3, 3, -3), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, 3, -1, 3, 3), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, 3, 3, 3, 3), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, -3, -3, 3, 3), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(3, 1, -3, 3, 3, 3), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 4, -3, 3, 4, 3), DARK_WOOD, 0.64F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, -2, -1, 2, -1), 0xFFFFB75C, 0.78F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, -2, 1, 1, -1), METAL, 0.82F)
            ),
            List.of(
                    new GuideBox(ZoneRole.INTERIOR, new StarterFortPlan.RelativeBox(-2, 1, -2, 2, 2, 2), 0.36F),
                    new GuideBox(ZoneRole.WORK_ZONE, new StarterFortPlan.RelativeBox(-2, 1, -2, 2, 1, 0), 0.42F)
            )
    );

    private static final SurveyorPlan STORAGE_PLAN = new SurveyorPlan(
            List.of(
                    new PreviewBox(new StarterFortPlan.RelativeBox(-4, 1, -3, 4, 3, -3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-4, 1, 3, -1, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, 3, 4, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-4, 1, -3, -4, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(4, 1, -3, 4, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-4, 4, -3, 4, 4, 3), DARK_WOOD, 0.64F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-2, 1, -1, -1, 2, 1), STORAGE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, -1, 2, 2, 1), STORAGE, 0.72F)
            ),
            List.of(
                    new GuideBox(ZoneRole.STORAGE, new StarterFortPlan.RelativeBox(-3, 1, -2, 3, 2, 2), 0.38F)
            )
    );

    private static final SurveyorPlan ARCHITECT = new SurveyorPlan(
            List.of(
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, -3, 3, 3, -3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, 3, -1, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(1, 1, 3, 3, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, -3, -3, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(3, 1, -3, 3, 3, 3), WOOD, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 4, -3, 3, 4, 3), DARK_WOOD, 0.66F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-1, 1, -2, 1, 1, -1), METAL, 0.80F)
            ),
            List.of(
                    new GuideBox(ZoneRole.INTERIOR, new StarterFortPlan.RelativeBox(-2, 1, -2, 2, 2, 2), 0.36F),
                    new GuideBox(ZoneRole.WORK_ZONE, new StarterFortPlan.RelativeBox(-1, 1, -2, 1, 1, -1), 0.42F)
            )
    );

    private static final SurveyorPlan BARRACKS = new SurveyorPlan(
            List.of(
                    new PreviewBox(new StarterFortPlan.RelativeBox(-5, 1, -4, 5, 3, -4), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-5, 1, 4, -2, 3, 4), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(2, 1, 4, 5, 3, 4), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-5, 1, -4, -5, 3, 4), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(5, 1, -4, 5, 3, 4), STONE, 0.72F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-5, 4, -4, 5, 4, 4), DARK_WOOD, 0.66F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(-3, 1, 1, -2, 2, 3), STORAGE, 0.70F),
                    new PreviewBox(new StarterFortPlan.RelativeBox(2, 1, 1, 3, 2, 3), STORAGE, 0.70F)
            ),
            List.of(
                    new GuideBox(ZoneRole.INTERIOR, new StarterFortPlan.RelativeBox(-4, 1, -3, 4, 2, 3), 0.34F),
                    new GuideBox(ZoneRole.SLEEPING, new StarterFortPlan.RelativeBox(-3, 1, -2, 3, 1, 0), 0.42F),
                    new GuideBox(ZoneRole.STORAGE, new StarterFortPlan.RelativeBox(-3, 1, 1, 3, 2, 3), 0.36F)
            )
    );

    private SurveyorStructurePlans() {
    }

    public static SurveyorPlan planFor(SurveyorMode mode) {
        return switch (mode == null ? SurveyorMode.BOOTSTRAP_FORT : mode) {
            case BOOTSTRAP_FORT, INSPECT_EXISTING -> null;
            case HOUSE -> HOUSE;
            case FARM -> FARM;
            case MINE -> MINE;
            case LUMBER_CAMP -> LUMBER_CAMP;
            case SMITHY -> SMITHY;
            case STORAGE -> STORAGE_PLAN;
            case ARCHITECT_BUILDER -> ARCHITECT;
            case BARRACKS -> BARRACKS;
        };
    }

    public record SurveyorPlan(List<PreviewBox> previewBoxes, List<GuideBox> guideBoxes) {
    }

    public record PreviewBox(StarterFortPlan.RelativeBox box, int color, float alpha) {
    }

    public record GuideBox(ZoneRole role, StarterFortPlan.RelativeBox box, float alpha) {
    }
}
