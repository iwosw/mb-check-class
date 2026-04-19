package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FactionMembershipService {
    private FactionMembershipService() {
    }

    public static void createTeam(boolean menu, ServerPlayer serverPlayer, @NotNull ServerLevel level, String teamName, String displayName, String playerName, ItemStack banner, net.minecraft.ChatFormatting color, byte colorByte) {
        FactionTeamTransactionService.createTeam(menu, serverPlayer, level, teamName, displayName, playerName, banner, color, colorByte);
    }

    public static void leaveTeam(boolean command, ServerPlayer player, String teamName, ServerLevel level, boolean fromLeader) {
        FactionTeamTransactionService.leaveTeam(command, player, teamName, level, fromLeader);
    }

    public static void modifyTeam(ServerLevel level, String stringID, RecruitsFaction editedTeam, @Nullable ServerPlayer serverPlayer, int cost) {
        FactionTeamModificationService.modifyTeam(level, stringID, editedTeam, serverPlayer, cost);
    }

    public static void removeTeam(ServerLevel level, String teamName) {
        FactionTeamTransactionService.removeTeam(level, teamName);
    }

    public static void addPlayerToTeam(@Nullable ServerPlayer player, ServerLevel level, String teamName, String namePlayerToAdd) {
        FactionTeamJoinService.addPlayerToTeam(player, level, teamName, namePlayerToAdd);
    }

    public static boolean isPlayerAlreadyAFactionLeader(ServerPlayer playerToCheck) {
        return FactionTeamJoinService.isPlayerAlreadyAFactionLeader(playerToCheck);
    }

    public static void addPlayerToData(ServerLevel level, String teamName, int x, String namePlayerToAdd) {
        FactionTeamJoinService.addPlayerToData(level, teamName, x, namePlayerToAdd);
    }

    public static void addNPCToData(ServerLevel level, String teamName, int x) {
        FactionTeamJoinService.addNPCToData(level, teamName, x);
    }

    public static void serverSideUpdateTeam(ServerLevel level) {
        FactionRuntimeSyncService.markRecruitsNeedingTeamUpdate(level);
    }

}
