package com.talhanation.bannermod.client.military.gui.widgets;

import com.talhanation.bannermod.client.military.gui.RecruitsScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

public abstract class ListScreenBase extends RecruitsScreenBase {

    private Runnable postRender;

    public ListScreenBase(Component title, int xSize, int ySize) {
        super(title, xSize, ySize);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        if (postRender != null) {
            postRender.run();
            postRender = null;
        }
    }

    public void postRender(Runnable postRender) {
        this.postRender = postRender;
    }

}

