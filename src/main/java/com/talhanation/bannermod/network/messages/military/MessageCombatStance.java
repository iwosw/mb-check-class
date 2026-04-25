package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.ai.military.CombatStance;
import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentDispatcher;
import com.talhanation.bannermod.army.command.CommandIntentPriority;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
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

public class MessageCombatStance implements Message<MessageCombatStance> {

    private UUID playerUuid;
    private UUID group;
    private CombatStance stance;

    public MessageCombatStance() {
    }

    public MessageCombatStance(UUID playerUuid, UUID group, CombatStance stance) {
        this.playerUuid = playerUuid;
        this.group = group;
        this.stance = stance;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer sender = Objects.requireNonNull(context.getSender());
        dispatchToServer(sender, this.playerUuid, this.group, this.stance);
    }

    public static void dispatchToServer(Player sender, UUID playerUuid, UUID group, CombatStance stance) {
        if (stance == null) {
            return;
        }
        List<AbstractRecruitEntity> recruits = resolveTargets(sender, playerUuid, group);
        if (recruits.isEmpty()) {
            return;
        }

        long gameTime = sender.getCommandSenderWorld().getGameTime();
        CommandIntent intent = new CommandIntent.CombatStanceChange(
                gameTime,
                CommandIntentPriority.NORMAL,
                false,
                stance,
                group
        );
        if (sender instanceof ServerPlayer serverSender) {
            CommandIntentDispatcher.dispatch(serverSender, intent, recruits);
            return;
        }

        // GameTests and other non-packet callers can still use the static helper even
        // when they do not have a concrete ServerPlayer instance.
        for (AbstractRecruitEntity recruit : recruits) {
            CommandEvents.onCombatStanceCommand(sender.getUUID(), recruit, stance, group);
        }
    }

    private static List<AbstractRecruitEntity> resolveTargets(Player sender, UUID playerUuid, UUID group) {
        if (playerUuid == null || !sender.getUUID().equals(playerUuid)) {
            BannerModMain.LOGGER.debug("Ignored combat-stance command with mismatched sender UUID");
            return List.of();
        }

        List<AbstractRecruitEntity> nearby = RecruitIndex.instance().groupInRange(
                sender.getCommandSenderWorld(),
                group,
                sender.position(),
                CommandTargeting.GROUP_COMMAND_RADIUS
        );
        if (nearby == null) {
            RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
            nearby = sender.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    sender.getBoundingBox().inflate(CommandTargeting.GROUP_COMMAND_RADIUS)
            );
        }
        CommandTargeting.GroupCommandSelection selection = CommandTargeting.forGroupCommand(
                sender.getUUID(),
                sender.getTeam() == null ? null : sender.getTeam().getName(),
                group,
                nearby.stream().map(recruit -> new CommandTargeting.RecruitSnapshot(
                        recruit.getUUID(),
                        recruit.getOwnerUUID(),
                        recruit.getGroup(),
                        recruit.getTeam() == null ? null : recruit.getTeam().getName(),
                        recruit.isOwned(),
                        recruit.isAlive(),
                        recruit.getListen(),
                        recruit.distanceToSqr(sender)
                )).toList()
        );

        if (!selection.isSuccess()) {
            BannerModMain.LOGGER.debug("Ignored combat-stance command: {}", selection.failure());
            return List.of();
        }

        Set<UUID> targetIds = new HashSet<>();
        for (CommandTargeting.RecruitSnapshot recruit : selection.recruits()) {
            targetIds.add(recruit.recruitUuid());
        }

        nearby.removeIf(recruit -> !targetIds.contains(recruit.getUUID()));
        return nearby;
    }

    public MessageCombatStance fromBytes(FriendlyByteBuf buf) {
        this.playerUuid = buf.readUUID();
        this.group = buf.readUUID();
        this.stance = CombatStance.fromName(buf.readUtf(32));
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerUuid);
        buf.writeUUID(this.group);
        buf.writeUtf(this.stance == null ? CombatStance.LOOSE.name() : this.stance.name());
    }
}
