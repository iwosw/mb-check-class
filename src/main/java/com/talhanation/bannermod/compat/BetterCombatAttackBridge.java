package com.talhanation.bannermod.compat;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.util.AttackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.WeakHashMap;

/**
 * BannerMod-owned Better Combat adapter for recruit melee attacks.
 *
 * <p>Better Mob Combat is deliberately not used here. Better Combat supplies
 * weapon metadata; BannerMod owns mob windup, target validation, and damage.
 */
public final class BetterCombatAttackBridge {
    private static final String BETTER_COMBAT_CLASS = "net.bettercombat.BetterCombat";
    private static final String WEAPON_REGISTRY_CLASS = "net.bettercombat.logic.WeaponRegistry";
    private static final String WEAPON_ATTRIBUTES_CLASS = "net.bettercombat.api.WeaponAttributes";
    private static final String ATTACK_CLASS = "net.bettercombat.api.WeaponAttributes$Attack";

    private static final double DEFAULT_DAMAGE_MULTIPLIER = 1.0D;
    private static final int MAX_SECONDARY_TARGETS = 3;
    private static final BetterCombatAttackBridge DEFAULT = new BetterCombatAttackBridge(new ReflectiveCompatAccess());

    private final ReflectiveCompatAccess access;
    private final Map<AbstractRecruitEntity, PendingAttack> pendingAttacks = new WeakHashMap<>();

    BetterCombatAttackBridge(ReflectiveCompatAccess access) {
        this.access = access;
    }

    public static OptionalInt tryStartAttack(AbstractRecruitEntity recruit, LivingEntity target, int bannerCooldownTicks) {
        return DEFAULT.tryStartLoadedAttack(recruit, target, bannerCooldownTicks);
    }

    public static void tickPendingAttack(AbstractRecruitEntity recruit) {
        DEFAULT.tickLoadedPendingAttack(recruit);
    }

    OptionalInt tryStartLoadedAttack(AbstractRecruitEntity recruit, LivingEntity target, int bannerCooldownTicks) {
        if (recruit == null || target == null || recruit.level().isClientSide()) {
            return OptionalInt.empty();
        }
        Optional<AttackProfile> profile = resolveLoadedProfile(
                recruit.getMainHandItem(),
                bannerCooldownTicks,
                recruit.getVehicle() instanceof LivingEntity,
                recruit.getOffhandItem().isEmpty(),
                recruit.getOffhandItem().getItem() instanceof ShieldItem);
        if (profile.isEmpty()) {
            return OptionalInt.empty();
        }

        AttackProfile attack = profile.get();
        recruit.swing(InteractionHand.MAIN_HAND);
        recruit.setBetterCombatAttackPresentation(attack.cooldownTicks(), attack.cooldownTicks(), attack.upswingTicks(), attack.shapeId());
        pendingAttacks.put(recruit, new PendingAttack(target.getId(), attack.upswingTicks(), attack.cooldownTicks(), attack));
        return OptionalInt.of(attack.cooldownTicks());
    }

    void tickLoadedPendingAttack(AbstractRecruitEntity recruit) {
        if (recruit == null || recruit.level().isClientSide()) {
            return;
        }
        PendingAttack pending = pendingAttacks.get(recruit);
        if (pending == null) {
            return;
        }
        int ticksLeft = pending.ticksLeft() - 1;
        int presentationTicks = Math.max(0, pending.presentationTicks() - 1);
        recruit.setBetterCombatAttackPresentation(
                presentationTicks,
                pending.profile().cooldownTicks(),
                pending.profile().upswingTicks(),
                pending.profile().shapeId());
        if (ticksLeft > 0) {
            pendingAttacks.put(recruit, new PendingAttack(pending.targetId(), ticksLeft, presentationTicks, pending.profile()));
            return;
        }
        pendingAttacks.remove(recruit);
        Entity entity = recruit.level().getEntity(pending.targetId());
        if (!(entity instanceof LivingEntity target) || target.isDeadOrDying() || target.isRemoved()) {
            return;
        }
        if (!recruit.shouldAttack(target) || recruit.distanceToSqr(target) > AttackUtil.getAttackReachSqr(recruit)) {
            return;
        }
        recruit.doHurtTarget(target, pending.profile().damageMultiplier());
        applySecondaryTargets(recruit, target, pending.profile());
    }

