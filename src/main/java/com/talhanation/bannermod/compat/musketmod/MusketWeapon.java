package com.talhanation.bannermod.compat.musketmod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MusketWeapon extends AbstractMusketWeapon {

    @Override
    protected Field getWeaponField() { return MusketModReflection.musketField; }

    @Override
    protected Field getShootSoundField() { return MusketModReflection.musketFireField; }

    @Override
    protected Method getIsLoadedMethod() { return MusketModReflection.musketItemIsLoadedMethod; }

    @Override
    protected Method getSetLoadedMethod() { return MusketModReflection.musketItemSetLoadedMethod; }

    @Override
    protected float getBulletDamage() { return 60F; }

    @Override
    protected float getBulletAccuracy() { return 2F; }

    @Override
    public int getAttackCooldown() {
        return 40;
    }

    @Override
    public int getWeaponLoadTime() {
        return 60;
    }
}