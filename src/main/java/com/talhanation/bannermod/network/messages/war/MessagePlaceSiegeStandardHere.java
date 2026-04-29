package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.runtime.SiegeStandardPlacementService;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.compat.BannerModPacketDistributor;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;

/**
 * Client → server: side leader places a siege standard at their current block position.
 *
 * <p>Mirrors {@code /bannermod siege place <warId> <side>} for the player-facing War Room
 * UI. Validation, audit, and block placement run through
 * {@link SiegeStandardPlacementService} so the slash and packet entry points stay in sync.</p>
 */
public class MessagePlaceSiegeStandardHere implements BannerModMessage<MessagePlaceSiegeStandardHere> {

    private UUID warId;
    private UUID sideId;
    private int requestedRadius;

    public MessagePlaceSiegeStandardHere() {
    }

    public MessagePlaceSiegeStandardHere(UUID warId, UUID sideId, int requestedRadius) {
        this.warId = warId;
        this.sideId = sideId;
        this.requestedRadius = requestedRadius;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null || this.warId == null || this.sideId == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (level == null) {
            return;
        }
        SiegeStandardPlacementService.Result result = SiegeStandardPlacementService.placeAt(
                level, player, this.warId, this.sideId, null, this.requestedRadius);
        if (result.ok()) {
            sendFeedback(player, Component.translatable("gui.bannermod.war.feedback.siege_standard_placed",
                    result.record().pos().toShortString(), result.record().radius()));
        } else {
            sendFeedback(player, Component.translatable("gui.bannermod.war.denial.siege_standard",
                    SiegeStandardPlacementService.describeComponent(result.outcome())));
        }
    }

    private static void sendFeedback(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
        BannerModMain.SIMPLE_CHANNEL.send(BannerModPacketDistributor.PLAYER.with(() -> player),
                new MessageToClientWarActionFeedback(message));
    }

    @Override
    public MessagePlaceSiegeStandardHere fromBytes(FriendlyByteBuf buf) {
        this.warId = buf.readUUID();
        this.sideId = buf.readUUID();
        this.requestedRadius = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.warId);
        buf.writeUUID(this.sideId);
        buf.writeInt(this.requestedRadius);
    }
}
