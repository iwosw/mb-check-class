package com.talhanation.bannermod.compat;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BetterCombatAttackBridgeTest {

    @Test
    void selectsMatchingAttackAndAppliesUpswingMultiplierAndCooldownCap() {
        BetterCombatAttackBridge bridge = new BetterCombatAttackBridge(fakeAccess());

        Optional<BetterCombatAttackBridge.AttackProfile> profile = bridge.resolveLoadedProfile(
                new FakeWeaponAttributes(new FakeAttack(new FakeCondition[]{FakeCondition.MOUNTED}, FakeHitBoxShape.HORIZONTAL_PLANE, 1.4D, 120.0D, 0.5D, "slash")),
                12,
                true,
                true,
                false);

        assertTrue(profile.isPresent());
        assertEquals(20, profile.get().cooldownTicks());
        assertEquals(15, profile.get().upswingTicks());
        assertEquals(1.4D, profile.get().damageMultiplier(), 1e-6D);
        assertEquals(120.0D, profile.get().angleDegrees(), 1e-6D);
        assertEquals(2, profile.get().shapeId());
        assertEquals("slash", profile.get().animation());
    }

    @Test
    void rejectsUnmatchedBetterCombatConditions() {
        BetterCombatAttackBridge bridge = new BetterCombatAttackBridge(fakeAccess());

        Optional<BetterCombatAttackBridge.AttackProfile> profile = bridge.resolveLoadedProfile(
                new FakeWeaponAttributes(new FakeAttack(new FakeCondition[]{FakeCondition.OFF_HAND_ONLY}, FakeHitBoxShape.FORWARD_BOX, 1.2D, 35.0D, 0.4D, "stab")),
                12,
                false,
                true,
                false);

        assertTrue(profile.isEmpty());
    }

    @Test
    void acceptsPlainAttackWithoutConditions() {
        BetterCombatAttackBridge bridge = new BetterCombatAttackBridge(fakeAccess());

        Optional<BetterCombatAttackBridge.AttackProfile> profile = bridge.resolveLoadedProfile(
                new FakeWeaponAttributes(new FakeAttack(null, FakeHitBoxShape.VERTICAL_PLANE, 1.1D, 0.0D, 0.25D, "chop")),
                16,
                false,
                true,
                false);

        assertTrue(profile.isPresent());
        assertEquals(20, profile.get().cooldownTicks());
        assertEquals(8, profile.get().upswingTicks());
        assertEquals(1.1D, profile.get().damageMultiplier(), 1e-6D);
        assertEquals(55.0D, profile.get().angleDegrees(), 1e-6D);
        assertEquals(3, profile.get().shapeId());
    }

    @Test
    void mainHandOnlyStillWorksWithShieldOffhand() {
        BetterCombatAttackBridge bridge = new BetterCombatAttackBridge(fakeAccess());

        Optional<BetterCombatAttackBridge.AttackProfile> profile = bridge.resolveLoadedProfile(
                new FakeWeaponAttributes(new FakeAttack(new FakeCondition[]{FakeCondition.MAIN_HAND_ONLY}, FakeHitBoxShape.FORWARD_BOX, 1.0D, 45.0D, 0.5D, "stab")),
                12,
                false,
                false,
                true);

        assertTrue(profile.isPresent());
    }

    private static ReflectiveCompatAccess fakeAccess() {
        return new ReflectiveCompatAccess(className -> switch (className) {
            case "net.bettercombat.BetterCombat" -> FakeBetterCombat.class;
            case "net.bettercombat.logic.WeaponRegistry" -> FakeWeaponRegistry.class;
            case "net.bettercombat.api.WeaponAttributes" -> FakeWeaponAttributes.class;
            case "net.bettercombat.api.WeaponAttributes$Attack" -> FakeAttack.class;
            default -> throw new ClassNotFoundException(className);
        });
    }

    public static final class FakeBetterCombat {
        public static final FakeServerConfig config = new FakeServerConfig();
    }

    public static final class FakeServerConfig {
        public int attack_interval_cap = 20;

        public float getUpswingMultiplier() {
            return 1.5F;
        }
    }

    public static final class FakeWeaponRegistry {
    }

    public static final class FakeWeaponAttributes {
        private final FakeAttack[] attacks;

        FakeWeaponAttributes(FakeAttack... attacks) {
            this.attacks = attacks;
        }

        public FakeAttack[] attacks() {
            return attacks;
        }
    }

    public static final class FakeAttack {
        private final FakeCondition[] conditions;
        private final FakeHitBoxShape hitbox;
        private final double damageMultiplier;
        private final double angle;
        private final double upswing;
        private final String animation;

        FakeAttack(FakeCondition[] conditions, FakeHitBoxShape hitbox, double damageMultiplier, double angle, double upswing, String animation) {
            this.conditions = conditions;
            this.hitbox = hitbox;
            this.damageMultiplier = damageMultiplier;
            this.angle = angle;
            this.upswing = upswing;
            this.animation = animation;
        }

        public FakeCondition[] conditions() {
            return conditions;
        }

        public double damageMultiplier() {
            return damageMultiplier;
        }

        public FakeHitBoxShape hitbox() {
            return hitbox;
        }

        public double angle() {
            return angle;
        }

        public double upswing() {
            return upswing;
        }

        public String animation() {
            return animation;
        }
    }

    enum FakeCondition {
        MOUNTED,
        OFF_HAND_ONLY,
        MAIN_HAND_ONLY
    }

    enum FakeHitBoxShape {
        FORWARD_BOX,
        VERTICAL_PLANE,
        HORIZONTAL_PLANE
    }
}
