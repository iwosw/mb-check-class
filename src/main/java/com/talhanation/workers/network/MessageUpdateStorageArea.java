package com.talhanation.workers.network;

import com.talhanation.bannerlord.shared.logistics.BannerModLogisticsRoute;
import com.talhanation.bannerlord.entity.civilian.workarea.StorageArea;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageUpdateStorageArea implements Message<MessageUpdateStorageArea> {

    public UUID uuid;
    public int mask;
    public String name;
    public List<BannerModLogisticsRoute> routes = new ArrayList<>();
    public MessageUpdateStorageArea() {

    }

    public MessageUpdateStorageArea(UUID uuid, int mask, String name) {
        this(uuid, mask, name, List.of());
    }

    public MessageUpdateStorageArea(UUID uuid, int mask, String name, List<BannerModLogisticsRoute> routes) {
        this.uuid = uuid;
        this.mask = mask;
        this.name = name;
        if (routes != null) {
            this.routes = new ArrayList<>(routes);
        }
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        Entity entity = player.serverLevel().getEntity(this.uuid);
        if (!(entity instanceof StorageArea storageArea)) {
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.AREA_NOT_FOUND);
            return;
        }

        WorkAreaAuthoringRules.Decision decision = WorkAreaAuthoringRules.modifyDecision(true, storageArea.getAuthoringAccess(player));
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            this.sendDecision(player, decision);
            return;
        }

        this.update(storageArea);

    }

    public void update(StorageArea storageArea){
        storageArea.setStorageTypes(mask);
        storageArea.setCustomName(Component.literal(name));
        storageArea.setLogisticsRoutes(this.routes);
    }

    public static boolean canApplyRouteUpdate(boolean areaExists, WorkAreaAuthoringRules.AccessLevel accessLevel) {
        return WorkAreaAuthoringRules.isAllowed(WorkAreaAuthoringRules.modifyDecision(areaExists, accessLevel));
    }

    private void sendDecision(ServerPlayer player, WorkAreaAuthoringRules.Decision decision) {
        String messageKey = WorkAreaAuthoringRules.getMessageKey(decision);
        if (messageKey != null) {
            player.sendSystemMessage(Component.translatable(messageKey));
        }
    }

    public MessageUpdateStorageArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.mask = buf.readInt();
        this.name = buf.readUtf();
        int routeCount = buf.readVarInt();
        this.routes = new ArrayList<>(routeCount);
        for (int i = 0; i < routeCount; i++) {
            this.routes.add(BannerModLogisticsRoute.fromBytes(buf));
        }
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(mask);
        buf.writeUtf(name);
        buf.writeVarInt(this.routes.size());
        for (BannerModLogisticsRoute route : this.routes) {
            route.toBytes(buf);
        }
    }

}
