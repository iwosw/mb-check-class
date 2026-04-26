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
 * Client → server: leader (or op) renames an existing political entity.
 *
 * <p>Authority is enforced server-side via {@link PoliticalEntityAuthority#isLeaderOrOp};
 * the client may render the rename button only when locally believed legal, but the server
 * never trusts that.</p>
 */
public class MessageRenamePoliticalEntity implements Message<MessageRenamePoliticalEntity> {

    private UUID entityId;
    private String newName;

    public MessageRenamePoliticalEntity() {
    }

    public MessageRenamePoliticalEntity(UUID entityId, String newName) {
        this.entityId = entityId;
        this.newName = newName == null ? "" : newName;
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
            player.sendSystemMessage(Component.literal("Cannot rename: state not found."));
            return;
        }
        PoliticalEntityRecord record = recordOpt.get();
        if (!PoliticalEntityAuthority.isLeaderOrOp(player, record)) {
            player.sendSystemMessage(Component.literal("Only the political entity leader (or an op) can do that."));
            return;
        }
        PoliticalRegistryValidation.Result validation = registry.canRename(this.entityId, this.newName);
        if (!validation.valid()) {
            player.sendSystemMessage(Component.literal("Cannot rename state: " + validation.reason()));
            return;
        }
        if (!registry.updateName(this.entityId, this.newName)) {
            player.sendSystemMessage(Component.literal("Failed to rename state."));
            return;
        }
        player.sendSystemMessage(Component.literal(
                "Renamed state to: " + PoliticalRegistryValidation.normalizeName(this.newName)));
    }

    @Override
    public MessageRenamePoliticalEntity fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readUUID();
        this.newName = buf.readUtf(64);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityId);
        buf.writeUtf(this.newName == null ? "" : this.newName, 64);
    }
}
