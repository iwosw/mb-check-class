package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.bootstrap.BootstrapResult;
import com.talhanation.bannermod.settlement.bootstrap.SettlementBootstrapService;
import com.talhanation.bannermod.settlement.bootstrap.SettlementRecord;
import com.talhanation.bannermod.settlement.bootstrap.SettlementRegistryData;
import com.talhanation.bannermod.settlement.building.BuildingDefinitionRegistry;
import com.talhanation.bannermod.settlement.building.BuildingType;
import com.talhanation.bannermod.settlement.building.BuildingValidationState;
import com.talhanation.bannermod.settlement.building.ValidatedBuildingRecord;
import com.talhanation.bannermod.settlement.building.ValidatedBuildingRegistryData;
import com.talhanation.bannermod.settlement.prefab.staffing.PrefabAutoStaffingRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public final class SettlementSurveyorService {
    private SettlementSurveyorService() {
    }

    public static void validateCurrentSession(ServerPlayer player, ValidationSession session) {
        if (player == null || session == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (session.mode() == SurveyorMode.INSPECT_EXISTING) {
            inspectExisting(player, level, session);
            return;
        }
        BuildingType type = toBuildingType(session.mode());
        if (type == null) {
            player.sendSystemMessage(Component.literal("Selected survey mode is not yet validation-enabled.").withStyle(ChatFormatting.RED));
            return;
        }

        SettlementRecord settlementAtAnchor = settlementForAnchor(level, session);
        if (type != BuildingType.STARTER_FORT && settlementAtAnchor == null) {
            player.sendSystemMessage(Component.literal("No settlement found at anchor. Bootstrap a fort first.").withStyle(ChatFormatting.RED));
            return;
        }
        UUID settlementId = settlementAtAnchor == null ? new UUID(0L, 0L) : settlementAtAnchor.settlementId();
        BuildingValidationRequest request = new BuildingValidationRequest(settlementId, type, session.anchorPos(), session.selections());
        BuildingValidationResult result = new DefaultBuildingValidator(new BuildingDefinitionRegistry()).validate(level, player, request);
        SurveyorFeedbackFormatter.sendValidationResult(player, result);
        if (!result.valid()) return;

        if (type == BuildingType.STARTER_FORT) {
            BootstrapResult bootstrap = SettlementBootstrapService.bootstrapSettlement(level, player, result);
            player.sendSystemMessage(Component.literal(bootstrap.message()).withStyle(
                    bootstrap.success() ? ChatFormatting.GREEN : ChatFormatting.RED));
            return;
        }
        if (result.snapshot() == null) {
            player.sendSystemMessage(Component.literal("Validation did not produce a snapshot.").withStyle(ChatFormatting.RED));
            return;
        }

        ValidatedBuildingRecord record = new ValidatedBuildingRecord(
                UUID.randomUUID(),
                settlementId,
                type,
                level.dimension(),
                result.snapshot().anchorPos(),
                result.snapshot().zones(),
                result.snapshot().bounds(),
                BuildingValidationState.VALID,
                result.capacity(),
                result.qualityScore(),
                level.getGameTime(),
                level.getGameTime(),
                0L
        );
        ValidatedBuildingRegistryData.get(level).registerBuilding(record);
        PrefabAutoStaffingRuntime.registerValidatedBuildingVacancy(record);
        player.sendSystemMessage(Component.literal(
                "Building validated: " + type.name() + " (capacity " + result.capacity() + ", quality " + result.qualityScore() + ")")
                .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal(PrefabAutoStaffingRuntime.describeManualVacancy(type)).withStyle(ChatFormatting.YELLOW));
    }

    private static SettlementRecord settlementForAnchor(ServerLevel level, ValidationSession session) {
        SettlementRegistryData registry = SettlementRegistryData.get(level);
        SettlementRecord settlement = registry.getSettlementAt(new ChunkPos(session.anchorPos()));
        if (settlement != null || ClaimEvents.recruitsClaimManager == null) {
            return settlement;
        }
        RecruitsClaim claim = ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(session.anchorPos()));
        return claim == null ? null : registry.getSettlementByClaimId(claim.getUUID());
    }

    private static BuildingType toBuildingType(SurveyorMode mode) {
        return switch (mode) {
            case BOOTSTRAP_FORT -> BuildingType.STARTER_FORT;
            case HOUSE -> BuildingType.HOUSE;
            case FARM -> BuildingType.FARM;
            case MINE -> BuildingType.MINE;
            case LUMBER_CAMP -> BuildingType.LUMBER_CAMP;
            case SMITHY -> BuildingType.SMITHY;
            case STORAGE -> BuildingType.STORAGE;
            case ARCHITECT_BUILDER -> BuildingType.ARCHITECT_WORKSHOP;
            case INSPECT_EXISTING -> null;
        };
    }

    private static void inspectExisting(ServerPlayer player, ServerLevel level, ValidationSession session) {
        ValidatedBuildingRecord record = ValidatedBuildingRegistryData.get(level).getByAnchor(session.anchorPos());
        if (record == null) {
            player.sendSystemMessage(Component.literal("No validated building found at selected anchor.").withStyle(ChatFormatting.RED));
            return;
        }
        player.sendSystemMessage(Component.literal("Inspect: " + record.type().name()).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("State: " + record.state().name()
                + " | Capacity: " + record.capacity()
                + " | Quality: " + record.qualityScore()).withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal(PrefabAutoStaffingRuntime.describeManualVacancy(record.type())).withStyle(ChatFormatting.YELLOW));
    }
}
