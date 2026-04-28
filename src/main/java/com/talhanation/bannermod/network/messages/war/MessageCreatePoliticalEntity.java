package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.registry.PoliticalRegistryValidation;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

/**
 * Client → server: a player asks the server to create a new {@code SETTLEMENT}-status
 * political entity with the requesting player as leader and the requesting player's current
 * block position as initial capital.
 *
 * <p>Mirrors {@code /bannermod state create <name>} but is reachable from the political
 * entity list UI without requiring command access. Validation reuses
 * {@link PoliticalRegistryValidation} so the wire payload cannot bypass duplicate or length
 * rules.</p>
 */
public class MessageCreatePoliticalEntity implements Message<MessageCreatePoliticalEntity> {

    private String name;

    public MessageCreatePoliticalEntity() {
    }

    public MessageCreatePoliticalEntity(String name) {
        this.name = name == null ? "" : name;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        ServerLevel level = player.serverLevel().getServer().overworld();
        if (level == null) {
            return;
        }
        PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
        PoliticalRegistryValidation.Result validation = registry.canCreate(this.name, player.getUUID());
        if (!validation.valid()) {
            player.sendSystemMessage(Component.literal("Cannot create state: " + validation.reason()));
            return;
        }
        Optional<PoliticalEntityRecord> created = registry.create(
                this.name,
                player.getUUID(),
                player.blockPosition(),
                "",
                "",
                "",
                "",
                level.getGameTime()
        );
        if (created.isEmpty()) {
            player.sendSystemMessage(Component.literal("Failed to create state."));
            return;
        }
        player.sendSystemMessage(Component.literal("Created state: " + created.get().name()));
    }

    @Override
    public MessageCreatePoliticalEntity fromBytes(FriendlyByteBuf buf) {
        this.name = buf.readUtf(64);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.name == null ? "" : this.name, 64);
    }
}
