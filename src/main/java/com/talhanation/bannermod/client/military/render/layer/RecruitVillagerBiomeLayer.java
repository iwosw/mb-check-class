package com.talhanation.bannermod.client.military.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.render.RecruitRenderProfiling;
import com.talhanation.bannermod.client.military.render.RecruitRenderLod;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;



public class RecruitVillagerBiomeLayer extends RenderLayer<AbstractRecruitEntity, HumanoidModel<AbstractRecruitEntity>> {
    private static final int FULL_WHITE = 0xFFFFFFFF;

    private static final ResourceLocation[] BIOME_TEXTURE = {
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/villager/biome/villager_desert.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/villager/biome/villager_jungle.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/villager/biome/villager_plains.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/villager/biome/villager_savanna.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/villager/biome/villager_snowy_tundra.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/villager/biome/villager_swamp.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/villager/biome/villager_taiga.png"),
    };
    public RecruitVillagerBiomeLayer(LivingEntityRenderer<AbstractRecruitEntity, HumanoidModel<AbstractRecruitEntity>> renderer) {
        super(renderer);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractRecruitEntity recruit, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if(recruit.isInvisible() || !RecruitRenderLod.shouldRenderCosmeticModelLayer(recruit)){
            RecruitRenderProfiling.layerSkipped("biome");
            return;
        }
        RecruitRenderProfiling.textureStateSwitch("biome");
        long start = RecruitRenderProfiling.start();
        this.getParentModel().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(BIOME_TEXTURE[recruit.getBiome()])), packedLight, OverlayTexture.NO_OVERLAY, FULL_WHITE);
        RecruitRenderProfiling.layerDuration("biome", start);
    }
}
