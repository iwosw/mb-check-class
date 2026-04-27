package com.talhanation.bannermod.compat.weapon;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.item.Item;

// Base interface for all ranged weapons used by recruits
public interface IRangedWeapon {
    Item getWeapon();
    double getMoveSpeedAmp();
    int getAttackCooldown();
    int getWeaponLoadTime();
    float getProjectileSpeed();
    void performRangedAttackIWeapon(AbstractRecruitEntity shooter, double x, double y, double z, float projectileSpeed);
}