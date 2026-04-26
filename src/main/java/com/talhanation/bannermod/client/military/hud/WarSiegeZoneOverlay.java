package com.talhanation.bannermod.client.military.hud;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.runtime.SiegeStandardRecord;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Lightweight in-world banner shown when the local player stands inside the radius of any
 * siege standard whose war is still {@code DECLARED} / {@code ACTIVE} / {@code IN_SIEGE_WINDOW}.
 *
 * <p>Reads the synced {@link WarClientState} so it follows whatever the war broadcaster pushed
 * — no client RPC, no per-tick scan past the in-memory siege list.</p>
 */
@Mod.EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WarSiegeZoneOverlay {

    private WarSiegeZoneOverlay() {
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                VanillaGuiOverlay.HOTBAR.id(),
                "bannermod_siege_zone",
                WarSiegeZoneOverlay::render
        );
    }

    private static void render(net.minecraftforge.client.gui.overlay.ForgeGui gui,
                               GuiGraphics graphics,
                               float partialTick,
                               int screenWidth,
                               int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options.hideGui) return;
        LocalPlayer player = mc.player;
        if (player == null) return;
        SiegeContext context = nearestActiveSiege(player);
        if (context == null) return;
        renderBanner(graphics, mc.font, screenWidth, context);
    }

    private static void renderBanner(GuiGraphics graphics, Font font, int screenWidth, SiegeContext context) {
        String headline = "Siege zone: " + context.warName;
        String subline = context.sideName + "  •  r=" + context.siege.radius() + "  •  " + context.warState.name();
        int width = Math.max(font.width(headline), font.width(subline)) + 12;
        int height = 24;
        int x = (screenWidth - width) / 2;
        int y = 6;
        graphics.fill(x, y, x + width, y + height, 0xC0101010);
        graphics.renderOutline(x, y, width, height, 0xFFFFFFFF);
        graphics.drawString(font, headline, x + 6, y + 4, 0xFFFFAA55, false);
        graphics.drawString(font, subline, x + 6, y + 14, 0xFFCCCCCC, false);
    }

    @Nullable
    private static SiegeContext nearestActiveSiege(Player player) {
        SiegeStandardRecord best = null;
        double bestSqr = Double.MAX_VALUE;
        WarDeclarationRecord bestWar = null;
        for (SiegeStandardRecord siege : WarClientState.sieges()) {
            if (siege.pos() == null) continue;
            WarDeclarationRecord war = warById(siege.warId());
            if (war == null || !isActiveState(war.state())) continue;
            double dx = player.getX() - (siege.pos().getX() + 0.5);
            double dz = player.getZ() - (siege.pos().getZ() + 0.5);
            double sqr = dx * dx + dz * dz;
            double radiusSqr = (double) siege.radius() * (double) siege.radius();
            if (sqr > radiusSqr) continue;
            if (sqr < bestSqr) {
                best = siege;
                bestSqr = sqr;
                bestWar = war;
            }
        }
        if (best == null || bestWar == null) return null;
        return new SiegeContext(
                best,
                bestWar.state(),
                entityName(bestWar.id()),
                entityName(best.sidePoliticalEntityId())
        );
    }

    @Nullable
    private static WarDeclarationRecord warById(UUID warId) {
        if (warId == null) return null;
        for (WarDeclarationRecord war : WarClientState.wars()) {
            if (warId.equals(war.id())) return war;
        }
        return null;
    }

    private static boolean isActiveState(WarState state) {
        return state == WarState.DECLARED || state == WarState.ACTIVE || state == WarState.IN_SIEGE_WINDOW;
    }

    private static String entityName(UUID id) {
        if (id == null) return "?";
        PoliticalEntityRecord entity = WarClientState.entityById(id);
        if (entity == null || entity.name().isBlank()) {
            return id.toString().substring(0, 8);
        }
        return entity.name();
    }

    private record SiegeContext(SiegeStandardRecord siege,
                                 WarState warState,
                                 String warName,
                                 String sideName) {
    }

    /** Renderer plug used in {@link RegisterGuiOverlaysEvent} (kept as a method ref above). */
    public static final IGuiOverlay HUD = WarSiegeZoneOverlay::render;
}
