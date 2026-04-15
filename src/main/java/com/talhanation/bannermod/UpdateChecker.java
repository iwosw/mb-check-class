package com.talhanation.bannermod;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import com.talhanation.bannermod.config.RecruitsClientConfig;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;

public class UpdateChecker {

    @SubscribeEvent
    public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event){
        if(!event.getEntity().getCommandSenderWorld().isClientSide()) return;
        if(RecruitsClientConfig.UpdateCheckerClientside.get()){
            VersionChecker.Status status = VersionChecker.getResult((ModList.get().getModContainerById("recruits").get()).getModInfo()).status();
            switch (status){
                case OUTDATED -> {
                    Player player = event.getEntity();
                    if(player != null){
						player.sendSystemMessage(Component.literal("A new version of Villager Recruits is available!").withStyle(ChatFormatting.GOLD));
						MutableComponent link = Component.literal("Download the update " + ChatFormatting.BLUE + "here").withStyle(ChatFormatting.GREEN);
						link.withStyle(link.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/villager-recruits/versions")));
						player.sendSystemMessage(link);
                    }
                    else{
                        BannerModMain.LOGGER.warn("Villager recruits is outdated!");
                    }
                }

                case FAILED -> {
                    BannerModMain.LOGGER.error("Villager recruits could not check for updates!");
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event){
        if(RecruitsServerConfig.UpdateCheckerServerside.get()){
            VersionChecker.Status status = VersionChecker.getResult((ModList.get().getModContainerById("recruits").get()).getModInfo()).status();

            switch (status){
                case OUTDATED -> {
                    BannerModMain.LOGGER.warn("A new version of Villager Recruits is available!");
                    BannerModMain.LOGGER.warn("Download the new update here: https://modrinth.com/mod/villager-recruits/versions");
                }

                case FAILED -> {
                    BannerModMain.LOGGER.error("Villager recruits could not check for updates!");
                }
            }
        }

    }
}