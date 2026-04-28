package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.LumberArea;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

import static com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity.DONE_TIME;

public class MessageUpdateLumberArea implements BannerModMessage<MessageUpdateLumberArea> {

    public UUID uuid;
    public CompoundTag tag;
    public boolean shearLeaves;
    public boolean stripLogs;
    public boolean replant;
    public MessageUpdateLumberArea() {

    }

    public MessageUpdateLumberArea(UUID uuid, ItemStack saplingItem, boolean shearLeaves, boolean stripLogs, boolean replant) {
        this.uuid = uuid;
        CompoundTag compoundnbt = new CompoundTag();
        this.tag = saplingItem.save(compoundnbt);
        this.shearLeaves = shearLeaves;
        this.stripLogs = stripLogs;
        this.replant = replant;
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        LumberArea lumberArea = WorkAreaMessageSupport.resolveAuthorizedWorkArea(player, this.uuid, LumberArea.class);
        if (lumberArea == null) {
            return;
        }

        this.update(lumberArea);
        WorkAreaMessageSupport.refreshSettlementSnapshot(player.serverLevel(), lumberArea.blockPosition());
    }

    public void update(LumberArea lumberArea){
        ItemStack itemStack = ItemStack.of(tag);
        lumberArea.setSaplingStack(itemStack);
        lumberArea.setShearLeaves(this.shearLeaves);
        lumberArea.setStripLogs(this.stripLogs);
        lumberArea.setReplant(this.replant);

        lumberArea.time += DONE_TIME;
    }

    public MessageUpdateLumberArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.tag = buf.readNbt();
        this.shearLeaves = buf.readBoolean();
        this.stripLogs = buf.readBoolean();
        this.replant = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeNbt(tag);
        buf.writeBoolean(shearLeaves);
        buf.writeBoolean(stripLogs);
        buf.writeBoolean(replant);
    }

}
