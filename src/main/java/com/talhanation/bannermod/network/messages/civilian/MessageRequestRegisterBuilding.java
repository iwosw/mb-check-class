package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.settlement.prefab.player.PlayerBuildingRegistrationService;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

public class MessageRequestRegisterBuilding implements BannerModMessage<MessageRequestRegisterBuilding> {
    public ResourceLocation prefabId;
    public BlockPos cornerA;
    public BlockPos cornerB;
    public BlockPos center;
    public BlockPos keyBlock;

    public MessageRequestRegisterBuilding() {
    }

    public MessageRequestRegisterBuilding(ResourceLocation prefabId,
                                          BlockPos cornerA,
                                          BlockPos cornerB,
                                          BlockPos center,
                                          BlockPos keyBlock) {
        this.prefabId = prefabId;
        this.cornerA = cornerA;
        this.cornerB = cornerB;
        this.center = center;
        this.keyBlock = keyBlock;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (!BuildingRequestSecurity.canUseWandAt(player, cornerA, cornerB, center, keyBlock)) {
                return;
            }
            PlayerBuildingRegistrationService.register(player, prefabId, cornerA, cornerB, center, keyBlock);
        });
    }

    @Override
    public MessageRequestRegisterBuilding fromBytes(FriendlyByteBuf buf) {
        this.prefabId = buf.readResourceLocation();
        this.cornerA = buf.readBlockPos();
        this.cornerB = buf.readBlockPos();
        this.center = buf.readBlockPos();
        this.keyBlock = buf.readBlockPos();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(prefabId);
        buf.writeBlockPos(cornerA);
        buf.writeBlockPos(cornerB);
        buf.writeBlockPos(center);
        buf.writeBlockPos(keyBlock);
    }
}
