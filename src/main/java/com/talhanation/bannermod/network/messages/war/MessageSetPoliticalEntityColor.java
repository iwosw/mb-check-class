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
 * Client → server: leader (or op) sets the political entity's color string. Validation is
 * server-authoritative through {@link PoliticalRegistryValidation#validateColor}; the client
 * may grey-out the input on local validation failures but the server never trusts it.
 */
public class MessageSetPoliticalEntityColor implements Message<MessageSetPoliticalEntityColor> {

    private UUID entityId;
    private String newColor;

    public MessageSetPoliticalEntityColor() {
    }

    public MessageSetPoliticalEntityColor(UUID entityId, String newColor) {
        this.entityId = entityId;
        this.newColor = newColor == null ? "" : newColor;
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
            player.sendSystemMessage(Component.literal("Cannot set color: state not found."));
            return;
        }
        PoliticalEntityRecord record = recordOpt.get();
        if (!PoliticalEntityAuthority.canAct(player, record)) {
            player.sendSystemMessage(Component.literal(PoliticalEntityAuthority.DENIAL_NOT_AUTHORIZED));
            return;
        }
        PoliticalRegistryValidation.Result validation = PoliticalRegistryValidation.validateColor(this.newColor);
        if (!validation.valid()) {
            player.sendSystemMessage(Component.literal("Cannot set color: " + validation.reason()));
            return;
        }
        if (!registry.updateColor(this.entityId, this.newColor)) {
            player.sendSystemMessage(Component.literal("Failed to set color."));
            return;
        }
        String normalized = PoliticalRegistryValidation.normalizeColor(this.newColor);
        player.sendSystemMessage(Component.literal(
                "Set state color to: " + (normalized.isEmpty() ? "(cleared)" : normalized)));
    }

    @Override
    public MessageSetPoliticalEntityColor fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readUUID();
        this.newColor = buf.readUtf(16);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityId);
        buf.writeUtf(this.newColor == null ? "" : this.newColor, 16);
    }
}
