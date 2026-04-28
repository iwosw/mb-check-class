package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;

import static com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity.DONE_TIME;

public class MessageUpdateCropArea implements BannerModMessage<MessageUpdateCropArea> {

    public UUID uuid;
    public CompoundTag tag;
    public MessageUpdateCropArea() {

    }

    public MessageUpdateCropArea(UUID uuid, ItemStack cropItem) {
        this.uuid = uuid;

        CompoundTag compoundnbt = new CompoundTag();
        this.tag = (CompoundTag) cropItem.save(RegistryAccess.EMPTY, compoundnbt);
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(BannerModNetworkContext context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        CropArea cropArea = WorkAreaMessageSupport.resolveAuthorizedWorkArea(player, this.uuid, CropArea.class);
        if (cropArea == null) {
            return;
        }

        this.update(cropArea);
        WorkAreaMessageSupport.refreshSettlementSnapshot(player.serverLevel(), cropArea.blockPosition());
    }

    public void update(CropArea cropArea){
        ItemStack itemStack = ItemStack.parseOptional(RegistryAccess.EMPTY, tag);
        cropArea.setSeedStack(itemStack);
        cropArea.updateType();
        cropArea.setTime(cropArea.getTime() + DONE_TIME);
    }

    public MessageUpdateCropArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.tag = buf.readNbt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeNbt(tag);

    }

}
