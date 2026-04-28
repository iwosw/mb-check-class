package com.talhanation.bannermod.client.civilian.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.talhanation.bannermod.client.render.ClientRenderPrimitives;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public interface IRenderWorkArea {
    default void renderWorkArea(PoseStack poseStack, VertexConsumer vertexConsumer, AABB aabb){
        ClientRenderPrimitives.lineBox(poseStack, vertexConsumer, aabb, 1F, 1F, 1F, 1F);
    }

    default void drawLine(PoseStack stack, VertexConsumer buffer, Vec3 from, Vec3 to, float r, float g, float b, float a) {
        ClientRenderPrimitives.line(stack, buffer, from, to, r, g, b, a);
    }

}
