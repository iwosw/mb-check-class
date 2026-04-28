package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.events.RecruitsToastManager;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import static com.talhanation.bannermod.client.military.events.RecruitsToastManager.*;


public class MessageToClientSetToast implements BannerModMessage<MessageToClientSetToast> {

    private int x;
    private String s;

    public MessageToClientSetToast() {
    }

    public MessageToClientSetToast(int x, String s) {
        this.x = x;
        this.s = s;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    public void executeClientSide(BannerModNetworkContext context) {
        switch (x){
            case 0 -> RecruitsToastManager.setToastForPlayer(Images.LETTER, TOAST_RECRUIT_ASSIGNED_TITLE, TOAST_RECRUIT_ASSIGNED_INFO(s));
            case 1 -> RecruitsToastManager.setToastForPlayer(Images.LETTER, TOAST_MESSENGER_ARRIVED_TITLE, TOAST_MESSENGER_ARRIVED_INFO(s));
            case 2 -> RecruitsToastManager.setToastForPlayer(Images.LETTER, TOAST_GROUP_ASSIGNED_TITLE, TOAST_GROUP_ASSIGNED_INFO(s));
        }
    }

    @Override
    public MessageToClientSetToast fromBytes(FriendlyByteBuf buf) {
       this.x = buf.readInt();
       this.s = buf.readUtf();
       return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(x);
        buf.writeUtf(s);
    }
}