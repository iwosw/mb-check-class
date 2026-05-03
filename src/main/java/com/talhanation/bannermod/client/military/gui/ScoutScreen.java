package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.widgets.RecruitsCheckBox;
import com.talhanation.bannermod.entity.military.ScoutEntity;
import com.talhanation.bannermod.network.messages.military.MessageScoutTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class ScoutScreen extends RecruitsScreenBase {

    private static final Component TITLE = Component.translatable("gui.recruits.more_screen.title");
    private final Player player;
    private final ScoutEntity scout;
    private ScoutEntity.State task;
    private static final MutableComponent SCOUTING = Component.translatable("gui.recruits.inv.text.scoutScoutTask");
    private static final MutableComponent TOOLTIP_SCOUTING = Component.translatable("gui.recruits.inv.tooltip.scoutScoutTask");
    private static final Component SUBTITLE = Component.translatable("gui.recruits.scout.subtitle");
    private RecruitsCheckBox checkBoxScouting;
    public boolean scouting;
    private Component statusLine = Component.empty();
    private int statusColor = MilitaryGuiStyle.TEXT_DARK;
    public ScoutScreen(ScoutEntity scout, Player player) {
        super(TITLE, 195,160);
        this.player = player;
        this.scout = scout;
    }

    @Override
    protected void init() {
        super.init();
        this.task = ScoutEntity.State.fromIndex(scout.getTaskState());
        this.scouting = task == ScoutEntity.State.SCOUTING;
        setButtons();
    }

    private void setButtons(){
        clearWidgets();

        checkBoxScouting = new RecruitsCheckBox(guiLeft + 32, guiTop + ySize - 120 - 7, 130, 20, SCOUTING, this.scouting,
        (bool) -> {
                this.scouting = bool;
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageScoutTask(scout.getUUID(), scouting ? 1 : 0));
                this.statusLine = Component.translatable(scouting
                        ? "gui.recruits.scout.status.accepted_start"
                        : "gui.recruits.scout.status.accepted_stop");
                this.statusColor = MilitaryGuiStyle.TEXT_GOOD;
            }
        );
        checkBoxScouting.setTooltip(Tooltip.create(TOOLTIP_SCOUTING));
        addRenderableWidget(checkBoxScouting);
        updateStatusLine();
    }

    private void updateStatusLine() {
        if (this.statusColor == MilitaryGuiStyle.TEXT_GOOD && this.statusLine != Component.empty()) {
            return;
        }
        this.statusLine = Component.translatable(scouting
                ? "gui.recruits.scout.status.active"
                : "gui.recruits.scout.status.idle");
        this.statusColor = scouting ? MilitaryGuiStyle.TEXT_GOOD : MilitaryGuiStyle.TEXT_MUTED;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.parchmentPanel(guiGraphics, guiLeft, guiTop, xSize, ySize);
        MilitaryGuiStyle.titleStrip(guiGraphics, guiLeft + 8, guiTop + 6, xSize - 16, 14);
        MilitaryGuiStyle.parchmentInset(guiGraphics, guiLeft + 18, guiTop + 24, xSize - 36, 40);
        MilitaryGuiStyle.insetPanel(guiGraphics, guiLeft + 24, guiTop + 108, xSize - 48, 18);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        Component clampedTitle = MilitaryGuiStyle.clampLabel(font, TITLE, xSize - 20);
        MilitaryGuiStyle.drawCenteredTitle(guiGraphics, font, clampedTitle, guiLeft, guiTop + 9, xSize);
        Component clampedSubtitle = MilitaryGuiStyle.clampLabel(font, SUBTITLE, xSize - 40);
        guiGraphics.drawCenteredString(font, clampedSubtitle, guiLeft + xSize / 2, guiTop + 33, MilitaryGuiStyle.TEXT_DARK);
        Component clampedStatus = MilitaryGuiStyle.clampLabel(font, statusLine, xSize - 56);
        guiGraphics.drawString(font, clampedStatus, guiLeft + 28, guiTop + 114, statusColor, false);
    }
}
