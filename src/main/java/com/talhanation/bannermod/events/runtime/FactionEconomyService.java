package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public final class FactionEconomyService {
    private FactionEconomyService() {
    }

    public static ItemStack getCurrency() {
        String str = RecruitsServerConfig.RecruitCurrency.get();
        Optional<net.minecraft.core.Holder<Item>> holder = ForgeRegistries.ITEMS.getHolder(ResourceLocation.tryParse(str));
        return holder.map(itemHolder -> itemHolder.value().getDefaultInstance()).orElseGet(Items.EMERALD::getDefaultInstance);
    }

    public static boolean playerHasEnoughEmeralds(ServerPlayer player, int price) {
        return playerGetEmeraldsInInventory(player, getCurrency().getItem()) >= price || player.isCreative();
    }

    public static void doPayment(Player player, int costs) {
        Inventory playerInv = player.getInventory();
        ItemStack currencyItemStack = getCurrency();
        int playerEmeralds = playerGetEmeraldsInInventory(player, currencyItemStack.getItem()) - costs;
        for (int i = 0; i < playerInv.getContainerSize(); i++) {
            ItemStack itemStackInSlot = playerInv.getItem(i);
            if (itemStackInSlot.getItem() == currencyItemStack.getItem()) {
                playerInv.removeItemNoUpdate(i);
            }
        }
        ItemStack emeraldsLeft = getCurrency();
        emeraldsLeft.setCount(playerEmeralds);
        playerInv.add(emeraldsLeft);
    }

    public static int playerGetEmeraldsInInventory(Player player, Item currency) {
        int emeralds = 0;
        Inventory playerInv = player.getInventory();
        for (int i = 0; i < playerInv.getContainerSize(); i++) {
            ItemStack itemStackInSlot = playerInv.getItem(i);
            if (itemStackInSlot.getItem() == currency) emeralds += itemStackInSlot.getCount();
        }
        return emeralds;
    }
}
