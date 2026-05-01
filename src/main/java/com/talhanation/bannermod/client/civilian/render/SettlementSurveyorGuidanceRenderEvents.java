package com.talhanation.bannermod.client.civilian.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.render.ClientRenderPrimitives;
import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import com.talhanation.bannermod.settlement.validation.SurveyorMode;
import com.talhanation.bannermod.settlement.validation.SurveyorSessionCodec;
import com.talhanation.bannermod.settlement.validation.ValidationSession;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class SettlementSurveyorGuidanceRenderEvents {
    private static final int PANEL_BG = 0xCC2A2116;
    private static final int PANEL_INNER = 0xD63B2D1C;
    private static final int PANEL_BORDER = 0xFFC49A55;
    private static final int PANEL_TEXT = 0xFFFFE9B8;
    private static final int PANEL_MUTED = 0xFFD7BE85;
    private static final int PANEL_OK = 0xFF8EE88A;
    private static final int PANEL_WARN = 0xFFFFD36A;

    private SettlementSurveyorGuidanceRenderEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        ItemStack stack = surveyorStack(player);
        if (stack.isEmpty()) {
            return;
        }

        ValidationSession session = SurveyorSessionCodec.read(stack);
        BlockPos anchor = session == null ? BlockPos.ZERO : session.anchorPos();
        BlockPos target = targetedBlock(mc);
        if (anchor.equals(BlockPos.ZERO) && target == null) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        pose.pushPose();
        pose.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.disableDepthTest();

        if (session != null) {
            for (ZoneSelection selection : session.selections()) {
                lineBox(pose, lines, selection.toAabb().inflate(0.02D), color(selection.role()), 0.88F);
            }
        }

        BlockPos pending = SettlementSurveyorToolItem.pendingCorner(stack);
        if (pending != null && target != null) {
            lineBox(pose, lines, selectionBox(pending, target).inflate(0.04D), color(SettlementSurveyorToolItem.selectedRole(stack)), 1.0F);
        }

        BlockPos previewAnchor = anchor.equals(BlockPos.ZERO) ? target : anchor;
        if (previewAnchor != null) {
            renderAnchorMarker(pose, lines, previewAnchor);
            renderAuthorityGuide(pose, lines, previewAnchor);
            if (session == null || session.mode() == SurveyorMode.BOOTSTRAP_FORT) {
                renderStarterFortPreview(pose, lines, previewAnchor);
            }
        }

        RenderSystem.enableDepthTest();
        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.screen != null) {
            return;
        }
        ItemStack stack = surveyorStack(player);
        if (stack.isEmpty()) {
            return;
        }

        ValidationSession session = SurveyorSessionCodec.read(stack);
        ZoneRole selected = SettlementSurveyorToolItem.selectedRole(stack);
        GuiGraphics graphics = event.getGuiGraphics();
        int x = 8;
        int y = 38;
        int width = 232;
        int line = 11;
        int rows = 8;
        int height = 16 + rows * line;

        graphics.fill(x, y, x + width, y + height, PANEL_BG);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_INNER);
        graphics.renderOutline(x, y, width, height, PANEL_BORDER);

        int textY = y + 6;
        graphics.drawString(mc.font, Component.translatable("bannermod.surveyor.hud.title"), x + 7, textY, PANEL_TEXT, true);
        textY += line + 2;
        graphics.drawString(mc.font, Component.translatable("bannermod.surveyor.hud.role", SettlementSurveyorToolItem.roleLabel(selected)), x + 7, textY, color(selected), true);
        textY += line;

        if (session == null || session.anchorPos().equals(BlockPos.ZERO)) {
            graphics.drawString(mc.font, Component.translatable("bannermod.surveyor.hud.step.anchor"), x + 7, textY, PANEL_WARN, true);
            textY += line;
            graphics.drawString(mc.font, Component.translatable("bannermod.surveyor.hud.preview"), x + 7, textY, PANEL_MUTED, true);
            return;
        }

        graphics.drawString(mc.font, Component.translatable("bannermod.surveyor.hud.anchor", session.anchorPos().toShortString()), x + 7, textY, PANEL_TEXT, true);
        textY += line;
        boolean hasAuthority = hasRole(session, ZoneRole.AUTHORITY_POINT);
        boolean hasInterior = hasRole(session, ZoneRole.INTERIOR);
        graphics.drawString(mc.font, Component.literal((hasAuthority ? "[x] " : "[ ] ")).append(SettlementSurveyorToolItem.roleLabel(ZoneRole.AUTHORITY_POINT)), x + 7, textY, hasAuthority ? PANEL_OK : PANEL_WARN, true);
        textY += line;
        graphics.drawString(mc.font, Component.literal((hasInterior ? "[x] " : "[ ] ")).append(SettlementSurveyorToolItem.roleLabel(ZoneRole.INTERIOR)), x + 7, textY, hasInterior ? PANEL_OK : PANEL_WARN, true);
        textY += line;

        ZoneSelection shown = findRole(session, selected);
        Component dimensions = shown == null
                ? Component.translatable("bannermod.surveyor.hud.dimensions.none")
                : Component.translatable("bannermod.surveyor.hud.dimensions", sizeX(shown), sizeY(shown), sizeZ(shown), shown.volume());
        graphics.drawString(mc.font, dimensions, x + 7, textY, PANEL_MUTED, true);
        textY += line;

        Component next = !hasAuthority
                ? Component.translatable("bannermod.surveyor.hud.next.authority")
                : !hasInterior
                ? Component.translatable("bannermod.surveyor.hud.next.interior")
                : Component.translatable("bannermod.surveyor.hud.next.validate");
        graphics.drawString(mc.font, next, x + 7, textY, hasAuthority && hasInterior ? PANEL_OK : PANEL_WARN, true);
    }

    private static ItemStack surveyorStack(LocalPlayer player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (main.getItem() instanceof SettlementSurveyorToolItem) {
            return main;
        }
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        return off.getItem() instanceof SettlementSurveyorToolItem ? off : ItemStack.EMPTY;
    }

    private static BlockPos targetedBlock(Minecraft mc) {
        if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            return hit.getBlockPos();
        }
        return null;
    }

    private static void renderStarterFortPreview(PoseStack pose, VertexConsumer lines, BlockPos anchor) {
        int x0 = anchor.getX() - 10;
        int x1 = anchor.getX() + 11;
        int z0 = anchor.getZ() - 10;
        int z1 = anchor.getZ() + 11;
        int y = anchor.getY() + 1;
        int h = 5;
        int gateLeft = anchor.getX() - 2;
        int gateRight = anchor.getX() + 3;
        float[] wood = rgb(0xD8A55A);
        float[] tower = rgb(0xF0C06A);
        float[] yard = rgb(0x8AD0FF);
        float[] shelter = rgb(0xB8793C);

        lineBox(pose, lines, new AABB(x0, y, z0, x1, y + h, z0 + 1), wood, 0.72F);
        lineBox(pose, lines, new AABB(x0, y, z1 - 1, gateLeft, y + h, z1), wood, 0.72F);
        lineBox(pose, lines, new AABB(gateRight, y, z1 - 1, x1, y + h, z1), wood, 0.72F);
        lineBox(pose, lines, new AABB(x0, y, z0, x0 + 1, y + h, z1), wood, 0.72F);
        lineBox(pose, lines, new AABB(x1 - 1, y, z0, x1, y + h, z1), wood, 0.72F);

        lineBox(pose, lines, new AABB(x0, y, z0, x0 + 3, y + 6, z0 + 3), tower, 0.9F);
        lineBox(pose, lines, new AABB(x1 - 3, y, z0, x1, y + 6, z0 + 3), tower, 0.9F);
        lineBox(pose, lines, new AABB(x0, y, z1 - 3, x0 + 3, y + 6, z1), tower, 0.9F);
        lineBox(pose, lines, new AABB(x1 - 3, y, z1 - 3, x1, y + 6, z1), tower, 0.9F);
        lineBox(pose, lines, new AABB(gateLeft, y, z1 - 1, gateRight, y + 6, z1), rgb(0xFFD36A), 0.88F);
        lineBox(pose, lines, new AABB(gateLeft + 1, y, z1 - 1, gateRight - 1, y + 4, z1), rgb(0x2A2116), 0.35F);

        lineBox(pose, lines, new AABB(anchor.getX() - 5, anchor.getY(), anchor.getZ() - 4,
                anchor.getX() + 6, anchor.getY() + 1, anchor.getZ() + 8), yard, 0.42F);
        lineBox(pose, lines, new AABB(anchor.getX() - 8, y, anchor.getZ() - 10,
                anchor.getX() + 9, y + 4, anchor.getZ() - 5), shelter, 0.72F);
        lineBox(pose, lines, new AABB(anchor.getX() - 10, y, anchor.getZ() - 4,
                anchor.getX() - 5, y + 4, anchor.getZ() + 6), shelter, 0.72F);
        lineBox(pose, lines, new AABB(anchor.getX() + 5, y, anchor.getZ() - 4,
                anchor.getX() + 10, y + 4, anchor.getZ() + 6), shelter, 0.72F);
        lineBox(pose, lines, new AABB(anchor.getX() - 1, y, anchor.getZ() - 5,
                anchor.getX() + 2, y + 3, anchor.getZ() - 5), rgb(0xFFF08A), 0.8F);
        lineBox(pose, lines, new AABB(anchor.getX(), y, anchor.getZ(), anchor.getX() + 1, y + 1, anchor.getZ() + 1), rgb(0xFF6F4A), 0.75F);
    }

    private static void renderAnchorMarker(PoseStack pose, VertexConsumer lines, BlockPos anchor) {
        double x = anchor.getX() + 0.5D;
        double y = anchor.getY() + 1.0D;
        double z = anchor.getZ() + 0.5D;
        ClientRenderPrimitives.line(pose, lines, new Vec3(x, y, z), new Vec3(x, y + 4.0D, z), 1.0F, 0.86F, 0.35F, 1.0F);
        lineBox(pose, lines, new AABB(x, y + 2.4D, z, x + 1.8D, y + 3.4D, z + 0.08D), rgb(0xFFD36A), 0.95F);
        lineBox(pose, lines, new AABB(anchor).inflate(0.08D), rgb(0xFFD36A), 1.0F);
    }

    private static void renderAuthorityGuide(PoseStack pose, VertexConsumer lines, BlockPos anchor) {
        AABB authority = new AABB(anchor.getX() - 2, anchor.getY(), anchor.getZ() - 2,
                anchor.getX() + 3, anchor.getY() + 2, anchor.getZ() + 3);
        lineBox(pose, lines, authority, rgb(0xFFB13B), 0.82F);
    }

    private static AABB selectionBox(BlockPos a, BlockPos b) {
        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX());
        int maxY = Math.max(a.getY(), b.getY());
        int maxZ = Math.max(a.getZ(), b.getZ());
        return new AABB(minX, minY, minZ, maxX + 1.0D, maxY + 1.0D, maxZ + 1.0D);
    }

    private static boolean hasRole(ValidationSession session, ZoneRole role) {
        return findRole(session, role) != null;
    }

    private static ZoneSelection findRole(ValidationSession session, ZoneRole role) {
        for (ZoneSelection selection : session.selections()) {
            if (selection.role() == role) {
                return selection;
            }
        }
        return null;
    }

    private static int sizeX(ZoneSelection selection) {
        return Math.abs(selection.max().getX() - selection.min().getX()) + 1;
    }

    private static int sizeY(ZoneSelection selection) {
        return Math.abs(selection.max().getY() - selection.min().getY()) + 1;
    }

    private static int sizeZ(ZoneSelection selection) {
        return Math.abs(selection.max().getZ() - selection.min().getZ()) + 1;
    }

    private static int color(ZoneRole role) {
        return switch (role) {
            case AUTHORITY_POINT -> 0xFFFFB13B;
            case INTERIOR -> 0xFF7FD7FF;
            case SLEEPING -> 0xFFB48CFF;
            case WORK_ZONE -> 0xFF83D36B;
            case FORT_PERIMETER -> 0xFFFF6F4A;
            case ENTRANCE -> 0xFFFFF08A;
            case STORAGE -> 0xFFCDA86B;
            case PREFAB_FOOTPRINT -> 0xFFE5E5E5;
        };
    }

    private static float[] rgb(int rgb) {
        return new float[]{((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F};
    }

    private static void lineBox(PoseStack pose, VertexConsumer lines, AABB box, int argb, float alpha) {
        lineBox(pose, lines, box, rgb(argb), alpha);
    }

    private static void lineBox(PoseStack pose, VertexConsumer lines, AABB box, float[] rgb, float alpha) {
        ClientRenderPrimitives.lineBox(pose, lines, box, rgb[0], rgb[1], rgb[2], alpha);
    }
}
