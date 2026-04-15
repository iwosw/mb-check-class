package com.talhanation.recruits.compat;

import com.talhanation.bannerlord.entity.shared.AbstractInventoryEntity;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IWeapon {
    Item getWeapon();
    double getMoveSpeedAmp();
    int getAttackCooldown();
    int getWeaponLoadTime();
    float getProjectileSpeed();
    AbstractHurtingProjectile getProjectile(LivingEntity shooter);
    AbstractArrow getProjectileArrow(LivingEntity shooter);
    AbstractHurtingProjectile shoot(LivingEntity shooter, AbstractHurtingProjectile projectile, double x, double y, double z);
    AbstractArrow shootArrow(LivingEntity shooter, AbstractArrow projectile, double x, double y, double z);

    SoundEvent getShootSound();
    SoundEvent getLoadSound();
    boolean isGun();
    boolean canMelee();
    boolean isBow();
    boolean isCrossBow();
    void performRangedAttackIWeapon(AbstractRecruitEntity shooter, double x, double y, double z, float projectileSpeed);

    default void performRangedAttackIWeapon(com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity shooter, double x, double y, double z, float projectileSpeed) {
        Class<AbstractRecruitEntity> legacyType = AbstractRecruitEntity.class;
        if (legacyType.isInstance(shooter)) {
            performRangedAttackIWeapon(legacyType.cast(shooter), x, y, z, projectileSpeed);
            return;
        }

        if (isBow() || isCrossBow()) {
            AbstractArrow projectile = getProjectileArrow(shooter);
            AbstractArrow firedProjectile = shootArrow(shooter, projectile, x, y, z);
            shooter.level().addFreshEntity(firedProjectile);
        }
        else {
            AbstractHurtingProjectile projectile = getProjectile(shooter);
            AbstractHurtingProjectile firedProjectile = shoot(shooter, projectile, x, y, z);
            shooter.level().addFreshEntity(firedProjectile);
        }

        shooter.consumeArrow();
        shooter.playSound(getShootSound(), 1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    static boolean isMusketModWeapon(ItemStack stack){
        return stack.getDescriptionId().equals("item.musketmod.musket") ||
                stack.getDescriptionId().equals("item.musketmod.musket_with_bayonet") ||
                stack.getDescriptionId().equals("item.musketmod.musket_with_scope") ||
                stack.getDescriptionId().equals("item.musketmod.blunderbuss") ||
                stack.getDescriptionId().equals("item.musketmod.cartridge") ||
                stack.getDescriptionId().equals("item.musketmod.pistol");
    }

    boolean isLoaded(ItemStack stack);

    void setLoaded(ItemStack stack, boolean loaded);
}
