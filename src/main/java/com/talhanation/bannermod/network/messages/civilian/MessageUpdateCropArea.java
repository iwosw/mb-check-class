package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

import static com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity.DONE_TIME;

public class MessageUpdateCropArea implements Message<MessageUpdateCropArea> {

    public UUID uuid;
    public CompoundTag tag;
    public MessageUpdateCropArea() {

    }

    public MessageUpdateCropArea(UUID uuid, ItemStack cropItem) {
        this.uuid = uuid;

        CompoundTag compoundnbt = new CompoundTag();
        this.tag = cropItem.save(compoundnbt);
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        CropArea cropArea = WorkAreaMessageSupport.resolveAuthorizedWorkArea(player, this.uuid, CropArea.class);
        if (cropArea == null) {
            return;
        }

        this.update(cropArea);
    }

    public void update(CropArea cropArea){
        ItemStack itemStack = ItemStack.of(tag);
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
