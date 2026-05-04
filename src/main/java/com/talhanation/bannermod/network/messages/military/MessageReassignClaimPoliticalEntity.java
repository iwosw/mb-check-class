package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * WORLDMAPCLAIMPE-001 — server-bound packet that reassigns the owning political
 * entity of an existing claim. Server-authoritative: the server re-validates
 * authority on BOTH the source PE (via {@link ClaimPacketAuthority#canEditClaim})
 * AND the target PE (via {@link PoliticalEntityAuthority#canAct}). Admins (op +
 * creative) bypass the political checks.
 *
 * <p>A null target id detaches the claim from any political entity; this is only
 * permitted for admins (handled implicitly by canEditClaim's admin override).
 */
public class MessageReassignClaimPoliticalEntity implements BannerModMessage<MessageReassignClaimPoliticalEntity> {

    private UUID claimUuid;
    @Nullable
    private UUID targetPoliticalEntityId;

    public MessageReassignClaimPoliticalEntity() {
    }

    public MessageReassignClaimPoliticalEntity(UUID claimUuid, @Nullable UUID targetPoliticalEntityId) {
        this.claimUuid = claimUuid;
        this.targetPoliticalEntityId = targetPoliticalEntityId;
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            if (!RecruitsServerConfig.AllowClaiming.get()) return;
            if (sender.level().dimension() != Level.OVERWORLD) return;
            if (ClaimEvents.claimManager() == null) return;
            if (claimUuid == null) {
                sendDenial(sender, "chat.bannermod.claim.transfer.denied.missing");
                return;
            }

            ServerLevel level = (ServerLevel) sender.getCommandSenderWorld();
            RecruitsClaim existingClaim = MessageUpdateClaim.getExistingClaim(claimUuid);
            if (existingClaim == null) {
                sendDenial(sender, "chat.bannermod.claim.transfer.denied.missing");
                return;
            }

            boolean isAdmin = MessageUpdateClaim.isAdmin(sender);
            PoliticalEntityRecord sourcePeRecord = MessageUpdateClaim.resolvePoliticalOwner(sender, existingClaim);

            PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
            PoliticalEntityRecord targetPeRecord = null;
            if (targetPoliticalEntityId != null) {
                targetPeRecord = registry.byId(targetPoliticalEntityId).orElse(null);
                if (targetPeRecord == null) {
                    sendDenial(sender, "chat.bannermod.claim.transfer.denied.target_missing");
                    return;
                }
            }

            UUID currentOwnerId = existingClaim.getOwnerPoliticalEntityId();
            if (java.util.Objects.equals(currentOwnerId, targetPoliticalEntityId)) {
                sendDenial(sender, "chat.bannermod.claim.transfer.denied.same");
                return;
            }

            // Source authority — must be able to edit the existing claim.
            if (!ClaimPacketAuthority.canEditClaim(sender.getUUID(), isAdmin, existingClaim, sourcePeRecord)) {
                sendDenial(sender, "chat.bannermod.claim.transfer.denied.no_source_authority");
                return;
            }

            // Target authority — only enforced when not admin and a target PE exists.
            // Detaching to no-state requires admin; non-admin callers must always pick
            // a target PE in which they hold authority.
            if (!isAdmin) {
                if (targetPeRecord == null) {
                    sendDenial(sender, "chat.bannermod.claim.transfer.denied.no_target_authority");
                    return;
                }
                if (!PoliticalEntityAuthority.canAct(sender.getUUID(), false, targetPeRecord)) {
                    String reasonKey = PoliticalEntityAuthority.denialReasonKey(sender.getUUID(), false, targetPeRecord);
                    sender.sendSystemMessage(Component.translatable("chat.bannermod.claim.transfer.denied.no_target_authority")
                            .append(Component.literal(" "))
                            .append(Component.translatable(reasonKey)));
                    return;
                }
            }

            existingClaim.setOwnerPoliticalEntityId(targetPoliticalEntityId);
            ClaimEvents.claimManager().addOrUpdateClaim(level, existingClaim);

            String targetName = targetPeRecord != null
                    ? targetPeRecord.name()
                    : Component.translatable("chat.bannermod.claim.transfer.detached").getString();
            sender.sendSystemMessage(Component.translatable(
                    "chat.bannermod.claim.transfer.success",
                    existingClaim.getName(),
                    targetName));
        });
    }

    private static void sendDenial(ServerPlayer sender, String key) {
        sender.sendSystemMessage(Component.translatable(key));
    }

    public MessageReassignClaimPoliticalEntity fromBytes(FriendlyByteBuf buf) {
        this.claimUuid = buf.readUUID();
        if (buf.readBoolean()) {
            this.targetPoliticalEntityId = buf.readUUID();
        } else {
            this.targetPoliticalEntityId = null;
        }
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(claimUuid);
        if (targetPoliticalEntityId != null) {
            buf.writeBoolean(true);
            buf.writeUUID(targetPoliticalEntityId);
        } else {
            buf.writeBoolean(false);
        }
    }
}
