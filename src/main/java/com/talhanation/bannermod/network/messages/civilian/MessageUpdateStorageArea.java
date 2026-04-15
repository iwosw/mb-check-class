package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageUpdateStorageArea implements Message<MessageUpdateStorageArea> {

    public UUID uuid;
    public int mask;
    public String name;
    public MessageUpdateStorageArea() {

    }

    public MessageUpdateStorageArea(UUID uuid, int mask, String name) {
        this.uuid = uuid;
        this.mask = mask;
        this.name = name;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        Entity entity = player.serverLevel().getEntity(this.uuid);
        if (!(entity instanceof StorageArea storageArea)) {
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.AREA_NOT_FOUND);
            return;
        }

        WorkAreaAuthoringRules.Decision decision = WorkAreaAuthoringRules.modifyDecision(true, storageArea.getAuthoringAccess(player));
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            this.sendDecision(player, decision);
            return;
        }

        this.update(storageArea);

    }

    public void update(StorageArea storageArea){
        storageArea.setStorageTypes(mask);
        storageArea.setCustomName(Component.literal(name));
    }

    private void sendDecision(ServerPlayer player, WorkAreaAuthoringRules.Decision decision) {
        String messageKey = WorkAreaAuthoringRules.getMessageKey(decision);
        if (messageKey != null) {
            player.sendSystemMessage(Component.translatable(messageKey));
        }
    }

    public MessageUpdateStorageArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.mask = buf.readInt();
        this.name = buf.readUtf();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(mask);
        buf.writeUtf(name);
    }

}
