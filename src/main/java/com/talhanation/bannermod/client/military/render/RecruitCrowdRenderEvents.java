package com.talhanation.bannermod.client.military.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.RecruitsClientConfig;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class RecruitCrowdRenderEvents {
    private static final double IMPOSTOR_QUERY_RADIUS = 96.0D;
    private static final float BODY_WIDTH = 0.58F;
    private static final float HALF_WIDTH = BODY_WIDTH * 0.5F;

    private RecruitCrowdRenderEvents() {
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() instanceof AbstractRecruitEntity recruit
                && RecruitRenderLod.shouldUseCrowdImpostor(recruit)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || !RecruitRenderLod.isCrowdedNearCamera()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        List<AbstractRecruitEntity> recruits = minecraft.level.getEntitiesOfClass(
                AbstractRecruitEntity.class,
                AABB.ofSize(cameraPos, IMPOSTOR_QUERY_RADIUS * 2.0D, IMPOSTOR_QUERY_RADIUS * 2.0D, IMPOSTOR_QUERY_RADIUS * 2.0D),
                recruit -> recruit.distanceToSqr(cameraPos) <= IMPOSTOR_QUERY_RADIUS * IMPOSTOR_QUERY_RADIUS
        );
        if (recruits.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Set<RenderType> usedRenderTypes = new HashSet<>();
        int rendered = 0;
        float partialTick = event.getPartialTick();

        for (AbstractRecruitEntity recruit : recruits) {
            if (!RecruitRenderLod.shouldUseCrowdImpostor(recruit)) {
                continue;
            }
            if (event.getFrustum() != null && !event.getFrustum().isVisible(recruit.getBoundingBox())) {
                continue;
            }
            ResourceLocation texture = crowdTexture(recruit);
            RenderType renderType = RenderType.entityCutoutNoCull(texture);
            VertexConsumer consumer = bufferSource.getBuffer(renderType);
            usedRenderTypes.add(renderType);
            renderImpostor(recruit, partialTick, camera, cameraPos, poseStack, consumer, minecraft);
            rendered++;
        }

        for (RenderType renderType : usedRenderTypes) {
            bufferSource.endBatch(renderType);
        }
        if (rendered > 0) {
            RuntimeProfilingCounters.add("recruit.render.crowd_impostors", rendered);
        }
    }

    private static ResourceLocation crowdTexture(AbstractRecruitEntity recruit) {
        return RecruitsClientConfig.RecruitsLookLikeVillagers.get()
                ? RecruitVillagerRenderer.crowdTexture(recruit)
                : RecruitHumanRenderer.crowdTexture(recruit);
    }

    private static void renderImpostor(AbstractRecruitEntity recruit,
                                       float partialTick,
                                       Camera camera,
                                       Vec3 cameraPos,
                                       PoseStack poseStack,
                                       VertexConsumer consumer,
                                       Minecraft minecraft) {
        double x = lerp(partialTick, recruit.xOld, recruit.getX()) - cameraPos.x;
        double y = lerp(partialTick, recruit.yOld, recruit.getY()) - cameraPos.y;
        double z = lerp(partialTick, recruit.zOld, recruit.getZ()) - cameraPos.z;
        int light = LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(recruit.getEyePosition(partialTick)));

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-1.0F, 1.0F, 1.0F);

        PoseStack.Pose pose = poseStack.last();
        addQuad(pose, consumer, -0.20F, 1.22F, 0.20F, 1.62F, 8.0F / 64.0F, 8.0F / 64.0F, 16.0F / 64.0F, 16.0F / 64.0F, light);
        addQuad(pose, consumer, -HALF_WIDTH, 0.72F, HALF_WIDTH, 1.22F, 20.0F / 64.0F, 20.0F / 64.0F, 28.0F / 64.0F, 32.0F / 64.0F, light);
        addQuad(pose, consumer, -HALF_WIDTH, 0.05F, 0.0F, 0.72F, 4.0F / 64.0F, 20.0F / 64.0F, 8.0F / 64.0F, 32.0F / 64.0F, light);
        addQuad(pose, consumer, 0.0F, 0.05F, HALF_WIDTH, 0.72F, 20.0F / 64.0F, 52.0F / 64.0F, 24.0F / 64.0F, 64.0F / 64.0F, light);
        poseStack.popPose();
    }

    private static void addQuad(PoseStack.Pose pose,
                                VertexConsumer consumer,
                                float minX,
                                float minY,
                                float maxX,
                                float maxY,
                                float minU,
                                float minV,
                                float maxU,
                                float maxV,
                                int light) {
        consumer.vertex(pose.pose(), minX, minY, 0.0F).color(255, 255, 255, 255).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, 0.0F).color(255, 255, 255, 255).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, 0.0F).color(255, 255, 255, 255).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, 0.0F).color(255, 255, 255, 255).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
    }

    private static double lerp(float partialTick, double previous, double current) {
        return previous + (current - previous) * partialTick;
    }
}
