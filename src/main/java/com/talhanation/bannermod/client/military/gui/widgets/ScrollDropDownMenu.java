package com.talhanation.bannermod.client.military.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
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


public class ScrollDropDownMenu<T> extends AbstractWidget {
    private int bgFill = FastColor.ARGB32.color(255, 60, 60, 60);
    private int bgFillHovered = FastColor.ARGB32.color(255, 100, 100, 100);
    private int bgFillSelected = FastColor.ARGB32.color(255, 10, 10, 10);
    private int displayColor = FastColor.ARGB32.color(255, 255, 255, 255);
    private int optionTextColor = FastColor.ARGB32.color(255, 255, 255, 255);
    private int scrollbarColor = FastColor.ARGB32.color(255, 100, 100, 100);
    private int scrollbarHandleColor = FastColor.ARGB32.color(255, 150, 150, 150);

    private final List<T> options;
    private final Consumer<T> onSelect;
    private final Function<T, String> optionTextGetter;
    private T selectedOption;
    private boolean isOpen;
    private final int optionHeight;

    // Scroll-related fields
    private int scrollOffset = 0; // Tracks how far the list is scrolled
    private int maxVisibleOptions; // Maximum number of options visible at once
    private boolean isScrolling = false; // Whether the scrollbar is being dragged
    private int scrollbarWidth = 6; // Width of the scrollbar
    public boolean canSelect = true;
    public ScrollDropDownMenu(T selectedOption, int x, int y, int width, int height, List<T> options, Function<T, String> optionTextGetter, Consumer<T> onSelect) {
        super(x, y, width, height, Component.literal(""));
        this.selectedOption = selectedOption;
        this.options = options;
        this.onSelect = onSelect;
        this.optionTextGetter = optionTextGetter;
        this.optionHeight = height;
        this.maxVisibleOptions = Math.min(5, options.size());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if(!visible) return;

        if (isMouseOverDisplay(mouseX, mouseY)) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgFillHovered);
        } else {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgFillSelected);
        }

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, getSelectedText(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, displayColor);

        if (isOpen) {
            int visibleOptions = getVisibleOptionCount();
            if (visibleOptions <= 0) {
                return;
            }

            int dropdownHeight = visibleOptions * optionHeight;
            guiGraphics.fill(this.getX(), this.getY() + this.height, this.getX() + this.width, this.getY() + this.height + dropdownHeight, bgFill);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 500); // Ensure the dropdown renders above other elements
            GuiWidgetBounds.enableScissor(this.getX(), this.getY() + this.height, this.width, dropdownHeight);

            for (int i = 0; i < options.size(); i++) {
                int optionY = this.getY() + this.height + (i - scrollOffset) * optionHeight;

                if (isMouseOverOption(mouseX, mouseY, optionY)) {
                    guiGraphics.fill(this.getX(), optionY, this.getX() + this.width, optionY + optionHeight, bgFillHovered);
                } else {
                    guiGraphics.fill(this.getX(), optionY, this.getX() + this.width, optionY + optionHeight, bgFill);
                }

                String text = optionTextGetter.apply(options.get(i));
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, this.getX() + this.width / 2, optionY + (optionHeight - 8) / 2, optionTextColor);
            }

            RenderSystem.disableScissor();


            // Render the scrollbar
            if (options.size() > visibleOptions) {
                int scrollbarX = this.getX() + this.width - scrollbarWidth;
                int scrollbarY = this.getY() + this.height + (int) ((float) scrollOffset / options.size() * dropdownHeight);
                int scrollbarHeight = Math.max(10, (int) ((float) visibleOptions / options.size() * dropdownHeight));

                // Scrollbar background
                guiGraphics.fill(scrollbarX, this.getY() + this.height, scrollbarX + scrollbarWidth, this.getY() + this.height + dropdownHeight, scrollbarColor);

                // Scrollbar handle
                guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, scrollbarHandleColor);
            }

            guiGraphics.pose().popPose();
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Do not use
    }

    public void onMouseClick(double mouseX, double mouseY) {
        if(!visible) return;
        if(!canSelect) return;
        if (isOpen) {
            // Check if the click is on the scrollbar
            if (isMouseOverScrollbar((int) mouseX, (int) mouseY)) {
                isScrolling = true;
                return;
            }

            // Check if the click is on an option
            for (int i = 0; i < options.size(); i++) {
                int optionY = this.getY() + this.height + (i - scrollOffset) * optionHeight;

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
        if(!visible) return;

        if (isOpen) {
            boolean isOverDropdown = isMouseOverDropdown((int) mouseX, (int) mouseY);
            boolean isOverDisplay = isMouseOverDisplay((int) mouseX, (int) mouseY);

            if (!isOverDropdown && !isOverDisplay) {
                isOpen = false;
            }
        }

        // Handle scrollbar dragging
        if (isScrolling) {
            int visibleOptions = getVisibleOptionCount();
            if (visibleOptions <= 0) {
                return;
            }

            int dropdownHeight = visibleOptions * optionHeight;
            int scrollbarY = (int) mouseY - (this.getY() + this.height);
            scrollOffset = (int) ((float) scrollbarY / dropdownHeight * options.size());
            scrollOffset = GuiWidgetBounds.clampScrollOffset(scrollOffset, options.size(), visibleOptions);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
        if(!visible) return false;

        if (isOpen) {
            scrollOffset -= (int) delta;
            scrollOffset = GuiWidgetBounds.clampScrollOffset(scrollOffset, options.size(), getVisibleOptionCount());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(!visible) return false;
        
        if (isScrolling) {
            isScrolling = false;
            return true;
        }
        return false;
    }

    private boolean isMouseOverScrollbar(int mouseX, int mouseY) {
        int visibleOptions = getVisibleOptionCount();
        if (!isOpen || options.size() <= visibleOptions || visibleOptions <= 0) return false;

        int scrollbarX = this.getX() + this.width - scrollbarWidth;
        int scrollbarY = this.getY() + this.height;
        int scrollbarHeight = visibleOptions * optionHeight;

        return GuiWidgetBounds.contains(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, mouseX, mouseY);
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

    public boolean isMouseOver(double x, double y) {
        return isMouseOverDisplay((int) x, (int) y) || isMouseOverDropdown((int) x, (int) y) || isMouseOverScrollbar((int) x, (int) y) || super.isMouseOver(x,y);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, Component.translatable(
                "gui.bannermod.widget.dropdown.narration", getSelectedText(), options.size()));
        if (isOpen) {
            narration.add(NarratedElementType.USAGE, Component.translatable("gui.bannermod.widget.dropdown.open"));
        }
    }

    private void selectOption(T option) {
        selectedOption = option;
        onSelect.accept(option);
        isOpen = false;
    }

    private String getSelectedText() {
        return selectedOption != null ? optionTextGetter.apply(selectedOption) : "";
    }

    private int getVisibleOptionCount() {
        return GuiWidgetBounds.visibleRowsBelow(this.getY() + this.height, optionHeight, maxVisibleOptions);
    }

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
