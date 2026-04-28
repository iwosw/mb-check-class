package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
public class MessageUpdateBuildArea implements Message<MessageUpdateBuildArea> {
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 32;

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

        BuildArea buildArea = WorkAreaMessageSupport.resolveAuthorizedWorkArea(
                player,
                this.uuid,
                BuildArea.class,
                area -> WorkAreaAuthoringRules.modifyDecision(true, area.getAuthoringAccess(player))
        );
        if (buildArea == null) {
            return;
        }

        String denial = validationDenial();
        if (denial != null) {
            player.sendSystemMessage(Component.literal("Build Area update rejected: " + denial));
            return;
        }

        this.update(buildArea);
        player.sendSystemMessage(Component.literal(build ? "Build Area build request accepted." : "Build Area scan settings accepted."));
        WorkAreaMessageSupport.refreshSettlementSnapshot(player.serverLevel(), buildArea.blockPosition());
    }

    private String validationDenial() {
        if (xSize < MIN_SIZE || xSize > MAX_SIZE || ySize < MIN_SIZE || ySize > MAX_SIZE || zSize < MIN_SIZE || zSize > MAX_SIZE) {
            return "dimensions must be between " + MIN_SIZE + " and " + MAX_SIZE;
        }
        if (structureNBT == null) {
            return "missing structure data";
        }
        if (!build) {
            return null;
        }
        if (structureNBT.isEmpty() || !structureNBT.contains("blocks", Tag.TAG_LIST)) {
            return "scan or load a structure before building";
        }
        if (structureNBT.getInt("width") != xSize || structureNBT.getInt("height") != ySize || structureNBT.getInt("depth") != zSize) {
            return "structure dimensions do not match the Build Area";
        }
        ListTag blocks = structureNBT.getList("blocks", Tag.TAG_COMPOUND);
        int maxBlocks = xSize * ySize * zSize;
        if (blocks.isEmpty()) {
            return "structure contains no blocks";
        }
        if (blocks.size() > maxBlocks) {
            return "structure contains more blocks than its bounds allow";
        }
        return null;
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
