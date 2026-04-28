package com.talhanation.bannermod.client.civilian.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.client.military.gui.widgets.GuiWidgetBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;


public class ItemScrollDropDownMenu extends AbstractWidget {
    private int bgFill = FastColor.ARGB32.color(255, 60, 60, 60);
    private int bgFillHovered = FastColor.ARGB32.color(255, 100, 100, 100);
    private int bgFillSelected = FastColor.ARGB32.color(255, 10, 10, 10);
    private int displayColor = FastColor.ARGB32.color(255, 255, 255, 255);
    private int optionTextColor = FastColor.ARGB32.color(255, 255, 255, 255);
    private int scrollbarColor = FastColor.ARGB32.color(255, 100, 100, 100);
    private int scrollbarHandleColor = FastColor.ARGB32.color(255, 150, 150, 150);
    public List<ItemStack> options;
    private final Consumer<ItemStack> onSelect;
    private ItemStack selectedOption;
    private boolean isOpen;
    private final int optionHeight;
    private int scrollOffset = 0;
    private int maxVisibleOptions;
    private boolean isScrolling = false;
    private int scrollbarWidth = 6;
    private int scrollbarHandleHeight;
    private boolean canSelect;
    private boolean resetCount;
    public ItemScrollDropDownMenu(ItemStack selectedOption, int x, int y, int width, int height, List<ItemStack> options, Consumer<ItemStack> onSelect) {
        super(x, y, width, height, Component.literal(""));
        this.selectedOption = selectedOption;
        this.options = options;
        this.onSelect = onSelect;
        this.optionHeight = height;
        this.maxVisibleOptions = getVisibleOptionCount();
        this.scrollbarHandleHeight = getScrollbarHandleHeight(this.maxVisibleOptions * this.optionHeight);
        this.setCanSelectItem(true);
        this.setResetCount(true);
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            if (this.isMouseOverDisplay(mouseX, mouseY)) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.bgFillHovered);
            } else {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.bgFillSelected);
            }

            guiGraphics.drawCenteredString(Minecraft.getInstance().font, selectedOption.getHoverName().getString(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, this.displayColor);

            int iconX = this.getX() + 2;
            int iconY = this.getY() + 2;

            if(resetCount) selectedOption.setCount(1);
            guiGraphics.renderFakeItem(selectedOption, iconX, iconY);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, selectedOption, iconX, iconY);

            if (this.isOpen) {
                int visibleOptions = getVisibleOptionCount();
                if (visibleOptions <= 0) return;
                this.maxVisibleOptions = visibleOptions;
                this.scrollOffset = GuiWidgetBounds.clampScrollOffset(this.scrollOffset, this.options.size(), visibleOptions);

                int dropdownHeight = visibleOptions * this.optionHeight;
                guiGraphics.fill(this.getX(), this.getY() + this.height, this.getX() + this.width, this.getY() + this.height + dropdownHeight, this.bgFill);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 500.0F);
                GuiWidgetBounds.enableScissor(this.getX(), this.getY() + this.height, this.width, dropdownHeight);

                int i;
                int optionY;
                for(i = 0; i < this.options.size(); ++i) {
                    optionY = this.getY() + this.height + (i - this.scrollOffset) * this.optionHeight;
                    if (this.isMouseOverOption(mouseX, mouseY, optionY)) {
                        guiGraphics.fill(this.getX(), optionY, this.getX() + this.width, optionY + this.optionHeight, this.bgFillHovered);
                    } else {
                        guiGraphics.fill(this.getX(), optionY, this.getX() + this.width, optionY + this.optionHeight, this.bgFill);
                    }

                    ItemStack stack = this.options.get(i);
                    String text = stack.getHoverName().getString();
                    guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, this.getX() + this.width / 2, optionY + (this.optionHeight - 8) / 2, this.optionTextColor);
                    iconY = optionY + 2;

                    if(resetCount) stack.setCount(1);
                    guiGraphics.renderFakeItem(stack, iconX, iconY);
                    guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, iconX, iconY);
                }

                RenderSystem.disableScissor();
                if (this.options.size() > visibleOptions) {
                    i = this.getX() + this.width - this.scrollbarWidth;
                    optionY = this.getY() + this.height + (int)((float)this.scrollOffset / (float)this.options.size() * (float)dropdownHeight);
                    int scrollbarHeight = getScrollbarHandleHeight(dropdownHeight);
                    guiGraphics.fill(i, this.getY() + this.height, i + this.scrollbarWidth, this.getY() + this.height + dropdownHeight, this.scrollbarColor);
                    guiGraphics.fill(i, optionY, i + this.scrollbarWidth, optionY + scrollbarHeight, this.scrollbarHandleColor);
                }

                guiGraphics.pose().popPose();
            }

        }
    }
    public void setResetCount(boolean reset) {
        this.resetCount = reset;
    }

    public void setOptions(List<ItemStack> options) {
        this.options = options;
    }

    public void setCanSelectItem(boolean can){
        this.canSelect = can;
    }

    public void insertOption(int index, ItemStack stack, String text) {
        ItemStack toInsert = stack;

        if (stack.isEmpty()) {
            toInsert = ItemStack.EMPTY;
            toInsert.set(DataComponents.CUSTOM_NAME, Component.literal(text));
        }

        if (index < 0 || index > options.size()) {
            options.add(toInsert); // Am Ende anhängen
        } else {
            options.add(index, toInsert);
        }

        // Scrollbar ggf. anpassen
        this.maxVisibleOptions = getVisibleOptionCount();
        this.scrollbarHandleHeight = getScrollbarHandleHeight(this.maxVisibleOptions * this.optionHeight);
    }

    public void onClick(double mouseX, double mouseY) {
    }

    public void onMouseClick(double mouseX, double mouseY) {
        if (this.visible) {
            if (this.isOpen) {
                if (this.isMouseOverScrollbar((int)mouseX, (int)mouseY)) {
                    this.isScrolling = true;
                    return;
                }

                for(int i = 0; i < this.options.size(); ++i) {
                    int optionY = this.getY() + this.height + (i - this.scrollOffset) * this.optionHeight;
                    if (this.isMouseOverOption((int)mouseX, (int)mouseY, optionY)) {
                        this.selectOption(this.options.get(i));
                        return;
                    }
                }
            }

            if (this.isMouseOverDisplay((int)mouseX, (int)mouseY)) {
                this.isOpen = !this.isOpen;
            } else {
                this.isOpen = false;
            }

        }
    }

    public void onMouseMove(double mouseX, double mouseY) {
        if (this.visible) {
            if (this.isOpen) {
                boolean isOverDropdown = this.isMouseOverDropdown((int)mouseX, (int)mouseY);
                boolean isOverDisplay = this.isMouseOverDisplay((int)mouseX, (int)mouseY);
                if (!isOverDropdown && !isOverDisplay) {
                    this.isOpen = false;
                }
            }

            if (this.isScrolling) {
                int visibleOptions = getVisibleOptionCount();
                int dropdownHeight = visibleOptions * this.optionHeight;
                if (dropdownHeight <= 0) return;
                int scrollbarY = (int)mouseY - (this.getY() + this.height);
                this.scrollOffset = (int)((float)scrollbarY / (float)dropdownHeight * (float)this.options.size());
                this.scrollOffset = GuiWidgetBounds.clampScrollOffset(this.scrollOffset, this.options.size(), visibleOptions);
            }

        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
        if (!this.visible) {
            return false;
        } else if (this.isOpen) {
            int visibleOptions = getVisibleOptionCount();
            this.scrollOffset -= (int)delta;
            this.scrollOffset = GuiWidgetBounds.clampScrollOffset(this.scrollOffset, this.options.size(), visibleOptions);
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!this.visible) {
            return false;
        } else if (this.isScrolling) {
            this.isScrolling = false;
            return true;
        } else {
            return false;
        }
    }

    private boolean isMouseOverScrollbar(int mouseX, int mouseY) {
        int visibleOptions = getVisibleOptionCount();
        if (this.isOpen && this.options.size() > visibleOptions) {
            int scrollbarX = this.getX() + this.width - this.scrollbarWidth;
            int scrollbarY = this.getY() + this.height;
            int scrollbarHeight = visibleOptions * this.optionHeight;
            return GuiWidgetBounds.contains(scrollbarX, scrollbarY, this.scrollbarWidth, scrollbarHeight, mouseX, mouseY);
        } else {
            return false;
        }
    }

    private boolean isMouseOverDisplay(int mouseX, int mouseY) {
        return GuiWidgetBounds.contains(this.getX(), this.getY(), this.width, this.height, mouseX, mouseY);
    }

    private boolean isMouseOverDropdown(int mouseX, int mouseY) {
        if (!this.isOpen) {
            return false;
        } else {
            int dropdownStartX = this.getX();
            int dropdownStartY = this.getY() + this.height;
            int dropdownEndX = dropdownStartX + this.width;
            int dropdownEndY = dropdownStartY + getVisibleOptionCount() * this.optionHeight;
            return GuiWidgetBounds.contains(dropdownStartX, dropdownStartY, dropdownEndX - dropdownStartX, dropdownEndY - dropdownStartY, mouseX, mouseY);
        }
    }

    private boolean isMouseOverOption(int mouseX, int mouseY, int optionY) {
        return GuiWidgetBounds.contains(this.getX(), optionY, this.width, this.optionHeight, mouseX, mouseY);
    }

    public boolean isMouseOver(double x, double y) {
        return this.isMouseOverDisplay((int)x, (int)y) || this.isMouseOverDropdown((int)x, (int)y) || this.isMouseOverScrollbar((int)x, (int)y) || super.isMouseOver(x, y);
    }

    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, this.selectedOption.getHoverName());
        narration.add(NarratedElementType.USAGE, Component.literal(this.options.size() + " options"));
    }

    private int getVisibleOptionCount() {
        return GuiWidgetBounds.visibleRowsBelow(this.getY() + this.height, this.optionHeight, Math.min(5, this.options.size()));
    }

    private int getScrollbarHandleHeight(int dropdownHeight) {
        if (this.options.isEmpty() || dropdownHeight <= 0) return 0;
        return Math.max(10, (int)((float)this.maxVisibleOptions / (float)this.options.size() * (float)dropdownHeight));
    }

    private void selectOption(ItemStack option) {
        if(!canSelect) return;
        this.selectedOption = option;
        this.onSelect.accept(option);
        this.isOpen = false;
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
