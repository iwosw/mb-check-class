package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.network.messages.military.MessageToClientSetDiplomaticToast;
import com.talhanation.bannermod.network.messages.military.MessageToClientSetToast;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public final class FactionNotifier {
    private FactionNotifier() {
    }

    public static void notifyFactionMembers(ServerLevel level, RecruitsFaction recruitsFaction, int id, String notification) {
        List<ServerPlayer> playersInTeam = FactionEvents.recruitsFactionManager.getPlayersInTeam(recruitsFaction.getStringID(), level);
        for (ServerPlayer teamPlayer : playersInTeam) {
            BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> teamPlayer), new MessageToClientSetDiplomaticToast(id, recruitsFaction, notification));
        }
    }

    public static void notifyPlayer(ServerLevel level, RecruitsPlayerInfo playerInfo, int id, String notification) {
        BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) level.getPlayerByUUID(playerInfo.getUUID())), new MessageToClientSetToast(id, notification));
    }

    static void notifyPlayerJoinedFaction(ServerLevel level, ServerPlayer playerToAdd, RecruitsFaction recruitsFaction) {
        BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> playerToAdd), new MessageToClientSetDiplomaticToast(8, recruitsFaction));
    }
}
