package com.talhanation.bannermod.client.military.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.component.ActivateableButton;
import com.talhanation.bannermod.client.military.gui.widgets.RecruitsCheckBox;
import com.talhanation.bannermod.entity.military.ScoutEntity;
import com.talhanation.bannermod.network.messages.military.MessageScoutTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ScoutScreen extends RecruitsScreenBase {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/gui_big.png");
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
    private int statusColor = FONT_COLOR;
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
                this.statusColor = 0x2E5D32;
            }
        );
        checkBoxScouting.setTooltip(Tooltip.create(TOOLTIP_SCOUTING));
        addRenderableWidget(checkBoxScouting);
        updateStatusLine();
    }

    private void updateStatusLine() {
        if (this.statusColor == 0x2E5D32 && this.statusLine != Component.empty()) {
            return;
        }
        this.statusLine = Component.translatable(scouting
                ? "gui.recruits.scout.status.active"
                : "gui.recruits.scout.status.idle");
        this.statusColor = scouting ? 0x2E5D32 : 0x6E5A45;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);
        drawFramedPanel(guiGraphics, guiLeft + 18, guiTop + 20, xSize - 36, 44);
        drawDarkInset(guiGraphics, guiLeft + 24, guiTop + 108, xSize - 48, 18);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawString(font, TITLE, guiLeft + xSize / 2 - font.width(TITLE) / 2, guiTop + 8, FONT_COLOR, false);
        guiGraphics.drawString(font, SUBTITLE, guiLeft + xSize / 2 - font.width(SUBTITLE) / 2, guiTop + 33, FONT_COLOR, false);
        guiGraphics.drawString(font, statusLine, guiLeft + 28, guiTop + 114, statusColor, false);
    }
}
