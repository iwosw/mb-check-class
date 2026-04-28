package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.MessengerScreen;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;


public class MessageToClientUpdateMessengerScreen implements BannerModMessage<MessageToClientUpdateMessengerScreen> {
    public String message;
    public CompoundTag nbt;
    public MessageToClientUpdateMessengerScreen() {
    }

    public MessageToClientUpdateMessengerScreen(String message, RecruitsPlayerInfo playerInfo) {
        this.message = message;

        if(playerInfo != null){
            this.nbt = playerInfo.toNBT();
        }
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        //MessengerScreen.message = this.message;

        if(nbt != null){
            MessengerScreen.playerInfo = RecruitsPlayerInfo.getFromNBT(nbt);
        }
    }

    @Override
    public MessageToClientUpdateMessengerScreen fromBytes(FriendlyByteBuf buf) {
        this.message = buf.readUtf();

        if(nbt != null){
            this.nbt = buf.readNbt();
        }

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(message);

        if(nbt != null){
            buf.writeNbt(nbt);
        }
    }

}