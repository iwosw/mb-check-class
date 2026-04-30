package com.talhanation.bannermod.settlement.onboarding;

import com.talhanation.bannermod.settlement.building.BuildingType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
