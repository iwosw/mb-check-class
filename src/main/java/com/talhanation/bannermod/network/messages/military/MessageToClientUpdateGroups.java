package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class MessageToClientUpdateGroups implements BannerModMessage<MessageToClientUpdateGroups> {
    private CompoundTag nbt;
    public MessageToClientUpdateGroups() {

    }

    public MessageToClientUpdateGroups(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    public void executeClientSide(BannerModNetworkContext context) {
        List<RecruitsGroup> updated = RecruitsGroup.listFromNbt(this.nbt);
        boolean changed = false;

        Map<UUID, RecruitsGroup> updatedMap = new HashMap<>();
        for (RecruitsGroup group : updated) {
            updatedMap.put(group.getUUID(), group);
        }

        changed |= ClientManager.groups.removeIf(
                clientGroup -> !updatedMap.containsKey(clientGroup.getUUID())
        );
        
        for (RecruitsGroup updatedGroup : updated) {

            RecruitsGroup existing = null;

            for (RecruitsGroup clientGroup : ClientManager.groups) {
                if (clientGroup.getUUID().equals(updatedGroup.getUUID())) {
                    existing = clientGroup;
                    break;
                }
            }

            if (existing == null) {
                ClientManager.groups.add(updatedGroup);
                changed = true;
            } else {
                if (!java.util.Objects.equals(existing.leaderUUID, updatedGroup.leaderUUID)
                        || !java.util.Objects.equals(existing.members, updatedGroup.members)
                        || existing.getSize() != updatedGroup.getSize()
                        || !java.util.Objects.equals(existing.getName(), updatedGroup.getName())
                        || existing.getCount() != updatedGroup.getCount()) {
                    changed = true;
                }
                existing.leaderUUID = updatedGroup.leaderUUID;
                existing.members    = updatedGroup.members;
                existing.setSize(updatedGroup.getSize());
                existing.setName(updatedGroup.getName());
                existing.setCount(updatedGroup.getCount());
            }
        }
        if (changed) ClientManager.markGroupsChanged();
    }

    @Override
    public MessageToClientUpdateGroups fromBytes(FriendlyByteBuf buf) {
        this.nbt = buf.readNbt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

}
