package com.talhanation.bannermod.client.civilian.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.civilian.SurveyorZonePalette;
import com.talhanation.bannermod.client.render.ClientRenderPrimitives;
import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import com.talhanation.bannermod.settlement.validation.SurveyorSessionCodec;
import com.talhanation.bannermod.settlement.validation.ValidationSession;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = BannerModMain.MOD_ID, value = Dist.CLIENT)
public final class SettlementSurveyorSelectionRenderEvents {
    private SettlementSurveyorSelectionRenderEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.level == null || player == null) {
            return;
        }

        ItemStack stack = surveyorStack(player);
        if (stack.isEmpty()) {
            return;
        }

        ValidationSession session = SurveyorSessionCodec.read(stack);
        BlockPos pendingCorner = SettlementSurveyorToolItem.pendingCorner(stack);
        BlockPos target = targetedBlock(minecraft);
        if (session == null && pendingCorner == null && target == null) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        if (session != null) {
            renderAnchor(poseStack, lines, session.anchorPos());
            for (ZoneSelection selection : session.selections()) {
                renderSelection(poseStack, lines, selection, false);
            }
        }

        if (pendingCorner != null && target != null) {
            ZoneRole role = SettlementSurveyorToolItem.selectedRole(stack);
            renderSelection(poseStack, lines, new ZoneSelection(role, pendingCorner, target, target), true);
        } else if (target != null) {
            ClientRenderPrimitives.lineBox(poseStack, lines, blockBox(target), 1.0F, 0.82F, 0.20F, 0.90F);
        }

        poseStack.popPose();
        bufferSource.endBatch(RenderType.lines());
    }

    private static ItemStack surveyorStack(Player player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (main.getItem() instanceof SettlementSurveyorToolItem) {
            return main;
        }
        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        return offhand.getItem() instanceof SettlementSurveyorToolItem ? offhand : ItemStack.EMPTY;
    }

    private static BlockPos targetedBlock(Minecraft minecraft) {
        HitResult hit = minecraft.hitResult;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getBlockPos();
        }
        return null;
    }

    private static void renderAnchor(PoseStack poseStack, VertexConsumer lines, BlockPos anchor) {
        if (anchor == null || anchor.equals(BlockPos.ZERO)) {
            return;
        }
        ClientRenderPrimitives.lineBox(poseStack, lines,
                new AABB(anchor.getX(), anchor.getY(), anchor.getZ(), anchor.getX() + 1.0D, anchor.getY() + 2.0D, anchor.getZ() + 1.0D).inflate(0.03D),
                0.20F, 0.95F, 1.0F, 1.0F);
    }

    private static void renderSelection(PoseStack poseStack, VertexConsumer lines, ZoneSelection selection, boolean active) {
        float[] color = SurveyorZonePalette.rgb(selection.role());
        ClientRenderPrimitives.lineBox(poseStack, lines, selection.toAabb().inflate(active ? 0.05D : 0.025D),
                color[0], color[1], color[2], active ? 1.0F : 0.88F);
        if (selection.marker() != null) {
            ClientRenderPrimitives.lineBox(poseStack, lines, blockBox(selection.marker()), color[0], color[1], color[2], 1.0F);
        }
    }

    private static AABB blockBox(BlockPos pos) {
        return new AABB(pos).inflate(0.035D);
    }

}
