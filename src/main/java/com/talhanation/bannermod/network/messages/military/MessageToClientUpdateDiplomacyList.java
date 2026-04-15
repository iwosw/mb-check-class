package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;


public class MessageToClientUpdateDiplomacyList implements Message<MessageToClientUpdateDiplomacyList> {
    private CompoundTag diplomacyNbt;
    public MessageToClientUpdateDiplomacyList() {
    }

    public MessageToClientUpdateDiplomacyList(Map<String, Map<String, RecruitsDiplomacyManager.DiplomacyStatus>> diplomacyStatusMap) {
        this.diplomacyNbt = RecruitsDiplomacyManager.mapToNbt(diplomacyStatusMap);
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        ClientManager.diplomacyMap = RecruitsDiplomacyManager.mapFromNbt(diplomacyNbt);
    }

    @Override
    public MessageToClientUpdateDiplomacyList fromBytes(FriendlyByteBuf buf) {
        this.diplomacyNbt = buf.readNbt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(this.diplomacyNbt);
    }

}