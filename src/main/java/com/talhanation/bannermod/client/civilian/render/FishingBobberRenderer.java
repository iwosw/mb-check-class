package com.talhanation.bannermod.client.civilian.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.talhanation.bannermod.client.render.ClientRenderPrimitives;
import com.talhanation.bannermod.entity.civilian.FishermanEntity;
import com.talhanation.bannermod.entity.civilian.FishingBobberEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class FishingBobberRenderer extends EntityRenderer<FishingBobberEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.parse("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
    private static final double VIEW_BOBBING_SCALE = 960.0D;

    public FishingBobberRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(FishingBobberEntity fishingBobber, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packetLight) {
        FishermanEntity fisherman = fishingBobber.getOwner();
        if (fisherman != null) {
            poseStack.pushPose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            Matrix4f bobberPose = poseStack.last().pose();
            VertexConsumer bobberBuffer = multiBufferSource.getBuffer(RENDER_TYPE);
            ClientRenderPrimitives.texturedBillboardQuad(bobberBuffer, bobberPose, packetLight);
            poseStack.popPose();
            int armSide = fisherman.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack itemstack = fisherman.getMainHandItem();
            if (!itemstack.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST)) {
                armSide = -armSide;
            }

            float bodyYawRadians = Mth.lerp(partialTicks, fisherman.yBodyRotO, fisherman.yBodyRot) * ((float) Math.PI / 180F);
            double yawSin = Mth.sin(bodyYawRadians);
            double yawCos = Mth.cos(bodyYawRadians);
            double armOffset = (double) armSide * 0.35D;
            double handX = Mth.lerp((double) partialTicks, fisherman.xo, fisherman.getX()) - yawCos * armOffset - yawSin * 0.8D;
            double handY = fisherman.yo + (double) fisherman.getEyeHeight() + (fisherman.getY() - fisherman.yo) * (double) partialTicks - 0.45D;
            double handZ = Mth.lerp((double) partialTicks, fisherman.zo, fisherman.getZ()) - yawSin * armOffset + yawCos * 0.8D;
            float crouchOffset = fisherman.isCrouching() ? -0.1875F : 0.0F;

            double bobberX = Mth.lerp((double) partialTicks, fishingBobber.xo, fishingBobber.getX());
            double bobberY = Mth.lerp((double) partialTicks, fishingBobber.yo, fishingBobber.getY()) + 0.25D;
            double bobberZ = Mth.lerp((double) partialTicks, fishingBobber.zo, fishingBobber.getZ());
            float lineX = (float) (handX - bobberX);
            float lineY = (float) (handY - bobberY) + crouchOffset;
            float lineZ = (float) (handZ - bobberZ);
            VertexConsumer lineBuffer = multiBufferSource.getBuffer(RenderType.lineStrip());
            PoseStack.Pose linePose = poseStack.last();

            for (int segment = 0; segment <= 16; ++segment) {
                fishingLineVertex(lineX, lineY, lineZ, lineBuffer, linePose, fraction(segment, 16), fraction(segment + 1, 16));
            }

            poseStack.popPose();
            super.render(fishingBobber, entityYaw, partialTicks, poseStack, multiBufferSource, packetLight);
        }
    }

    private static float fraction(int segment, int totalSegments) {
        return (float) segment / (float) totalSegments;
    }

    private static void fishingLineVertex(float lineX, float lineY, float lineZ, VertexConsumer consumer, PoseStack.Pose pose, float start, float end) {
        float x = lineX * start;
        float y = lineY * (start * start + start) * 0.5F + 0.25F;
        float z = lineZ * start;
        float nextX = lineX * end - x;
        float nextY = lineY * (end * end + end) * 0.5F + 0.25F - y;
        float nextZ = lineZ * end - z;
        float length = Mth.sqrt(nextX * nextX + nextY * nextY + nextZ * nextZ);
        if (length > 1.0E-6F) {
            nextX /= length;
            nextY /= length;
            nextZ /= length;
        }
        ClientRenderPrimitives.lineStripVertex(pose, consumer, x, y, z, 0.0F, 0.0F, 0.0F, 1.0F, nextX, nextY, nextZ);
    }

    public ResourceLocation getTextureLocation(FishingBobberEntity fishingBobber) {
        return TEXTURE_LOCATION;
    }
}
