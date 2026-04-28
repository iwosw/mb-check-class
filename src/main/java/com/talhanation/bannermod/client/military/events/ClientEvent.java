package com.talhanation.bannermod.client.military.events;


import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.models.RecruitVillagerModel;
import com.talhanation.bannermod.client.military.render.RecruitHumanRenderer;
import com.talhanation.bannermod.client.military.render.RecruitVillagerRenderer;
import com.talhanation.bannermod.client.military.render.SiegeStandardBlockEntityRenderer;
import com.talhanation.bannermod.client.military.render.layer.RecruitArmorLayer;
import com.talhanation.bannermod.config.RecruitsClientConfig;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import com.talhanation.bannermod.registry.war.ModWarBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD , value = Dist.CLIENT)
public class ClientEvent {

    public static ModelLayerLocation RECRUIT = new ModelLayerLocation(new ResourceLocation(BannerModMain.MOD_ID + "recruit"), "recruit");
    public static ModelLayerLocation RECRUIT_OUTER_ARMOR = new ModelLayerLocation(new ResourceLocation(BannerModMain.MOD_ID + "recruit_outer_layer"), "recruit_outer_layer");
    public static ModelLayerLocation RECRUIT_INNER_ARMOR = new ModelLayerLocation(new ResourceLocation(BannerModMain.MOD_ID + "recruit_inner_layer"), "recruit_inner_layer");

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void entityRenderersEvent(EntityRenderersEvent.RegisterRenderers event){
        // Config read is deferred into the provider lambda — `provider.create(ctx)` runs in
        // `EntityRenderDispatcher.onResourceManagerReload`, AFTER `ModConfigEvent.Loading`, so
        // `.get()` is safe. Reading at RegisterRenderers time trips `ForgeConfigSpec$ConfigValue.get`
        // precondition (IllegalStateException: Cannot get config value before config is loaded).
        net.minecraft.client.renderer.entity.EntityRendererProvider<com.talhanation.bannermod.entity.military.AbstractRecruitEntity> recruitProvider =
                ctx -> RecruitsClientConfig.RecruitsLookLikeVillagers.get()
                        ? new RecruitVillagerRenderer(ctx)
                        : new RecruitHumanRenderer(ctx);
        EntityRenderers.register(ModEntityTypes.RECRUIT.get(), recruitProvider);
        EntityRenderers.register(ModEntityTypes.BOWMAN.get(), recruitProvider);
        EntityRenderers.register(ModEntityTypes.NOMAD.get(), recruitProvider);
        EntityRenderers.register(ModEntityTypes.HORSEMAN.get(), recruitProvider);
        EntityRenderers.register(ModEntityTypes.CROSSBOWMAN.get(), recruitProvider);
        EntityRenderers.register(ModEntityTypes.RECRUIT_SHIELDMAN.get(), recruitProvider);

        //COMPANIONS
        EntityRenderers.register(ModEntityTypes.MESSENGER.get(), recruitProvider);
        EntityRenderers.register(ModEntityTypes.SCOUT.get(), recruitProvider);
        EntityRenderers.register(ModEntityTypes.PATROL_LEADER.get(), recruitProvider);
        EntityRenderers.register(ModEntityTypes.CAPTAIN.get(), recruitProvider);

        //OTHER
        EntityRenderers.register(ModEntityTypes.VILLAGER_NOBLE.get(), recruitProvider);

        // War — political colour cap above the static siege-standard block model.
        event.registerBlockEntityRenderer(ModWarBlockEntities.SIEGE_STANDARD.get(), SiegeStandardBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ClientEvent.RECRUIT, RecruitVillagerModel::createLayerDefinition);
        event.registerLayerDefinition(ClientEvent.RECRUIT_OUTER_ARMOR, RecruitArmorLayer::createOuterArmorLayer);
        event.registerLayerDefinition(ClientEvent.RECRUIT_INNER_ARMOR, RecruitArmorLayer::createInnerArmorLayer);

    }

    @Nullable
    public static Entity getEntityByLooking() {
        HitResult hit = Minecraft.getInstance().hitResult;

        if (hit instanceof EntityHitResult entityHitResult){
            return entityHitResult.getEntity();
        }
        return null;
    }
}