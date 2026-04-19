package com.talhanation.bannermod.events;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.BowmanEntity;
import com.talhanation.bannermod.entity.military.CrossBowmanEntity;
import com.talhanation.bannermod.network.messages.military.MessageAddRecruitToTeam;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.UUID;

final class RecruitCommandActionService {

    private RecruitCommandActionService() {
    }

    static boolean handleRecruiting(Player player, RecruitsGroup group, AbstractRecruitEntity recruit, boolean message) {
        String name = recruit.getName().getString() + ": ";
        int sollPrice = recruit.getCost();
        Inventory playerInv = player.getInventory();
        int playerEmeralds = 0;

        String str = RecruitsServerConfig.RecruitCurrency.get();
        Optional<Holder<Item>> holder = ForgeRegistries.ITEMS.getHolder(ResourceLocation.tryParse(str));

        ItemStack currencyItemStack = holder.map(itemHolder -> itemHolder.value().getDefaultInstance()).orElseGet(Items.EMERALD::getDefaultInstance);

        Item currency = currencyItemStack.getItem();

        for (int i = 0; i < playerInv.getContainerSize(); i++) {
            ItemStack itemStackInSlot = playerInv.getItem(i);
            Item itemInSlot = itemStackInSlot.getItem();
            if (itemInSlot.equals(currency)) {
                playerEmeralds = playerEmeralds + itemStackInSlot.getCount();
            }
        }

        boolean playerCanPay = playerEmeralds >= sollPrice;

        if (playerCanPay || player.isCreative()) {
            if (recruit.hire(player, group, message)) {
                playerEmeralds = playerEmeralds - sollPrice;

                for (int i = 0; i < playerInv.getContainerSize(); i++) {
                    ItemStack itemStackInSlot = playerInv.getItem(i);
                    Item itemInSlot = itemStackInSlot.getItem();
                    if (itemInSlot.equals(currency)) {
                        playerInv.removeItemNoUpdate(i);
                    }
                }

                ItemStack emeraldsLeft = currencyItemStack.copy();
                emeraldsLeft.setCount(playerEmeralds);
                playerInv.add(emeraldsLeft);

                if (player.getTeam() != null) {
                    if (player.getCommandSenderWorld().isClientSide) {
                        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAddRecruitToTeam(player.getTeam().getName(), 1));
                    } else {
                        ServerPlayer serverPlayer = (ServerPlayer) player;
                        FactionEvents.addNPCToData(serverPlayer.serverLevel(), player.getTeam().getName(), 1);
                    }
                }

                return true;
            }
        } else {
            player.sendSystemMessage(textHireCosts(name, sollPrice, currency));
        }

        return false;
    }

    static void onMountButton(UUID playerUuid, AbstractRecruitEntity recruit, UUID mountUuid, UUID group) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            if (mountUuid != null) {
                recruit.shouldMount(true, mountUuid);
            } else if (recruit.getMountUUID() != null) {
                recruit.shouldMount(true, recruit.getMountUUID());
            }
            recruit.dismount = 0;
        }
    }

    static void onDismountButton(UUID playerUuid, AbstractRecruitEntity recruit, UUID group) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            recruit.shouldMount(false, null);
            if (recruit.isPassenger()) {
                recruit.stopRiding();
                recruit.dismount = 180;
            }
        }
    }

    static void onProtectButton(UUID playerUuid, AbstractRecruitEntity recruit, UUID protectUuid, UUID group) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            recruit.shouldProtect(true, protectUuid);
        }
    }

    static void onClearTargetButton(UUID playerUuid, AbstractRecruitEntity recruit, UUID group) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            recruit.setTarget(null);
            recruit.setLastHurtByPlayer(null);
            recruit.setLastHurtMob(null);
            recruit.setLastHurtByMob(null);
        }
    }

    static void onClearUpkeepButton(UUID playerUuid, AbstractRecruitEntity recruit, UUID group) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            recruit.clearUpkeepEntity();
            recruit.clearUpkeepPos();
        }
    }

    static void onUpkeepCommand(UUID playerUuid, AbstractRecruitEntity recruit, UUID group, boolean isEntity, UUID entityUuid, BlockPos blockPos) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            if (isEntity) {
                recruit.setUpkeepUUID(Optional.of(entityUuid));
                recruit.clearUpkeepPos();
            } else {
                recruit.setUpkeepPos(blockPos);
                recruit.clearUpkeepEntity();
            }
            recruit.forcedUpkeep = true;
            recruit.setUpkeepTimer(0);
            onClearTargetButton(playerUuid, recruit, group);
        }
    }

    static void onShieldsCommand(Player player, UUID playerUuid, AbstractRecruitEntity recruit, UUID group, boolean shields) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            recruit.setShouldBlock(shields);
        }
    }

    static void onRangedFireCommand(ServerPlayer serverPlayer, UUID playerUuid, AbstractRecruitEntity recruit, UUID group, boolean should) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            recruit.setShouldRanged(should);

            if (should) {
                if (recruit instanceof CrossBowmanEntity) {
                    recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof CrossbowItem);
                }
                if (recruit instanceof BowmanEntity) {
                    recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof BowItem);
                }
            } else {
                recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof SwordItem);
            }
        }
    }

    static void onRestCommand(ServerPlayer serverPlayer, UUID playerUuid, AbstractRecruitEntity recruit, UUID group, boolean should) {
        if (recruit.isEffectedByCommand(playerUuid, group)) {
            onClearTargetButton(playerUuid, recruit, group);
            recruit.setShouldRest(should);
        }
    }

    private static MutableComponent textHireCosts(String name, int sollPrice, Item item) {
        return Component.translatable("chat.recruits.text.hire_costs", name, String.valueOf(sollPrice), item.getDescription().getString());
    }
}
