package com.talhanation.bannermod.client.military.gui.widgets;

import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact dropdown trigger that opens a vertical list of {@link ContextMenuEntry} actions.
 *
 * <p>Unlike {@link DropDownMenu}, this widget does not track a selected option — it is
 * intended to collapse a fixed group of related buttons (aggro presets, fire policy,
 * stance, etc.) under a single labeled trigger. Use it to satisfy the "no button wall"
 * UI rule while keeping every action one click away.
 *
 * <p>The trigger uses the same parchment/iron palette as
 * {@link MilitaryGuiStyle#commandButton}, and the open menu floats above siblings
 * because rendering happens in {@link #renderWidget}; callers must add this widget
 * AFTER any siblings the menu should overlap, or render it last manually.
 */
public class ActionMenuButton extends AbstractWidget {
    private final Component triggerLabel;
    private List<ContextMenuEntry> entries;
    private final int rowHeight;
    private final int menuWidth;
    private boolean openUpward;
    private boolean isOpen;

    public ActionMenuButton(int x, int y, int width, int height, Component triggerLabel, List<ContextMenuEntry> entries) {
        super(x, y, width, height, triggerLabel);
        this.triggerLabel = triggerLabel;
        this.entries = new ArrayList<>(entries);
        this.rowHeight = Math.max(12, height);
        this.menuWidth = Math.max(width, 80);
    }

    public void setOpenUpward(boolean openUpward) {
        this.openUpward = openUpward;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        isOpen = false;
    }

    /**
     * Replace the menu entries. Required when entry-level enabled flags depend on
     * runtime selection state (e.g. WarListScreen's outcome menu). Closes the menu
     * to avoid the open list pointing at stale lambdas.
     */
    public void setEntries(List<ContextMenuEntry> newEntries) {
        this.entries = new ArrayList<>(newEntries);
        this.isOpen = false;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        // Trigger
        MilitaryGuiStyle.commandButton(graphics, mc.font, mouseX, mouseY,
                getX(), getY(), width, height, triggerLabel, this.active, isOpen);

        if (!isOpen) {
            return;
        }

        int menuX = getX();
        int totalH = entries.size() * rowHeight;
        int menuY = openUpward ? getY() - totalH : getY() + height;

        // Background frame using parchment-iron contrast for readability over busy panels.
        graphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + totalH + 1, 0xFF1A130A);
        graphics.renderOutline(menuX - 1, menuY - 1, menuWidth + 2, totalH + 2, 0xFFE0B86A);

        for (int i = 0; i < entries.size(); i++) {
            ContextMenuEntry entry = entries.get(i);
            int rowY = menuY + i * rowHeight;
            boolean hovered = mouseX >= menuX && mouseX < menuX + menuWidth
                    && mouseY >= rowY && mouseY < rowY + rowHeight;
            int bg;
            int textColor;
            if (!entry.enabled) {
                bg = 0xCC2A1F14;
                textColor = MilitaryGuiStyle.TEXT_MUTED;
            } else if (hovered) {
                bg = 0xFF5A4025;
                textColor = MilitaryGuiStyle.TEXT;
            } else {
                bg = 0xEE2A1F14;
                textColor = MilitaryGuiStyle.TEXT;
            }
            graphics.fill(menuX, rowY, menuX + menuWidth, rowY + rowHeight, bg);
            graphics.renderOutline(menuX, rowY, menuWidth, rowHeight, 0xFF8A6A3A);
            String clamped = MilitaryGuiStyle.clampLabel(mc.font, entry.label, menuWidth - 6);
            graphics.drawCenteredString(mc.font, clamped,
                    menuX + menuWidth / 2, rowY + (rowHeight - 8) / 2, textColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (isOpen) {
            int menuX = getX();
            int totalH = entries.size() * rowHeight;
            int menuY = openUpward ? getY() - totalH : getY() + height;
            if (mouseX >= menuX && mouseX < menuX + menuWidth
                    && mouseY >= menuY && mouseY < menuY + totalH) {
                int idx = (int) ((mouseY - menuY) / rowHeight);
                if (idx >= 0 && idx < entries.size()) {
                    ContextMenuEntry entry = entries.get(idx);
                    if (entry.enabled && entry.action != null) {
                        entry.action.run();
                    }
                    isOpen = false;
                    return true;
                }
            }
        }
        if (isMouseOverTrigger(mouseX, mouseY)) {
            isOpen = !isOpen;
            return true;
        }
        if (isOpen) {
            isOpen = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (isMouseOverTrigger(mouseX, mouseY)) {
            return true;
        }
        if (isOpen) {
            int menuX = getX();
            int totalH = entries.size() * rowHeight;
            int menuY = openUpward ? getY() - totalH : getY() + height;
            return mouseX >= menuX && mouseX < menuX + menuWidth
                    && mouseY >= menuY && mouseY < menuY + totalH;
        }
        return false;
    }

    private boolean isMouseOverTrigger(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + width
                && mouseY >= getY() && mouseY < getY() + height;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // routed through mouseClicked
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, triggerLabel);
        narration.add(NarratedElementType.USAGE, Component.translatable(
                isOpen ? "gui.bannermod.widget.dropdown.open" : "gui.bannermod.widget.dropdown.closed"));
    }
}
