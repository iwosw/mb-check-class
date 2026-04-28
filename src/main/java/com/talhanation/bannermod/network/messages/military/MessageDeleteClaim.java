package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;


public class MessageDeleteClaim implements BannerModMessage<MessageDeleteClaim> {

    private CompoundTag claimNBT;

    public MessageDeleteClaim(){

    }

    public MessageDeleteClaim(RecruitsClaim claim) {
        this.claimNBT = claim.toNBT();
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(BannerModNetworkContext context){
        ServerPlayer sender = context.getSender();
        if (sender == null) return;
        if (!RecruitsServerConfig.AllowClaiming.get()) return;
        if (sender.level().dimension() != Level.OVERWORLD) return;
        if (ClaimEvents.recruitsClaimManager == null) return;

        UUID claimUuid = claimUuid(this.claimNBT);
        if (claimUuid == null) return;
        RecruitsClaim existingClaim = MessageUpdateClaim.getExistingClaim(claimUuid);
        if (existingClaim == null) return;

        boolean isAdmin = MessageUpdateClaim.isAdmin(sender);
        if (!ClaimPacketAuthority.canEditClaim(
                sender.getUUID(),
                isAdmin,
                existingClaim,
                MessageUpdateClaim.resolvePoliticalOwner(sender, existingClaim))) return;

        ClaimEvents.recruitsClaimManager.removeClaim(existingClaim);
        ClaimEvents.recruitsClaimManager.broadcastClaimsToAll((ServerLevel) context.getSender().getCommandSenderWorld());
    }

    private static UUID claimUuid(CompoundTag tag) {
        return tag != null && tag.contains("UUID", Tag.TAG_INT_ARRAY) ? tag.getUUID("UUID") : null;
    }

    public MessageDeleteClaim fromBytes(FriendlyByteBuf buf) {
        this.claimNBT = buf.readNbt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(claimNBT);
    }
}
