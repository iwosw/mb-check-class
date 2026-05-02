package com.talhanation.bannermod.client.military.gui.group;

import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

@OnlyIn(Dist.CLIENT)
public class RecruitsCommandButton extends ExtendedButton {

    public RecruitsCommandButton(int xPos, int yPos, Component displayString, OnPress handler) {
        super(xPos - 40, yPos - 10, 80, 20, displayString, handler);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        MilitaryGuiStyle.commandButton(guiGraphics, Minecraft.getInstance().font, mouseX, mouseY,
                getX(), getY(), width, height, getMessage(), active, false);
    }
}
