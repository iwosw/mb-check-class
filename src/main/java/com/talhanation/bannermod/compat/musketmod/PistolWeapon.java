package com.talhanation.bannermod.compat.musketmod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PistolWeapon extends AbstractMusketWeapon {

    @Override
    protected Field getWeaponField() { return MusketModReflection.pistolField; }

    @Override
    protected Field getShootSoundField() { return MusketModReflection.pistolFireField; }

    @Override
    protected Method getIsLoadedMethod() { return MusketModReflection.pistolItemIsLoadedMethod; }

    @Override
    protected Method getSetLoadedMethod() { return MusketModReflection.pistolItemSetLoadedMethod; }

    @Override
    protected float getBulletDamage() { return 40F; }

    @Override
    protected float getBulletAccuracy() { return 1F; }

    @Override
    public int getAttackCooldown() {
        return 30;
    }

    @Override
    public int getWeaponLoadTime() {
        return 50;
    }
}