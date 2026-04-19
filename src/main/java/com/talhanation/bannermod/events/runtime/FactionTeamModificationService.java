package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

final class FactionTeamModificationService {
    private FactionTeamModificationService() {
    }

    static void modifyTeam(ServerLevel level, String stringID, RecruitsFaction editedTeam, @Nullable ServerPlayer serverPlayer, int cost) {
        MinecraftServer server = level.getServer();
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(stringID);
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(stringID);

        if (!validateAndPay(serverPlayer, cost)) {
            return;
        }
        if (recruitsFaction == null || playerTeam == null) {
            return;
        }

        applyFactionUpdates(level, recruitsFaction, editedTeam);
        syncScoreboardTeam(playerTeam, editedTeam);
        syncClaims(level, editedTeam);
        FactionRuntimeSyncService.save(level);
    }

    private static boolean validateAndPay(@Nullable ServerPlayer serverPlayer, int cost) {
        if (serverPlayer == null) {
            return true;
        }
        if (cost > 0 && !FactionEconomyService.playerHasEnoughEmeralds(serverPlayer, cost)) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.noenough_money").withStyle(net.minecraft.ChatFormatting.RED));
            return false;
        }
        if (cost > 0) {
            FactionEconomyService.doPayment(serverPlayer, cost);
        }
        return true;
    }

    private static void applyFactionUpdates(ServerLevel level, RecruitsFaction recruitsFaction, RecruitsFaction editedTeam) {
        if (!recruitsFaction.getTeamLeaderUUID().equals(editedTeam.getTeamLeaderUUID())) {
            FactionNotifier.notifyFactionMembers(level, recruitsFaction, 10, editedTeam.getTeamLeaderName());
            recruitsFaction.setTeamLeaderID(editedTeam.getTeamLeaderUUID());
            recruitsFaction.setTeamLeaderName(editedTeam.getTeamLeaderName());
        }
        if (!recruitsFaction.getTeamDisplayName().equals(editedTeam.getTeamDisplayName())) {
            FactionNotifier.notifyFactionMembers(level, recruitsFaction, 11, editedTeam.getTeamDisplayName());
            recruitsFaction.setTeamDisplayName(editedTeam.getTeamDisplayName());
        }
        if (!recruitsFaction.getBanner().equals(editedTeam.getBanner())) {
            FactionNotifier.notifyFactionMembers(level, recruitsFaction, 12, "");
            recruitsFaction.setBanner(editedTeam.getBanner());
        }
        recruitsFaction.setUnitColor(editedTeam.getUnitColor());
        recruitsFaction.setTeamColor(editedTeam.getTeamColor());
        recruitsFaction.setMaxNPCsPerPlayer(editedTeam.getMaxNPCsPerPlayer());
    }

    private static void syncScoreboardTeam(PlayerTeam playerTeam, RecruitsFaction editedTeam) {
        playerTeam.setDisplayName(Component.literal(editedTeam.getTeamDisplayName()));
        playerTeam.setColor(net.minecraft.ChatFormatting.getById(editedTeam.getTeamColor()));
    }

    private static void syncClaims(ServerLevel level, RecruitsFaction editedTeam) {
        for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
            if (claim.getOwnerFaction().getStringID().equals(editedTeam.getStringID())) {
                claim.setOwnerFaction(editedTeam);
            }
        }
        FactionRuntimeSyncService.broadcastClaims(level);
    }
}
