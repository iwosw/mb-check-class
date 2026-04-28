package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class MessageFormationFollowMovement implements Message<MessageFormationFollowMovement> {

    private UUID player_uuid;

    private UUID group;
    private int formation;

    public MessageFormationFollowMovement(){
    }

    public MessageFormationFollowMovement(UUID player_uuid, UUID group, int formation) {
        this.player_uuid = player_uuid;
        this.group  = group;
        this.formation = formation;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        dispatchToServer(Objects.requireNonNull(context.getSender()), this.player_uuid, this.group, this.formation);
    }

    public static void dispatchToServer(Player sender, UUID playerUuid, UUID group, int formation) {
        List<AbstractRecruitEntity> list = resolveTargets(sender, playerUuid, group);
        if (list.isEmpty()) {
            return;
        }
        CommandEvents.applyFormation(formation, list, sender, sender.position());
    }

    private static List<AbstractRecruitEntity> resolveTargets(Player sender, UUID playerUuid, UUID group) {
        return RecruitCommandTargetResolver.resolveGroupTargets(sender, playerUuid, group, "formation");
    }

    public MessageFormationFollowMovement fromBytes(FriendlyByteBuf buf) {
        this.player_uuid = buf.readUUID();
        this.group = buf.readUUID();
        this.formation = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player_uuid);
        buf.writeUUID(this.group);
        buf.writeInt(this.formation);
    }

}
