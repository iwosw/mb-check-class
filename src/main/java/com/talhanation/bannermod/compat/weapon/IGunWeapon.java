package com.talhanation.bannermod.compat.weapon;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;

// Interface for gun-type weapons (muskets, pistols, blunderbusses)
public interface IGunWeapon extends IRangedWeapon {
    AbstractHurtingProjectile getProjectile(LivingEntity shooter);
    AbstractHurtingProjectile shoot(LivingEntity shooter, AbstractHurtingProjectile projectile, double x, double y, double z);
    SoundEvent getShootSound();
    SoundEvent getLoadSound();
    boolean isLoaded(ItemStack stack);
    void setLoaded(ItemStack stack, boolean loaded);
    boolean canMelee();
}