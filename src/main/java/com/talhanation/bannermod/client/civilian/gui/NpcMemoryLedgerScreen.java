package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.society.NpcMemorySummarySnapshot;
import com.talhanation.bannermod.society.NpcPhaseOneSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;

public class NpcMemoryLedgerScreen extends Screen {
    private static final int WIDTH = 278;
    private static final int HEIGHT = 240;

    private final Screen parent;
    private final NpcPhaseOneSnapshot snapshot;
    private int left;
    private int top;

    public NpcMemoryLedgerScreen(Screen parent, NpcPhaseOneSnapshot snapshot) {
        super(Component.translatable("gui.bannermod.society.memory.title"));
        this.parent = parent;
        this.snapshot = snapshot == null ? NpcPhaseOneSnapshot.empty() : snapshot;
    }

    @Override
    protected void init() {
        super.init();
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.addRenderableWidget(new MemoryButton(
                this.left + WIDTH - 62,
                this.top + HEIGHT - 26,
                48,
                16,
                MilitaryGuiStyle.clampLabel(this.font, Component.translatable("gui.bannermod.common.back"), 42),
                button -> onClose()
        ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        MilitaryGuiStyle.parchmentPanel(graphics, this.left, this.top, WIDTH, HEIGHT);
        MilitaryGuiStyle.titleStrip(graphics, this.left + 8, this.top + 8, WIDTH - 16, 16);
        MilitaryGuiStyle.drawCenteredTitle(graphics, this.font, this.title, this.left + 8, this.top + 12, WIDTH - 16);

        renderAxisBox(graphics, this.left + 14, this.top + 34, Component.translatable("gui.bannermod.society.social.trust"), this.snapshot.trustScore(), MilitaryGuiStyle.TEXT_GOOD);
        renderAxisBox(graphics, this.left + 102, this.top + 34, Component.translatable("gui.bannermod.society.social.fear"), this.snapshot.fearScore(), MilitaryGuiStyle.TEXT_DENIED);
        renderAxisBox(graphics, this.left + 190, this.top + 34, Component.translatable("gui.bannermod.society.social.anger"), this.snapshot.angerScore(), 0xFF9B4D33);
        renderAxisBox(graphics, this.left + 58, this.top + 66, Component.translatable("gui.bannermod.society.social.gratitude"), this.snapshot.gratitudeScore(), 0xFF477A42);
        renderAxisBox(graphics, this.left + 146, this.top + 66, Component.translatable("gui.bannermod.society.social.loyalty"), this.snapshot.loyaltyScore(), MilitaryGuiStyle.TEXT_DARK);

        MilitaryGuiStyle.parchmentInset(graphics, this.left + 14, this.top + 104, WIDTH - 28, 96);
        graphics.drawString(this.font, Component.translatable("gui.bannermod.society.memory.recent"), this.left + 20, this.top + 110, MilitaryGuiStyle.TEXT_MUTED, false);
        List<NpcMemorySummarySnapshot> memories = this.snapshot.safeRecentMemories();
        if (memories.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("gui.bannermod.society.memory.none"), this.left + 20, this.top + 126, MilitaryGuiStyle.TEXT_DARK, false);
        } else {
            for (int i = 0; i < Math.min(3, memories.size()); i++) {
                renderMemoryEntry(graphics, this.left + 18, this.top + 122 + i * 24, WIDTH - 36, memories.get(i));
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderAxisBox(GuiGraphics graphics, int x, int y, Component label, int value, int color) {
        MilitaryGuiStyle.parchmentInset(graphics, x, y, 74, 24);
        graphics.drawString(this.font, label, x + 6, y + 4, MilitaryGuiStyle.TEXT_MUTED, false);
        graphics.drawString(this.font, Integer.toString(value), x + 6, y + 14, color, false);
    }

    private void renderMemoryEntry(GuiGraphics graphics, int x, int y, int width, NpcMemorySummarySnapshot memory) {
        MilitaryGuiStyle.insetPanel(graphics, x, y, width, 20);
        String title = Component.translatable(memory.typeTranslationKey()).getString();
        String detail = Component.translatable(memory.scopeTranslationKey()).getString()
                + " | " + memory.actorLabelOrDash()
                + " | " + memory.intensity();
        graphics.drawString(this.font, this.font.plainSubstrByWidth(title, width - 12), x + 6, y + 4,
                memory.positive() ? MilitaryGuiStyle.TEXT_GOOD : MilitaryGuiStyle.TEXT_DARK, false);
        graphics.drawString(this.font, this.font.plainSubstrByWidth(detail, width - 12), x + 6, y + 12, MilitaryGuiStyle.TEXT_MUTED, false);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class MemoryButton extends ExtendedButton {
        MemoryButton(int x, int y, int width, int height, Component label, OnPress handler) {
            super(x, y, width, height, label, handler);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            MilitaryGuiStyle.commandButton(graphics, Minecraft.getInstance().font, mouseX, mouseY,
                    getX(), getY(), width, height, getMessage(), active, false);
        }
    }
}
