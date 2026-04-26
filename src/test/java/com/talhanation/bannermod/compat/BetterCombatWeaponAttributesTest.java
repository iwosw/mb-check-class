package com.talhanation.bannermod.compat;

import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BetterCombatWeaponAttributesTest {

    @AfterEach
    void resetFakeRegistry() {
        FakeWeaponRegistry.attributes = new FakeWeaponAttributes(6.5D);
    }

    @Test
    void absentBetterCombatReturnsEmpty() {
        BetterCombatWeaponAttributes attributes = new BetterCombatWeaponAttributes(new ReflectiveCompatAccess(className -> {
            throw new ClassNotFoundException(className);
        }));

        assertTrue(attributes.resolveLoadedAttackRange(null).isEmpty());
    }

    @Test
    void reflectedAttackRangeReturnsPositiveNumber() {
        BetterCombatWeaponAttributes attributes = new BetterCombatWeaponAttributes(fakeAccess());

        OptionalDouble range = attributes.resolveLoadedAttackRange(null);

        assertTrue(range.isPresent());
        assertEquals(6.5D, range.getAsDouble(), 1e-6D);
    }

    @Test
    void missingAttributesReturnEmpty() {
        FakeWeaponRegistry.attributes = null;
        BetterCombatWeaponAttributes attributes = new BetterCombatWeaponAttributes(fakeAccess());

        assertTrue(attributes.resolveLoadedAttackRange(null).isEmpty());
    }

    @Test
    void nonPositiveRangeReturnsEmpty() {
        FakeWeaponRegistry.attributes = new FakeWeaponAttributes(0.0D);
        BetterCombatWeaponAttributes attributes = new BetterCombatWeaponAttributes(fakeAccess());

        assertTrue(attributes.resolveLoadedAttackRange(null).isEmpty());
    }

    @Test
    void extraReachOnlyCountsAboveBaseline() {
        FakeWeaponRegistry.attributes = new FakeWeaponAttributes(7.25D);
        BetterCombatWeaponAttributes attributes = new BetterCombatWeaponAttributes(fakeAccess());

        assertEquals(2.25D,
                attributes.resolveLoadedAttackRange(null).getAsDouble() - 5.0D,
                1e-6D);
    }

    private static ReflectiveCompatAccess fakeAccess() {
        return new ReflectiveCompatAccess(className -> switch (className) {
            case "net.bettercombat.logic.WeaponRegistry" -> FakeWeaponRegistry.class;
            case "net.bettercombat.api.WeaponAttributes" -> FakeWeaponAttributes.class;
            default -> throw new ClassNotFoundException(className);
        });
    }

    public static final class FakeWeaponRegistry {
        static FakeWeaponAttributes attributes = new FakeWeaponAttributes(6.5D);

        public static FakeWeaponAttributes getAttributes(ItemStack stack) {
            return attributes;
        }
    }

    public static final class FakeWeaponAttributes {
        private final double attackRange;

        FakeWeaponAttributes(double attackRange) {
            this.attackRange = attackRange;
        }

        public double attackRange() {
            return attackRange;
        }
    }
}
