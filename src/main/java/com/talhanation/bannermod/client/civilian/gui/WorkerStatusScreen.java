package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.military.gui.group.RecruitsCommandButton;
import com.talhanation.bannermod.entity.civilian.WorkerInspectionSnapshot;
import com.talhanation.bannermod.network.messages.civilian.MessageConvertWorkerToCitizen;
import com.talhanation.bannermod.network.messages.civilian.MessageOpenWorkerScreen;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorkerStatusScreen extends Screen {
    private static final int WIDTH = 252;
    private static final int HEIGHT = 188;

    private final WorkerInspectionSnapshot snapshot;
    private int left;
    private int top;

    public WorkerStatusScreen(WorkerInspectionSnapshot snapshot) {
        super(Component.translatable("gui.bannermod.worker_screen.title"));
        this.snapshot = snapshot;
    }

    @Override
    protected void init() {
        super.init();
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;

        RecruitsCommandButton refresh = this.addRenderableWidget(new RecruitsCommandButton(
                this.left + 52,
                this.top + HEIGHT - 16,
                text("gui.bannermod.worker_screen.refresh"),
                button -> BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenWorkerScreen(this.snapshot.workerUuid()))
        ));
        refresh.setTooltip(Tooltip.create(text("gui.bannermod.worker_screen.refresh.tooltip")));

        RecruitsCommandButton convert = this.addRenderableWidget(new RecruitsCommandButton(
                this.left + WIDTH / 2,
                this.top + HEIGHT - 16,
                text("gui.bannermod.worker_screen.convert"),
                button -> {
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageConvertWorkerToCitizen(this.snapshot.workerUuid()));
                    this.onClose();
                }
        ));
        convert.active = this.snapshot.canConvert();
        if (this.snapshot.convertBlockedReasonKey() != null) {
            convert.setTooltip(Tooltip.create(text(this.snapshot.convertBlockedReasonKey())));
        }

        RecruitsCommandButton close = this.addRenderableWidget(new RecruitsCommandButton(
                this.left + WIDTH - 52,
                this.top + HEIGHT - 16,
                text("gui.bannermod.worker_screen.close"),
                button -> this.onClose()
        ));
        close.setTooltip(Tooltip.create(text("gui.bannermod.worker_screen.close.tooltip")));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        MilitaryGuiStyle.parchmentPanel(graphics, this.left, this.top, WIDTH, HEIGHT);
        MilitaryGuiStyle.titleStrip(graphics, this.left + 8, this.top + 8, WIDTH - 16, 16);
        MilitaryGuiStyle.drawCenteredTitle(graphics, this.font, this.title, this.left + 8, this.top + 12, WIDTH - 16);
        MilitaryGuiStyle.drawBadge(graphics, this.font, Component.translatable(this.snapshot.claimRelationKey()), this.left + 16, this.top + 32, 110, MilitaryGuiStyle.TEXT_WARN);
        graphics.drawString(this.font, this.snapshot.workerName(), this.left + 132, this.top + 35, MilitaryGuiStyle.TEXT_DARK, false);

        renderInfoBlock(graphics, this.left + 14, this.top + 52, WIDTH - 28, 58);
        drawLabelValue(graphics, text("gui.bannermod.worker_screen.profession"), Component.translatable(this.snapshot.professionKey()), this.left + 20, this.top + 58);
        drawLabelValue(graphics, text("gui.bannermod.worker_screen.owner"), Component.literal(this.snapshot.ownerLabel()), this.left + 20, this.top + 72);
        drawLabelValue(graphics, text("gui.bannermod.worker_screen.political"), Component.literal(this.snapshot.politicalLabel()), this.left + 20, this.top + 86);
        drawLabelValue(graphics, text("gui.bannermod.worker_screen.assignment"), Component.literal(this.snapshot.assignmentLabel()), this.left + 20, this.top + 100);

        renderTextBox(graphics, this.left + 14, this.top + 116, WIDTH - 28, 24,
                text("gui.bannermod.worker_screen.problem"),
                Component.literal(this.snapshot.problemLabel()),
                isClearState(this.snapshot.problemLabel()) ? MilitaryGuiStyle.TEXT_GOOD : MilitaryGuiStyle.TEXT_DENIED);
        renderTextBox(graphics, this.left + 14, this.top + 144, WIDTH - 28, 24,
                text("gui.bannermod.worker_screen.transport"),
                Component.literal(this.snapshot.transportLabel()),
                MilitaryGuiStyle.TEXT_DARK);
        graphics.drawCenteredString(this.font, text("gui.bannermod.worker_screen.hint"), this.left + WIDTH / 2, this.top + 171, MilitaryGuiStyle.TEXT_MUTED);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderInfoBlock(GuiGraphics graphics, int x, int y, int width, int height) {
        MilitaryGuiStyle.insetPanel(graphics, x, y, width, height);
    }

    private void renderTextBox(GuiGraphics graphics, int x, int y, int width, int height, Component label, Component value, int color) {
        MilitaryGuiStyle.parchmentInset(graphics, x, y, width, height);
        graphics.drawString(this.font, label, x + 6, y + 4, MilitaryGuiStyle.TEXT_MUTED, false);
        List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(value, width - 12);
        if (!lines.isEmpty()) {
            graphics.drawString(this.font, lines.getFirst(), x + 6, y + 14, color, false);
        }
    }

    private void drawLabelValue(GuiGraphics graphics, Component label, Component value, int x, int y) {
        graphics.drawString(this.font, label, x, y, MilitaryGuiStyle.TEXT_MUTED, false);
        graphics.drawString(this.font, this.font.plainSubstrByWidth(value.getString(), 136), x + 78, y, MilitaryGuiStyle.TEXT_DARK, false);
    }

    private boolean isClearState(String value) {
        return value == null || value.isBlank() || "none reported".equalsIgnoreCase(value);
    }

    private Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
