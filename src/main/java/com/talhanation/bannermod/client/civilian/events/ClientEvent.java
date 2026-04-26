package com.talhanation.bannermod.client.civilian.events;

import com.talhanation.bannermod.config.RecruitsClientConfig;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.citizen.render.CitizenRenderer;
import com.talhanation.bannermod.client.civilian.render.FishingBobberRenderer;
import com.talhanation.bannermod.client.civilian.render.WorkerAreaRenderer;
import com.talhanation.bannermod.client.civilian.render.WorkerVillagerRenderer;
import com.talhanation.bannermod.registry.citizen.ModCitizenEntityTypes;
import com.talhanation.bannermod.registry.civilian.ModEntityTypes;
import com.talhanation.bannermod.client.civilian.render.WorkerHumanRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD , value = Dist.CLIENT)
public class ClientEvent {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void entityRenderersEvent(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntityTypes.CROPAREA.get(), WorkerAreaRenderer::new);
        EntityRenderers.register(ModEntityTypes.LUMBERAREA.get(), WorkerAreaRenderer::new);
        EntityRenderers.register(ModEntityTypes.BUILDAREA.get(), WorkerAreaRenderer::new);
        EntityRenderers.register(ModEntityTypes.MININGAREA.get(), WorkerAreaRenderer::new);
        EntityRenderers.register(ModEntityTypes.STORAGEAREA.get(), WorkerAreaRenderer::new);
        EntityRenderers.register(ModEntityTypes.MARKETAREA.get(), WorkerAreaRenderer::new);
        EntityRenderers.register(ModEntityTypes.FISHINGAREA.get(), WorkerAreaRenderer::new);
        EntityRenderers.register(ModEntityTypes.ANIMAL_PEN_AREA.get(), WorkerAreaRenderer::new);

        EntityRenderers.register(ModEntityTypes.FISHING_BOBBER.get(), FishingBobberRenderer::new);
        EntityRenderers.register(ModCitizenEntityTypes.CITIZEN.get(), CitizenRenderer::new);


        // Config read is deferred into the provider lambda — `provider.create(ctx)` runs in
        // `EntityRenderDispatcher.onResourceManagerReload`, AFTER `FMLClientSetupEvent` / `ModConfigEvent.Loading`,
        // so `RecruitsClientConfig.CLIENT` is loaded by the time `.get()` is invoked. Reading at
        // RegisterRenderers time would trip `ForgeConfigSpec$ConfigValue.get` precondition (#21-UAT gap).
        net.minecraft.client.renderer.entity.EntityRendererProvider<com.talhanation.bannermod.entity.military.AbstractRecruitEntity> workerProvider =
                ctx -> RecruitsClientConfig.RecruitsLookLikeVillagers.get()
                        ? new WorkerVillagerRenderer(ctx)
                        : new WorkerHumanRenderer(ctx);
        EntityRenderers.register(ModEntityTypes.FARMER.get(), workerProvider);
        EntityRenderers.register(ModEntityTypes.LUMBERJACK.get(), workerProvider);
        EntityRenderers.register(ModEntityTypes.MINER.get(), workerProvider);
        EntityRenderers.register(ModEntityTypes.BUILDER.get(), workerProvider);
        EntityRenderers.register(ModEntityTypes.MERCHANT.get(), workerProvider);
        EntityRenderers.register(ModEntityTypes.FISHERMAN.get(), workerProvider);
        EntityRenderers.register(ModEntityTypes.ANIMAL_FARMER.get(), workerProvider);
    }
}
