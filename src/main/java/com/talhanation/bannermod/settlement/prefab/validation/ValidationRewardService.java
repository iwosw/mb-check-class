package com.talhanation.bannermod.settlement.prefab.validation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Grants player-visible rewards based on a {@link ValidationResult}. Currently emerald-only,
 * scaled by {@link ArchitectureTier}; the seam is kept small so future tiers can also grant
 * reputation, XP, or settlement-tier bumps without changing callers.
 */
public final class ValidationRewardService {
    private ValidationRewardService() {
    }

    public static int grantFor(ServerPlayer player, ValidationResult result) {
        if (player == null || result == null || !result.passed()) {
            return 0;
        }
        ArchitectureTier tier = result.architectureTier();
        int emeralds = switch (tier) {
            case HOVEL -> 0;
            case ACCEPTABLE -> 1;
            case GOOD -> 3;
            case GREAT -> 6;
            case MAJESTIC -> 12;
        };
        if (emeralds > 0) {
            ItemStack stack = new ItemStack(Items.EMERALD, emeralds);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        return emeralds;
    }
}
