package com.talhanation.bannermod.compat.weapon;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;

// Interface for bow-type weapons
public interface IBowWeapon extends IRangedWeapon {
    AbstractArrow getProjectileArrow(LivingEntity shooter);
    AbstractArrow shootArrow(LivingEntity shooter, AbstractArrow projectile, double x, double y, double z);

    // Convenient default – can be overridden if needed
    default boolean isBow() {
        return true;
    }
}