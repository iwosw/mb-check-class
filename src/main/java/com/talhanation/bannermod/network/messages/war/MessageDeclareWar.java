package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationService;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.UUID;

public class MessageDeclareWar implements BannerModMessage<MessageDeclareWar> {
    private UUID attackerId;
    private UUID defenderId;
    private byte goalOrdinal;
    private String casusBelli;

    public MessageDeclareWar() {
    }

    public MessageDeclareWar(UUID attackerId, UUID defenderId, WarGoalType goal, String casusBelli) {
        this.attackerId = attackerId;
        this.defenderId = defenderId;
        this.goalOrdinal = goal == null ? (byte) WarGoalType.TRIBUTE.ordinal() : (byte) goal.ordinal();
        this.casusBelli = casusBelli == null ? "" : casusBelli;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || this.attackerId == null || this.defenderId == null) {
            return;
        }
        ServerLevel level = player.serverLevel().getServer().overworld();
        PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
        Optional<PoliticalEntityRecord> attacker = registry.byId(this.attackerId);
        Optional<PoliticalEntityRecord> defender = registry.byId(this.defenderId);
        if (attacker.isEmpty() || defender.isEmpty()) {
            player.sendSystemMessage(Component.literal("Political entity not found."));
            return;
        }

        WarDeclarationService.Result result = WarDeclarationService.declare(
                player.server, level, player.getUUID(), player.hasPermissions(2),
                attacker.get(), defender.get(), decodeGoal(this.goalOrdinal), this.casusBelli);
        player.sendSystemMessage(result.message());
        if (result.success()) {
            CompoundTag payload = WarClientState.encode(
                    registry.all(),
                    WarRuntimeContext.declarations(level).all(),
                    WarRuntimeContext.sieges(level).all(),
                    resolveSchedule(),
                    WarRuntimeContext.allyInvites(level).all(),
                    WarRuntimeContext.occupations(level).all(),
                    WarRuntimeContext.revolts(level).all());
            BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MessageToClientUpdateWarState(payload));
        }
    }

    private static WarGoalType decodeGoal(byte ordinal) {
        WarGoalType[] values = WarGoalType.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return WarGoalType.TRIBUTE;
        }
        return values[ordinal];
    }

    private static com.talhanation.bannermod.war.runtime.BattleWindowSchedule resolveSchedule() {
        try {
            return WarServerConfig.resolveSchedule();
        } catch (IllegalStateException ex) {
            return com.talhanation.bannermod.war.runtime.BattleWindowSchedule.defaultSchedule();
        }
    }

    @Override
    public MessageDeclareWar fromBytes(FriendlyByteBuf buf) {
        this.attackerId = buf.readUUID();
        this.defenderId = buf.readUUID();
        this.goalOrdinal = buf.readByte();
        this.casusBelli = buf.readUtf(256);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.attackerId);
        buf.writeUUID(this.defenderId);
        buf.writeByte(this.goalOrdinal);
        buf.writeUtf(this.casusBelli == null ? "" : this.casusBelli, 256);
    }
}
