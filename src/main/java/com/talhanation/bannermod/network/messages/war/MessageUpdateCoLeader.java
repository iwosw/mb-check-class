package com.talhanation.bannermod.network.messages.war;

import com.mojang.authlib.GameProfile;
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
    private String coLeaderToken;
    private boolean add;

    public MessageUpdateCoLeader() {
    }

    public MessageUpdateCoLeader(UUID entityId, String coLeaderToken, boolean add) {
        this.entityId = entityId;
        this.coLeaderToken = coLeaderToken == null ? "" : coLeaderToken.trim();
        this.add = add;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null || this.entityId == null || this.coLeaderToken == null || this.coLeaderToken.isBlank()) {
            return;
        }
        ServerLevel level = player.serverLevel().getServer().overworld();
        PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
        Optional<PoliticalEntityRecord> recordOpt = registry.byId(this.entityId);
        if (recordOpt.isEmpty()) {
            player.sendSystemMessage(Component.translatable("gui.bannermod.states.co_leader.state_not_found"));
            return;
        }
        PoliticalEntityRecord record = recordOpt.get();
        if (!PoliticalEntityAuthority.isLeaderOrOp(player, record)) {
            player.sendSystemMessage(Component.literal(PoliticalEntityAuthority.DENIAL_LEADER_ONLY));
            return;
        }
        ResolvedPlayer resolved = resolvePlayer(player, this.coLeaderToken);
        if (resolved == null) {
            player.sendSystemMessage(Component.translatable("gui.bannermod.states.co_leader.player_not_found"));
            return;
        }
        boolean changed = this.add
                ? registry.addCoLeader(this.entityId, resolved.uuid())
                : registry.removeCoLeader(this.entityId, resolved.uuid());
        if (!changed) {
            player.sendSystemMessage(Component.translatable("gui.bannermod.states.co_leader.no_change"));
            return;
        }
        player.sendSystemMessage(Component.translatable(
                this.add ? "gui.bannermod.states.co_leader.added" : "gui.bannermod.states.co_leader.removed",
                resolved.label(),
                record.name()));
    }

    private static ResolvedPlayer resolvePlayer(ServerPlayer requester, String token) {
        try {
            UUID uuid = UUID.fromString(token);
            String label = token;
            if (requester.getServer() != null) {
                ServerPlayer online = requester.getServer().getPlayerList().getPlayer(uuid);
                if (online != null) {
                    label = online.getGameProfile().getName();
                }
            }
            return new ResolvedPlayer(uuid, label);
        } catch (IllegalArgumentException ignored) {
        }

        if (requester.getServer() == null) {
            return null;
        }
        ServerPlayer online = requester.getServer().getPlayerList().getPlayerByName(token);
        if (online != null) {
            return new ResolvedPlayer(online.getUUID(), online.getGameProfile().getName());
        }
        Optional<GameProfile> profile = requester.getServer().getProfileCache().get(token);
        return profile.map(gameProfile -> new ResolvedPlayer(gameProfile.getId(), gameProfile.getName())).orElse(null);
    }

    @Override
    public MessageUpdateCoLeader fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readUUID();
        this.coLeaderToken = buf.readUtf(64);
        this.add = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityId);
        buf.writeUtf(this.coLeaderToken == null ? "" : this.coLeaderToken, 64);
        buf.writeBoolean(this.add);
    }

    private record ResolvedPlayer(UUID uuid, String label) {
    }
}
