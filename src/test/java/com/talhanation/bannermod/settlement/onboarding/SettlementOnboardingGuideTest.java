package com.talhanation.bannermod.settlement.onboarding;

import com.talhanation.bannermod.settlement.building.BuildingType;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabDescriptor;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabProfession;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SettlementOnboardingGuideTest {
    @Test
    void nextTrackedStepStartsWithStorageThenFarm() {
        Map<BuildingType, Integer> counts = new EnumMap<>(BuildingType.class);

        assertEquals("bannermod.onboarding.next.storage", SettlementOnboardingGuide.nextTrackedStepKey(counts));

        counts.put(BuildingType.STORAGE, 1);
        assertEquals("bannermod.onboarding.next.farm", SettlementOnboardingGuide.nextTrackedStepKey(counts));
    }

    @Test
    void nextTrackedStepMovesIntoHousingThenProfessionExpansion() {
        Map<BuildingType, Integer> counts = new EnumMap<>(BuildingType.class);
        counts.put(BuildingType.STORAGE, 1);
        counts.put(BuildingType.FARM, 1);

        assertEquals("bannermod.onboarding.next.house", SettlementOnboardingGuide.nextTrackedStepKey(counts));

        counts.put(BuildingType.HOUSE, 1);
        assertEquals("bannermod.onboarding.next.professions", SettlementOnboardingGuide.nextTrackedStepKey(counts));
    }

    @Test
    void nextTrackedStepFallsBackToMarketReminderAfterTrackedLoop() {
        Map<BuildingType, Integer> counts = new EnumMap<>(BuildingType.class);
        counts.put(BuildingType.STORAGE, 1);
        counts.put(BuildingType.FARM, 1);
        counts.put(BuildingType.HOUSE, 2);
        counts.put(BuildingType.MINE, 1);
        counts.put(BuildingType.LUMBER_CAMP, 1);
        counts.put(BuildingType.ARCHITECT_WORKSHOP, 1);

        assertEquals("bannermod.onboarding.next.market", SettlementOnboardingGuide.nextTrackedStepKey(counts));
    }

    @Test
    void professionLabelUsesExpectedTranslationKeys() {
        assertEquals("bannermod.prefab.profession.none", translationKey(SettlementOnboardingGuide.professionLabel(null)));
        assertEquals("bannermod.prefab.profession.farmer", translationKey(SettlementOnboardingGuide.professionLabel(BuildingPrefabProfession.FARMER)));
        assertEquals("bannermod.prefab.profession.recruit_crossbow", translationKey(SettlementOnboardingGuide.professionLabel(BuildingPrefabProfession.RECRUIT_CROSSBOW)));
    }

    @Test
    void placementHintUsesSpecificKeysForKnownPrefabsAndGenericFallback() {
        assertEquals("bannermod.onboarding.prefab_hint.storage",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("storage", BuildingPrefabProfession.NONE))));
        assertEquals("bannermod.onboarding.prefab_hint.farm",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("farm", BuildingPrefabProfession.FARMER))));
        assertEquals("bannermod.onboarding.prefab_hint.market_stall",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("market_stall", BuildingPrefabProfession.MERCHANT))));
        assertEquals("bannermod.onboarding.prefab_hint.house",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("house", BuildingPrefabProfession.NONE))));
        assertEquals("bannermod.onboarding.prefab_hint.mine",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("mine", BuildingPrefabProfession.MINER))));
        assertEquals("bannermod.onboarding.prefab_hint.lumber_camp",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("lumber_camp", BuildingPrefabProfession.LUMBERJACK))));
        assertEquals("bannermod.onboarding.prefab_hint.barracks",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("barracks", BuildingPrefabProfession.RECRUIT_SWORDSMAN))));
        assertEquals("bannermod.onboarding.prefab_hint.gatehouse",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("gatehouse", BuildingPrefabProfession.NONE))));
        assertEquals("bannermod.onboarding.prefab_hint.generic",
                translationKey(SettlementOnboardingGuide.placementHint(descriptor("watchtower", BuildingPrefabProfession.NONE))));
    }

    @Test
    void vacancyHintIsNullForNonStaffedManualBuilding() {
        assertNull(SettlementOnboardingGuide.vacancyHint(null));
        assertNull(SettlementOnboardingGuide.vacancyHint(BuildingType.HOUSE));
        assertNull(SettlementOnboardingGuide.vacancyHint(BuildingType.STORAGE));
    }

    @Test
    void vacancyHintUsesVacancyTranslationForStaffedManualBuilding() {
        MutableComponent farmHint = SettlementOnboardingGuide.vacancyHint(BuildingType.FARM);
        MutableComponent builderHint = SettlementOnboardingGuide.vacancyHint(BuildingType.ARCHITECT_WORKSHOP);

        assertNotNull(farmHint);
        assertEquals("bannermod.onboarding.vacancy", translationKey(farmHint));
        assertNotNull(builderHint);
        assertEquals("bannermod.onboarding.vacancy", translationKey(builderHint));
    }

    private static BuildingPrefabDescriptor descriptor(String path, BuildingPrefabProfession profession) {
        return new BuildingPrefabDescriptor(
                ResourceLocation.fromNamespaceAndPath("bannermod", path),
                "display",
                "description",
                5,
                5,
                5,
                profession,
                "minecraft:stick"
        );
    }

    private static String translationKey(Component component) {
        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, component.getContents());
        return contents.getKey();
    }
}
