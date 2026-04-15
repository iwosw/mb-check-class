package com.talhanation.recruits.compat;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MusketWeapon implements IWeapon {

    @FunctionalInterface
    interface ReflectiveSupplier<T> {
        T get() throws ReflectiveOperationException;
    }

    static <T> T safelyResolveFallback(ReflectiveSupplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (ReflectiveOperationException | LinkageError e) {
            return fallback;
        }
    }

    @Override
    @Nullable
    public Item getWeapon() {
        return safelyResolveFallback(() -> {
            Class<?> itemClass = Class.forName("ewewukek.musketmod.Items");
            Object musketWeaponInstance = itemClass.newInstance();

            Field musketItemField = musketWeaponInstance.getClass().getField("MUSKET");
            Object item = musketItemField.get("MUSKET");
            return (Item) item;
        }, null);
    }

    private static void logMissingCompat(String target) {
        try {
            if (target != null) {
                Main.LOGGER.info("{} was not found", target);
            } else {
                Main.LOGGER.info("Optional musket compatibility classes were not found");
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public double getMoveSpeedAmp() {
        return 0.4D;
    }

    @Override
    public int getAttackCooldown() {
        return 40;//MusketItem.RELOAD_DURATION;
    }

    @Override
    public int getWeaponLoadTime() {
        return 60; //return MusketItem.LOADING_STAGE_1 + MusketItem.LOADING_STAGE_2 + MusketItem.LOADING_STAGE_3;
    }

    @Override
    public float getProjectileSpeed() {
        return 2.0F;
    }

    public boolean isLoaded(ItemStack stack) {
        Boolean loaded = safelyResolveFallback(() -> {
            Class<?> musketItemClass = Class.forName("ewewukek.musketmod.MusketItem");

            Method musketItemIsLoaded = musketItemClass.getMethod("isLoaded", ItemStack.class);
            return (boolean) musketItemIsLoaded.invoke(musketItemClass, stack);
        }, Boolean.FALSE);

        if (!loaded) {
            logMissingCompat("MusketItem");
        }
        return loaded;
    }

    @Override
    public void setLoaded(ItemStack stack, boolean loaded) {
        boolean success = safelyResolveFallback(() -> {
            Class<?> musketItemClass = Class.forName("ewewukek.musketmod.MusketItem");

            Method musketItemSetLoaded = musketItemClass.getMethod("setLoaded", ItemStack.class, boolean.class);

            musketItemSetLoaded.invoke(musketItemClass, stack, loaded);
            return true;
        }, Boolean.FALSE);

        if (!success) {
            logMissingCompat("MusketItem");
        }
    }

    @Override
    public AbstractHurtingProjectile getProjectile(LivingEntity shooter) {
        AbstractHurtingProjectile projectile = safelyResolveFallback(() -> {
            Class<?> bulletClass = Class.forName("ewewukek.musketmod.BulletEntity");
            Class<?>[] constructorParamTypes = {Level.class};
            Constructor<?> bulletConstructor = bulletClass.getConstructor(constructorParamTypes);
            Level level = shooter.getCommandSenderWorld();
            Object bulletInstance = bulletConstructor.newInstance(level);

            if(bulletInstance instanceof AbstractHurtingProjectile bullet){
                bullet.setOwner(shooter);
                bullet.setPos(shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1D, shooter.getZ());

                return bullet;
            }
            return null;
        }, null);

        if (projectile == null) {
            logMissingCompat("MusketItem");
        }
        return projectile;
    }

    @Override
    public AbstractArrow getProjectileArrow(LivingEntity shooter) {
        return null;
    }

    @Override
    @Nullable
    public AbstractHurtingProjectile shoot(LivingEntity shooter, AbstractHurtingProjectile projectile, double x, double y, double z) {
        if(!shooter.getCommandSenderWorld().isClientSide()){
            double d3 = Mth.sqrt((float) (x * x + z * z));
            Vec3 vec3 = (new Vec3(x, y + d3 * (double) 0.065, z)).normalize().scale(10F);
            try {
                Class<?> bulletClass = Class.forName("ewewukek.musketmod.BulletEntity");
                if (bulletClass.isInstance(projectile)) {
                    Object bullet = bulletClass.cast(projectile);

                    Field bulletDamageField = bullet.getClass().getField("damage");
                    bulletDamageField.setAccessible(true);

                    Method bulletClassSetInitialSpeedMethod = bullet.getClass().getMethod("setInitialSpeed", float.class);

                    bulletClassSetInitialSpeedMethod.invoke(bullet, 5F);
                    bulletDamageField.setFloat(bullet, 60F);//player damage is 15 hp this value is tasted to match



                    projectile.setDeltaMovement(vec3);
                    projectile.shoot(x, y + d3 * (double) 0.065, z, 4.5F, (float) (2));
                }

            } catch (NoSuchFieldException e) {
                Main.LOGGER.error("bulletDamageField was not found (NoSuchFieldException)");
            } catch (ClassNotFoundException e) {
                Main.LOGGER.error("BulletEntity.class was not found (ClassNotFoundException)");
            } catch (InvocationTargetException e) {
                Main.LOGGER.error("bulletClassSetInitialSpeedMethod was not found (InvocationTargetException)");
            } catch (NoSuchMethodException e) {
                Main.LOGGER.error("bulletClassSetDeltaMovementMethod was not found (NoSuchMethodException)");
            } catch (IllegalAccessException e) {
                Main.LOGGER.error("BulletEntity.class was not found (IllegalAccessException)");
            }

            Vec3 forward = new Vec3(x, y, z).normalize();
            Vec3 origin = new Vec3(shooter.getX(), shooter.getEyeY(), shooter.getZ());

            try{
                Class<?> musketModClass = Class.forName("ewewukek.musketmod.MusketMod");
                Method sendSmokeEffectMethod = musketModClass.getMethod("sendSmokeEffect", ServerLevel.class, Vec3.class, Vec3.class);
                sendSmokeEffectMethod.invoke(musketModClass, (ServerLevel) shooter.getCommandSenderWorld(), origin, forward);

            } catch (ClassNotFoundException e) {
                Main.LOGGER.error("MusketMod.class was not found (ClassNotFoundException)");

            } catch (InvocationTargetException e) {
                Main.LOGGER.error("sendSmokeEffectMethod was not found (InvocationTargetException)");

            } catch (NoSuchMethodException e) {
                Main.LOGGER.error("sendSmokeEffectMethod was not found (NoSuchMethodException)");

            } catch (IllegalAccessException e) {
                Main.LOGGER.error("MusketMod.class was not found (IllegalAccessException)");

            }
            return projectile;
        }
        return null;
    }

    @Override
    public AbstractArrow shootArrow(LivingEntity shooter, AbstractArrow projectile, double x, double y, double z) {
        return null;
    }

    @Override
    public SoundEvent getShootSound() {
        SoundEvent sound = safelyResolveFallback(() -> {
            Class<?> itemClass = Class.forName("ewewukek.musketmod.Sounds");
            Object musketWeaponInstance = itemClass.newInstance();

            Field musketItemField = musketWeaponInstance.getClass().getField("MUSKET_FIRE");
            Object soundEvent = musketItemField.get("MUSKET_FIRE");
            return (SoundEvent) soundEvent;
        }, null);

        if (sound == null) {
            logMissingCompat("Musket sounds");
        }
        return sound;
    }

    @Override
    public SoundEvent getLoadSound() {
        SoundEvent sound = safelyResolveFallback(() -> {
            Class<?> itemClass = Class.forName("ewewukek.musketmod.Sounds");
            Object musketWeaponInstance = itemClass.newInstance();

            Field musketItemField = musketWeaponInstance.getClass().getField("MUSKET_READY");
            Object soundEvent = musketItemField.get("MUSKET_READY");
            return (SoundEvent) soundEvent;
        }, null);

        if (sound == null) {
            logMissingCompat("Musket sounds");
        }
        return sound;
    }

    @Override
    public boolean isGun() {
        return true;
    }

    @Override
    public boolean canMelee() {
        return false;
    }

    @Override
    public boolean isBow(){
        return false;
    }

    @Override
    public boolean isCrossBow() {
        return false;
    }

    @Override
    public void performRangedAttackIWeapon(AbstractRecruitEntity shooter, double x, double y, double z, float projectileSpeed) {
        AbstractHurtingProjectile projectileEntity = this.getProjectile(shooter);
        double d0 = x - shooter.getX();
        double d1 = y + 0.5D - projectileEntity.getY();
        double d2 = z - shooter.getZ();


        this.shoot(shooter, projectileEntity, d0, d1, d2);

        shooter.playSound(this.getShootSound(), 1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
        shooter.getCommandSenderWorld().addFreshEntity(projectileEntity);

        shooter.damageMainHandItem();
    }

}
