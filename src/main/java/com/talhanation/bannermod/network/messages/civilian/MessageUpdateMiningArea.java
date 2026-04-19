package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.MiningArea;
import com.talhanation.bannermod.entity.civilian.workarea.MiningPatternSettings;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageUpdateMiningArea implements Message<MessageUpdateMiningArea> {

    public UUID uuid;
    public int xSize;
    public int ySize;
    public int yOffset;
    public boolean closeFloor;
    public int miningMode;
    public int branchSpacing;
    public int branchLength;
    public int descentStep;
    public MessageUpdateMiningArea() {}

    public MessageUpdateMiningArea(UUID uuid, int xSize, int ySize, int yOffset, boolean closeFloor, int miningMode, int branchSpacing, int branchLength, int descentStep) {
        this.uuid = uuid;
        this.xSize = xSize;
        this.ySize = ySize;
        this.yOffset = yOffset;
        this.closeFloor = closeFloor;
        this.miningMode = miningMode;
        this.branchSpacing = branchSpacing;
        this.branchLength = branchLength;
        this.descentStep = descentStep;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        MiningArea miningArea = WorkAreaMessageSupport.resolveAuthorizedWorkArea(player, this.uuid, MiningArea.class);
        if (miningArea == null) {
            return;
        }
        this.update(miningArea);
        WorkAreaMessageSupport.refreshSettlementSnapshot(player.serverLevel(), miningArea.blockPosition());
    }

    public void update(MiningArea miningArea){
        miningArea.applyPatternSettings(new MiningPatternSettings(
                MiningPatternSettings.Mode.fromIndex(this.miningMode),
                this.xSize,
                this.ySize,
                this.yOffset,
                this.closeFloor,
                this.branchSpacing,
                this.branchLength,
                this.descentStep
        ));
    }

    public MessageUpdateMiningArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.xSize = buf.readInt();
        this.ySize = buf.readInt();
        this.yOffset = buf.readInt();
        this.closeFloor = buf.readBoolean();
        this.miningMode = buf.readInt();
        this.branchSpacing = buf.readInt();
        this.branchLength = buf.readInt();
        this.descentStep = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(xSize);
        buf.writeInt(ySize);
        buf.writeInt(yOffset);
        buf.writeBoolean(closeFloor);
        buf.writeInt(miningMode);
        buf.writeInt(branchSpacing);
        buf.writeInt(branchLength);
        buf.writeInt(descentStep);
    }
}
