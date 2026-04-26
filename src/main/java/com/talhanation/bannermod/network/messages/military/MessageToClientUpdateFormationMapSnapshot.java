package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.map.FormationMapContact;
import com.talhanation.bannermod.client.military.ClientManager;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

public class MessageToClientUpdateFormationMapSnapshot implements Message<MessageToClientUpdateFormationMapSnapshot> {
    private CompoundTag contacts;

    public MessageToClientUpdateFormationMapSnapshot() {
    }

    public MessageToClientUpdateFormationMapSnapshot(List<FormationMapContact> contacts) {
        this.contacts = FormationMapContact.listToNbt(contacts);
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(NetworkEvent.Context context) {
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
