package com.talhanation.bannermod.client.citizen.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;

public class CitizenRenderer extends MobRenderer<CitizenEntity, HumanoidModel<CitizenEntity>> {
    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_new_0.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_new_1.png"),
            ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/entity/human/human_new_2.png")
    };

    public CitizenRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(CitizenEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.applyModelProperties(entity);
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CitizenEntity entity) {
        return TEXTURES[Math.floorMod(entity.getUUID().hashCode(), TEXTURES.length)];
    }

    @Override
    protected void scale(CitizenEntity entity, PoseStack poseStack, float partialTickTime) {
        float scale = 0.9375F * entity.renderScaleFactor();
        poseStack.scale(scale, scale, scale);
    }

    private void applyModelProperties(CitizenEntity entity) {
        HumanoidModel<CitizenEntity> model = this.getModel();
        model.setAllVisible(true);
        model.crouching = entity.isCrouching();
        HumanoidModel.ArmPose mainHandPose = this.getArmPose(entity, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offHandPose = this.getArmPose(entity, InteractionHand.OFF_HAND);
        if (mainHandPose.isTwoHanded()) {
            offHandPose = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = mainHandPose;
            model.leftArmPose = offHandPose;
        } else {
            model.rightArmPose = offHandPose;
            model.leftArmPose = mainHandPose;
        }
    }

    private HumanoidModel.ArmPose getArmPose(CitizenEntity entity, InteractionHand hand) {
        ItemStack itemStack = entity.getItemInHand(hand);
        if (itemStack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }

        if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
            UseAnim useAnim = itemStack.getUseAnimation();
            if (useAnim == UseAnim.BLOCK) {
                return HumanoidModel.ArmPose.BLOCK;
            }
            if (useAnim == UseAnim.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            if (useAnim == UseAnim.SPEAR) {
                return HumanoidModel.ArmPose.THROW_SPEAR;
            }
            if (useAnim == UseAnim.CROSSBOW && hand == entity.getUsedItemHand()) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (useAnim == UseAnim.SPYGLASS) {
                return HumanoidModel.ArmPose.SPYGLASS;
            }
        } else if (!entity.swinging && itemStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }

        HumanoidModel.ArmPose forgeArmPose = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(itemStack)
                .getArmPose(entity, hand, itemStack);
        return forgeArmPose != null ? forgeArmPose : HumanoidModel.ArmPose.ITEM;
    }
}
