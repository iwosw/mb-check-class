package com.talhanation.recruits.network;

import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MessageShields implements Message<MessageShields> {

    private UUID player;
    private UUID group;
    private boolean should;

    public MessageShields() {
    }

    public MessageShields(UUID player, UUID group, boolean shields) {
        this.player = player;
        this.group = group;
        this.should = shields;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        dispatchToServer(player, this.player, this.group, this.should);
    }

    public static void dispatchToServer(ServerPlayer player, UUID playerUuid, UUID group, boolean should) {
        dispatchToServer((Player) player, playerUuid, group, should);
    }

    public static void dispatchToServer(Player player, UUID playerUuid, UUID group, boolean should) {
        List<AbstractRecruitEntity> recruits = resolveTargets(player, playerUuid, group);
        if (recruits.isEmpty()) {
            return;
        }

        for (AbstractRecruitEntity recruit : recruits) {
            CommandEvents.onShieldsCommand(player, player.getUUID(), recruit, group, should);
        }
    }

    private static List<AbstractRecruitEntity> resolveTargets(Player player, UUID playerUuid, UUID group) {
        if (playerUuid == null || !player.getUUID().equals(playerUuid)) {
            Main.LOGGER.debug("Ignored shields command with mismatched sender UUID");
            return List.of();
        }

        List<AbstractRecruitEntity> nearby = player.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                player.getBoundingBox().inflate(CommandTargeting.GROUP_COMMAND_RADIUS)
        );
        CommandTargeting.GroupCommandSelection selection = CommandTargeting.forGroupCommand(
                player.getUUID(),
                player.getTeam() != null ? player.getTeam().getName() : null,
                group,
                nearby.stream().map(recruit -> new CommandTargeting.RecruitSnapshot(
                        recruit.getUUID(),
                        recruit.getOwnerUUID(),
                        recruit.getTeam() != null ? recruit.getTeam().getName() : null,
                        recruit.getGroup(),
                        recruit.isOwned(),
                        recruit.isAlive(),
                        recruit.getListen(),
                        recruit.distanceToSqr(player)
                )).toList()
        );

        if (!selection.isSuccess()) {
            Main.LOGGER.debug("Ignored shields command: {}", selection.failure());
            return List.of();
        }

        Set<UUID> targetIds = new HashSet<>();
        for (CommandTargeting.RecruitSnapshot recruit : selection.recruits()) {
            targetIds.add(recruit.recruitUuid());
        }

        nearby.removeIf(recruit -> !targetIds.contains(recruit.getUUID()));
        return nearby;
    }

    public MessageShields fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.group = buf.readUUID();
        this.should = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player);
        buf.writeUUID(this.group);
        buf.writeBoolean(this.should);
    }
}
