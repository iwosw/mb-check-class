package com.talhanation.bannerlord.client.shared.gui.group;

import com.talhanation.bannerlord.entity.shared.*;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

@OnlyIn(Dist.CLIENT)
public class RecruitsCommandButton extends ExtendedButton {

    public RecruitsCommandButton(int xPos, int yPos, Component displayString, OnPress handler) {
        super(xPos - 40, yPos - 10, 80, 20, displayString, handler);
    }
}
