package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.api.ClientClaimEvent;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

public class MessageToClientUpdateClaim implements Message<MessageToClientUpdateClaim> {
    private CompoundTag claimNBT;

    public MessageToClientUpdateClaim() {
    }

    public MessageToClientUpdateClaim(RecruitsClaim claim) {
        this.claimNBT = claim.toNBT();
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(NetworkEvent.Context context) {
        RuntimeProfilingCounters.recordNbtPacket("network.single_sync.claim", claimNBT);
        this.updateOrAddClaimFromNBT(claimNBT);
    }
    @OnlyIn(Dist.CLIENT)
    public void updateOrAddClaimFromNBT(CompoundTag claimNBT) {
        RecruitsClaim newClaim = RecruitsClaim.fromNBT(claimNBT);

        for (int i = 0; i < ClientManager.recruitsClaims.size(); i++) {
            RecruitsClaim existing = ClientManager.recruitsClaims.get(i);
            if (existing.getUUID().equals(newClaim.getUUID())) {
                ClientManager.recruitsClaims.set(i, newClaim);

                boolean isCurrentClaim = ClientManager.currentClaim != null
                        && ClientManager.currentClaim.getUUID().equals(newClaim.getUUID());

                // Aktuellen Claim-Zeiger ebenfalls aktualisieren
                if (isCurrentClaim) {
                    ClientManager.currentClaim = newClaim;
                }

                ClientManager.markClaimsChanged();

                MinecraftForge.EVENT_BUS.post(new ClientClaimEvent.DataUpdated(newClaim, isCurrentClaim));
                return;
            }
        }

        ClientManager.recruitsClaims.add(newClaim);
        ClientManager.markClaimsChanged();
        MinecraftForge.EVENT_BUS.post(
                new ClientClaimEvent.DataUpdated(newClaim, false));
    }
    @Override
    public MessageToClientUpdateClaim fromBytes(FriendlyByteBuf buf) {
        this.claimNBT = buf.readNbt();

        return this;
    }
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(this.claimNBT);
    }
}
