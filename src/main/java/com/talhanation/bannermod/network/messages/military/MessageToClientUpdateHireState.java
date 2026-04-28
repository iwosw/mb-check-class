package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.RecruitHireScreen;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;


public class MessageToClientUpdateHireState implements BannerModMessage<MessageToClientUpdateHireState> {
    public ItemStack currency;
    public boolean canHire;
    public MessageToClientUpdateHireState() {
    }

    public MessageToClientUpdateHireState(boolean canHire) {
        this.canHire = canHire;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        ClientManager.canPlayerHire = this.canHire;
    }

    @Override
    public MessageToClientUpdateHireState fromBytes(FriendlyByteBuf buf) {
        this.canHire = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(canHire);
    }

}