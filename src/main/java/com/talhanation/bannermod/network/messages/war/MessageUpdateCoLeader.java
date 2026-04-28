package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.Optional;
import java.util.UUID;

public class MessageUpdateCoLeader implements BannerModMessage<MessageUpdateCoLeader> {
    private UUID entityId;
    private UUID coLeaderUuid;
    private boolean add;

    public MessageUpdateCoLeader() {
    }

    public MessageUpdateCoLeader(UUID entityId, UUID coLeaderUuid, boolean add) {
        this.entityId = entityId;
        this.coLeaderUuid = coLeaderUuid;
        this.add = add;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null || this.entityId == null || this.coLeaderUuid == null) {
            return;
        }
        ServerLevel level = player.serverLevel().getServer().overworld();
        PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
        Optional<PoliticalEntityRecord> recordOpt = registry.byId(this.entityId);
        if (recordOpt.isEmpty()) {
            player.sendSystemMessage(Component.literal("Cannot update co-leader: state not found."));
            return;
        }
        PoliticalEntityRecord record = recordOpt.get();
        if (!PoliticalEntityAuthority.isLeaderOrOp(player, record)) {
            player.sendSystemMessage(Component.literal(PoliticalEntityAuthority.DENIAL_LEADER_ONLY));
            return;
        }
        boolean changed = this.add
                ? registry.addCoLeader(this.entityId, this.coLeaderUuid)
                : registry.removeCoLeader(this.entityId, this.coLeaderUuid);
        if (!changed) {
            player.sendSystemMessage(Component.literal("Co-leader update did not change the state."));
            return;
        }
        String action = this.add ? "added to" : "removed from";
        player.sendSystemMessage(Component.literal(this.coLeaderUuid + " " + action + " co-leaders for " + record.name() + "."));
    }

    @Override
    public MessageUpdateCoLeader fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readUUID();
        this.coLeaderUuid = buf.readUUID();
        this.add = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityId);
        buf.writeUUID(this.coLeaderUuid);
        buf.writeBoolean(this.add);
    }
}
