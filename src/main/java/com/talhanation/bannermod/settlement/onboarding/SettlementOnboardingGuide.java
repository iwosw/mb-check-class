package com.talhanation.bannermod.settlement.onboarding;

import com.talhanation.bannermod.settlement.bootstrap.SettlementRecord;
import com.talhanation.bannermod.settlement.building.BuildingType;
import com.talhanation.bannermod.settlement.building.ValidatedBuildingRegistryData;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabDescriptor;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabProfession;
import com.talhanation.bannermod.settlement.prefab.staffing.PrefabAutoStaffingRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class SettlementOnboardingGuide {
    private SettlementOnboardingGuide() {
    }

    public static void sendFoundingGuidance(ServerPlayer player, ServerLevel level, @Nullable SettlementRecord settlement) {
        if (player == null || level == null || settlement == null) {
            return;
        }
        player.sendSystemMessage(Component.translatable("bannermod.onboarding.founded.next").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("bannermod.onboarding.founded.staffing").withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(nextTrackedStep(settlement, level).withStyle(ChatFormatting.AQUA));
    }

    public static void sendRegisteredBuildingGuidance(ServerPlayer player,
                                                      ServerLevel level,
                                                      @Nullable SettlementRecord settlement,
                                                      BuildingType buildingType) {
        if (player == null || level == null || settlement == null || buildingType == null) {
            return;
        }
        MutableComponent vacancy = vacancyHint(buildingType);
        if (vacancy != null) {
            player.sendSystemMessage(vacancy.withStyle(ChatFormatting.YELLOW));
        }
        player.sendSystemMessage(nextTrackedStep(settlement, level).withStyle(ChatFormatting.AQUA));
    }

    public static MutableComponent professionLabel(BuildingPrefabProfession profession) {
        String key = profession == null ? "none" : profession.name().toLowerCase(Locale.ROOT);
        return Component.translatable("bannermod.prefab.profession." + key);
    }

    public static MutableComponent placementHint(BuildingPrefabDescriptor descriptor) {
        String key = switch (descriptor.id().getPath()) {
            case "storage" -> "bannermod.onboarding.prefab_hint.storage";
            case "farm" -> "bannermod.onboarding.prefab_hint.farm";
            case "market_stall" -> "bannermod.onboarding.prefab_hint.market_stall";
            case "house" -> "bannermod.onboarding.prefab_hint.house";
            case "mine" -> "bannermod.onboarding.prefab_hint.mine";
            case "lumber_camp" -> "bannermod.onboarding.prefab_hint.lumber_camp";
            case "barracks" -> "bannermod.onboarding.prefab_hint.barracks";
            case "gatehouse" -> "bannermod.onboarding.prefab_hint.gatehouse";
            default -> "bannermod.onboarding.prefab_hint.generic";
        };
        return Component.translatable(key, professionLabel(descriptor.profession()));
    }

    public static @Nullable MutableComponent vacancyHint(BuildingType buildingType) {
        BuildingPrefabProfession profession = PrefabAutoStaffingRuntime.professionForManualBuilding(buildingType);
        int slots = PrefabAutoStaffingRuntime.vacancySlotsForManualBuilding(buildingType);
        if (profession == BuildingPrefabProfession.NONE || slots <= 0) {
            return null;
        }
        return Component.translatable("bannermod.onboarding.vacancy", professionLabel(profession), slots);
    }

    static String nextTrackedStepKey(Map<BuildingType, Integer> counts) {
        if (count(counts, BuildingType.STORAGE) == 0) {
            return "bannermod.onboarding.next.storage";
        }
        if (count(counts, BuildingType.FARM) == 0) {
            return "bannermod.onboarding.next.farm";
        }
        if (count(counts, BuildingType.HOUSE) == 0) {
            return "bannermod.onboarding.next.house";
        }
        if (count(counts, BuildingType.MINE) == 0 || count(counts, BuildingType.LUMBER_CAMP) == 0 || count(counts, BuildingType.ARCHITECT_WORKSHOP) == 0) {
            return "bannermod.onboarding.next.professions";
        }
        return "bannermod.onboarding.next.market";
    }

    private static MutableComponent nextTrackedStep(SettlementRecord settlement, ServerLevel level) {
        return Component.translatable(nextTrackedStepKey(trackedCounts(settlement, level)));
    }

    private static Map<BuildingType, Integer> trackedCounts(SettlementRecord settlement, ServerLevel level) {
        EnumMap<BuildingType, Integer> counts = new EnumMap<>(BuildingType.class);
        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        for (BuildingType type : BuildingType.values()) {
            counts.put(type, registry.getBuildings(settlement.settlementId(), type).size());
        }
        return counts;
    }

    private static int count(Map<BuildingType, Integer> counts, BuildingType type) {
        return Math.max(0, counts.getOrDefault(type, 0));
    }
}
