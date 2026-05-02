package com.talhanation.bannermod.settlement;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementWorkflowVerificationTest {
    private static final Path ROOT = Path.of("");

    @Test
    void importedStructuresUseManualSurveyorMarkingAndOptionalDrafts() throws IOException {
        String surveyorScreen = read("src/main/java/com/talhanation/bannermod/client/civilian/gui/SettlementSurveyorScreen.java");
        String surveyorTool = read("src/main/java/com/talhanation/bannermod/items/civilian/SettlementSurveyorToolItem.java");
        String surveyorService = read("src/main/java/com/talhanation/bannermod/settlement/validation/SettlementSurveyorService.java");
        String surveyorDrafts = read("src/main/java/com/talhanation/bannermod/settlement/validation/SurveyorDraftSuggestionService.java");
        String sessionMessage = read("src/main/java/com/talhanation/bannermod/network/messages/civilian/MessageModifySurveyorSession.java");
        String enGuide = read("MULTIPLAYER_GUIDE_EN.md");
        String ruGuide = read("MULTIPLAYER_GUIDE_RU.md");
        String almanac = read("docs/BANNERMOD_ALMANAC.html");

        assertTrue(surveyorScreen.contains("SUGGEST_DRAFT_ZONES"));
        assertTrue(surveyorScreen.contains("TOGGLE_GUIDE_PREVIEW"));
        assertTrue(surveyorTool.contains("suggestDraftZones(ServerPlayer player, ItemStack stack)"));
        assertTrue(surveyorTool.contains("toggleGuidePreview(ServerPlayer player, ItemStack stack)"));
        assertTrue(sessionMessage.contains("case SUGGEST_DRAFT_ZONES -> SettlementSurveyorToolItem.suggestDraftZones(player, stack)"));
        assertTrue(sessionMessage.contains("case TOGGLE_GUIDE_PREVIEW -> SettlementSurveyorToolItem.toggleGuidePreview(player, stack)"));

        assertTrue(surveyorDrafts.contains("case HOUSE, FARM, MINE, LUMBER_CAMP, SMITHY, STORAGE, ARCHITECT_BUILDER, BARRACKS -> true"));
        assertTrue(surveyorDrafts.contains("return DraftSuggestionResult.applied(session.withSelections(updatedSelections), added);"));

        assertTrue(surveyorService.contains("ValidatedBuildingRegistryData.get(level).registerBuilding(record);"));
        assertTrue(surveyorService.contains("PrefabAutoStaffingRuntime.registerValidatedBuildingVacancy(record);"));
        assertTrue(surveyorService.contains("bannermod.surveyor.validation.registered"));

        assertTrue(enGuide.contains("turn off the canned guide preview"));
        assertTrue(enGuide.contains("helper, not a guaranteed-correct validator"));
        assertTrue(enGuide.contains("does not need any extra metadata file"));
        assertTrue(ruGuide.contains("отключи шаблонную голограмму"));
        assertTrue(ruGuide.contains("Это именно помощник, а не гарантированно точный валидатор"));
        assertTrue(ruGuide.contains("не требует отдельного metadata-файла"));
        assertTrue(almanac.contains("Suggest Draft"));
        assertTrue(almanac.contains("turn off the canned guide preview"));
        assertTrue(almanac.contains("without needing any extra metadata file"));
    }

    @Test
    void gatehousePrefabIsRegisteredAndDocumentedInPlacementFlow() throws IOException {
        String gatehousePrefab = read("src/main/java/com/talhanation/bannermod/settlement/prefab/impl/GatehousePrefab.java");
        String prefabCatalog = read("src/main/java/com/talhanation/bannermod/settlement/prefab/BuildingPrefabCatalog.java");
        String placeBuildingScreen = read("src/main/java/com/talhanation/bannermod/client/civilian/gui/PlaceBuildingScreen.java");
        String enLang = read("src/main/resources/assets/bannermod/lang/en_us.json");
        String ruLang = read("src/main/resources/assets/bannermod/lang/ru_ru.json");
        String enGuide = read("MULTIPLAYER_GUIDE_EN.md");
        String ruGuide = read("MULTIPLAYER_GUIDE_RU.md");
        String almanac = read("docs/BANNERMOD_ALMANAC.html");

        assertTrue(gatehousePrefab.contains("Compact fortified entrance prefab"));
        assertTrue(gatehousePrefab.contains("bannermod.prefab.gatehouse.name"));
        assertTrue(gatehousePrefab.contains("BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, \"bannermod:gatehouse\")"));

        assertTrue(prefabCatalog.contains("registry.register(new GatehousePrefab());"));
        assertTrue(placeBuildingScreen.contains("registry.ensureDefaultsLoaded();"));
        assertTrue(placeBuildingScreen.contains("SettlementOnboardingGuide.placementHint(descriptor)"));

        assertTrue(enLang.contains("bannermod.prefab.gatehouse.name"));
        assertTrue(enLang.contains("bannermod.onboarding.prefab_hint.gatehouse"));
        assertTrue(ruLang.contains("bannermod.prefab.gatehouse.name"));
        assertTrue(ruLang.contains("bannermod.onboarding.prefab_hint.gatehouse"));

        assertTrue(enGuide.contains("Gatehouse"));
        assertTrue(enGuide.contains("roofed twin-tower entrance"));
        assertTrue(ruGuide.contains("Gatehouse"));
        assertTrue(ruGuide.contains("крытый вход с двумя башнями"));
        assertTrue(almanac.contains("Gatehouse"));
        assertTrue(almanac.contains("wand-only prefab"));
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }
}
