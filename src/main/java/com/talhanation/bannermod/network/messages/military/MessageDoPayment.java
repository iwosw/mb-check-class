package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.util.RegistryLookup;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;


public class MessageDoPayment implements BannerModMessage<MessageDoPayment> {

    private int amount;
    private UUID uuid;
    public MessageDoPayment(){

    }

    public MessageDoPayment(UUID uuid, int amount) {
        this.amount = amount;
        this.uuid = uuid;
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(BannerModNetworkContext context){
        ServerPlayer serverPlayer = context.getSender();
        if(serverPlayer == null) return;

        if(!serverPlayer.getUUID().equals(uuid)) return;

        if(serverPlayer.isCreative() && serverPlayer.hasPermissions(2)){
            return;
        }

        removeCurrency(serverPlayer, this.amount);
    }

    private static void removeCurrency(ServerPlayer player, int amount) {
        Item currency = getRecruitCurrency();
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(currency)) continue;
            int removed = Math.min(stack.getCount(), remaining);
            stack.shrink(removed);
            remaining -= removed;
        }
    }

    private static Item getRecruitCurrency() {
        String currencyId = RecruitsServerConfig.RecruitCurrency.get();
        return RegistryLookup.itemHolder(ResourceLocation.tryParse(currencyId))
                .map(Holder::value)
                .orElse(Items.EMERALD);
    }
    public MessageDoPayment fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.amount = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(amount);
    }
}
