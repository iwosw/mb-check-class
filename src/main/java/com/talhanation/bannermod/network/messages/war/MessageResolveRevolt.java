package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.compat.BannerModPacketDistributor;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.runtime.RevoltInteractionService;
import com.talhanation.bannermod.war.runtime.RevoltState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class MessageResolveRevolt implements BannerModMessage<MessageResolveRevolt> {
    private UUID revoltId;
    private byte outcomeOrdinal;

    public MessageResolveRevolt() {
    }

    public MessageResolveRevolt(UUID revoltId, RevoltState outcome) {
        this.revoltId = revoltId;
        this.outcomeOrdinal = (byte) (outcome == null ? RevoltState.FAILED.ordinal() : outcome.ordinal());
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null || revoltId == null) {
            return;
        }
        ServerLevel level = player.serverLevel().getServer().overworld();
        RevoltState outcome = decodeOutcome(outcomeOrdinal);
        RevoltInteractionService.Result result = RevoltInteractionService.resolve(
                WarRuntimeContext.revolts(level),
                WarRuntimeContext.occupations(level),
                WarRuntimeContext.applierFor(level),
                revoltId,
                outcome,
                player.hasPermissions(2),
                level.getGameTime());
        if (!result.allowed()) {
            player.sendSystemMessage(Component.translatable("chat.bannermod.revolt.denied." + result.reason()));
            return;
        }
        player.sendSystemMessage(Component.translatable("chat.bannermod.revolt.resolved." + result.outcome().name().toLowerCase(java.util.Locale.ROOT)));
        sendSnapshot(player, level);
    }

    private static RevoltState decodeOutcome(byte ordinal) {
        RevoltState[] values = RevoltState.values();
        if (ordinal < 0 || ordinal >= values.length || values[ordinal] == RevoltState.PENDING) {
            return RevoltState.FAILED;
        }
        return values[ordinal];
    }

    private static void sendSnapshot(ServerPlayer player, ServerLevel level) {
        CompoundTag payload = WarClientState.encode(
                WarRuntimeContext.registry(level).all(),
                WarRuntimeContext.declarations(level).all(),
                WarRuntimeContext.sieges(level).all(),
                resolveSchedule(),
                WarRuntimeContext.allyInvites(level).all(),
                WarRuntimeContext.occupations(level).all(),
                WarRuntimeContext.revolts(level).all());
        BannerModMain.SIMPLE_CHANNEL.send(BannerModPacketDistributor.PLAYER.with(() -> player),
                new MessageToClientUpdateWarState(payload));
    }

    private static com.talhanation.bannermod.war.runtime.BattleWindowSchedule resolveSchedule() {
        try {
            return WarServerConfig.resolveSchedule();
        } catch (IllegalStateException ex) {
            return com.talhanation.bannermod.war.runtime.BattleWindowSchedule.defaultSchedule();
        }
    }

    @Override
    public MessageResolveRevolt fromBytes(FriendlyByteBuf buf) {
        this.revoltId = buf.readUUID();
        this.outcomeOrdinal = buf.readByte();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.revoltId);
        buf.writeByte(this.outcomeOrdinal);
    }
}
