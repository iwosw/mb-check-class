package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.GovernmentForm;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Client → server: leader (or op) toggles a political entity's {@link GovernmentForm}
 * between {@link GovernmentForm#MONARCHY} and {@link GovernmentForm#REPUBLIC}.
 *
 * <p>The wire format encodes the form ordinal as a byte so the packet stays the same
 * three-field shape (id + ordinal) when more forms are added later.</p>
 */
public class MessageSetGovernmentForm implements Message<MessageSetGovernmentForm> {

    private UUID entityId;
    private byte formOrdinal;

    public MessageSetGovernmentForm() {
    }

    public MessageSetGovernmentForm(UUID entityId, GovernmentForm form) {
        this.entityId = entityId;
        this.formOrdinal = form == null ? (byte) GovernmentForm.MONARCHY.ordinal() : (byte) form.ordinal();
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
            player.sendSystemMessage(Component.literal("Cannot set government form: state not found."));
            return;
        }
        PoliticalEntityRecord record = recordOpt.get();
        if (!PoliticalEntityAuthority.isLeaderOrOp(player, record)) {
            // Government-form changes are leader-only; co-leaders can't reshape the regime.
            player.sendSystemMessage(Component.literal(PoliticalEntityAuthority.DENIAL_LEADER_ONLY));
            return;
        }
        GovernmentForm form = decodeForm(this.formOrdinal);
        if (!registry.updateGovernmentForm(this.entityId, form)) {
            player.sendSystemMessage(Component.literal("Failed to update government form."));
            return;
        }
        player.sendSystemMessage(Component.literal(
                "Government form of " + record.name() + " set to " + form.name() + "."));
    }

    private static GovernmentForm decodeForm(byte ordinal) {
        GovernmentForm[] values = GovernmentForm.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return GovernmentForm.MONARCHY;
        }
        return values[ordinal];
    }

    @Override
    public MessageSetGovernmentForm fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readUUID();
        this.formOrdinal = buf.readByte();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityId);
        buf.writeByte(this.formOrdinal);
    }
}
