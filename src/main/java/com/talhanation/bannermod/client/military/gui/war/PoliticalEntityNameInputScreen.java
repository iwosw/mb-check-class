package com.talhanation.bannermod.client.military.gui.war;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
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
    private static final int H = 116;
    private static final int LEATHER = 0xFF6F4728;
    private static final int LEATHER_DARK = 0xFF3E2515;
    private static final int PAGE_BG = 0xFFF3E2B6;
    private static final int PAGE_SHADE = 0xFF7A5A33;
    private static final int GOLD = 0xFFE0B45C;
    private static final int INK = 0xFF2D2418;
    private static final int INK_MUTED = 0xFF6C5B45;

    private final Screen parent;
    private final Component prompt;
    private final String initialValue;
    private final Consumer<String> onSubmit;
    private final int maxLength;
    private final boolean allowEmpty;
    private int guiLeft;
    private int guiTop;
    private EditBox editBox;
    private Button submitButton;

    public PoliticalEntityNameInputScreen(Screen parent,
                                          Component title,
                                          Component prompt,
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
                                          Component title,
                                          Component prompt,
                                          String initialValue,
                                          Consumer<String> onSubmit,
                                          int maxLength,
                                          boolean allowEmpty) {
        super(title);
        this.parent = parent;
        this.prompt = prompt == null ? Component.empty() : prompt;
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

        this.editBox = new EditBox(this.font, guiLeft + 16, guiTop + 46, W - 32, 20, Component.literal(""));
        this.editBox.setMaxLength(this.maxLength);
        this.editBox.setValue(this.initialValue);
        this.editBox.setFocused(true);
        this.editBox.setEditable(true);
        this.editBox.setTextColor(INK);
        this.editBox.setTextColorUneditable(INK_MUTED);
        addRenderableWidget(this.editBox);
        setInitialFocus(this.editBox);

        this.submitButton = addRenderableWidget(Button.builder(Component.translatable("gui.bannermod.common.submit"), btn -> submit())
                .bounds(guiLeft + 16, guiTop + H - 30, 84, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.bannermod.common.cancel"), btn -> onClose())
                .bounds(guiLeft + W - 100, guiTop + H - 30, 84, 20).build());
        updateSubmitState();
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
        if (this.editBox != null && this.editBox.keyPressed(keyCode, scanCode, modifiers)) {
            updateSubmitState();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.editBox != null && this.editBox.charTyped(codePoint, modifiers)) {
            updateSubmitState();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        updateSubmitState();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x66000000);
        graphics.fill(guiLeft + 4, guiTop + 5, guiLeft + W + 4, guiTop + H + 5, 0x55000000);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, LEATHER_DARK);
        graphics.fill(guiLeft + 2, guiTop + 2, guiLeft + W - 2, guiTop + H - 2, LEATHER);
        renderParchmentPanel(graphics, guiLeft + 10, guiTop + 18, W - 20, H - 30);
        graphics.drawCenteredString(font, this.title, guiLeft + W / 2, guiTop + 8, GOLD);
        graphics.drawString(font, this.prompt, guiLeft + 16, guiTop + 30, INK_MUTED, false);
        graphics.drawString(font, font.plainSubstrByWidth(inputStatus().getString(), W - 32), guiLeft + 16, guiTop + 72, INK, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void updateSubmitState() {
        if (this.submitButton == null || this.editBox == null) {
            return;
        }
        this.submitButton.active = this.allowEmpty || !this.editBox.getValue().trim().isEmpty();
        // Surface the same denial reason the status line already shows so the
        // greyed Submit explains why it is gated.
        this.submitButton.setTooltip(this.submitButton.active ? null
                : Tooltip.create(Component.translatable("gui.bannermod.political_entity.name.submit.disabled")));
    }

    private Component inputStatus() {
        if (this.allowEmpty || (this.editBox != null && !this.editBox.getValue().trim().isEmpty())) {
            return Component.translatable("gui.bannermod.states.dialog.status.ready");
        }
        return Component.translatable("gui.bannermod.states.dialog.status.required");
    }

    private void renderParchmentPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, PAGE_BG);
        graphics.fill(x, y, x + w, y + 2, 0x88FFF1BE);
        graphics.fill(x, y + h - 2, x + w, y + h, PAGE_SHADE);
        graphics.fill(x, y, x + 2, y + h, 0x66FFF1BE);
        graphics.fill(x + w - 2, y, x + w, y + h, 0x66B88245);
        graphics.renderOutline(x, y, w, h, PAGE_SHADE);
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
