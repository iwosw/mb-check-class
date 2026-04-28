package com.talhanation.bannermod.client.military.gui.war;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Modal dialog that prompts the player for a state name and dispatches the trimmed value to
 * {@link #onSubmit}. Used by the political-entity list UI for both {@code Create} and
 * {@code Rename} flows; the parent screen passes whichever callback wires the right packet.
 */
public class PoliticalEntityNameInputScreen extends Screen {

    private static final int W = 240;
    private static final int H = 96;

    private final Screen parent;
    private final String prompt;
    private final String initialValue;
    private final Consumer<String> onSubmit;
    private final int maxLength;
    private final boolean allowEmpty;
    private int guiLeft;
    private int guiTop;
    private EditBox editBox;

    public PoliticalEntityNameInputScreen(Screen parent,
                                          String title,
                                          String prompt,
                                          String initialValue,
                                          Consumer<String> onSubmit) {
        this(parent, title, prompt, initialValue, onSubmit, 32, false);
    }

    /**
     * Extended constructor for color / charter editing where:
     *   - {@code maxLength} differs from the 32-character name cap; and
     *   - {@code allowEmpty} controls whether submitting an empty string is a meaningful
     *     value (e.g. clearing the field) vs. a "do nothing" guard like for renames.
     */
    public PoliticalEntityNameInputScreen(Screen parent,
                                          String title,
                                          String prompt,
                                          String initialValue,
                                          Consumer<String> onSubmit,
                                          int maxLength,
                                          boolean allowEmpty) {
        super(Component.literal(title));
        this.parent = parent;
        this.prompt = prompt == null ? "" : prompt;
        this.initialValue = initialValue == null ? "" : initialValue;
        this.onSubmit = onSubmit;
        this.maxLength = Math.max(1, maxLength);
        this.allowEmpty = allowEmpty;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - W) / 2;
        this.guiTop = (this.height - H) / 2;

        this.editBox = new EditBox(this.font, guiLeft + 10, guiTop + 36, W - 20, 18, Component.literal(""));
        this.editBox.setMaxLength(this.maxLength);
        this.editBox.setValue(this.initialValue);
        this.editBox.setFocused(true);
        this.editBox.setEditable(true);
        addRenderableWidget(this.editBox);
        setInitialFocus(this.editBox);

        addRenderableWidget(Button.builder(Component.literal("Submit"), btn -> submit())
                .bounds(guiLeft + 10, guiTop + H - 26, 80, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> onClose())
                .bounds(guiLeft + W - 90, guiTop + H - 26, 80, 18).build());
    }

    private void submit() {
        String value = this.editBox.getValue().trim();
        if (value.isEmpty() && !this.allowEmpty) {
            return;
        }
        this.onSubmit.accept(value);
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, 0xC0101010);
        graphics.renderOutline(guiLeft, guiTop, W, H, 0xFFFFFFFF);
        graphics.drawCenteredString(font, this.title.getString(), guiLeft + W / 2, guiTop + 8, 0xFFFFFF);
        graphics.drawString(font, this.prompt, guiLeft + 10, guiTop + 24, 0xCCCCCC, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (parent != null) {
            this.minecraft.setScreen(parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
