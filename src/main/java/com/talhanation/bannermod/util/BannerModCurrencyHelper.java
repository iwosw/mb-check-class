package com.talhanation.bannermod.util;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public final class BannerModCurrencyHelper {
    private BannerModCurrencyHelper() {
    }

    public static Item currencyItem() {
        String currencyId = RecruitsServerConfig.RecruitCurrency.get();
        Optional<? extends Holder<Item>> holder = RegistryLookup.itemHolder(ResourceLocation.tryParse(currencyId));
        return holder.map(Holder::value).orElse(Items.EMERALD);
    }

    public static ItemStack currencyStack(int count) {
        ItemStack stack = currencyItem().getDefaultInstance();
        stack.setCount(Math.max(1, count));
        return stack;
    }

    public static int countCurrency(Player player) {
        if (player == null) {
            return 0;
        }
        Item currency = currencyItem();
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(currency)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static boolean canAfford(Player player, int amount) {
        if (player == null) {
            return false;
        }
        if (amount <= 0) {
            return true;
        }
        return isCreativeAdmin(player) || countCurrency(player) >= amount;
    }

    public static boolean removeCurrency(Player player, int amount) {
        if (player == null) {
            return false;
        }
        if (amount <= 0 || isCreativeAdmin(player)) {
            return true;
        }
        if (!canAfford(player, amount)) {
            return false;
        }
        Item currency = currencyItem();
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(currency)) {
                continue;
            }
            int removed = Math.min(stack.getCount(), remaining);
            stack.shrink(removed);
            remaining -= removed;
        }
        return remaining == 0;
    }

    public static boolean isCreativeAdmin(Player player) {
        return player != null && player.isCreative() && player.hasPermissions(2);
    }
}
