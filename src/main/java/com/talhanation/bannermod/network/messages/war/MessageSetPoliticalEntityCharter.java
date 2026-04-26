package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.registry.PoliticalRegistryValidation;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Client → server: leader (or op) sets the political entity's free-text charter. Server-
 * authoritative length validation through {@link PoliticalRegistryValidation#validateCharter}.
 */
public class MessageSetPoliticalEntityCharter implements Message<MessageSetPoliticalEntityCharter> {

    private static final int CHARTER_WIRE_CAP = PoliticalRegistryValidation.MAX_CHARTER_LENGTH + 32;

    private UUID entityId;
    private String newCharter;

    public MessageSetPoliticalEntityCharter() {
    }

    public MessageSetPoliticalEntityCharter(UUID entityId, String newCharter) {
        this.entityId = entityId;
        this.newCharter = newCharter == null ? "" : newCharter;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
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
            player.sendSystemMessage(Component.literal("Cannot set charter: state not found."));
            return;
        }
        PoliticalEntityRecord record = recordOpt.get();
        if (!PoliticalEntityAuthority.isLeaderOrOp(player, record)) {
            player.sendSystemMessage(Component.literal("Only the political entity leader (or an op) can do that."));
            return;
        }
        PoliticalRegistryValidation.Result validation = PoliticalRegistryValidation.validateCharter(this.newCharter);
        if (!validation.valid()) {
            player.sendSystemMessage(Component.literal("Cannot set charter: " + validation.reason()));
            return;
        }
        if (!registry.updateCharter(this.entityId, this.newCharter)) {
            player.sendSystemMessage(Component.literal("Failed to set charter."));
            return;
        }
        String normalized = PoliticalRegistryValidation.normalizeCharter(this.newCharter);
        player.sendSystemMessage(Component.literal(
                "Set state charter (" + normalized.length() + " chars)."));
    }

    @Override
    public MessageSetPoliticalEntityCharter fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readUUID();
        this.newCharter = buf.readUtf(CHARTER_WIRE_CAP);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityId);
        buf.writeUtf(this.newCharter == null ? "" : this.newCharter, CHARTER_WIRE_CAP);
    }
}
