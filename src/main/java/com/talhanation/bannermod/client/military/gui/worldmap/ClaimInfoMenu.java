package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.client.military.gui.component.BannerRenderer;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ClaimInfoMenu {
    private final WorldMapScreen parent;
    private boolean visible = false;
    private RecruitsClaim currentClaim;
    private BannerRenderer bannerRenderer;
    public int x, y;
    public int width = 152, height = 206;

    public ClaimInfoMenu(WorldMapScreen parent) {
        this.parent = parent;
    }

    public void init() {
        this.bannerRenderer = new BannerRenderer(null);
    }

    public void openForClaim(RecruitsClaim claim, int x, int y) {
        this.currentClaim = claim;
        this.visible = true;
        bannerRenderer.setBannerItem(ItemStack.EMPTY);

        this.x = x;
        this.y = y;

        ensureWithinScreen();
    }

    private void ensureWithinScreen() {
        if (x + width > parent.width) {
            x = parent.width - width - 10;
        }
        if (x < 10) {
            x = 10;
        }
        if (y + height > parent.height) {
            y = parent.height - height - 10;
        }
        if (y < 10) {
            y = 10;
        }
    }

    public void render(GuiGraphics guiGraphics) {
        if (!visible || currentClaim == null) return;

        WorldMapRenderPrimitives.panel(guiGraphics, x, y, width, height);
        guiGraphics.fill(x + 1, y + 18, x + width - 1, y + 19, 0x665A4025);

        guiGraphics.drawCenteredString(parent.getMinecraft().font, currentClaim.getName(), x + width / 2, y + 5, 0xFFFFFF);

        bannerRenderer.renderBanner(guiGraphics, x - 7 + width / 2, y + 60,  this.width, this.height, 46);

        int textY = y + 98;
        guiGraphics.drawString(parent.getMinecraft().font, Component.translatable("gui.bannermod.claim.faction"), x + 6, textY, 0xFFE0B86A, false);
        textY += 11;
        guiGraphics.drawWordWrap(parent.getMinecraft().font, Component.literal(ownerPoliticalEntityName(currentClaim.getOwnerPoliticalEntityId())), x + 6, textY, width - 12, 0xFFFFFF);

        textY += 24;
        guiGraphics.drawString(parent.getMinecraft().font, Component.translatable("gui.bannermod.claim.player"), x + 6, textY, 0xFFE0B86A, false);
        textY += 11;
        guiGraphics.drawWordWrap(parent.getMinecraft().font,
                Component.literal(currentClaim.getPlayerInfo() != null ? currentClaim.getPlayerInfo().getName() : Component.translatable("gui.bannermod.common.unknown").getString()),
                x + 6, textY, width - 12, 0xFFFFFF);

        textY += 22;
        guiGraphics.drawString(parent.getMinecraft().font,
                Component.translatable("gui.bannermod.claim.block_placing").getString() + ": " + boolText(currentClaim.isBlockPlacementAllowed()),
                x + 6, textY, 0xFFFFFF);

        textY += 15;
        guiGraphics.drawString(parent.getMinecraft().font,
                Component.translatable("gui.bannermod.claim.block_breaking").getString() + ": " + boolText(currentClaim.isBlockBreakingAllowed()),
                x + 6, textY, 0xFFFFFF);

        textY += 15;
        guiGraphics.drawString(parent.getMinecraft().font,
                Component.translatable("gui.bannermod.claim.block_interaction").getString() + ": " + boolText(currentClaim.isBlockInteractionAllowed()),
                x + 6, textY, 0xFFFFFF);

        textY += 15;
        guiGraphics.drawString(parent.getMinecraft().font,
                Component.translatable("gui.bannermod.claim.info.chunks", currentClaim.getClaimedChunks().size(), 50).getString(),
                x + 6, textY, 0xFFFFFF);

        textY += 16;
        Component hint = parent.isPlayerClaimLeader(currentClaim)
                ? Component.translatable("gui.bannermod.claim.info.hint.leader")
                : Component.translatable("gui.bannermod.claim.info.hint.viewer");
        guiGraphics.drawWordWrap(parent.getMinecraft().font, hint, x + 6, textY, width - 12, 0xFFB8A17A);

    }

    private static String boolText(boolean value) {
        return Component.translatable(value ? "gui.bannermod.claim.true" : "gui.bannermod.claim.false").getString();
    }

    private static String ownerPoliticalEntityName(UUID entityId) {
        if (entityId == null) {
            return Component.translatable("gui.bannermod.common.none").getString();
        }
        PoliticalEntityRecord entity = WarClientState.entityById(entityId);
        if (entity == null) {
            return Component.translatable("gui.bannermod.common.unknown").getString();
        }
        return entity.name().isBlank()
                ? Component.translatable("gui.bannermod.states.unnamed").getString()
                : entity.name();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;


        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!visible) return false;

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        return false;
    }

    private boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void close() {
        this.visible = false;
        this.currentClaim = null;
        this.bannerRenderer.setBannerItem(ItemStack.EMPTY);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
