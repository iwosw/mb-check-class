package com.talhanation.bannermod.client.military.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

public final class GuiWidgetBounds {
    private GuiWidgetBounds() {
    }

    public static boolean contains(int left, int top, int width, int height, int mouseX, int mouseY) {
        if (width <= 0 || height <= 0) {
            return false;
        }
        return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
    }

    public static int visibleRowsBelow(int top, int rowHeight, int requestedRows) {
        if (rowHeight <= 0 || requestedRows <= 0) {
            return 0;
        }

        int availableHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight() - top;
        return Math.max(0, Math.min(requestedRows, availableHeight / rowHeight));
    }

    public static int clampScrollOffset(int scrollOffset, int optionCount, int visibleRows) {
        return Math.max(0, Math.min(scrollOffset, Math.max(0, optionCount - visibleRows)));
    }

    public static void enableScissor(int x, int y, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        double scale = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor(
                (int) (x * scale),
                (int) (minecraft.getWindow().getHeight() - (y + height) * scale),
                (int) (width * scale),
                (int) (height * scale));
    }
}
