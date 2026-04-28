package com.talhanation.bannermod.client.military.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenEntryBase;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.util.GameProfileUtils;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;


public class RecruitsPlayerEntry extends ListScreenEntryBase<RecruitsPlayerEntry> {
    protected final Minecraft minecraft;
    protected final IPlayerSelection screen;
    protected final @NotNull RecruitsPlayerInfo player;

    public RecruitsPlayerEntry(IPlayerSelection screen, @NotNull RecruitsPlayerInfo player) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.player = player;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        renderElement(guiGraphics, top, left, width, height, hovered, iconX(left), iconY(top, height), textX(left), textY(minecraft, top, height));
    }

    public void renderElement(GuiGraphics guiGraphics, int top, int left, int width, int height, boolean hovered, int skinX, int skinY, int textX, int textY){
        boolean selected = screen.getSelected() != null && player.getUUID().equals(screen.getSelected().getUUID());
        renderRowBackground(guiGraphics, left, top, width, height, hovered, selected, ROW_FILL);

        int nameColor = player.isOnline() ? ROW_TEXT : ROW_TEXT_MUTED;

        RenderSystem.setShaderTexture(0, GameProfileUtils.getSkin(player.getUUID()));
        guiGraphics.blit(GameProfileUtils.getSkin(player.getUUID()), skinX, skinY, ICON_SIZE, ICON_SIZE, 8, 8, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        guiGraphics.blit(GameProfileUtils.getSkin(player.getUUID()), skinX, skinY, ICON_SIZE, ICON_SIZE, 40, 8, 8, 8, 64, 64);
        if (!player.isOnline()) {
            guiGraphics.fill(skinX, skinY, skinX + ICON_SIZE, skinY + ICON_SIZE, FastColor.ARGB32.color(120, 0, 0, 0));
        }
        RenderSystem.disableBlend();
        guiGraphics.drawString(minecraft.font, player.getName(), (float) textX, (float) textY, nameColor, false);

    }

    @NotNull
    public RecruitsPlayerInfo getPlayerInfo() {
        return player;
    }

    @Override
    public ListScreenListBase<RecruitsPlayerEntry> getList() {
        return screen.getPlayerList();
    }
}
