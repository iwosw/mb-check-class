package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.military.RecruitPoliticalContext;
import com.talhanation.bannermod.registry.citizen.ModCitizenEntityTypes;
import com.talhanation.bannermod.settlement.prefab.staffing.PrefabAutoStaffingRuntime;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementRefreshSupport;
import com.talhanation.bannermod.war.WarRuntimeContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;
import java.util.UUID;

public final class WorkerCitizenConversionService {
    public static final int MANUAL_ASSIGNMENT_PAUSE_TICKS = 20 * 60;

    private WorkerCitizenConversionService() {
    }

    @Nullable
    public static String convertDeniedReasonKey(@Nullable ServerPlayer player, @Nullable AbstractWorkerEntity worker) {
        if (player == null || worker == null || !(worker.level() instanceof ServerLevel serverLevel)) {
            return "gui.bannermod.worker_screen.convert.denied.missing";
        }
        if (!worker.isAlive() || worker.isRemoved()) {
            return "gui.bannermod.worker_screen.convert.denied.dead";
        }
        if (player.distanceToSqr(worker) > 16.0D * 16.0D) {
            return "gui.bannermod.worker_screen.convert.denied.too_far";
        }
        if (player.isCreative() && player.hasPermissions(2)) {
            return null;
        }
        UUID playerPoliticalEntityId = RecruitPoliticalContext.politicalEntityIdOf(player, WarRuntimeContext.registry(serverLevel));
        UUID workerPoliticalEntityId = RecruitPoliticalContext.politicalEntityIdOf(worker, WarRuntimeContext.registry(serverLevel));
        if (player.getUUID().equals(worker.getOwnerUUID())
                || (playerPoliticalEntityId != null && playerPoliticalEntityId.equals(workerPoliticalEntityId))) {
            return null;
        }
        return "gui.bannermod.worker_screen.convert.denied.not_controller";
    }

    public static boolean canConvert(@Nullable ServerPlayer player, @Nullable AbstractWorkerEntity worker) {
        return convertDeniedReasonKey(player, worker) == null;
    }

    public static boolean convert(@Nullable ServerPlayer player, @Nullable AbstractWorkerEntity worker) {
        if (convertDeniedReasonKey(player, worker) != null || worker == null || !(worker.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        CitizenEntity citizen = ModCitizenEntityTypes.CITIZEN.get().create(serverLevel);
        if (citizen == null) {
            return false;
        }

        citizen.moveTo(worker.getX(), worker.getY(), worker.getZ(), worker.getYRot(), worker.getXRot());
        citizen.apply(worker.getCitizenCore().snapshot());
        citizen.setFollowState(0);
        citizen.setBoundWorkAreaUUID(null);
        citizen.clearHoldPos();
        citizen.clearMovePos();
        citizen.getPersistentData().putLong(
                PrefabAutoStaffingRuntime.TAG_ASSIGNMENT_PAUSE_UNTIL,
                serverLevel.getGameTime() + MANUAL_ASSIGNMENT_PAUSE_TICKS
        );
        if (worker.hasCustomName()) {
            citizen.setCustomName(worker.getCustomName());
        }

        serverLevel.addFreshEntity(citizen);
        if (worker.getTeam() instanceof PlayerTeam team) {
            serverLevel.getScoreboard().addPlayerToTeam(citizen.getScoreboardName(), team);
        }
        if (worker.getCurrentWorkArea() != null) {
            worker.getCurrentWorkArea().setBeingWorkedOn(false);
        }
        worker.discard();
        BannerModSettlementRefreshSupport.refreshSnapshot(serverLevel, citizen.blockPosition());
        return true;
    }
}
