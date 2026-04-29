package com.talhanation.bannermod.client.military.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ClaimOverlayRenderer {
    private static final int PANEL_HEIGHT_FULL = 45;
    private static final int PANEL_HEIGHT_COMPACT = 15;
    private static final int BACKGROUND_ALPHA = 0x0F;
    private static final int SIEGE_BACKGROUND_ALPHA = 0x70;
    private static final int OCCUPATION_BACKGROUND_ALPHA = 0x65;
    private static final int SIEGE_LABEL_COLOR = 0xFFFF5555;
    private static final int OCCUPATION_LABEL_COLOR = 0xFFFFDD88;

    private boolean dataChanged = true;

    public void render(GuiGraphics guiGraphics, Minecraft minecraft, RecruitsClaim claim, HudOverlayCoordinator.OverlayState state, ClaimAuthorityStatus authorityStatus, float alpha, int panelWidth, int x, int y, boolean underSiege, boolean occupied) {
        if (state == HudOverlayCoordinator.OverlayState.HIDDEN) return;

        Font font = minecraft.font;
        int panelHeight = (state == HudOverlayCoordinator.OverlayState.FULL) ? PANEL_HEIGHT_FULL : PANEL_HEIGHT_COMPACT;

        int bgRawAlpha = underSiege ? SIEGE_BACKGROUND_ALPHA : (occupied ? OCCUPATION_BACKGROUND_ALPHA : BACKGROUND_ALPHA);
        int bgAlpha = (int)(bgRawAlpha * alpha);

        int chrome = underSiege ? 0xFF3030 : (occupied ? 0xBB8A30 : authorityStatus.chromeColor());
        int backgroundColor = claim == null ? applyAlpha(authorityStatus.backgroundColor(), alpha) : (bgAlpha << 24) | (chrome & 0x00FFFFFF);

        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, backgroundColor);
        guiGraphics.renderOutline(x, y, panelWidth, panelHeight, withAlpha(authorityStatus.chromeColor(), alpha));

        if (state == HudOverlayCoordinator.OverlayState.FULL) {
            renderNormalFullContent(guiGraphics, claim, authorityStatus, x, y, panelWidth, font, alpha);
        } else {
            renderNormalCompactContent(guiGraphics, claim, authorityStatus, x, y, panelWidth, panelHeight, font, alpha);
        }

        if (underSiege) {
            renderSiegeBadge(guiGraphics, font, x, y + panelHeight, panelWidth, alpha);
        } else if (occupied) {
            renderOccupationBadge(guiGraphics, font, x, y + panelHeight, panelWidth, alpha);
        }

        dataChanged = false;
    }

    private void renderSiegeBadge(GuiGraphics guiGraphics, Font font, int x, int yBelowPanel, int panelWidth, float alpha) {
        String label = Component.translatable("gui.bannermod.claim_overlay.under_siege").getString();
        int textWidth = font.width(label);
        int badgeX = x + (panelWidth - textWidth) / 2;
        int badgeY = yBelowPanel + 2;
        int textAlpha = (int)(0xFF * alpha);
        int textColor = (textAlpha << 24) | (SIEGE_LABEL_COLOR & 0x00FFFFFF);
        guiGraphics.drawString(font, label, badgeX, badgeY, textColor, true);
    }

    private void renderOccupationBadge(GuiGraphics guiGraphics, Font font, int x, int yBelowPanel, int panelWidth, float alpha) {
        String label = Component.translatable("gui.bannermod.claim_overlay.occupied").getString();
        int textWidth = font.width(label);
        int badgeX = x + (panelWidth - textWidth) / 2;
        int badgeY = yBelowPanel + 2;
        int textAlpha = (int)(0xFF * alpha);
        int textColor = (textAlpha << 24) | (OCCUPATION_LABEL_COLOR & 0x00FFFFFF);
        guiGraphics.drawString(font, label, badgeX, badgeY, textColor, true);

        String consequence = Component.translatable("gui.bannermod.claim_overlay.occupied_consequence").getString();
        String truncated = truncateText(font, consequence, panelWidth - 12);
        int consequenceWidth = font.width(truncated);
        guiGraphics.drawString(font, truncated, x + (panelWidth - consequenceWidth) / 2, badgeY + 10, textColor, true);
    }

    private void renderNormalFullContent(GuiGraphics guiGraphics, RecruitsClaim claim, ClaimAuthorityStatus authorityStatus, int x, int y, int width, Font font, float alpha) {
        int textAlpha = (int)(0xFF * alpha);
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        String claimName = claim == null
                ? Component.translatable("gui.bannermod.claim_overlay.unclaimed").getString()
                : claim.getName();
        claimName = truncateText(font, claimName, width - 80);
        guiGraphics.drawString(font, claimName, x + 60, y + 10, textColor, false);

        String authorityLabel = Component.translatable(authorityStatus.labelKey()).getString();
        guiGraphics.drawString(font, truncateText(font, authorityLabel, width - 80), x + 60, y + 21, withAlpha(authorityStatus.textColor(), alpha), false);


        if (claim != null && claim.getPlayerInfo() != null) {
            String claimOwner = truncateText(font, claim.getPlayerInfo().getName(), width - 80);

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            try {
                float scale = 0.5f;
                int originalX = x + 60;
                int originalY = y + 20;
                poseStack.translate(originalX, originalY, 0);
                poseStack.scale(scale, scale, 1.0f);
                guiGraphics.drawString(font, claimOwner, 0, 10, 0xAAAAAA, false);
            } finally {
                poseStack.popPose();
            }
        }
    }

    private void renderNormalCompactContent(GuiGraphics guiGraphics, RecruitsClaim claim, ClaimAuthorityStatus authorityStatus, int x, int y, int width, int height, Font font, float alpha) {
        int textAlpha = (int)(0xFF * alpha);
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        String displayText = claim == null
                ? Component.translatable("gui.bannermod.claim_overlay.unclaimed").getString()
                : claim.getName();
        displayText = truncateText(font, displayText, width - 20);

        int textWidth = font.width(displayText);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 9) / 2;

        guiGraphics.drawString(font, displayText, textX, textY, textColor, false);
    }

    private String truncateText(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;

        while (font.width(text + "...") > maxWidth && text.length() > 3) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    private int applyAlpha(int color, float alpha) {
        int colorAlpha = (color >>> 24) & 0xFF;
        int adjustedAlpha = (int)(colorAlpha * alpha);
        return (adjustedAlpha << 24) | (color & 0x00FFFFFF);
    }

    private int withAlpha(int color, float alpha) {
        int adjustedAlpha = (int)(0xFF * alpha);
        return (adjustedAlpha << 24) | (color & 0x00FFFFFF);
    }

    public void markDataChanged() {
        dataChanged = true;
    }

    public void clearCache() {
        dataChanged = true;
    }
}
