package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.inventory.military.DisbandContainer;
import com.talhanation.bannermod.inventory.military.TeamEditMenu;
import com.talhanation.bannermod.network.messages.military.MessageOpenDisbandScreen;
import com.talhanation.bannermod.network.messages.military.MessageOpenTeamEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public final class FactionMenuService {

    private FactionMenuService() {
    }

    public static void openDisbandingScreen(Player player, UUID recruit) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("disband_screen");
                }

                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return new DisbandContainer(i, playerEntity, recruit);
                }
            }, packetBuffer -> packetBuffer.writeUUID(recruit));
            return;
        }

        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenDisbandScreen(player, recruit));
    }

    public static void openTeamEditScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("team_edit_screen");
                }

                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return new TeamEditMenu(i, playerInventory);
                }
            }, packetBuffer -> packetBuffer.writeUUID(player.getUUID()));
            return;
        }

        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenTeamEditScreen(player));
    }
}
