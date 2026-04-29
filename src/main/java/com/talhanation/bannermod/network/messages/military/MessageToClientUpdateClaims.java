package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.List;

public class MessageToClientUpdateClaims implements BannerModMessage<MessageToClientUpdateClaims> {
    private CompoundTag claimsListNBT;
    private int claimCost;
    private int chunkCost;
    private boolean cascadeOfCost;
    private boolean allowClaiming;
    private boolean fogOfWarEnabled;
    private ItemStack currencyItemStack;
    public MessageToClientUpdateClaims() {
    }

    public MessageToClientUpdateClaims(List<RecruitsClaim> list, int claimCost, int chunkCost, boolean cascadeOfCost, boolean allowClaiming, boolean fogOfWarEnabled, ItemStack currencyItemStack) {
        this.claimsListNBT = RecruitsClaim.toNBT(list);
        this.claimCost = claimCost;
        this.chunkCost = chunkCost;
        this.cascadeOfCost = cascadeOfCost;
        this.currencyItemStack = currencyItemStack;
        this.allowClaiming = allowClaiming;
        this.fogOfWarEnabled = fogOfWarEnabled;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(BannerModNetworkContext context) {
        RuntimeProfilingCounters.recordNbtPacket("network.full_sync.claims", claimsListNBT);
        ClientManager.recruitsClaims = RecruitsClaim.getListFromNBT(claimsListNBT);
        ClientManager.markClaimsChanged();
        ClientManager.markClaimsSynchronized();
        ClientManager.configValueClaimCost = this.claimCost;
        ClientManager.configValueChunkCost = this.chunkCost;
        ClientManager.configValueCascadeClaimCost = this.cascadeOfCost;
        ClientManager.currencyItemStack = this.currencyItemStack;
        ClientManager.configValueIsClaimingAllowed = this.allowClaiming;
        ClientManager.configFogOfWarEnabled = this.fogOfWarEnabled;
    }

    @Override
    public MessageToClientUpdateClaims fromBytes(RegistryFriendlyByteBuf buf) {
        this.claimsListNBT = buf.readNbt();
        this.claimCost = buf.readInt();
        this.chunkCost = buf.readInt();
        this.cascadeOfCost = buf.readBoolean();
        this.currencyItemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        this.allowClaiming = buf.readBoolean();
        this.fogOfWarEnabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(this.claimsListNBT);
        buf.writeInt(this.claimCost);
        buf.writeInt(this.chunkCost);
        buf.writeBoolean(this.cascadeOfCost);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.currencyItemStack);
        buf.writeBoolean(this.allowClaiming);
        buf.writeBoolean(this.fogOfWarEnabled);
    }

}
