package com.talhanation.recruits.compat;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompatSafeDegradationTest {

    @Test
    void smallShipsHelperReturnsFalseWhenOptionalClassesAreAbsentOrBroken() throws Exception {
        Method helper = SmallShips.class.getDeclaredMethod("isSmallShipEntity", Object.class, SmallShips.ReflectiveClassResolver.class);
        helper.setAccessible(true);

        boolean nullResult = (boolean) helper.invoke(null, null, (SmallShips.ReflectiveClassResolver) name -> {
            throw new ClassNotFoundException(name);
        });
        boolean brokenDependencyResult = (boolean) helper.invoke(null, new Object(), (SmallShips.ReflectiveClassResolver) name -> {
            throw new NoClassDefFoundError(name);
        });

        assertFalse(nullResult);
        assertFalse(brokenDependencyResult);
        assertFalse(SmallShips.isSmallShip(null));
    }

    @Test
    void musketWeaponMethodsReturnSafeFallbacksWhenOptionalClassesAreAbsent() throws Exception {
        Method helper = MusketWeapon.class.getDeclaredMethod("safelyResolveFallback", MusketWeapon.ReflectiveSupplier.class, Object.class);
        helper.setAccessible(true);

        Object fallbackFromMissingClass = helper.invoke(null, (MusketWeapon.ReflectiveSupplier<Object>) () -> {
            throw new ClassNotFoundException("missing musket class");
        }, "fallback");
        Object fallbackFromBrokenDependency = helper.invoke(null, (MusketWeapon.ReflectiveSupplier<Object>) () -> {
            throw new NoClassDefFoundError("missing dependency");
        }, Boolean.FALSE);

        MusketWeapon weapon = new MusketWeapon();

        assertNull(weapon.getWeapon());
        assertFalse(weapon.isLoaded(null));
        assertDoesNotThrow(() -> weapon.setLoaded(null, true));
        assertNull(weapon.getProjectile(null));
        assertNull(weapon.getShootSound());
        assertNull(weapon.getLoadSound());
        assertFalse((Boolean) fallbackFromBrokenDependency);
        org.junit.jupiter.api.Assertions.assertEquals("fallback", fallbackFromMissingClass);
    }
}
