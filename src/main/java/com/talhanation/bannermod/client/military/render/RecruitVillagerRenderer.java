package com.talhanation.bannermod.client.military.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.events.ClientEvent;
import com.talhanation.bannermod.client.military.models.RecruitVillagerModel;
import com.talhanation.bannermod.client.military.render.layer.RecruitVillagerBiomeLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitVillagerCompanionLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitVillagerTeamColorLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitLodArmorLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitLodItemInHandLayer;
import com.talhanation.bannermod.client.military.render.layer.VillagerRecruitCustomHeadLayer;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RecruitVillagerRenderer extends AbstractRecruitRenderer<HumanoidModel<AbstractRecruitEntity>> {

    private static final ResourceLocation[] TEXTURE = {
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/villager/villager_1.png"),
    };
    public ResourceLocation getTextureLocation(AbstractRecruitEntity recruit) {
        RecruitRenderProfiling.textureStateSwitch("base_model");
        return TEXTURE[0];
    }

    public static ResourceLocation crowdTexture(AbstractRecruitEntity recruit) {
        return TEXTURE[0];
    }

    public RecruitVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new RecruitVillagerModel(context.bakeLayer(ClientEvent.RECRUIT)), 0.5F);
        this.addLayer(new RecruitLodArmorLayer(this, new HumanoidModel<>(context.bakeLayer(ClientEvent.RECRUIT_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ClientEvent.RECRUIT_OUTER_ARMOR)), context.getModelManager()));
        this.addLayer(new RecruitVillagerTeamColorLayer(this));
        this.addLayer(new RecruitVillagerBiomeLayer(this));
        this.addLayer(new RecruitVillagerCompanionLayer(this));
        this.addLayer(new RecruitLodItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new VillagerRecruitCustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(AbstractRecruitEntity entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
        matrixStackIn.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
