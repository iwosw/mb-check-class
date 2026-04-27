package com.talhanation.bannermod.compat.musketmod;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BlunderbussWeapon extends AbstractMusketWeapon {

    @Override
    protected Field getWeaponField() { return MusketModReflection.blunderbussField; }

    @Override
    protected Field getShootSoundField() { return MusketModReflection.blunderbussFireField; }

    @Override
    protected Method getIsLoadedMethod() { return MusketModReflection.pistolItemIsLoadedMethod; }

    @Override
    protected Method getSetLoadedMethod() { return MusketModReflection.pistolItemSetLoadedMethod; }

    @Override
    protected float getBulletDamage() { return 10F; }

    @Override
    protected float getBulletAccuracy() { return 6F; }

    @Override
    public int getAttackCooldown() {
        return 50;
    }

    @Override
    public int getWeaponLoadTime() {
        return 70;
    }

    @Override
    public void performRangedAttackIWeapon(AbstractRecruitEntity shooter, double x, double y, double z, float projectileSpeed) {
        // Unique logic
        boolean hasFired = false;
        for(int i = 0; i < 9; i++) {
            AbstractHurtingProjectile projectileEntity = this.getProjectile(shooter);
            if (projectileEntity == null) continue;

            double d0 = x - shooter.getX();
            double d1 = y + 0.5D - projectileEntity.getY();
            double d2 = z - shooter.getZ();

            this.shoot(shooter, projectileEntity, d0, d1, d2);

            shooter.getCommandSenderWorld().addFreshEntity(projectileEntity);
            hasFired = true;
        }

        if (hasFired) {
            shooter.playSound(this.getShootSound(), 1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
            shooter.damageMainHandItem();
        }
    }
}