package com.talhanation.bannermod.compat.weapon;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

// Interface for crossbow-type weapons
public interface ICrossbowWeapon extends IRangedWeapon {
    AbstractArrow getProjectileArrow(LivingEntity shooter);
    AbstractArrow shootArrow(LivingEntity shooter, AbstractArrow projectile, double x, double y, double z);

    boolean isLoaded(ItemStack stack);
    void setLoaded(ItemStack stack, boolean loaded);
    SoundEvent getLoadSound(); // crossbows have loading sound (optional to implement)

    default boolean isCrossBow() {
        return true;
    }
}