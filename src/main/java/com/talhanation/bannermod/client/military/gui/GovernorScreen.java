package com.talhanation.bannermod.client.military.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.governance.BannerModGovernorPolicy;
import com.talhanation.bannermod.inventory.military.GovernorContainer;
import com.talhanation.bannermod.network.messages.military.MessageOpenGovernorScreen;
import com.talhanation.bannermod.network.messages.military.MessageUpdateGovernorPolicy;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GovernorScreen extends ScreenBase<GovernorContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(BannerModMain.MOD_ID, "textures/gui/professions/blank_gui.png");
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
        addPolicyButtons(BannerModGovernorPolicy.GARRISON_PRIORITY, 88);
        addPolicyButtons(BannerModGovernorPolicy.FORTIFICATION_PRIORITY, 112);
        addPolicyButtons(BannerModGovernorPolicy.TAX_PRESSURE, 136);
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenGovernorScreen(this.recruit.getUUID(), false));
    }

    private void addPolicyButtons(BannerModGovernorPolicy policy, int yOffset) {
        addRenderableWidget(new ExtendedButton(this.leftPos + 150, this.topPos + yOffset, 16, 16, Component.literal("-"), button -> stepPolicy(policy, -1)));
        addRenderableWidget(new ExtendedButton(this.leftPos + 170, this.topPos + yOffset, 16, 16, Component.literal("+"), button -> stepPolicy(policy, 1)));
    }

    private void stepPolicy(BannerModGovernorPolicy policy, int delta) {
        GovernorViewState state = latestState;
        int currentValue = switch (policy) {
            case GARRISON_PRIORITY -> state.garrisonPriority;
            case FORTIFICATION_PRIORITY -> state.fortificationPriority;
            case TAX_PRESSURE -> state.taxPressure;
        };
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateGovernorPolicy(this.recruit.getUUID(), policy, policy.clamp(currentValue + delta)));
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
        guiGraphics.drawString(font, Component.literal("Treasury: " + state.treasuryBalance + " (net " + state.lastTreasuryNet + ")"), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, Component.literal("Projected: " + state.projectedTreasuryBalance), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, Component.literal("Heartbeat: " + state.lastHeartbeatTick), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, Component.literal("Garrison guidance: " + readableToken(state.garrisonRecommendation)), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, Component.literal("Fortification advice: " + readableToken(state.fortificationRecommendation)), x, y, 4210752, false);
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
        guiGraphics.drawString(font, Component.literal("garrison priority: " + BannerModGovernorPolicy.GARRISON_PRIORITY.valueLabel(state.garrisonPriority)), leftPos + 10, topPos + 92, 4210752, false);
        guiGraphics.drawString(font, Component.literal("fortification priority: " + BannerModGovernorPolicy.FORTIFICATION_PRIORITY.valueLabel(state.fortificationPriority)), leftPos + 10, topPos + 116, 4210752, false);
        guiGraphics.drawString(font, Component.literal("tax pressure: " + BannerModGovernorPolicy.TAX_PRESSURE.valueLabel(state.taxPressure)), leftPos + 10, topPos + 140, 4210752, false);
    }

    public static void applyUpdate(UUID recruitId,
                                    String settlementStatus,
                                    int citizenCount,
                                    int taxesDue,
                                    int taxesCollected,
                                    long lastHeartbeatTick,
                                    String garrisonRecommendation,
                                    String fortificationRecommendation,
                                     int garrisonPriority,
                                     int fortificationPriority,
                                     int taxPressure,
                                     int treasuryBalance,
                                     int lastTreasuryNet,
                                     int projectedTreasuryBalance,
                                     List<String> incidents,
                                     List<String> recommendations) {
        latestState = new GovernorViewState(recruitId, settlementStatus, citizenCount, taxesDue, taxesCollected, lastHeartbeatTick,
                garrisonRecommendation, fortificationRecommendation, garrisonPriority, fortificationPriority, taxPressure,
                treasuryBalance, lastTreasuryNet, projectedTreasuryBalance,
                new ArrayList<>(incidents), new ArrayList<>(recommendations));
    }

    private static String readableToken(String token) {
        return token == null || token.isBlank() ? "none" : token.replace('_', ' ');
    }

    private record GovernorViewState(UUID recruitId,
                                     String settlementStatus,
                                     int citizenCount,
                                     int taxesDue,
                                     int taxesCollected,
                                     long lastHeartbeatTick,
                                     String garrisonRecommendation,
                                     String fortificationRecommendation,
                                     int garrisonPriority,
                                     int fortificationPriority,
                                     int taxPressure,
                                     int treasuryBalance,
                                     int lastTreasuryNet,
                                     int projectedTreasuryBalance,
                                     List<String> incidents,
                                     List<String> recommendations) {
        private static GovernorViewState empty() {
            return new GovernorViewState(new UUID(0L, 0L), "unknown", 0, 0, 0, 0L,
                    "hold_course", "hold_course",
                    BannerModGovernorPolicy.DEFAULT_VALUE, BannerModGovernorPolicy.DEFAULT_VALUE, BannerModGovernorPolicy.DEFAULT_VALUE,
                    0, 0, 0,
                    List.of(), List.of());
        }
    }
}
