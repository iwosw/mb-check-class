package com.talhanation.bannermod.combat;

import com.talhanation.bannermod.ai.military.WeaponReach;
import com.talhanation.bannermod.entity.military.HorsemanEntity;
import com.talhanation.bannermod.entity.military.IRangedRecruit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;

/**
 * Maps a {@link LivingEntity} to a {@link CombatRole} for the cavalry-charge / pike-brace
 * interaction in {@link CavalryChargePolicy}.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>Cavalry — entity is a {@link HorsemanEntity} OR is currently mounted on an
 *       {@link AbstractHorse}. The "mounted on a horse" branch covers patrol-time
 *       horsemen plus any other recruit that climbs onto a horse for a charge.</li>
 *   <li>Ranged — entity implements {@link IRangedRecruit}.</li>
 *   <li>Pike — entity holds a polearm-tier weapon (per {@link WeaponReach#PIKE_EXTRA_REACH}
 *       or higher). Sarissa-tier reach also classifies as PIKE for the pure-policy
 *       interaction; the upcoming weapon-class refactor can split it later.</li>
 *   <li>Infantry — fallback for everything else (including non-recruit hostile mobs).</li>
 * </ol>
 *
 * <p>The resolver is a pure function — it does not cache, mutate, or touch any per-entity
 * state — so call sites can use it from both the outgoing-damage hook and any future
 * sampler without coordinating lifecycle.
 */
public final class RecruitRoleResolver {

    private RecruitRoleResolver() {
    }

    public static CombatRole roleOf(LivingEntity entity) {
        if (entity == null) {
            return CombatRole.INFANTRY;
        }
        if (entity instanceof HorsemanEntity) {
            return CombatRole.CAVALRY;
        }
        if (entity.getVehicle() instanceof AbstractHorse) {
            return CombatRole.CAVALRY;
        }
        if (entity instanceof IRangedRecruit) {
            return CombatRole.RANGED;
        }
        ItemStack held = entity.getMainHandItem();
        if (held != null && !held.isEmpty()) {
            double extraReach = WeaponReach.effectiveReachFor(held);
            if (extraReach >= WeaponReach.PIKE_EXTRA_REACH) {
                return CombatRole.PIKE;
            }
        }
        return CombatRole.INFANTRY;
    }
}
