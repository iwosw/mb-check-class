package com.talhanation.bannermod;


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

public class WorkersUpdateChecker {

    @SubscribeEvent
    public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event){
        VersionChecker.Status status = VersionChecker.getResult((ModList.get().getModContainerById("workers").get()).getModInfo()).status();

        switch (status){
            case OUTDATED -> {
                Player player = event.getEntity();
                if(player != null){
                    player.sendSystemMessage(Component.literal("A new version of Villager Workers is available!").withStyle(ChatFormatting.GOLD));

                    MutableComponent link = Component.literal("Download the update " + ChatFormatting.BLUE + "here").withStyle(ChatFormatting.GREEN);
                    link.withStyle(link.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/workers/files")));
                    player.sendSystemMessage(link);
                }
                else{
                    com.talhanation.bannermod.bootstrap.BannerModMain.LOGGER.warn("Villager workers is outdated!");
                }
            }

            case FAILED -> {
                com.talhanation.bannermod.bootstrap.BannerModMain.LOGGER.error("Villager workers could not check for updates!");
            }
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event){
        VersionChecker.Status status = VersionChecker.getResult((ModList.get().getModContainerById("workers").get()).getModInfo()).status();

        switch (status){
            case OUTDATED -> {
                com.talhanation.bannermod.bootstrap.BannerModMain.LOGGER.warn("A new version of Villager Workers is available!");
                com.talhanation.bannermod.bootstrap.BannerModMain.LOGGER.warn("Download the new update here: https://www.curseforge.com/minecraft/mc-mods/workers/files");
            }

            case FAILED -> {
                com.talhanation.bannermod.bootstrap.BannerModMain.LOGGER.error("Villager workers could not check for updates!");
            }
        }
    }
}
