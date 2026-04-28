package com.talhanation.bannermod.client.military.gui.widgets;


import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.util.FastColor;

import java.util.List;

public abstract class ListScreenEntryBase<T extends ContainerObjectSelectionList.Entry<T>> extends ContainerObjectSelectionList.Entry<T> {

    protected static final int ROW_FILL = FastColor.ARGB32.color(255, 60, 60, 60);
    protected static final int ROW_FILL_ALT = FastColor.ARGB32.color(255, 80, 80, 80);
    protected static final int ROW_FILL_HOVERED = FastColor.ARGB32.color(255, 100, 100, 100);
    protected static final int ROW_FILL_SELECTED = FastColor.ARGB32.color(255, 10, 10, 10);
    protected static final int ROW_TEXT = FastColor.ARGB32.color(255, 255, 255, 255);
    protected static final int ROW_TEXT_MUTED = FastColor.ARGB32.color(255, 140, 140, 140);
    protected static final int ICON_SIZE = 24;
    protected static final int ROW_PADDING = 4;

    protected final List<AbstractWidget> children;

    public ListScreenEntryBase() {
        this.children = Lists.newArrayList();
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return children;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            getList().setFocused(this);
            return true;
        }
        return false;
    }

    public abstract ListScreenListBase<?> getList();

    protected void renderRowBackground(GuiGraphics guiGraphics, int left, int top, int width, int height, boolean hovered, boolean selected, int fillColor) {
        int color = selected ? ROW_FILL_SELECTED : hovered ? ROW_FILL_HOVERED : fillColor;
        guiGraphics.fill(left, top, left + width, top + height, color);
    }

    protected int iconX(int left) {
        return left + ROW_PADDING;
    }

    protected int iconY(int top, int height) {
        return top + (height - ICON_SIZE) / 2;
    }

    protected int textX(int left) {
        return iconX(left) + ICON_SIZE + ROW_PADDING;
    }

    protected int textY(Minecraft minecraft, int top, int height) {
        return top + (height - minecraft.font.lineHeight) / 2;
    }

}
