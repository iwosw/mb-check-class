package com.talhanation.recruits.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.inventory.GovernorContainer;
import com.talhanation.recruits.network.MessageOpenGovernorScreen;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GovernorScreen extends ScreenBase<GovernorContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MOD_ID, "textures/gui/professions/blank_gui.png");
    private static GovernorViewState latestState = GovernorViewState.empty();

    private final Player player;
    private final AbstractRecruitEntity recruit;

    public GovernorScreen(GovernorContainer container, Inventory playerInventory, Component title) {
        super(TEXTURE, container, playerInventory, title);
        this.imageWidth = 195;
        this.imageHeight = 160;
        this.player = container.getPlayerEntity();
        this.recruit = container.getRecruit();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        Main.SIMPLE_CHANNEL.sendToServer(new MessageOpenGovernorScreen(this.recruit.getUUID(), false));
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        GovernorViewState state = latestState;
        int x = leftPos + 10;
        int y = topPos + 10;
        guiGraphics.drawString(font, Component.literal("Governor: " + recruit.getName().getString()), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, Component.literal("Settlement: " + state.settlementStatus), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, Component.literal("Citizens: " + state.citizenCount), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, Component.literal("Taxes: " + state.taxesCollected + "/" + state.taxesDue), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, Component.literal("Heartbeat: " + state.lastHeartbeatTick), x, y, 4210752, false);
        y += 16;
        guiGraphics.drawString(font, Component.literal("Incidents:"), x, y, 4210752, false);
        y += 12;
        for (String incident : state.incidents) {
            guiGraphics.drawString(font, Component.literal("- " + incident), x, y, 4210752, false);
            y += 10;
        }
        if (state.incidents.isEmpty()) {
            guiGraphics.drawString(font, Component.literal("- none"), x, y, 4210752, false);
            y += 10;
        }
        y += 6;
        guiGraphics.drawString(font, Component.literal("Recommendations:"), x, y, 4210752, false);
        y += 12;
        for (String recommendation : state.recommendations) {
            guiGraphics.drawString(font, Component.literal("- " + recommendation), x, y, 4210752, false);
            y += 10;
        }
        if (state.recommendations.isEmpty()) {
            guiGraphics.drawString(font, Component.literal("- none"), x, y, 4210752, false);
        }
    }

    public static void applyUpdate(UUID recruitId,
                                   String settlementStatus,
                                   int citizenCount,
                                   int taxesDue,
                                   int taxesCollected,
                                   long lastHeartbeatTick,
                                   List<String> incidents,
                                   List<String> recommendations) {
        latestState = new GovernorViewState(recruitId, settlementStatus, citizenCount, taxesDue, taxesCollected, lastHeartbeatTick,
                new ArrayList<>(incidents), new ArrayList<>(recommendations));
    }

    private record GovernorViewState(UUID recruitId,
                                     String settlementStatus,
                                     int citizenCount,
                                     int taxesDue,
                                     int taxesCollected,
                                     long lastHeartbeatTick,
                                     List<String> incidents,
                                     List<String> recommendations) {
        private static GovernorViewState empty() {
            return new GovernorViewState(new UUID(0L, 0L), "unknown", 0, 0, 0, 0L, List.of(), List.of());
        }
    }
}
