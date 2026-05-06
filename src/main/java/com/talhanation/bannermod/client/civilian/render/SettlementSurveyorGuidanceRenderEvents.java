package com.talhanation.bannermod.client.civilian.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.civilian.SurveyorZonePalette;
import com.talhanation.bannermod.client.render.ClientRenderPrimitives;
import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import com.talhanation.bannermod.settlement.validation.SurveyorModeGuidance;
import com.talhanation.bannermod.settlement.validation.StarterFortPlan;
import com.talhanation.bannermod.settlement.validation.SurveyorMode;
import com.talhanation.bannermod.settlement.validation.SurveyorStructurePlans;
import com.talhanation.bannermod.settlement.validation.SurveyorSessionCodec;
import com.talhanation.bannermod.settlement.validation.ValidationSession;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

@EventBusSubscriber(modid = BannerModMain.MOD_ID, value = Dist.CLIENT)
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
        PreviewContext context = previewContext(mc, player);
        if (context == null) {
            return;
        }

        ValidationSession session = context.session();
        SurveyorMode mode = context.mode();
        ZoneRole selectedRole = context.selectedRole();
        BlockPos anchor = session == null ? BlockPos.ZERO : session.anchorPos();
        BlockPos target = context.target();
        if (anchor.equals(BlockPos.ZERO) && target == null) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        pose.pushPose();
        pose.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.disableDepthTest();

        if (session != null) {
            for (ZoneSelection selection : session.selections()) {
                lineBox(pose, buffers, selection.toAabb().inflate(0.02D), color(selection.role()), 0.88F);
            }
        }

        BlockPos pending = context.pendingCorner();
        if (pending != null && target != null) {
            lineBox(pose, buffers, selectionBox(pending, target).inflate(0.04D), color(selectedRole), 1.0F);
        }

        BlockPos previewAnchor = anchor.equals(BlockPos.ZERO) ? target : anchor;
        if (previewAnchor != null && !previewAnchor.equals(BlockPos.ZERO)) {
            renderAnchorMarker(pose, buffers, previewAnchor);
            if (context.showGuidePreview() && mode != SurveyorMode.BOOTSTRAP_FORT) {
                renderSurveyorPlanPreview(pose, buffers, previewAnchor, mode, session, selectedRole);
            }
        }

        RenderSystem.enableDepthTest();
        pose.popPose();
        buffers.endBatch();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.screen != null) {
            return;
        }
        PreviewContext context = previewContext(mc, player);
        if (context == null) {
            return;
        }

        ValidationSession session = context.session();
        SurveyorMode mode = context.mode();
        List<ZoneRole> requiredRoles = SurveyorModeGuidance.requiredRoles(mode);
        ZoneRole selected = context.selectedRole();
        GuiGraphics graphics = event.getGuiGraphics();
        int x = 8;
        int y = 38;
        int width = 232;
        int contentWidth = width - 14;
        int line = 11;
        boolean hasAnchor = session != null && !session.anchorPos().equals(BlockPos.ZERO);
        Component modeHint = modeHint(mode);
        Component nextStep = context.pinned()
                ? Component.translatable("bannermod.surveyor.hud.pinned_help")
                : currentStep(mode, session);
        Component roleHelp = context.pinned()
                ? Component.translatable("bannermod.surveyor.hud.pinned")
                : Component.translatable("bannermod.surveyor.hud.role_help", SettlementSurveyorToolItem.roleLabel(selected), roleHint(selected));
        ZoneSelection shown = hasAnchor ? findRole(session, selected) : null;
        Component dimensions = shown == null
                ? Component.translatable("bannermod.surveyor.hud.dimensions.none", SettlementSurveyorToolItem.roleLabel(selected))
                : Component.translatable("bannermod.surveyor.hud.dimensions",
                SettlementSurveyorToolItem.roleLabel(selected), sizeX(shown), sizeY(shown), sizeZ(shown), shown.volume());
        int rows = 3
                + wrappedLineCount(mc, modeHint, contentWidth)
                + 1
                + (hasAnchor
                ? 1 + requiredRoles.size() + wrappedLineCount(mc, dimensions, contentWidth) + wrappedLineCount(mc, roleHelp, contentWidth) + wrappedLineCount(mc, nextStep, contentWidth)
                : wrappedLineCount(mc, roleHelp, contentWidth) + wrappedLineCount(mc, Component.translatable("bannermod.surveyor.hud.preview"), contentWidth));
        int height = 16 + rows * line;

        graphics.fill(x, y, x + width, y + height, PANEL_BG);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_INNER);
        graphics.renderOutline(x, y, width, height, PANEL_BORDER);

        int textY = y + 6;
        Component title = Component.translatable("bannermod.surveyor.hud.title_mode", SettlementSurveyorToolItem.modeLabel(mode));
        graphics.drawString(mc.font, title, x + 7, textY, PANEL_TEXT, true);
        textY += line + 2;
        graphics.drawString(mc.font, Component.translatable("bannermod.surveyor.hud.role", SettlementSurveyorToolItem.roleLabel(selected)), x + 7, textY, color(selected), true);
        textY += line;
        textY = drawWrapped(graphics, mc, modeHint, x + 7, textY, contentWidth, PANEL_MUTED);

        textY = drawCheckLine(graphics, mc, x + 7, textY, hasAnchor, Component.translatable("bannermod.surveyor.hud.check.anchor"));
        if (!hasAnchor) {
            textY = drawWrapped(graphics, mc, roleHelp, x + 7, textY, contentWidth, PANEL_MUTED);
            drawWrapped(graphics, mc, Component.translatable("bannermod.surveyor.hud.preview"), x + 7, textY, contentWidth, PANEL_MUTED);
            return;
        }

        graphics.drawString(mc.font, Component.translatable("bannermod.surveyor.hud.anchor", session.anchorPos().toShortString()), x + 7, textY, PANEL_TEXT, true);
        textY += line;
        boolean ready = true;
        for (ZoneRole role : requiredRoles) {
            boolean complete = hasRole(session, role);
            ready &= complete;
            textY = drawCheckLine(graphics, mc, x + 7, textY, complete, SettlementSurveyorToolItem.roleLabel(role));
        }

        textY = drawWrapped(graphics, mc, dimensions, x + 7, textY, contentWidth, PANEL_MUTED);
        textY = drawWrapped(graphics, mc, roleHelp, x + 7, textY, contentWidth, PANEL_MUTED);

        drawWrapped(graphics, mc, nextStep, x + 7, textY, contentWidth, ready ? PANEL_OK : PANEL_WARN);
    }

    private static int drawCheckLine(GuiGraphics graphics,
                                     Minecraft mc,
                                     int x,
                                     int textY,
                                     boolean complete,
                                     Component label) {
        graphics.drawString(mc.font,
                Component.literal(complete ? "[x] " : "[ ] ").append(label),
                x,
                textY,
                complete ? PANEL_OK : PANEL_WARN,
                true);
        return textY + 11;
    }

    private static int drawWrapped(GuiGraphics graphics,
                                   Minecraft mc,
                                   Component text,
                                   int x,
                                   int y,
                                   int width,
                                   int color) {
        int lineY = y;
        for (net.minecraft.util.FormattedCharSequence sequence : mc.font.split(text, width)) {
            graphics.drawString(mc.font, sequence, x, lineY, color, true);
            lineY += 11;
        }
        return lineY;
    }

    private static int wrappedLineCount(Minecraft mc, Component text, int width) {
        return Math.max(1, mc.font.split(text, width).size());
    }

    private static Component modeHint(SurveyorMode mode) {
        return Component.translatable("bannermod.surveyor.mode_hint." + (mode == null ? SurveyorMode.BOOTSTRAP_FORT : mode).name().toLowerCase(Locale.ROOT));
    }

    private static Component roleHint(ZoneRole role) {
        return Component.translatable("bannermod.surveyor.role_hint." + (role == null ? ZoneRole.INTERIOR : role).name().toLowerCase(Locale.ROOT));
    }

    private static Component currentStep(SurveyorMode mode, ValidationSession session) {
        if (session == null || session.anchorPos().equals(BlockPos.ZERO)) {
            return Component.translatable("bannermod.surveyor.hud.next.anchor");
        }
        ZoneRole nextMissingRole = SurveyorModeGuidance.nextMissingRole(mode, session);
        if (nextMissingRole != null) {
            return Component.translatable("bannermod.surveyor.hud.next.role", SettlementSurveyorToolItem.roleLabel(nextMissingRole), roleHint(nextMissingRole));
        }
        return Component.translatable("bannermod.surveyor.hud.next.validate");
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

    @Nullable
    private static PreviewContext previewContext(Minecraft mc, LocalPlayer player) {
        ItemStack stack = surveyorStack(player);
        if (!stack.isEmpty()) {
            ValidationSession session = SurveyorModeGuidance.normalizeSession(SurveyorSessionCodec.read(stack));
            SurveyorMode mode = session == null ? SurveyorMode.BOOTSTRAP_FORT : session.mode();
            return new PreviewContext(
                    session,
                    mode,
                    SettlementSurveyorToolItem.selectedRole(stack),
                    SettlementSurveyorToolItem.pendingCorner(stack),
                    targetedBlock(mc),
                    session == null || session.showGuidePreview(),
                    false
            );
        }

        SettlementSurveyorPinnedPreviewState.PinnedSurveyPreview pinnedPreview = SettlementSurveyorPinnedPreviewState.previewFor(mc.level);
        if (pinnedPreview == null) {
            return null;
        }
        return new PreviewContext(
                SurveyorModeGuidance.normalizeSession(pinnedPreview.session()),
                pinnedPreview.session().mode(),
                pinnedPreview.selectedRole(),
                null,
                null,
                pinnedPreview.session().showGuidePreview(),
                true
        );
    }

    private static void renderSurveyorPlanPreview(PoseStack pose,
                                                  MultiBufferSource buffers,
                                                  BlockPos anchor,
                                                  SurveyorMode mode,
                                                  ValidationSession session,
                                                  ZoneRole selectedRole) {
        SurveyorStructurePlans.SurveyorPlan plan = SurveyorStructurePlans.planFor(mode);
        if (plan == null) {
            return;
        }
        for (SurveyorStructurePlans.PreviewBox previewBox : plan.previewBoxes()) {
            lineBox(pose, buffers, previewBox.box().toAabb(anchor), previewBox.color(), previewBox.alpha());
        }
        for (SurveyorStructurePlans.GuideBox guideBox : plan.guideBoxes()) {
            renderGuideBox(pose, buffers, anchor, guideBox.role(), guideBox.box(), session, selectedRole, guideBox.alpha());
        }
    }

    private static void renderAnchorMarker(PoseStack pose, MultiBufferSource buffers, BlockPos anchor) {
        double x = anchor.getX() + 0.5D;
        double y = anchor.getY() + 1.0D;
        double z = anchor.getZ() + 0.5D;
        ClientRenderPrimitives.line(pose, buffers.getBuffer(RenderType.lines()), new Vec3(x, y, z), new Vec3(x, y + 4.0D, z), 1.0F, 0.86F, 0.35F, 1.0F);
        lineBox(pose, buffers, new AABB(x, y + 2.4D, z, x + 1.8D, y + 3.4D, z + 0.08D), rgb(0xFFD36A), 0.95F);
        lineBox(pose, buffers, new AABB(anchor).inflate(0.08D), rgb(0xFFD36A), 1.0F);
    }

    private static void renderGuideBox(PoseStack pose,
                                       MultiBufferSource buffers,
                                       BlockPos anchor,
                                       ZoneRole role,
                                       StarterFortPlan.RelativeBox box,
                                       ValidationSession session,
                                       ZoneRole selectedRole,
                                       float baseAlpha) {
        boolean captured = session != null && hasRole(session, role);
        float alpha = selectedRole == role
                ? Math.min(1.0F, baseAlpha + 0.18F)
                : captured
                ? Math.max(0.18F, baseAlpha - 0.16F)
                : baseAlpha;
        lineBox(pose, buffers, box.toAabb(anchor), color(role), alpha);
        renderGuideLabel(pose, buffers, anchor, role, box);
    }

    private static void renderGuideLabel(PoseStack pose,
                                         MultiBufferSource buffers,
                                         BlockPos anchor,
                                         ZoneRole role,
                                         StarterFortPlan.RelativeBox box) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        AABB area = box.toAabb(anchor);
        Vec3 center = area.getCenter();
        double labelY = area.maxY + 0.55D + labelYOffset(role);
        FormattedCharSequence label = SettlementSurveyorToolItem.roleLabel(role).getVisualOrderText();

        pose.pushPose();
        pose.translate(center.x, labelY, center.z);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(-0.025F, -0.025F, 0.025F);
        float textX = -font.width(label) / 2.0F;
        font.drawInBatch(label,
                textX,
                0.0F,
                color(role),
                false,
                pose.last().pose(),
                buffers,
                Font.DisplayMode.SEE_THROUGH,
                0x66000000,
                LightTexture.FULL_BRIGHT);
        pose.popPose();
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
        return SurveyorZonePalette.color(role);
    }

    private static double labelYOffset(ZoneRole role) {
        return switch (role) {
            case AUTHORITY_POINT -> 0.9D;
            case SLEEPING, STORAGE -> 0.4D;
            default -> 0.0D;
        };
    }

    private static float[] rgb(int rgb) {
        return SurveyorZonePalette.rgb(rgb);
    }

    private static void lineBox(PoseStack pose, MultiBufferSource buffers, AABB box, int argb, float alpha) {
        lineBox(pose, buffers, box, rgb(argb), alpha);
    }

    private static void lineBox(PoseStack pose, MultiBufferSource buffers, AABB box, float[] rgb, float alpha) {
        ClientRenderPrimitives.lineBox(pose, buffers.getBuffer(RenderType.lines()), box, rgb[0], rgb[1], rgb[2], alpha);
    }

    private record PreviewContext(@Nullable ValidationSession session,
                                  SurveyorMode mode,
                                  ZoneRole selectedRole,
                                  @Nullable BlockPos pendingCorner,
                                  @Nullable BlockPos target,
                                  boolean showGuidePreview,
                                  boolean pinned) {
    }
}
