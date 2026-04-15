package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;


public class MessageUpdateOwner implements Message<MessageUpdateOwner> {

    public UUID uuid;
    public UUID playerUUID;
    public String playerName;
    public MessageUpdateOwner() {

    }

    public MessageUpdateOwner(UUID uuid, RecruitsPlayerInfo playerInfo) {
        this.uuid = uuid;
        this.playerUUID = playerInfo.getUUID();
        this.playerName = playerInfo.getName();
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        Entity entity = player.serverLevel().getEntity(this.uuid);
        if (!(entity instanceof AbstractWorkAreaEntity workArea)) {
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.AREA_NOT_FOUND);
            return;
        }

        WorkAreaAuthoringRules.Decision decision = WorkAreaAuthoringRules.modifyDecision(true, workArea.getAuthoringAccess(player));
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            this.sendDecision(player, decision);
            return;
        }

        this.updateWorkArea(workArea);

    }

    public void updateWorkArea(AbstractWorkAreaEntity workArea){
        workArea.setPlayerUUID(this.playerUUID);
        workArea.setPlayerName(this.playerName);
        workArea.setTeamStringID("");

        Player player = workArea.level().getPlayerByUUID(playerUUID);

        if(player == null || player.getTeam() == null) return;

        workArea.setTeamStringID(player.getTeam().getName());
    }

    private void sendDecision(ServerPlayer player, WorkAreaAuthoringRules.Decision decision) {
        String messageKey = WorkAreaAuthoringRules.getMessageKey(decision);
        if (messageKey != null) {
            player.sendSystemMessage(Component.translatable(messageKey));
        }
    }

    public MessageUpdateOwner fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.playerUUID = buf.readUUID();
        this.playerName = buf.readUtf();
        return this;
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(playerUUID);
        buf.writeUtf(playerName);
    }
}
