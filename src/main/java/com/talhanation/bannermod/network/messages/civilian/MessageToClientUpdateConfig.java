package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.client.civilian.WorkersClientManager;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class MessageToClientUpdateConfig implements BannerModMessage<MessageToClientUpdateConfig> {
    private boolean allowWorkAreaOnlyInFactionClaim;
    public MessageToClientUpdateConfig() {
    }

    public MessageToClientUpdateConfig(boolean allowWorkAreaOnlyInFactionClaim) {
        this.allowWorkAreaOnlyInFactionClaim = allowWorkAreaOnlyInFactionClaim;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(NetworkEvent.Context context) {
        WorkersClientManager.configValueWorkAreaOnlyInFactionClaim = this.allowWorkAreaOnlyInFactionClaim;
    }

    @Override
    public MessageToClientUpdateConfig fromBytes(FriendlyByteBuf buf) {
        this.allowWorkAreaOnlyInFactionClaim = buf.readBoolean();

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.allowWorkAreaOnlyInFactionClaim);
    }

}
