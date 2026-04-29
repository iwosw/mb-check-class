package com.talhanation.bannermod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class ClientRenderPrimitives {
    private ClientRenderPrimitives() {
    }

    public static void texturedBillboardQuad(VertexConsumer consumer, Matrix4f pose, int packedLight) {
        texturedVertex(consumer, pose, packedLight, -0.5F, -0.5F, 0.0F, 0.0F, 1.0F);
        texturedVertex(consumer, pose, packedLight, 0.5F, -0.5F, 0.0F, 1.0F, 1.0F);
        texturedVertex(consumer, pose, packedLight, 0.5F, 0.5F, 0.0F, 1.0F, 0.0F);
        texturedVertex(consumer, pose, packedLight, -0.5F, 0.5F, 0.0F, 0.0F, 0.0F);
    }

    public static void line(PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to, float red, float green, float blue, float alpha) {
        line(poseStack.last(), consumer,
                (float) from.x, (float) from.y, (float) from.z,
                (float) to.x, (float) to.y, (float) to.z,
                red, green, blue, alpha);
    }

    public static void line(PoseStack.Pose pose, VertexConsumer consumer,
                            float fromX, float fromY, float fromZ,
                            float toX, float toY, float toZ,
                            float red, float green, float blue, float alpha) {
        float normalX = toX - fromX;
        float normalY = toY - fromY;
        float normalZ = toZ - fromZ;
        float length = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (length > 1.0E-6F) {
            normalX /= length;
            normalY /= length;
            normalZ /= length;
        } else {
            normalX = 0.0F;
            normalY = 1.0F;
            normalZ = 0.0F;
        }

        lineVertex(pose, consumer, fromX, fromY, fromZ, red, green, blue, alpha, normalX, normalY, normalZ);
        lineVertex(pose, consumer, toX, toY, toZ, red, green, blue, alpha, normalX, normalY, normalZ);
    }

    public static void lineBox(PoseStack poseStack, VertexConsumer consumer, AABB box, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        line(pose, consumer, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        line(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(pose, consumer, minX, minY, maxZ, maxX, minY, maxZ, red, green, blue, alpha);
        line(pose, consumer, minX, maxY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);

        line(pose, consumer, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        line(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        line(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);

        line(pose, consumer, minX, minY, minZ, minX, minY, maxZ, red, green, blue, alpha);
        line(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
        line(pose, consumer, minX, maxY, minZ, minX, maxY, maxZ, red, green, blue, alpha);
        line(pose, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
    }

    public static void lineStripVertex(PoseStack.Pose pose, VertexConsumer consumer,
                                       float x, float y, float z,
                                       float red, float green, float blue, float alpha,
                                       float normalX, float normalY, float normalZ) {
        lineVertex(pose, consumer, x, y, z, red, green, blue, alpha, normalX, normalY, normalZ);
    }

    private static void texturedVertex(VertexConsumer consumer, Matrix4f pose, int packedLight,
                                       float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void lineVertex(PoseStack.Pose pose, VertexConsumer consumer,
                                   float x, float y, float z,
                                   float red, float green, float blue, float alpha,
                                   float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(red, green, blue, alpha)
                .setNormal(normalX, normalY, normalZ);
    }
}
