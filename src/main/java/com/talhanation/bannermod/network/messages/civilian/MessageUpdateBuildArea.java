package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
public class MessageUpdateBuildArea implements Message<MessageUpdateBuildArea> {

    public UUID uuid;
    public CompoundTag structureNBT;
    public int xSize;
    public int ySize;
    public int zSize;
    public boolean build;
    public boolean isCreative;
    public MessageUpdateBuildArea() {}
    public MessageUpdateBuildArea(UUID uuid, int xSize, int ySize, int zSize, CompoundTag structureNBT, boolean build, boolean isCreative) {
        this.uuid = uuid;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.structureNBT = structureNBT;
        this.build = build;
        this.isCreative = isCreative;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        Entity entity = player.serverLevel().getEntity(this.uuid);
        if (!(entity instanceof BuildArea buildArea)) {
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.AREA_NOT_FOUND);
            return;
        }

        WorkAreaAuthoringRules.Decision decision = BuildAreaUpdateAuthoring.authorize(true, buildArea.getAuthoringAccess(player));
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            this.sendDecision(player, decision);
            return;
        }

        this.update(buildArea);
    }

    public void update(BuildArea buildArea){
        buildArea.setWidthSize(this.xSize);
        buildArea.setHeightSize(this.ySize);
        buildArea.setDepthSize(this.zSize);
        buildArea.setStructureNBT(this.structureNBT);
        if(build){
            buildArea.setStartBuild(this.isCreative);
        } else {
            buildArea.clearPlannedBuild();
        }
    }

    private void sendDecision(ServerPlayer player, WorkAreaAuthoringRules.Decision decision) {
        String messageKey = WorkAreaAuthoringRules.getMessageKey(decision);
        if (messageKey != null) {
            player.sendSystemMessage(Component.translatable(messageKey));
        }
    }

    @Override
    public MessageUpdateBuildArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.xSize = buf.readInt();
        this.ySize = buf.readInt();
        this.zSize = buf.readInt();

        byte[] compressed = buf.readByteArray();
        try {
            this.structureNBT = NbtIo.readCompressed(new ByteArrayInputStream(compressed));
        } catch (IOException e) {
            e.printStackTrace();
            this.structureNBT = new CompoundTag(); // Fallback
        }

        this.build = buf.readBoolean();
        this.isCreative = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(xSize);
        buf.writeInt(ySize);
        buf.writeInt(zSize);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.writeCompressed(structureNBT, out);
            byte[] compressed = out.toByteArray();
            buf.writeByteArray(compressed);
        } catch (IOException e) {
            e.printStackTrace();
            buf.writeByteArray(new byte[0]);
        }

        buf.writeBoolean(build);
        buf.writeBoolean(isCreative);
    }
}
