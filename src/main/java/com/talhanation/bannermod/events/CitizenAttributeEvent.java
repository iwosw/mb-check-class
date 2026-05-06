package com.talhanation.bannermod.events;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.registry.citizen.ModCitizenEntityTypes;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Forge attribute creation for the single unified {@link CitizenEntity}
 * type. One attribute supplier for every profession — per-profession
 * buffs apply as attribute modifiers via the profession controllers
 * in Cit-03+, not as base-attribute differences.
 */
@EventBusSubscriber(modid = BannerModMain.MOD_ID)
public final class CitizenAttributeEvent {

    private CitizenAttributeEvent() {
    }

    @SubscribeEvent
    public static void entityAttributeEvent(final EntityAttributeCreationEvent event) {
        event.put(ModCitizenEntityTypes.CITIZEN.get(), CitizenEntity.createAttributes().build());
    }
}
