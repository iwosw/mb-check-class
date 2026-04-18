package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsAuthoringState;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageUpdateStorageArea implements Message<MessageUpdateStorageArea> {

    public UUID uuid;
    public int mask;
    public String name;
    public String routeDestination;
    public String routeFilter;
    public String routeCount;
    public String routePriority;
    public MessageUpdateStorageArea() {

    }

    public MessageUpdateStorageArea(UUID uuid, int mask, String name, String routeDestination, String routeFilter, String routeCount, String routePriority) {
        this.uuid = uuid;
        this.mask = mask;
        this.name = name;
        this.routeDestination = routeDestination;
        this.routeFilter = routeFilter;
        this.routeCount = routeCount;
        this.routePriority = routePriority;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        StorageArea storageArea = WorkAreaMessageSupport.resolveAuthorizedWorkArea(player, this.uuid, StorageArea.class);
        if (storageArea == null) {
            return;
        }

        this.update(storageArea, player);

    }

    public void update(StorageArea storageArea, ServerPlayer player){
        storageArea.setStorageTypes(mask);
        storageArea.setCustomName(Component.literal(name));
        try {
            storageArea.setLogisticsRoute(BannerModLogisticsAuthoringState.parse(this.routeDestination, this.routeFilter, this.routeCount, this.routePriority));
            storageArea.clearRouteBlockedState();
        } catch (IllegalArgumentException exception) {
            player.sendSystemMessage(Component.literal(exception.getMessage()));
        }
    }
    public MessageUpdateStorageArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.mask = buf.readInt();
        this.name = buf.readUtf();
        this.routeDestination = buf.readUtf();
        this.routeFilter = buf.readUtf();
        this.routeCount = buf.readUtf();
        this.routePriority = buf.readUtf();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(mask);
        buf.writeUtf(name);
        buf.writeUtf(routeDestination == null ? "" : routeDestination);
        buf.writeUtf(routeFilter == null ? "" : routeFilter);
        buf.writeUtf(routeCount == null ? "" : routeCount);
        buf.writeUtf(routePriority == null ? "" : routePriority);
    }

}
