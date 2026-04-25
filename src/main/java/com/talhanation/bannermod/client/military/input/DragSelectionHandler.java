package com.talhanation.bannermod.client.military.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.network.messages.military.MessageSelectRecruits;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HYW-style drag-box selection for recruits.
 *
 * <p>Interaction model:</p>
 * <ul>
 *   <li>Hold the {@link #SELECT_KEY} key (default G). While held, each frame captures
 *       the mouse position and renders a translucent selection rectangle between the
 *       press-time mouse position and the current cursor.</li>
 *   <li>On release, project every owned recruit in a generous radius to screen space
 *       via {@link WorldToScreenProjector}; any recruit whose projection falls inside
 *       the rectangle is sent to the server via {@link MessageSelectRecruits}.</li>
 *   <li>Holding the sprint key (left-shift by default in the vanilla binding) additively
 *       merges with the existing selection; otherwise the server clears first.</li>
 * </ul>
 *
 * <p>Renders on the Forge client event bus; key mapping registered on the mod event bus.</p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DragSelectionHandler {
    public static final KeyMapping SELECT_KEY = new KeyMapping(
            "key.bannermod.drag_select",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G),
            "Bannermod"
    );

    private static final double CANDIDATE_RADIUS = 96.0D;
    private static final int RECT_FILL_COLOR = 0x22_44_AA_CC;
    private static final int RECT_BORDER_COLOR = 0xFF_99_FF_FF;

    private static boolean dragging;
    private static double dragStartX;
    private static double dragStartY;
    private static double dragCurrentX;
    private static double dragCurrentY;

    private DragSelectionHandler() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SELECT_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.level == null) {
            resetState();
            return;
        }
        boolean down = SELECT_KEY.isDown();
        double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();

        if (down && !dragging) {
            dragging = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            dragCurrentX = mouseX;
            dragCurrentY = mouseY;
        } else if (down) {
            dragCurrentX = mouseX;
            dragCurrentY = mouseY;
        } else if (!down && dragging) {
            dragCurrentX = mouseX;
            dragCurrentY = mouseY;
            finaliseSelection(mc);
            resetState();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!dragging) return;
        GuiGraphics graphics = event.getGuiGraphics();
        if (graphics == null) return;
        int x1 = (int) Math.min(dragStartX, dragCurrentX);
        int y1 = (int) Math.min(dragStartY, dragCurrentY);
        int x2 = (int) Math.max(dragStartX, dragCurrentX);
        int y2 = (int) Math.max(dragStartY, dragCurrentY);

        if (x2 - x1 < 2 || y2 - y1 < 2) return;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        graphics.fill(x1, y1, x2, y2, RECT_FILL_COLOR);
        graphics.fill(x1, y1, x2, y1 + 1, RECT_BORDER_COLOR);
        graphics.fill(x1, y2 - 1, x2, y2, RECT_BORDER_COLOR);
        graphics.fill(x1, y1, x1 + 1, y2, RECT_BORDER_COLOR);
        graphics.fill(x2 - 1, y1, x2, y2, RECT_BORDER_COLOR);
        pose.popPose();
    }

    private static void finaliseSelection(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        double x1 = Math.min(dragStartX, dragCurrentX);
        double y1 = Math.min(dragStartY, dragCurrentY);
        double x2 = Math.max(dragStartX, dragCurrentX);
        double y2 = Math.max(dragStartY, dragCurrentY);

        if (x2 - x1 < 4 || y2 - y1 < 4) {
            // Too small — treat as an accidental click. Don't alter server selection.
            return;
        }

        AABB candidateBounds = player.getBoundingBox().inflate(CANDIDATE_RADIUS);
        List<UUID> selected = new ArrayList<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof AbstractRecruitEntity recruit)) continue;
            if (!candidateBounds.intersects(recruit.getBoundingBox())) continue;
            if (!recruit.isAlive() || !ownedByUs(recruit, player)) continue;
            WorldToScreenProjector.Projection projection = WorldToScreenProjector.project(recruit.getBoundingBox().getCenter());
            if (projection == null || !projection.visible()) continue;
            double sx = projection.screenX();
            double sy = projection.screenY();
            if (sx >= x1 && sx <= x2 && sy >= y1 && sy <= y2) {
                selected.add(recruit.getUUID());
            }
        }
        boolean additive = mc.options.keyShift.isDown();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSelectRecruits(selected, !additive));
    }

    private static boolean ownedByUs(AbstractRecruitEntity recruit, Player player) {
        if (!recruit.isOwned()) return false;
        UUID ownerUuid = recruit.getOwnerUUID();
        return ownerUuid != null && ownerUuid.equals(player.getUUID());
    }

    private static void resetState() {
        dragging = false;
        dragStartX = 0;
        dragStartY = 0;
        dragCurrentX = 0;
        dragCurrentY = 0;
    }
}
