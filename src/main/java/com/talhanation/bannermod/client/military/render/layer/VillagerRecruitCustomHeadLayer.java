package com.talhanation.bannermod.client.military.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.talhanation.bannermod.client.military.render.RecruitRenderProfiling;
import com.talhanation.bannermod.client.military.render.RecruitRenderLod;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class VillagerRecruitCustomHeadLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final ItemInHandRenderer itemInHandRenderer;

    public VillagerRecruitCustomHeadLayer(RenderLayerParent<T, M> renderer, EntityModelSet modelSet, ItemInHandRenderer itemInHandRenderer) {
        this(renderer, modelSet, 1.0F, 1.0F, 1.0F, itemInHandRenderer);
    }

    public VillagerRecruitCustomHeadLayer(RenderLayerParent<T, M> renderer, EntityModelSet modelSet, float scaleX, float scaleY, float scaleZ, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.skullModels = SkullBlockRenderer.createSkullRenderers(modelSet);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof AbstractRecruitEntity recruit && !RecruitRenderLod.shouldRenderCustomHead(recruit)) {
            RecruitRenderProfiling.layerSkipped("custom_head");
            return;
        }
        RecruitRenderProfiling.textureStateSwitch("custom_head");
        long start = RecruitRenderProfiling.start();
        ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!itemstack.isEmpty()) {
            Item item = itemstack.getItem();
            poseStack.pushPose();
            poseStack.scale(this.scaleX, this.scaleY, this.scaleZ);
            if (entity.isBaby() && !(entity instanceof Villager)) {
                poseStack.translate(0.0D, 0.03125D, 0.0D);
                poseStack.scale(0.7F, 0.7F, 0.7F);
                poseStack.translate(0.0D, 1.0D, 0.0D);
            }

            this.getParentModel().getHead().translateAndRotate(poseStack);
            if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock) {
                float f2 = 1.1875F;
                poseStack.scale(f2, -f2, -f2);
                poseStack.translate(0.0D, 0.0625D, 0.0D);

                ResolvableProfile profile = itemstack.get(DataComponents.PROFILE);
                poseStack.translate(-0.5D, 0.0D, -0.5D);
                SkullBlock.Type skullType = ((AbstractSkullBlock) ((BlockItem) item).getBlock()).getType();
                SkullModelBase skullModel = this.skullModels.get(skullType);
                RenderType renderType = SkullBlockRenderer.getRenderType(skullType, profile);
                SkullBlockRenderer.renderSkull((Direction) null, 180.0F, limbSwing, poseStack, bufferSource, packedLight, skullModel, renderType);
            } else if (!(item instanceof ArmorItem) || ((ArmorItem) item).getEquipmentSlot() != EquipmentSlot.HEAD) {
                translateToHead(poseStack);
                this.itemInHandRenderer.renderItem(entity, itemstack, ItemDisplayContext.HEAD, false, poseStack, bufferSource, packedLight);
            }
            poseStack.popPose();

        }
        RecruitRenderProfiling.layerDuration("custom_head", start);
    }

    public static void translateToHead(PoseStack poseStack) {
        float scale = 0.625F;
        poseStack.translate(0.0D, -0.25D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(scale, -scale, -scale);
        poseStack.translate(0.0D, 0.1875D, 0.0D);
    }
}
