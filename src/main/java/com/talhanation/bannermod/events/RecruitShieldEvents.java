package com.talhanation.bannermod.events;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.neoforged.neoforge.event.entity.living.ShieldBlockEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Stage 2 combat AI: disable vanilla's full-block shield path for recruits so that
 * BannerMod's stance-aware directional mitigation in
 * {@link com.talhanation.bannermod.entity.military.RecruitCombatOverrideService#prepareIncomingDamage}
 * is the single source of shield behaviour.
 *
 * <p>Without this, vanilla {@code LivingEntity.hurt} zeroes the damage and separately
 * calls {@code hurtCurrentlyUsedShield(amount)}, which (a) bypasses our stance-driven
 * damage reduction and (b) double-charges shield durability.
 */
public final class RecruitShieldEvents {

    @SubscribeEvent
    public void onRecruitShieldBlock(ShieldBlockEvent event) {
        if (event.getEntity() instanceof AbstractRecruitEntity) {
            // Cancelling the event aborts vanilla shield blocking for this hit: damage
            // continues unzeroed and shield durability is not charged. BannerMod then
            // applies its own stance-aware directional mitigation in prepareIncomingDamage.
            event.setCanceled(true);
        }
    }
}
