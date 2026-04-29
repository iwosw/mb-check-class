package com.talhanation.bannermod.client.military.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.compat.IWeapon;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.CrossBowmanEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;

public abstract class AbstractRecruitRenderer<M extends HumanoidModel<AbstractRecruitEntity>> extends MobRenderer<AbstractRecruitEntity, M> {
    protected AbstractRecruitRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    public void render(AbstractRecruitEntity recruit, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        long poseStart = RecruitRenderProfiling.start();
        this.applyModelProperties(recruit);
        RecruitRenderProfiling.duration("animation_pose", poseStart);
        RecruitRenderProfiling.beginNormalRender();
        long renderStart = RecruitRenderProfiling.start();
        super.render(recruit, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        RecruitRenderProfiling.endNormalRender(renderStart);
    }

    @Override
    protected boolean shouldShowName(AbstractRecruitEntity recruit) {
        boolean showName = RecruitRenderLod.shouldRenderName(recruit) && super.shouldShowName(recruit);
        if (showName) {
            RecruitRenderProfiling.increment("nameplates.visible");
        } else {
            RecruitRenderProfiling.skipped("nameplates");
        }
        return showName;
    }

    @Override
    protected void renderNameTag(AbstractRecruitEntity recruit, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        long start = RecruitRenderProfiling.start();
        super.renderNameTag(recruit, displayName, poseStack, bufferSource, packedLight, partialTick);
        RecruitRenderProfiling.duration("nameplates", start);
    }

    protected void applyModelProperties(AbstractRecruitEntity recruit) {
        M model = this.getModel();
        model.setAllVisible(true);
        model.crouching = recruit.isCrouching();
        HumanoidModel.ArmPose mainHandPose = this.getArmPose(recruit, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offHandPose = this.getArmPose(recruit, InteractionHand.OFF_HAND);
        if (mainHandPose.isTwoHanded()) {
            offHandPose = recruit.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        if (recruit.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = mainHandPose;
            model.leftArmPose = offHandPose;
        } else {
            model.rightArmPose = offHandPose;
            model.leftArmPose = mainHandPose;
        }
    }

    protected HumanoidModel.ArmPose getArmPose(AbstractRecruitEntity recruit, InteractionHand hand) {
        ItemStack itemStack = recruit.getItemInHand(hand);
        boolean isMusket = IWeapon.isMusketModWeapon(itemStack) && recruit instanceof CrossBowmanEntity crossBowman && crossBowman.isAggressive();
        if (itemStack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }

        if (recruit.getUsedItemHand() == hand && recruit.getUseItemRemainingTicks() > 0) {
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

            if (useAnim == UseAnim.CROSSBOW && hand == recruit.getUsedItemHand() || isMusket) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }

            if (useAnim == UseAnim.SPYGLASS) {
                return HumanoidModel.ArmPose.SPYGLASS;
            }
        } else if (!recruit.swinging && itemStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack) || isMusket) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }

        return this.getItemFallbackArmPose(recruit, hand, itemStack);
    }

    protected HumanoidModel.ArmPose getItemFallbackArmPose(AbstractRecruitEntity recruit, InteractionHand hand, ItemStack itemStack) {
        return HumanoidModel.ArmPose.ITEM;
    }
}
