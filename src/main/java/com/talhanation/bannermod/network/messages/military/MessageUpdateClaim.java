package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;


public class MessageUpdateClaim implements Message<MessageUpdateClaim> {

    private CompoundTag claimNBT;

    public MessageUpdateClaim(){

    }

    public MessageUpdateClaim(RecruitsClaim claim) {
        this.claimNBT = claim.toNBT();
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer sender = context.getSender();
        if (sender == null) return;
        RecruitsClaim updatedClaim = RecruitsClaim.fromNBT(this.claimNBT);
        if(!RecruitsServerConfig.AllowClaiming.get()) return;
        if(sender.level().dimension() != Level.OVERWORLD) return;
        if (ClaimEvents.recruitsClaimManager == null) return;

        ServerLevel level = (ServerLevel) sender.getCommandSenderWorld();
        RecruitsClaim existingClaim = getExistingClaim(updatedClaim);
        boolean isAdmin = sender.isCreative() && sender.hasPermissions(2);
        if (existingClaim == null && !isAdmin) return;
        if (existingClaim != null && !isAdmin && !canEditClaim(sender, existingClaim)) return;
        if (!isAdmin && overlapsOtherClaim(updatedClaim, existingClaim)) return;

        if (existingClaim != null && !isAdmin) {
            updatedClaim.setOwnerFaction(existingClaim.getOwnerFaction());
            updatedClaim.setPlayer(existingClaim.getPlayerInfo());
            updatedClaim.setAdminClaim(existingClaim.isAdmin);
        }
        if (ClaimEvents.recruitsClaimManager.isTownTooCloseToSameNationTown(
                updatedClaim,
                existingClaim,
                RecruitsServerConfig.TownMinCenterDistance.get())) return;

        ClaimEvents.recruitsClaimManager.addOrUpdateClaim(level, updatedClaim);
    }

    private static RecruitsClaim getExistingClaim(RecruitsClaim updatedClaim) {
        if (updatedClaim == null || ClaimEvents.recruitsClaimManager == null) return null;
        for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
            if (claim.getUUID().equals(updatedClaim.getUUID())) {
                return claim;
            }
        }
        return null;
    }

    private static boolean canEditClaim(ServerPlayer sender, RecruitsClaim existingClaim) {
        if (existingClaim.getPlayerInfo() != null && sender.getUUID().equals(existingClaim.getPlayerInfo().getUUID())) {
            return true;
        }
        return sender.getTeam() != null
                && existingClaim.getOwnerFaction() != null
                && Objects.equals(sender.getTeam().getName(), existingClaim.getOwnerFactionStringID())
                && Objects.equals(sender.getUUID(), existingClaim.getOwnerFaction().getTeamLeaderUUID());
    }

    private static boolean overlapsOtherClaim(RecruitsClaim updatedClaim, RecruitsClaim existingClaim) {
        if (updatedClaim == null || updatedClaim.isRemoved || ClaimEvents.recruitsClaimManager == null) return false;
        for (ChunkPos pos : updatedClaim.getClaimedChunks()) {
            RecruitsClaim occupied = ClaimEvents.recruitsClaimManager.getClaim(pos);
            if (occupied != null && (existingClaim == null || !occupied.getUUID().equals(existingClaim.getUUID()))) {
                return true;
            }
        }
        return false;
    }
    public MessageUpdateClaim fromBytes(FriendlyByteBuf buf) {
        this.claimNBT = buf.readNbt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(claimNBT);
    }
}
