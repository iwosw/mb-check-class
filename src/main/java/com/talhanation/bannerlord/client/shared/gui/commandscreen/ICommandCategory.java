package com.talhanation.bannerlord.client.shared.gui.commandscreen;

import com.talhanation.bannerlord.entity.shared.*;

import com.talhanation.bannerlord.client.shared.gui.CommandScreen;
import com.talhanation.bannerlord.persistence.military.RecruitsGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ICommandCategory {
    Component getToolTipName();
    ItemStack getIcon();
    void createButtons(CommandScreen screen, int centerX, int centerY, List<RecruitsGroup> groups, Player player);
}
