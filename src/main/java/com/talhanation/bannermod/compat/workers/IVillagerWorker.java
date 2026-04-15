package com.talhanation.bannermod.compat.workers;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IVillagerWorker {
   ItemStack getCustomProfessionItem();
   //ONLY FOR SCREEN
   Screen getSpecialScreen(AbstractRecruitEntity recruit, Player player);
   //For SCREEN + CONTAINER
   void openSpecialGUI(ServerPlayer player);
   //SET IF SCREEN OR SCREEN+CONTAINER
   boolean hasOnlyScreen();
}
