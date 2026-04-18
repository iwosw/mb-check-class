package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import com.talhanation.bannermod.persistence.military.RecruitsFactionManager;
import com.talhanation.bannermod.persistence.military.RecruitsTreatyManager;
import com.talhanation.bannermod.util.DelayedExecutor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

public final class FactionLifecycleService {
    private FactionLifecycleService() {
    }

    public static void onServerStarting(MinecraftServer server) {
        ServerLevel level = server.overworld();
        applyGlobalTeamSettings(level);

        FactionEvents.recruitsFactionManager = new RecruitsFactionManager();
        FactionEvents.recruitsFactionManager.load(level);

        FactionEvents.recruitsDiplomacyManager = new RecruitsDiplomacyManager();
        FactionEvents.recruitsDiplomacyManager.load(level);

        FactionEvents.recruitsTreatyManager = new RecruitsTreatyManager();
        FactionEvents.recruitsTreatyManager.load(level);
    }

    public static void saveAll(MinecraftServer server) {
        ServerLevel level = server.overworld();
        FactionEvents.recruitsFactionManager.save(level);
        FactionEvents.recruitsDiplomacyManager.save(level);
        FactionEvents.recruitsTreatyManager.save(level);
    }

    public static void onPlayerJoin(ServerLevel level, Player player, MinecraftServer server) {
        migrateLegacyFactionMembership(player);

        if (player instanceof ServerPlayer serverPlayer) {
            FactionEvents.recruitsFactionManager.broadcastOnlinePlayersToAll(level);
            FactionEvents.recruitsFactionManager.broadcastFactionsToPlayer(serverPlayer);
            FactionEvents.recruitsDiplomacyManager.broadcastDiplomacyMapToPlayer(serverPlayer);
            FactionEvents.recruitsTreatyManager.broadcastTreatiesToPlayer(serverPlayer);
        }
    }

    public static void onPlayerLeave(MinecraftServer server) {
        DelayedExecutor.runLater(() -> FactionEvents.recruitsFactionManager.broadcastOnlinePlayersToAll(server.overworld()), 1000L);
    }

    private static void applyGlobalTeamSettings(ServerLevel level) {
        for (PlayerTeam playerTeam : level.getScoreboard().getPlayerTeams()) {
            playerTeam.setAllowFriendlyFire(RecruitsServerConfig.GlobalTeamSetting.get() && RecruitsServerConfig.GlobalTeamFriendlyFireSetting.get());
            playerTeam.setSeeFriendlyInvisibles(RecruitsServerConfig.GlobalTeamSetting.get() && RecruitsServerConfig.GlobalTeamSeeFriendlyInvisibleSetting.get());
        }
    }

    private static void migrateLegacyFactionMembership(Player player) {
        if (player.getTeam() == null) {
            return;
        }

        RecruitsFaction faction = FactionEvents.recruitsFactionManager.getFactionByStringID(player.getTeam().getName());
        if (faction != null && faction.getMembers().stream().noneMatch(member -> member.getUUID().equals(player.getUUID()))) {
            faction.addMember(player.getUUID(), player.getName().getString());
        }
    }
}
