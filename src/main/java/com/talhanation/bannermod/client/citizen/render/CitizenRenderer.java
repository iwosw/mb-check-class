package com.talhanation.bannermod.client.citizen.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class CitizenRenderer extends MobRenderer<CitizenEntity, HumanoidModel<CitizenEntity>> {
    private static final ResourceLocation[] TEXTURE = {
            new ResourceLocation("bannermod", "textures/entity/human/human_new_0.png"),
            new ResourceLocation("bannermod", "textures/entity/human/human_new_1.png"),
            new ResourceLocation("bannermod", "textures/entity/human/human_new_2.png")
    };

    public CitizenRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CitizenEntity entity) {
        return TEXTURE[Math.abs(entity.getUUID().hashCode()) % TEXTURE.length];
    }

    @Override
    protected void scale(CitizenEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
