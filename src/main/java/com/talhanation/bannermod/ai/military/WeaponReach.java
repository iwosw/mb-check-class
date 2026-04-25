package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.compat.BetterCombatWeaponAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Stage 3.A: per-item reach tag.
 *
 * <p>Returns the "extra reach" (in blocks, beyond the baseline sword reach of 5)
 * granted by the weapon currently held. This is a pure function that inspects
 * the item's registry id as a string — BannerMod does not currently ship any
 * dedicated {@code SpearItem} / {@code PikeItem} classes, so we fall back to
 * string-id heuristics. When such classes appear later, this helper is the one
 * place to extend with {@code instanceof} checks.
 *
 * <p>Kept framework-free (no Minecraft tick access) so unit tests only need the
 * Forge registry stubs to resolve an {@link Item}'s id. Callers that already
 * hold a registry key can use {@link #effectiveReachForId(String)} directly —
 * that overload is the one the unit test exercises.
 */
public final class WeaponReach {

    /** Extra reach, in blocks, for a spear-tagged weapon. */
    public static final double SPEAR_EXTRA_REACH = 1.0D;
    /** Extra reach, in blocks, for a pike/halberd-tagged weapon. */
    public static final double PIKE_EXTRA_REACH = 2.0D;
    /** Extra reach, in blocks, for a long-spear / sarissa-tagged weapon. */
    public static final double SARISSA_EXTRA_REACH = 2.5D;

    private WeaponReach() {
    }

    /**
     * Returns the extra reach (blocks beyond the 5-block sword baseline) granted
     * by holding the given item. Null / empty / unknown items return 0.0.
     */
    public static double effectiveReachFor(Item item) {
        if (item == null) {
            return 0.0D;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            // Unregistered items fall back to the description-id heuristic so
            // tests / mods with quirky registration still pick up the tag.
            return effectiveReachForId(item.getDescriptionId());
        }
        return effectiveReachForId(key.toString());
    }

    public static double effectiveReachFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        double idReach = effectiveReachFor(stack.getItem());
        double betterCombatReach = BetterCombatWeaponAttributes.extraReach(stack);
        return Math.max(idReach, betterCombatReach);
    }

    /**
     * Pure-string overload used for unit tests and for callers that already have
     * the item's registry id (e.g. {@code "recruits:iron_spear"}) or description
     * id (e.g. {@code "item.recruits.iron_spear"}).
     *
     * <p>Matching is case-insensitive and looks for substrings so variants such
     * as {@code long_spear} still resolve correctly. Precedence: sarissa → pike →
     * spear → plain.
     */
    public static double effectiveReachForId(String id) {
        if (id == null || id.isEmpty()) {
            return 0.0D;
        }
        String lower = id.toLowerCase(java.util.Locale.ROOT);
        // Longest-reach tags first so "sarissa" doesn't get swallowed by a later
        // substring test and "long_spear" / "longspear" promote to sarissa reach.
        if (lower.contains("sarissa") || lower.contains("long_spear") || lower.contains("longspear")) {
            return SARISSA_EXTRA_REACH;
        }
        if (lower.contains("pike") || lower.contains("halberd") || lower.contains("polearm")
                || lower.contains("lance") || lower.contains("glaive") || lower.contains("partisan")
                || lower.contains("billhook") || lower.contains("bardiche") || lower.contains("voulge")) {
            return PIKE_EXTRA_REACH;
        }
        if (lower.contains("spear")) {
            return SPEAR_EXTRA_REACH;
        }
        return 0.0D;
    }
}
