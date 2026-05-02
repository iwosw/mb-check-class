package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.client.civilian.gui.WorkerStatusScreen;
import com.talhanation.bannermod.entity.civilian.WorkerInspectionSnapshot;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class MessageToClientOpenWorkerScreen implements BannerModMessage<MessageToClientOpenWorkerScreen> {
    private WorkerInspectionSnapshot snapshot;

    public MessageToClientOpenWorkerScreen() {
    }

    public MessageToClientOpenWorkerScreen(WorkerInspectionSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(BannerModNetworkContext context) {
        if (this.snapshot == null) {
            return;
        }
        Minecraft.getInstance().setScreen(new WorkerStatusScreen(this.snapshot));
    }

    @Override
    public MessageToClientOpenWorkerScreen fromBytes(FriendlyByteBuf buf) {
        this.snapshot = WorkerInspectionSnapshot.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        this.snapshot.toBytes(buf);
    }
}
