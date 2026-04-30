package com.talhanation.bannermod.client.military.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
public class DropDownMenu<T> extends AbstractWidget {
    private int bgFill = FastColor.ARGB32.color(255, 60, 60, 60);
    private int bgFillHovered = FastColor.ARGB32.color(255, 100, 100, 100);
    private int bgFillSelected = FastColor.ARGB32.color(255, 10, 10, 10);
    private int displayColor = FastColor.ARGB32.color(255, 255, 255, 255);
    private int optionTextColor = FastColor.ARGB32.color(255, 255, 255, 255);

    private final List<T> options;
    private final Consumer<T> onSelect;
    private final Function<T, String> optionTextGetter;
    private T selectedOption;
    private boolean isOpen;
    private final int optionHeight;

    public DropDownMenu(
            T selectedOption, int x, int y, int width, int height,
            List<T> options, Function<T, String> optionTextGetter,
            Consumer<T> onSelect
    ) {
        super(x, y, width, height, Component.translatable("gui.bannermod.widget.dropdown"));
        this.selectedOption = selectedOption;
        this.options = options;
        this.onSelect = onSelect;
        this.optionTextGetter = optionTextGetter;
        this.optionHeight = height;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Render the main button
        if(isMouseOverDisplay(mouseX, mouseY)){
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgFillHovered);
        }
        else
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgFillSelected);

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, getSelectedText(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, displayColor);

        if (isOpen) {
            int visibleOptions = getVisibleOptionCount();
            for (int i = 0; i < visibleOptions; i++) {
                int optionY = this.getY() + this.height + i * optionHeight;
                T option = options.get(i);

                if (isMouseOverOption(mouseX, mouseY, optionY)) {
                    guiGraphics.fill(this.getX(), optionY, this.getX() + this.width, optionY + optionHeight, bgFillHovered);
                } else {
                    guiGraphics.fill(this.getX(), optionY, this.getX() + this.width, optionY + optionHeight, bgFill);
                }

                String text = optionTextGetter.apply(option);
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, this.getX() + this.width / 2, optionY + (optionHeight - 8) / 2, optionTextColor);
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Do not use
    }

    public void onMouseClick(double mouseX, double mouseY) {
        if (isOpen) {
            int visibleOptions = getVisibleOptionCount();
            for (int i = 0; i < visibleOptions; i++) {
                int optionY = this.getY() + this.height + i * optionHeight;

                if (isMouseOverOption((int) mouseX, (int) mouseY, optionY)) {
                    selectOption(options.get(i));
                    return;
                }
            }
        }

        if (isMouseOverDisplay((int) mouseX, (int) mouseY)) {
            isOpen = !isOpen;
        } else {
            isOpen = false;
        }
    }

    public void onMouseMove(double mouseX, double mouseY) {
        if (isOpen) {
            boolean isOverDropdown = isMouseOverDropdown((int) mouseX, (int) mouseY);
            boolean isOverDisplay = isMouseOverDisplay((int) mouseX, (int) mouseY);

            if (!isOverDropdown && !isOverDisplay) {
                isOpen = false;
            }
        }
    }
    public boolean isMouseOver(double x, double y) {
        return isMouseOverDisplay((int) x, (int) y) || isMouseOverDropdown((int) x, (int) y) || super.isMouseOver(x,y);
    }

    private boolean isMouseOverDisplay(int mouseX, int mouseY) {
        return GuiWidgetBounds.contains(this.getX(), this.getY(), this.width, this.height, mouseX, mouseY);
    }

    private boolean isMouseOverDropdown(int mouseX, int mouseY) {
        if (!isOpen) return false;

        int dropdownStartX = this.getX();
        int dropdownStartY = this.getY() + this.height;
        int dropdownEndX = dropdownStartX + this.width;
        int dropdownEndY = dropdownStartY + getVisibleOptionCount() * optionHeight;

        return GuiWidgetBounds.contains(dropdownStartX, dropdownStartY, dropdownEndX - dropdownStartX, dropdownEndY - dropdownStartY, mouseX, mouseY);
    }

    private boolean isMouseOverOption(int mouseX, int mouseY, int optionY) {
        return GuiWidgetBounds.contains(this.getX(), optionY, this.width, optionHeight, mouseX, mouseY);
    }

    private int getVisibleOptionCount() {
        return GuiWidgetBounds.visibleRowsBelow(this.getY() + this.height, optionHeight, options.size());
    }

    private void selectOption(T option) {
        selectedOption = option;
        onSelect.accept(option);
        isOpen = false;
    }

    private String getSelectedText() {
        return selectedOption != null ? optionTextGetter.apply(selectedOption) : "";
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, Component.translatable(
                "gui.bannermod.widget.dropdown.narration", getSelectedText(), options.size()));
        narration.add(NarratedElementType.USAGE, Component.translatable(isOpen
                ? "gui.bannermod.widget.dropdown.open"
                : "gui.bannermod.widget.dropdown.closed"));
    }

    public boolean isOpen() { return isOpen; }

    public void setBgFill(int bgFill) {
        this.bgFill = bgFill;
    }

    public void setBgFillHovered(int bgFillHovered) {
        this.bgFillHovered = bgFillHovered;
    }

    public void setBgFillSelected(int bgFillSelected) {
        this.bgFillSelected = bgFillSelected;
    }

    public void setDisplayColor(int displayColor) {
        this.displayColor = displayColor;
    }

    public void setOptionTextColor(int optionTextColor) {
        this.optionTextColor = optionTextColor;
    }
}
