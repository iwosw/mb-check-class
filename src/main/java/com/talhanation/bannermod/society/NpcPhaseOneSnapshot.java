package com.talhanation.bannermod.society;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public record NpcPhaseOneSnapshot(
        String lifeStageTag,
        String sexTag,
        @Nullable UUID householdId,
        @Nullable UUID homeBuildingUuid,
        @Nullable UUID workBuildingUuid,
        @Nullable String cultureId,
        @Nullable String faithId,
        String dailyPhaseTag,
        String currentIntentTag,
        String currentAnchorTag,
        int householdSize,
        String householdHousingStateTag,
        int hungerNeed,
        int fatigueNeed,
        int socialNeed,
        int safetyNeed,
        int trustScore,
        int fearScore,
        int angerScore,
        int gratitudeScore,
        int loyaltyScore,
        String housingRequestStatusTag,
        List<NpcMemorySummarySnapshot> recentMemories
) {
    public static NpcPhaseOneSnapshot empty() {
        return new NpcPhaseOneSnapshot(
                NpcLifeStage.UNSPECIFIED.name(),
                NpcSex.UNSPECIFIED.name(),
                null,
                null,
                null,
                null,
                null,
                NpcDailyPhase.UNSPECIFIED.name(),
                NpcIntent.UNSPECIFIED.name(),
                NpcAnchorType.NONE.name(),
                0,
                NpcHouseholdHousingState.HOMELESS.name(),
                0,
                0,
                0,
                0,
                50,
                0,
                0,
                0,
                50,
                NpcHousingRequestStatus.NONE.name()
                ,
                List.of()
        );
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(safeTag(this.lifeStageTag));
        buf.writeUtf(safeTag(this.sexTag));
        writeNullableUuid(buf, this.householdId);
        writeNullableUuid(buf, this.homeBuildingUuid);
        writeNullableUuid(buf, this.workBuildingUuid);
        writeNullableString(buf, this.cultureId);
        writeNullableString(buf, this.faithId);
        buf.writeUtf(safeTag(this.dailyPhaseTag));
        buf.writeUtf(safeTag(this.currentIntentTag));
        buf.writeUtf(safeTag(this.currentAnchorTag));
        buf.writeVarInt(Math.max(0, this.householdSize));
        buf.writeUtf(safeTag(this.householdHousingStateTag));
        buf.writeVarInt(Math.max(0, this.hungerNeed));
        buf.writeVarInt(Math.max(0, this.fatigueNeed));
        buf.writeVarInt(Math.max(0, this.socialNeed));
        buf.writeVarInt(Math.max(0, this.safetyNeed));
        buf.writeVarInt(Math.max(0, this.trustScore));
        buf.writeVarInt(Math.max(0, this.fearScore));
        buf.writeVarInt(Math.max(0, this.angerScore));
        buf.writeVarInt(Math.max(0, this.gratitudeScore));
        buf.writeVarInt(Math.max(0, this.loyaltyScore));
        buf.writeUtf(safeTag(this.housingRequestStatusTag));
        buf.writeVarInt(this.recentMemories == null ? 0 : this.recentMemories.size());
        if (this.recentMemories != null) {
            for (NpcMemorySummarySnapshot memory : this.recentMemories) {
                (memory == null ? new NpcMemorySummarySnapshot("UNSPECIFIED", "PERSONAL", null, 0, false) : memory).toBytes(buf);
            }
        }
    }

    public static NpcPhaseOneSnapshot fromBytes(FriendlyByteBuf buf) {
        List<NpcMemorySummarySnapshot> memories = new ArrayList<>();
        String lifeStageTag = buf.readUtf();
        String sexTag = buf.readUtf();
        UUID householdId = readNullableUuid(buf);
        UUID homeBuildingUuid = readNullableUuid(buf);
        UUID workBuildingUuid = readNullableUuid(buf);
        String cultureId = readNullableString(buf);
        String faithId = readNullableString(buf);
        String dailyPhaseTag = buf.readUtf();
        String currentIntentTag = buf.readUtf();
        String currentAnchorTag = buf.readUtf();
        int householdSize = buf.readVarInt();
        String householdHousingStateTag = buf.readUtf();
        int hungerNeed = buf.readVarInt();
        int fatigueNeed = buf.readVarInt();
        int socialNeed = buf.readVarInt();
        int safetyNeed = buf.readVarInt();
        int trustScore = buf.readVarInt();
        int fearScore = buf.readVarInt();
        int angerScore = buf.readVarInt();
        int gratitudeScore = buf.readVarInt();
        int loyaltyScore = buf.readVarInt();
        String housingRequestStatusTag = buf.readUtf();
        int memoryCount = buf.readVarInt();
        for (int i = 0; i < memoryCount; i++) {
            memories.add(NpcMemorySummarySnapshot.fromBytes(buf));
        }
        return new NpcPhaseOneSnapshot(
                lifeStageTag,
                sexTag,
                householdId,
                homeBuildingUuid,
                workBuildingUuid,
                cultureId,
                faithId,
                dailyPhaseTag,
                currentIntentTag,
                currentAnchorTag,
                householdSize,
                householdHousingStateTag,
                hungerNeed,
                fatigueNeed,
                socialNeed,
                safetyNeed,
                trustScore,
                fearScore,
                angerScore,
                gratitudeScore,
                loyaltyScore,
                housingRequestStatusTag,
                List.copyOf(memories)
        );
    }

    public String lifeStageTranslationKey() {
        return "gui.bannermod.society.life_stage." + safeTag(this.lifeStageTag).toLowerCase(Locale.ROOT);
    }

    public String sexTranslationKey() {
        return "gui.bannermod.society.sex." + safeTag(this.sexTag).toLowerCase(Locale.ROOT);
    }

    public String dailyPhaseTranslationKey() {
        return "gui.bannermod.society.daily_phase." + safeTag(this.dailyPhaseTag).toLowerCase(Locale.ROOT);
    }

    public String currentIntentTranslationKey() {
        return "gui.bannermod.society.intent." + safeTag(this.currentIntentTag).toLowerCase(Locale.ROOT);
    }

    public String currentAnchorTranslationKey() {
        return "gui.bannermod.society.anchor." + safeTag(this.currentAnchorTag).toLowerCase(Locale.ROOT);
    }

    public String householdHousingStateTranslationKey() {
        return "gui.bannermod.society.household_housing." + safeTag(this.householdHousingStateTag).toLowerCase(Locale.ROOT);
    }

    public String housingRequestTranslationKey() {
        return "gui.bannermod.society.housing_request." + safeTag(this.housingRequestStatusTag).toLowerCase(Locale.ROOT);
    }

    public String cultureLabel() {
        return this.cultureId == null || this.cultureId.isBlank() ? "-" : this.cultureId;
    }

    public String faithLabel() {
        return this.faithId == null || this.faithId.isBlank() ? "-" : this.faithId;
    }

    public static String shortId(@Nullable UUID uuid) {
        return uuid == null ? "-" : uuid.toString().substring(0, 8);
    }

    public List<NpcMemorySummarySnapshot> safeRecentMemories() {
        return this.recentMemories == null ? List.of() : this.recentMemories;
    }

    private static void writeNullableUuid(FriendlyByteBuf buf, @Nullable UUID value) {
        buf.writeBoolean(value != null);
        if (value != null) {
            buf.writeUUID(value);
        }
    }

    private static @Nullable UUID readNullableUuid(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readUUID() : null;
    }

    private static void writeNullableString(FriendlyByteBuf buf, @Nullable String value) {
        buf.writeBoolean(value != null);
        if (value != null) {
            buf.writeUtf(value);
        }
    }

    private static @Nullable String readNullableString(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readUtf() : null;
    }

    private static String safeTag(@Nullable String value) {
        return value == null || value.isBlank() ? "UNSPECIFIED" : value;
    }
}
