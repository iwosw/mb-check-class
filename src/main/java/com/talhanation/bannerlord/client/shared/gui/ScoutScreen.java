package com.talhanation.bannerlord.client.shared.gui;

import com.talhanation.bannerlord.entity.shared.*;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.recruits.Main;
import com.talhanation.bannerlord.client.shared.gui.component.ActivateableButton;
import com.talhanation.bannerlord.client.shared.gui.widgets.RecruitsCheckBox;
import com.talhanation.bannerlord.entity.military.ScoutEntity;
import com.talhanation.recruits.network.MessageScoutTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ScoutScreen extends RecruitsScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MOD_ID, "textures/gui/gui_big.png");
    private static final Component TITLE = Component.translatable("gui.bannermod.more_screen.title");
    private final Player player;
    private final ScoutEntity scout;
    private ScoutEntity.State task;
    private static final MutableComponent SCOUTING = Component.translatable("gui.bannermod.inv.text.scoutScoutTask");
    private static final MutableComponent TOOLTIP_SCOUTING = Component.translatable("gui.bannermod.inv.tooltip.scoutScoutTask");
    private RecruitsCheckBox checkBoxScouting;
    public boolean scouting;
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
                Main.SIMPLE_CHANNEL.sendToServer(new MessageScoutTask(scout.getUUID(), scouting ? 1 : 0));
            }
        );
        checkBoxScouting.setTooltip(Tooltip.create(TOOLTIP_SCOUTING));
        addRenderableWidget(checkBoxScouting);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
    }
}
