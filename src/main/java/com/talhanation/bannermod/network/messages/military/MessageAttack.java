package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentDispatcher;
import com.talhanation.bannermod.army.command.CommandIntentPriority;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
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

public class MessageAttack implements Message<MessageAttack> {

    private UUID playerUuid;
    private UUID group;

    public MessageAttack() {
    }

    public MessageAttack(UUID playerUuid, UUID group) {
        this.playerUuid = playerUuid;
        this.group = group;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = Objects.requireNonNull(context.getSender());
        dispatchToServer(serverPlayer, this.playerUuid, this.group);
    }

    public static void dispatchToServer(ServerPlayer serverPlayer, UUID playerUuid, UUID group) {
        dispatchToServer((Player) serverPlayer, playerUuid, group);
    }

    public static void dispatchToServer(Player player, UUID playerUuid, UUID group) {
        List<AbstractRecruitEntity> list = resolveTargets(player, playerUuid, group);
        if (list.isEmpty()) {
            return;
        }

        long gameTime = player.getCommandSenderWorld().getGameTime();
        CommandIntent intent = new CommandIntent.Attack(
                gameTime,
                CommandIntentPriority.NORMAL,
                false,
                group
        );
        ServerPlayer serverSender = player instanceof ServerPlayer sp ? sp : null;
        CommandIntentDispatcher.dispatch(serverSender, intent, list);
    }

    private static List<AbstractRecruitEntity> resolveTargets(Player player, UUID playerUuid, UUID group) {
        if (playerUuid == null || !player.getUUID().equals(playerUuid)) {
            BannerModMain.LOGGER.debug("Ignored attack command with mismatched sender UUID");
            return List.of();
        }

        List<AbstractRecruitEntity> nearby = player.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                player.getBoundingBox().inflate(CommandTargeting.GROUP_COMMAND_RADIUS)
        );
        CommandTargeting.GroupCommandSelection selection = CommandTargeting.forGroupCommand(
                player.getUUID(),
                player.getTeam() == null ? null : player.getTeam().getName(),
                group,
                nearby.stream().map(recruit -> new CommandTargeting.RecruitSnapshot(
                        recruit.getUUID(),
                        recruit.getOwnerUUID(),
                        recruit.getGroup(),
                        recruit.getTeam() == null ? null : recruit.getTeam().getName(),
                        recruit.isOwned(),
                        recruit.isAlive(),
                        recruit.getListen(),
                        recruit.distanceToSqr(player)
                )).toList()
        );

        if (!selection.isSuccess()) {
            BannerModMain.LOGGER.debug("Ignored attack command: {}", selection.failure());
            return List.of();
        }

        Set<UUID> targetIds = new HashSet<>();
        for (CommandTargeting.RecruitSnapshot recruit : selection.recruits()) {
            targetIds.add(recruit.recruitUuid());
        }

        nearby.removeIf(recruit -> !targetIds.contains(recruit.getUUID()));
        return nearby;
    }

    public MessageAttack fromBytes(FriendlyByteBuf buf) {
        this.playerUuid = buf.readUUID();
        this.group = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerUuid);
        buf.writeUUID(this.group);
    }
}
