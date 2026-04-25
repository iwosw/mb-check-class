package com.talhanation.bannermod.compat;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Reflection-only Better Combat weapon metadata bridge.
 *
 * <p>This deliberately reads reach metadata only. Attack execution, animations,
 * windup, and target selection remain BannerMod/server-authoritative.
 */
public final class BetterCombatWeaponAttributes {

    private static final String WEAPON_REGISTRY_CLASS = "net.bettercombat.logic.WeaponRegistry";
    private static final String WEAPON_ATTRIBUTES_CLASS = "net.bettercombat.api.WeaponAttributes";
    private static final double BASELINE_REACH = 5.0D;
    private static final BetterCombatWeaponAttributes DEFAULT = new BetterCombatWeaponAttributes(
            new ReflectiveCompatAccess());

    private final ReflectiveCompatAccess access;
    private Optional<Lookup> cachedLookup;

    BetterCombatWeaponAttributes(ReflectiveCompatAccess access) {
        this.access = access;
    }

    public static OptionalDouble attackRange(ItemStack stack) {
        return DEFAULT.resolveAttackRange(stack);
    }

    public static double extraReach(ItemStack stack) {
        OptionalDouble attackRange = attackRange(stack);
        return attackRange.isPresent() ? Math.max(0.0D, attackRange.getAsDouble() - BASELINE_REACH) : 0.0D;
    }

    OptionalDouble resolveAttackRange(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return OptionalDouble.empty();
        }
        return resolveLoadedAttackRange(stack);
    }

    OptionalDouble resolveLoadedAttackRange(ItemStack stack) {
        Optional<Lookup> lookup = lookup();
        if (lookup.isEmpty()) {
            return OptionalDouble.empty();
        }
        Optional<Object> attributes = access.invoke(lookup.get().getAttributesMethod(), null, stack);
        if (attributes.isEmpty()) {
            return OptionalDouble.empty();
        }
        return access.invoke(lookup.get().attackRangeMethod(), attributes.get())
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .filter(value -> value.doubleValue() > 0.0D)
                .map(value -> OptionalDouble.of(value.doubleValue()))
                .orElseGet(OptionalDouble::empty);
    }

    private Optional<Lookup> lookup() {
        if (cachedLookup != null) {
            return cachedLookup;
        }
        Optional<Method> getAttributes = access.findMethod(WEAPON_REGISTRY_CLASS, "getAttributes", ItemStack.class);
        Optional<Method> attackRange = access.findMethod(WEAPON_ATTRIBUTES_CLASS, "attackRange");
        if (getAttributes.isEmpty() || attackRange.isEmpty()) {
            cachedLookup = Optional.empty();
        }
        else {
            cachedLookup = Optional.of(new Lookup(getAttributes.get(), attackRange.get()));
        }
        return cachedLookup;
    }

    private record Lookup(Method getAttributesMethod, Method attackRangeMethod) {
    }
}
