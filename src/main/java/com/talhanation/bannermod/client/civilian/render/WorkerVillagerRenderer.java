package com.talhanation.bannermod.client.civilian.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.client.military.render.RecruitVillagerRenderer;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class WorkerVillagerRenderer extends RecruitVillagerRenderer implements IRenderWorkArea{
    public WorkerVillagerRenderer(EntityRendererProvider.Context mgr) {
        super(mgr);
        //this.addLayer(new WorkersProfessionLayer(this));
    }

    @Override
    public void render(AbstractRecruitEntity recruit, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(recruit, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

    }

}