    Optional<AttackProfile> resolveLoadedProfile(
            ItemStack stack,
            int bannerCooldownTicks,
            boolean mounted,
            boolean offhandEmpty,
            boolean offhandShield) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        Optional<Object> attributes = access.findMethod(WEAPON_REGISTRY_CLASS, "getAttributes", ItemStack.class)
                .flatMap(method -> access.invoke(method, null, stack));
        if (attributes.isEmpty()) {
            return Optional.empty();
        }
        return resolveLoadedProfile(attributes.get(), bannerCooldownTicks, mounted, offhandEmpty, offhandShield);
    }

    Optional<AttackProfile> resolveLoadedProfile(
            Object attributes,
            int bannerCooldownTicks,
            boolean mounted,
            boolean offhandEmpty,
            boolean offhandShield) {
        if (attributes == null) {
            return Optional.empty();
        }
        Optional<Object> selectedAttack = selectAttack(attributes, mounted, offhandEmpty, offhandShield);
        if (selectedAttack.isEmpty()) {
            return Optional.empty();
        }
        double damageMultiplier = readDouble(selectedAttack.get(), "damageMultiplier", DEFAULT_DAMAGE_MULTIPLIER);
        if (damageMultiplier <= 0.0D) {
            damageMultiplier = DEFAULT_DAMAGE_MULTIPLIER;
        }

        int cooldown = Math.max(1, bannerCooldownTicks);
        int cap = attackIntervalCap();
        if (cap > 0) {
            cooldown = Math.max(cooldown, cap);
        }
        double upswing = readDouble(selectedAttack.get(), "upswing", 0.5D) * upswingMultiplier();
        upswing = Math.max(0.0D, Math.min(1.0D, upswing));
        int upswingTicks = Math.max(1, (int) Math.round(cooldown * upswing));
        double angle = readDouble(selectedAttack.get(), "angle", 0.0D);
        String hitboxShape = readName(selectedAttack.get(), "hitbox", "FORWARD_BOX");
        String animation = readString(selectedAttack.get(), "animation", "");
        return Optional.of(new AttackProfile(cooldown, upswingTicks, damageMultiplier,
                normalizeAngle(angle, hitboxShape), shapeId(hitboxShape), animation));
    }

    private void applySecondaryTargets(AbstractRecruitEntity recruit, LivingEntity primaryTarget, AttackProfile profile) {
        if (profile.angleDegrees() <= 0.0D) {
            return;
        }
        double reach = Math.sqrt(AttackUtil.getAttackReachSqr(recruit));
        Vec3 forward = recruit.getLookAngle().normalize();
        if (forward.lengthSqr() == 0.0D) {
            return;
        }
        double halfAngleCos = Math.cos(Math.toRadians(profile.angleDegrees() * 0.5D));
        int hitCount = 0;
        for (LivingEntity candidate : recruit.level().getEntitiesOfClass(
                LivingEntity.class,
                recruit.getBoundingBox().inflate(reach),
                entity -> entity != recruit && entity != primaryTarget && entity.isAlive() && !entity.isRemoved())) {
            if (hitCount >= MAX_SECONDARY_TARGETS) {
                break;
            }
            if (!isSecondaryTarget(recruit, candidate, reach, forward, halfAngleCos)) {
                continue;
            }
            recruit.doHurtTarget(candidate, profile.damageMultiplier());
            hitCount++;
        }
    }

    private boolean isSecondaryTarget(AbstractRecruitEntity recruit, LivingEntity candidate, double reach, Vec3 forward, double halfAngleCos) {
        if (!recruit.shouldAttack(candidate) || !recruit.hasLineOfSight(candidate)) {
            return false;
        }
        Vec3 offset = candidate.position().subtract(recruit.position());
        double distance = offset.length();
        if (distance <= 0.0D || distance > reach) {
            return false;
        }
        return forward.dot(offset.normalize()) >= halfAngleCos;
    }

    private Optional<Object> selectAttack(Object attributes, boolean mounted, boolean offhandEmpty, boolean offhandShield) {
        Optional<Object> attacks = access.findMethod(WEAPON_ATTRIBUTES_CLASS, "attacks")
                .flatMap(method -> access.invoke(method, attributes));
        if (attacks.isEmpty()) {
            return Optional.empty();
        }
        Object attacksValue = attacks.get();
        if (attacksValue.getClass().isArray()) {
            Object firstAttack = null;
            int length = Array.getLength(attacksValue);
            for (int i = 0; i < length; i++) {
                Object attack = Array.get(attacksValue, i);
                if (attack != null && firstAttack == null) {
                    firstAttack = attack;
                }
                if (attack != null && conditionsMatch(attack, mounted, offhandEmpty, offhandShield)) {
                    return Optional.of(attack);
                }
            }
            // Better Combat condition sets differ between versions/packs; if no strict
            // condition match exists, still use the first declared attack so recruit
            // animation/cadence do not silently disappear.
            return Optional.ofNullable(firstAttack);
        }
        if (attacksValue instanceof Iterable<?> iterable) {
            Object firstAttack = null;
            for (Object attack : iterable) {
                if (attack != null && firstAttack == null) {
                    firstAttack = attack;
                }
                if (attack != null && conditionsMatch(attack, mounted, offhandEmpty, offhandShield)) {
                    return Optional.of(attack);
                }
            }
            return Optional.ofNullable(firstAttack);
        }
        return Optional.empty();
    }

    private boolean conditionsMatch(Object attack, boolean mounted, boolean offhandEmpty, boolean offhandShield) {
        Optional<Object> conditions = access.findMethod(ATTACK_CLASS, "conditions")
                .flatMap(method -> access.invoke(method, attack));
        if (conditions.isEmpty()) {
            return true;
        }
        Object conditionsValue = conditions.get();
        if (conditionsValue.getClass().isArray()) {
            int length = Array.getLength(conditionsValue);
            for (int i = 0; i < length; i++) {
                Object condition = Array.get(conditionsValue, i);
                String name = condition instanceof Enum<?> enumValue ? enumValue.name() : String.valueOf(condition);
                if (!conditionMatches(name, mounted, offhandEmpty, offhandShield)) {
                    return false;
                }
            }
            return true;
        }
        if (conditionsValue instanceof Iterable<?> iterable) {
            for (Object condition : iterable) {
                String name = condition instanceof Enum<?> enumValue ? enumValue.name() : String.valueOf(condition);
                if (!conditionMatches(name, mounted, offhandEmpty, offhandShield)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean conditionMatches(String name, boolean mounted, boolean offhandEmpty, boolean offhandShield) {
        return switch (name) {
            case "MOUNTED" -> mounted;
            case "NOT_MOUNTED" -> !mounted;
            case "NO_OFFHAND_ITEM" -> offhandEmpty;
            case "OFF_HAND_SHIELD" -> offhandShield;
            case "MAIN_HAND_ONLY", "NOT_DUAL_WIELDING" -> true;
            case "OFF_HAND_ONLY", "DUAL_WIELDING_ANY", "DUAL_WIELDING_SAME", "DUAL_WIELDING_SAME_CATEGORY" -> false;
            default -> true;
        };
    }

    private double readDouble(Object target, String methodName, double fallback) {
        return access.findMethod(target.getClass(), methodName)
                .flatMap(method -> access.invoke(method, target))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::doubleValue)
                .orElse(fallback);
    }

    private String readName(Object target, String methodName, String fallback) {
        return access.findMethod(target.getClass(), methodName)
                .flatMap(method -> access.invoke(method, target))
                .map(value -> value instanceof Enum<?> enumValue ? enumValue.name() : String.valueOf(value))
                .orElse(fallback);
    }

    private String readString(Object target, String methodName, String fallback) {
        return access.findMethod(target.getClass(), methodName)
                .flatMap(method -> access.invoke(method, target))
                .map(String::valueOf)
                .orElse(fallback);
    }

    private double normalizeAngle(double angle, String hitboxShape) {
        double base = angle > 0.0D ? angle : switch (hitboxShape) {
            case "HORIZONTAL_PLANE" -> 120.0D;
            case "VERTICAL_PLANE" -> 55.0D;
            default -> 35.0D;
        };
        return switch (hitboxShape) {
            case "HORIZONTAL_PLANE" -> Math.min(180.0D, base);
            case "VERTICAL_PLANE" -> Math.min(70.0D, base);
            default -> Math.min(60.0D, base);
        };
    }

    private int shapeId(String hitboxShape) {
        return switch (hitboxShape) {
            case "HORIZONTAL_PLANE" -> 2;
            case "VERTICAL_PLANE" -> 3;
            default -> 1;
        };
    }

    private double upswingMultiplier() {
        Optional<Object> config = betterCombatConfig();
        if (config.isEmpty()) {
            return 1.0D;
        }
        return access.findMethod(config.get().getClass(), "getUpswingMultiplier")
                .flatMap(method -> access.invoke(method, config.get()))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::doubleValue)
                .filter(value -> value > 0.0D)
                .orElse(1.0D);
    }

    private int attackIntervalCap() {
        Optional<Object> config = betterCombatConfig();
        if (config.isEmpty()) {
            return 0;
        }
        Optional<Field> field = access.findField(config.get().getClass(), "attack_interval_cap");
        return field.flatMap(value -> access.getInt(value, config.get()))
                .filter(value -> value > 0)
                .orElse(0);
    }

    private Optional<Object> betterCombatConfig() {
        return access.findField(BETTER_COMBAT_CLASS, "config")
                .flatMap(field -> {
                    try {
                        return Optional.ofNullable(field.get(null));
                    }
                    catch (IllegalAccessException | RuntimeException ignored) {
                        return Optional.empty();
                    }
                });
    }

    record AttackProfile(
            int cooldownTicks,
            int upswingTicks,
            double damageMultiplier,
            double angleDegrees,
            int shapeId,
            String animation) {
    }

    private record PendingAttack(int targetId, int ticksLeft, int presentationTicks, AttackProfile profile) {
    }
}
