package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.Optional;
import java.util.UUID;

/**
 * Client → server: leader (or op) sets the capital block of a political entity. When
 * {@code usePlayerPos} is true the server ignores the wire-supplied position and uses the
 * sender's current block position; this is what the "Set capital here" UI button sends so
 * the client never needs to round-trip the player coordinate.
 */
public class MessageSetPoliticalEntityCapital implements BannerModMessage<MessageSetPoliticalEntityCapital> {

    private UUID entityId;
    private boolean usePlayerPos;
    private BlockPos capital;

    public MessageSetPoliticalEntityCapital() {
    }

    public MessageSetPoliticalEntityCapital(UUID entityId) {
        this(entityId, true, BlockPos.ZERO);
    }

    public MessageSetPoliticalEntityCapital(UUID entityId, boolean usePlayerPos, BlockPos capital) {
        this.entityId = entityId;
        this.usePlayerPos = usePlayerPos;
        this.capital = capital == null ? BlockPos.ZERO : capital.immutable();
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || this.entityId == null) {
                return;
            }
            ServerLevel level = player.serverLevel().getServer().overworld();
            if (level == null) {
                return;
            }
            PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
            Optional<PoliticalEntityRecord> recordOpt = registry.byId(this.entityId);
            if (recordOpt.isEmpty()) {
                player.sendSystemMessage(Component.literal("Cannot set capital: state not found."));
                return;
            }
            PoliticalEntityRecord record = recordOpt.get();
            if (!PoliticalEntityAuthority.canAct(player, record)) {
                player.sendSystemMessage(Component.literal(PoliticalEntityAuthority.DENIAL_NOT_AUTHORIZED));
                return;
            }
            BlockPos pos = this.usePlayerPos ? player.blockPosition() : this.capital;
            if (!registry.updateCapital(this.entityId, pos)) {
                player.sendSystemMessage(Component.literal("Failed to set capital."));
                return;
            }
            player.sendSystemMessage(Component.literal(
                    "Capital of " + record.name() + " set to " + pos.toShortString() + "."));
        });
    }

    @Override
    public MessageSetPoliticalEntityCapital fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readUUID();
        this.usePlayerPos = buf.readBoolean();
        this.capital = buf.readBlockPos();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityId);
        buf.writeBoolean(this.usePlayerPos);
        buf.writeBlockPos(this.capital == null ? BlockPos.ZERO : this.capital);
    }
}
