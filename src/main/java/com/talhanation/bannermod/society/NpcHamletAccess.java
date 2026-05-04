package com.talhanation.bannermod.society;

import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NpcHamletAccess {
    private NpcHamletAccess() {
    }

    public static @Nullable NpcHamletRecord reconcileHousehold(ServerLevel level,
                                                               BannerModSettlementSnapshot snapshot,
                                                               UUID householdId,
                                                               long gameTime) {
        if (level == null || snapshot == null || householdId == null) {
            return null;
        }
        NpcHouseholdRecord household = NpcHouseholdAccess.householdFor(level, householdId).orElse(null);
        NpcHousingRequestRecord request = NpcHousingRequestAccess.requestForHousehold(level, householdId);
        if (household == null
                || household.homeBuildingUuid() == null
                || request == null
                || request.reservedPlotPos() == null
                || !NpcHousingPlotPlanner.isHamletPlot(snapshot, request.reservedPlotPos())) {
            NpcHamletSavedData.get(level).runtime().removeHousehold(householdId, gameTime);
            return null;
        }
        return NpcHamletSavedData.get(level).runtime().reconcileHousehold(
                snapshot.claimUuid(),
                householdId,
                household.homeBuildingUuid(),
                request.reservedPlotPos(),
                gameTime
        );
    }

    public static Optional<NpcHamletRecord> hamletFor(ServerLevel level, UUID hamletId) {
        return NpcHamletSavedData.get(level).runtime().hamletFor(hamletId);
    }

    public static Optional<NpcHamletRecord> hamletForHousehold(ServerLevel level, UUID householdId) {
        return NpcHamletSavedData.get(level).runtime().hamletForHousehold(householdId);
    }

    public static List<NpcHamletRecord> hamletsForClaim(ServerLevel level, UUID claimUuid) {
        return NpcHamletSavedData.get(level).runtime().hamletsForClaim(claimUuid);
    }

    public static @Nullable NpcHamletRecord nearestHamlet(ServerLevel level,
                                                          UUID claimUuid,
                                                          BlockPos pos,
                                                          double maxDistance,
                                                          boolean includeAbandoned) {
        return NpcHamletSavedData.get(level).runtime().nearestHamlet(claimUuid, pos, maxDistance, includeAbandoned);
    }

    public static NpcHamletRecord register(ServerLevel level, UUID hamletId, long gameTime) {
        return NpcHamletSavedData.get(level).runtime().register(hamletId, gameTime);
    }

    public static NpcHamletRecord rename(ServerLevel level, UUID hamletId, String name, long gameTime) {
        return NpcHamletSavedData.get(level).runtime().rename(hamletId, name, gameTime);
    }

    public static boolean noteHostileAction(ServerLevel level, UUID hamletId, long gameTime, long cooldownTicks) {
        return NpcHamletSavedData.get(level).runtime().noteHostileAction(hamletId, gameTime, cooldownTicks);
    }

    public static boolean hasActiveHamlet(ServerLevel level, UUID claimUuid) {
        for (NpcHamletRecord hamlet : hamletsForClaim(level, claimUuid)) {
            if (hamlet != null && hamlet.isInhabited()) {
                return true;
            }
        }
        return false;
    }

    public static @Nullable UUID representativeResidentUuid(ServerLevel level, NpcHamletRecord hamlet) {
        if (level == null || hamlet == null) {
            return null;
        }
        for (NpcHamletHouseholdEntry entry : hamlet.householdEntries()) {
            NpcHouseholdRecord household = NpcHouseholdAccess.householdFor(level, entry.householdId()).orElse(null);
            if (household == null) {
                continue;
            }
            if (household.headResidentUuid() != null) {
                return household.headResidentUuid();
            }
            if (!household.memberResidentUuids().isEmpty()) {
                return household.memberResidentUuids().getFirst();
            }
        }
        return null;
    }

    public static Component displayName(@Nullable NpcHamletRecord hamlet) {
        if (hamlet == null) {
            return Component.literal("-");
        }
        return Component.translatable("gui.bannermod.society.hamlet.named", hamlet.name());
    }
}
