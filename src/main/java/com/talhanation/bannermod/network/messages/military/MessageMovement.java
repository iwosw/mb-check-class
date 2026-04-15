package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class MessageMovement implements Message<MessageMovement> {

    private UUID player_uuid;
    private int state;
    private UUID group;
    private int formation;
    private boolean tight;

    public MessageMovement(){
    }

    public MessageMovement(UUID player_uuid, int state, UUID group, int formation, boolean tight) {
        this.player_uuid = player_uuid;
        this.state  = state;
        this.group  = group;
        this.formation = formation;
        this.tight = tight;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        dispatchToServer(Objects.requireNonNull(context.getSender()), this.player_uuid, this.group, this.state, this.formation, this.tight);
    }

    public static void dispatchToServer(Player sender, UUID playerUuid, UUID group, int state, int formation, boolean tight) {
        List<AbstractRecruitEntity> list = resolveTargets(sender, playerUuid, group);
        if (list.isEmpty()) {
            return;
        }
        CommandEvents.onMovementCommand(sender, list, state, formation, tight);
    }

    private static List<AbstractRecruitEntity> resolveTargets(Player sender, UUID playerUuid, UUID group) {
        if (playerUuid == null || !sender.getUUID().equals(playerUuid)) {
            BannerModMain.LOGGER.debug("Ignored movement command with mismatched sender UUID");
            return List.of();
        }

        List<AbstractRecruitEntity> nearby = sender.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                sender.getBoundingBox().inflate(CommandTargeting.GROUP_COMMAND_RADIUS)
        );
        CommandTargeting.GroupCommandSelection selection = CommandTargeting.forGroupCommand(
                sender.getUUID(),
                group,
                nearby.stream().map(recruit -> new CommandTargeting.RecruitSnapshot(
                        recruit.getUUID(),
                        recruit.getOwnerUUID(),
                        recruit.getGroup(),
                        recruit.isOwned(),
                        recruit.isAlive(),
                        recruit.getListen(),
                        recruit.distanceToSqr(sender)
                )).toList()
        );

        if (!selection.isSuccess()) {
            BannerModMain.LOGGER.debug("Ignored movement command: {}", selection.failure());
            return List.of();
        }

        Set<UUID> targetIds = new HashSet<>();
        for (CommandTargeting.RecruitSnapshot recruit : selection.recruits()) {
            targetIds.add(recruit.recruitUuid());
        }

        nearby.removeIf(recruit -> !targetIds.contains(recruit.getUUID()));
        return nearby;
    }

    public MessageMovement fromBytes(FriendlyByteBuf buf) {
        this.player_uuid = buf.readUUID();
        this.state = buf.readInt();
        this.group = buf.readUUID();
        this.formation = buf.readInt();
        this.tight = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player_uuid);
        buf.writeInt(this.state);
        buf.writeUUID(this.group);
        buf.writeInt(this.formation);
        buf.writeBoolean(this.tight);
    }

}
