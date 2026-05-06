package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.settlement.prefab.BuildingPlacementService;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

/**
 * Client → server request to place a prefab building at a target position.
 *
 * <p>The actual placement work is handled by {@link BuildingPlacementService}, which
 * validates the prefab id, spawns the {@code BuildArea}, and loads the blueprint.</p>
 */
public class MessageRequestPlaceBuilding implements BannerModMessage<MessageRequestPlaceBuilding> {
    public ResourceLocation prefabId;
    public BlockPos pos;
    public int facingIndex;

    public MessageRequestPlaceBuilding() {
    }

    public MessageRequestPlaceBuilding(ResourceLocation prefabId, BlockPos pos, Direction facing) {
        this.prefabId = prefabId;
        this.pos = pos;
        this.facingIndex = (facing == null ? Direction.SOUTH : facing).get3DDataValue();
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
            if (prefabId == null || pos == null) {
                return;
            }
            if (!BuildingRequestSecurity.canUseWandAt(player, pos)) {
                return;
            }
            Direction facing = Direction.from3DDataValue(this.facingIndex);
            BuildingPlacementService.placeFor(player, prefabId, pos, facing);
        });
    }

    @Override
    public MessageRequestPlaceBuilding fromBytes(FriendlyByteBuf buf) {
        this.prefabId = buf.readResourceLocation();
        this.pos = buf.readBlockPos();
        this.facingIndex = buf.readVarInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(prefabId);
        buf.writeBlockPos(pos);
        buf.writeVarInt(facingIndex);
    }
}
