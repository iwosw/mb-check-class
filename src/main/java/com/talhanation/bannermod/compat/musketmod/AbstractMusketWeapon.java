package com.talhanation.bannermod.compat.musketmod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.compat.weapon.IGunWeapon;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

// general realization IGunWeapon for musket mod

public abstract class AbstractMusketWeapon implements IGunWeapon {

    public AbstractMusketWeapon() {
        MusketModReflection.init();
    }

    protected abstract Field getWeaponField();
    protected abstract Field getShootSoundField();
    protected abstract Method getIsLoadedMethod();
    protected abstract Method getSetLoadedMethod();
    protected abstract float getBulletDamage();
    protected abstract float getBulletAccuracy();

    @Override
    @Nullable
    public Item getWeapon() {
        if (!MusketModReflection.hasMusketMod) return null;
        try {
            Field field = getWeaponField();
            if (field == null) return null;
            return (Item) field.get(null);
        } catch (Exception e) {
            BannerModMain.LOGGER.error("Items of MusketMod was not found", e);
            return null;
        }
    }

    @Override
    public double getMoveSpeedAmp() {
        return 0.4D;
    }

    @Override
    public float getProjectileSpeed() {
        return 2.0F;
    }

    @Override
    public boolean isLoaded(ItemStack stack) {
        if (!MusketModReflection.hasMusketMod) return false;
        try {
            Method isLoadedMethod = getIsLoadedMethod();
            if (isLoadedMethod == null) return false;
            return (boolean) isLoadedMethod.invoke(null, stack);
        } catch (Exception e) {
            BannerModMain.LOGGER.info("isLoaded was not found");
            return false;
        }
    }

    @Override
    public void setLoaded(ItemStack stack, boolean loaded) {
        if (!MusketModReflection.hasMusketMod) return;
        try {
            Method setLoadedMethod = getSetLoadedMethod();
            if (setLoadedMethod == null) return;
            setLoadedMethod.invoke(null, stack, loaded);
        } catch (Exception e) {
            BannerModMain.LOGGER.info("setLoaded was not found");
        }
    }

    @Override
    @Nullable
    public AbstractHurtingProjectile getProjectile(LivingEntity shooter) {
        if (!MusketModReflection.hasMusketMod) return null;
        try {
            if (MusketModReflection.bulletConstructor == null) return null;
            Object bulletInstance = MusketModReflection.bulletConstructor.newInstance(shooter.getCommandSenderWorld());
            if (bulletInstance instanceof AbstractHurtingProjectile bullet) {
                bullet.setOwner(shooter);
                bullet.setPos(shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1D, shooter.getZ());
                return bullet;
            }
        } catch (Exception e) {
            BannerModMain.LOGGER.info("MusketItem was not found (projectile)");
        }
        return null;
    }

    @Override
    @Nullable
    public AbstractHurtingProjectile shoot(LivingEntity shooter, AbstractHurtingProjectile projectile, double x, double y, double z) {
        if (!shooter.getCommandSenderWorld().isClientSide() && MusketModReflection.hasMusketMod) {
            double d3 = Mth.sqrt((float) (x * x + z * z));
            Vec3 vec3 = (new Vec3(x, y + d3 * (double) 0.065, z)).normalize().scale(10F);

            try {
                if (MusketModReflection.bulletClass != null && MusketModReflection.bulletClass.isInstance(projectile)) {
                    if (MusketModReflection.bulletSetInitialSpeedMethod != null) {
                        MusketModReflection.bulletSetInitialSpeedMethod.invoke(projectile, 5F);
                    }
                    if (MusketModReflection.bulletDamageField != null) {
                        MusketModReflection.bulletDamageField.setFloat(projectile, getBulletDamage());
                    }

                    projectile.setDeltaMovement(vec3);
                    projectile.shoot(x, y + d3 * (double) 0.065, z, 4.5F, getBulletAccuracy());
                }
            } catch (Exception e) {
                BannerModMain.LOGGER.error("Error setting bullet properties via reflection", e);
            }

            Vec3 forward = new Vec3(x, y, z).normalize();
            Vec3 origin = new Vec3(shooter.getX(), shooter.getEyeY(), shooter.getZ());

            try {
                if (MusketModReflection.sendSmokeEffectMethod != null) {
                    MusketModReflection.sendSmokeEffectMethod.invoke(null, (ServerLevel) shooter.getCommandSenderWorld(), origin, forward);
                }
            } catch (Exception e) {
                BannerModMain.LOGGER.error("sendSmokeEffectMethod was not found / failed", e);
            }

            return projectile;
        }
        return null;
    }

    @Override
    @Nullable
    public SoundEvent getShootSound() {
        if (!MusketModReflection.hasMusketMod) return null;
        try {
            Field field = getShootSoundField();
            if (field == null) return null;
            return (SoundEvent) field.get(null);
        } catch (Exception e) {
            BannerModMain.LOGGER.error("Sounds of MusketMod was not found", e);
            return null;
        }
    }

    @Override
    @Nullable
    public SoundEvent getLoadSound() {
        if (!MusketModReflection.hasMusketMod) return null;
        try {
            if (MusketModReflection.musketReadyField == null) return null;
            return (SoundEvent) MusketModReflection.musketReadyField.get(null);
        } catch (Exception e) {
            BannerModMain.LOGGER.error("Sounds of MusketMod was not found", e);
            return null;
        }
    }

    @Override
    public boolean canMelee() {
        return false;
    }

    @Override
    public void performRangedAttackIWeapon(AbstractRecruitEntity shooter, double x, double y, double z, float projectileSpeed) {
        AbstractHurtingProjectile projectileEntity = this.getProjectile(shooter);
        if (projectileEntity == null) return;

        double d0 = x - shooter.getX();
        double d1 = y + 0.5D - projectileEntity.getY();
        double d2 = z - shooter.getZ();

        this.shoot(shooter, projectileEntity, d0, d1, d2);

        SoundEvent sound = this.getShootSound();
        if (sound != null) {
            shooter.playSound(sound, 1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
        }
        shooter.getCommandSenderWorld().addFreshEntity(projectileEntity);

        shooter.damageMainHandItem();
    }
}