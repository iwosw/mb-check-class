package com.talhanation.bannermod.client.military.render;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.models.RecruitHumanModel;
import com.talhanation.bannermod.client.military.render.layer.RecruitHumanBiomeLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitHumanCompanionLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitHumanTeamColorLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitLodArmorLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitLodCustomHeadLayer;
import com.talhanation.bannermod.client.military.render.layer.RecruitLodItemInHandLayer;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.item.ItemStack;

public class RecruitHumanRenderer extends AbstractRecruitRenderer<HumanoidModel<AbstractRecruitEntity>> {

    private static final ResourceLocation[] TEXTURE = {
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_0.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_1.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_2.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_3.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_4.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_5.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_6.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_7.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_8.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_9.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_10.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_11.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_12.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_13.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_14.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_15.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_16.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_17.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_18.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_19.png")
    };

    @Override
    public ResourceLocation getTextureLocation(AbstractRecruitEntity recruit) {
        RecruitRenderProfiling.textureStateSwitch("base_model");
        return TEXTURE[recruit.getVariant()];
    }

    public static ResourceLocation crowdTexture(AbstractRecruitEntity recruit) {
        return TEXTURE[recruit.getVariant()];
    }

    public RecruitHumanRenderer(EntityRendererProvider.Context mgr) {
        super(mgr, new RecruitHumanModel(mgr.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new RecruitLodArmorLayer(this, new HumanoidModel<>(mgr.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(mgr.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), mgr.getModelManager()));
        this.addLayer(new RecruitHumanTeamColorLayer(this));
        this.addLayer(new RecruitHumanBiomeLayer(this));
        this.addLayer(new RecruitHumanCompanionLayer(this));
        //this.addLayer(new ArrowLayer<>(mgr, this));
        this.addLayer(new RecruitLodItemInHandLayer<>(this, mgr.getItemInHandRenderer()));
        this.addLayer(new RecruitLodCustomHeadLayer<>(this, mgr.getModelSet(), mgr.getItemInHandRenderer()));

    }


    @Override
    protected HumanoidModel.ArmPose getItemFallbackArmPose(AbstractRecruitEntity recruit, InteractionHand hand, ItemStack itemStack) {
        HumanoidModel.ArmPose forgeArmPose = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(itemStack).getArmPose(recruit, hand, itemStack);
        return forgeArmPose != null ? forgeArmPose : HumanoidModel.ArmPose.ITEM;
    }

}
