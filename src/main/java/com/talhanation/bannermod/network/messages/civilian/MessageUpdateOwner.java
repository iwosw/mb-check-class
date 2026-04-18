package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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

        AbstractWorkAreaEntity workArea = WorkAreaMessageSupport.resolveAuthorizedWorkArea(player, this.uuid, AbstractWorkAreaEntity.class);
        if (workArea == null) {
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
