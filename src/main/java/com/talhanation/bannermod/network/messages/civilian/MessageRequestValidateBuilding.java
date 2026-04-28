package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.settlement.prefab.validation.BuildingValidationService;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

/**
 * Client → server request to validate a player-built structure.
 *
 * <p>Carries the chosen prefab id and three world positions: two opposite corners of the
 * bounding box and the player's center tap (sanity check for intent). Handled by
 * {@link BuildingValidationService}.</p>
 */
public class MessageRequestValidateBuilding implements BannerModMessage<MessageRequestValidateBuilding> {
    public ResourceLocation prefabId;
    public BlockPos cornerA;
    public BlockPos cornerB;
    public BlockPos center;

    public MessageRequestValidateBuilding() {
    }

    public MessageRequestValidateBuilding(ResourceLocation prefabId, BlockPos cornerA, BlockPos cornerB, BlockPos center) {
        this.prefabId = prefabId;
        this.cornerA = cornerA;
        this.cornerB = cornerB;
        this.center = center;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (!BuildingRequestSecurity.canUseWandAt(player, cornerA, cornerB, center)) {
            return;
        }
        BuildingValidationService.validate(player, prefabId, cornerA, cornerB, center);
    }

    @Override
    public MessageRequestValidateBuilding fromBytes(FriendlyByteBuf buf) {
        this.prefabId = buf.readResourceLocation();
        this.cornerA = buf.readBlockPos();
        this.cornerB = buf.readBlockPos();
        this.center = buf.readBlockPos();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(prefabId);
        buf.writeBlockPos(cornerA);
        buf.writeBlockPos(cornerB);
        buf.writeBlockPos(center);
    }
}
