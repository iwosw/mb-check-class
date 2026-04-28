package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.map.FormationMapContact;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.List;

public class MessageToClientUpdateFormationMapSnapshot implements BannerModMessage<MessageToClientUpdateFormationMapSnapshot> {
    private CompoundTag contacts;

    public MessageToClientUpdateFormationMapSnapshot() {
    }

    public MessageToClientUpdateFormationMapSnapshot(List<FormationMapContact> contacts) {
        this.contacts = FormationMapContact.listToNbt(contacts);
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(BannerModNetworkContext context) {
        ClientManager.formationMapContacts = FormationMapContact.listFromNbt(this.contacts);
        ClientManager.markFormationMapContactsChanged();
    }

    @Override
    public MessageToClientUpdateFormationMapSnapshot fromBytes(FriendlyByteBuf buf) {
        this.contacts = buf.readNbt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(this.contacts);
    }
}
