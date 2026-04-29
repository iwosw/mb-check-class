package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.commands.war.WarAnnexEffects;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.rp.WarNoticeService;
import com.talhanation.bannermod.war.runtime.ClaimRepublisher;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarDeclarationRuntime;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import com.talhanation.bannermod.war.runtime.WarOutcomeApplier;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.compat.BannerModPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessageResolveWarOutcome implements BannerModMessage<MessageResolveWarOutcome> {
    private UUID warId;
    private byte actionOrdinal;

    public MessageResolveWarOutcome() {
    }

    public MessageResolveWarOutcome(UUID warId, Action action) {
        this.warId = warId;
        this.actionOrdinal = action == null ? (byte) Action.CANCEL.ordinal() : (byte) action.ordinal();
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null || this.warId == null) {
            return;
        }
        ServerLevel level = player.serverLevel().getServer().overworld();
        WarDeclarationRuntime declarations = WarRuntimeContext.declarations(level);
        Optional<WarDeclarationRecord> warOpt = declarations.byId(this.warId);
        if (warOpt.isEmpty()) {
            sendFeedback(player, Component.translatable("chat.bannermod.war_outcome.denied.not_found"));
            return;
        }
        WarDeclarationRecord war = warOpt.get();
        Action action = decodeAction(this.actionOrdinal);
        PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
        Optional<PoliticalEntityRecord> attacker = registry.byId(war.attackerPoliticalEntityId());
        if (action == Action.TRIBUTE && !player.hasPermissions(2)) {
            sendFeedback(player, Component.translatable("chat.bannermod.war_outcome.denied.op_only", actionName(action)));
            return;
        }
        if (attacker.isEmpty() || !PoliticalEntityAuthority.canAct(player, attacker.get())) {
            sendFeedback(player, PoliticalEntityAuthority.denialReason(player.getUUID(), player.hasPermissions(2), attacker.orElse(null)));
            return;
        }
        if (!isOutcomeSupportedForGoal(action, war.goalType())) {
            sendFeedback(player, Component.translatable("chat.bannermod.war_outcome.denied.unsupported", actionName(action), war.goalType().name()));
            return;
        }

        WarOutcomeApplier.Result result = apply(action, player, level, war);
        if (!result.valid()) {
            sendFeedback(player, Component.translatable("chat.bannermod.war_outcome.denied.rejected", actionName(action), result.reason()));
            return;
        }

        WarDeclarationRecord finalWar = declarations.byId(war.id()).orElse(war);
        if (action == Action.CANCEL) {
            WarNoticeService.broadcastCancelled(player.server, finalWar, registry);
            sendFeedback(player, Component.translatable("chat.bannermod.war_outcome.accepted.cancel"));
        } else {
            String outcomeName = result.outcome() == null ? "RESOLVED" : result.outcome().name();
            WarNoticeService.broadcastOutcome(player.server, finalWar, registry, outcomeName);
            sendFeedback(player, Component.translatable("chat.bannermod.war_outcome.accepted.resolved", outcomeName));
        }
        sendSnapshot(player, level, registry);
    }

    private static void sendFeedback(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
        BannerModMain.SIMPLE_CHANNEL.send(BannerModPacketDistributor.PLAYER.with(() -> player),
                new MessageToClientWarActionFeedback(message));
    }

    static boolean isOutcomeSupportedForGoal(Action action, WarGoalType goal) {
        return switch (action) {
            case CANCEL -> true;
            case TRIBUTE -> goal == WarGoalType.TRIBUTE;
            case OCCUPY -> goal == WarGoalType.OCCUPATION;
            case ANNEX -> goal == WarGoalType.ANNEX_LIMITED_CHUNKS;
        };
    }

    private static Component actionName(Action action) {
        return Component.translatable("chat.bannermod.war_outcome.action." + action.name().toLowerCase(java.util.Locale.ROOT));
    }

    private static WarOutcomeApplier.Result apply(Action action, ServerPlayer player, ServerLevel level, WarDeclarationRecord war) {
        WarOutcomeApplier applier = WarRuntimeContext.applierFor(level);
        long gameTime = level.getGameTime();
        return switch (action) {
            case CANCEL -> applier.cancel(war.id(), gameTime, "ui_cancel");
            case TRIBUTE -> applier.applyTribute(war.id(), 0L, gameTime);
            case OCCUPY -> applier.applyOccupy(war.id(), chunksAround(player.blockPosition(), 0), gameTime);
            case ANNEX -> {
                ChunkPos centre = new ChunkPos(BlockPos.containing(player.position()));
                RecruitsClaimManager claimManager = ClaimEvents.recruitsClaimManager;
                ClaimRepublisher publisher = claim -> claimManager.addOrUpdateClaim(level, claim);
                RecruitsClaim claim = claimManager.getClaim(centre);
                WarOutcomeApplier.Result result = applier.applyAnnex(war.id(), centre, gameTime, publisher);
                if (result.valid() && claim != null) {
                    int rebound = WarAnnexEffects.rebindEntitiesToNewOwner(
                            level, claim, war.defenderPoliticalEntityId(), war.attackerPoliticalEntityId());
                    WarRuntimeContext.audit(level).append(war.id(), "ANNEX_REBIND",
                            "claim=" + claim.getUUID() + ";rebound=" + rebound,
                            level.getGameTime());
                }
                yield result;
            }
        };
    }

    private static List<ChunkPos> chunksAround(BlockPos pos, int radius) {
        ChunkPos centre = new ChunkPos(pos);
        int r = Math.max(0, Math.min(radius, 8));
        List<ChunkPos> chunks = new ArrayList<>((2 * r + 1) * (2 * r + 1));
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                chunks.add(new ChunkPos(centre.x + dx, centre.z + dz));
            }
        }
        return chunks;
    }

    private static void sendSnapshot(ServerPlayer player, ServerLevel level, PoliticalRegistryRuntime registry) {
        CompoundTag payload = WarClientState.encode(
                registry.all(),
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

    private static Action decodeAction(byte ordinal) {
        Action[] values = Action.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return Action.CANCEL;
        }
        return values[ordinal];
    }

    @Override
    public MessageResolveWarOutcome fromBytes(FriendlyByteBuf buf) {
        this.warId = buf.readUUID();
        this.actionOrdinal = buf.readByte();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.warId);
        buf.writeByte(this.actionOrdinal);
    }

    public enum Action {
        CANCEL,
        TRIBUTE,
        OCCUPY,
        ANNEX
    }
}
