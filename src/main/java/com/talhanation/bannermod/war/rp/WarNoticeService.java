package com.talhanation.bannermod.war.rp;

import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class WarNoticeService {
    private WarNoticeService() {
    }

    public static void broadcastDeclaration(MinecraftServer server,
                                            WarDeclarationRecord war,
                                            PoliticalRegistryRuntime registry) {
        if (server == null || war == null) {
            return;
        }
        MutableComponent header = Component.literal("[WAR DECLARED] ").withStyle(ChatFormatting.RED);
        header.append(WarDeclarationFormatter.summary(war, registry));
        broadcast(server, header);
    }

    public static void broadcastOutcome(MinecraftServer server,
                                        WarDeclarationRecord war,
                                        PoliticalRegistryRuntime registry,
                                        String outcomeName) {
        if (server == null || war == null) {
            return;
        }
        MutableComponent header = Component.literal("[WAR RESOLVED] ").withStyle(ChatFormatting.GOLD);
        header.append(WarDeclarationFormatter.summary(war, registry));
        header.append(Component.literal(" outcome=" + outcomeName));
        broadcast(server, header);
    }

    public static void broadcastCancelled(MinecraftServer server,
                                          WarDeclarationRecord war,
                                          PoliticalRegistryRuntime registry) {
        if (server == null || war == null) {
            return;
        }
        MutableComponent header = Component.literal("[WAR CANCELLED] ").withStyle(ChatFormatting.GRAY);
        header.append(WarDeclarationFormatter.summary(war, registry));
        broadcast(server, header);
    }

    private static void broadcast(MinecraftServer server, Component message) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            player.sendSystemMessage(message);
        }
    }
}
