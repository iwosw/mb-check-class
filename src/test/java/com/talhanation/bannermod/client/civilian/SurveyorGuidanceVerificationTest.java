package com.talhanation.bannermod.client.civilian;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SurveyorGuidanceVerificationTest {
    private static final Path ROOT = Path.of("");

    @Test
    void fortSurveyGuidanceExplainsSingleInteriorZoneAndColorLegend() throws IOException {
        String selectionRender = read("src/main/java/com/talhanation/bannermod/client/civilian/render/SettlementSurveyorSelectionRenderEvents.java");
        String hudRender = read("src/main/java/com/talhanation/bannermod/client/civilian/render/SettlementSurveyorGuidanceRenderEvents.java");
        String surveyorScreen = read("src/main/java/com/talhanation/bannermod/client/civilian/gui/SettlementSurveyorScreen.java");
        String modeGuidance = read("src/main/java/com/talhanation/bannermod/settlement/validation/SurveyorModeGuidance.java");
        String enLang = read("src/main/resources/assets/bannermod/lang/en_us.json");
        String ruLang = read("src/main/resources/assets/bannermod/lang/ru_ru.json");
        String enGuide = read("MULTIPLAYER_GUIDE_EN.md");
        String ruGuide = read("MULTIPLAYER_GUIDE_RU.md");
        String almanac = read("docs/BANNERMOD_ALMANAC.html");

        assertTrue(selectionRender.contains("SurveyorZonePalette.rgb(selection.role())"));
        assertTrue(hudRender.contains("bannermod.surveyor.hud.fort_legend"));
        assertTrue(hudRender.contains("context.showGuidePreview()"));
        assertTrue(surveyorScreen.contains("TOGGLE_GUIDE_PREVIEW"));
        assertTrue(surveyorScreen.contains("SUGGEST_DRAFT_ZONES"));
        assertTrue(modeGuidance.contains("bannermod.surveyor.role_blocks.bootstrap_fort.interior"));
        assertTrue(enLang.contains("Suggest Draft"));
        assertTrue(ruLang.contains("Черновик по подсказкам"));
        assertTrue(enLang.contains("split fort interior zones are not supported yet"));
        assertTrue(ruLang.contains("Несколько отдельных INTERIOR-зон"));
        assertTrue(enGuide.contains("Color key: gold/wood lines show walls and towers"));
        assertTrue(enGuide.contains("Suggest Draft"));
        assertTrue(enGuide.contains("helper, not a guaranteed-correct validator"));
        assertTrue(enGuide.contains("turn off the canned guide preview"));
        assertTrue(ruGuide.contains("Черновик по подсказкам"));
        assertTrue(ruGuide.contains("Это помощник, а не гарантированно точный валидатор"));
        assertTrue(ruGuide.contains("отключить шаблонную голограмму"));
        assertTrue(ruGuide.contains("Цвета читаются так"));
        assertTrue(almanac.contains("split fort interior zones are not supported yet"));
        assertTrue(almanac.contains("helper, not a guaranteed-correct validator"));
        assertTrue(almanac.contains("turn off the canned guide preview"));
        assertTrue(almanac.contains("Несколько отдельных INTERIOR-зон пока не поддерживаются"));
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }
}
