package com.talhanation.bannermod.logistics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

@Deprecated(forRemoval = false)
public record BannerModLogisticsRoute(
        UUID routeId,
        UUID sourceEndpointId,
        UUID destinationEndpointId,
        @Nullable String itemFilterId,
        int priority,
        int minSourceCount,
        int destinationThreshold,
        String blockedReasonToken,
        boolean enabled
) {
    private static BannerModLogisticsRoute fromShared(com.talhanation.bannerlord.shared.logistics.BannerModLogisticsRoute route) {
        return new BannerModLogisticsRoute(route.routeId(), route.sourceEndpointId(), route.destinationEndpointId(), route.itemFilterId(), route.priority(), route.minSourceCount(), route.destinationThreshold(), route.blockedReasonToken(), route.enabled());
    }

    private com.talhanation.bannerlord.shared.logistics.BannerModLogisticsRoute toShared() {
        return new com.talhanation.bannerlord.shared.logistics.BannerModLogisticsRoute(this.routeId, this.sourceEndpointId, this.destinationEndpointId, this.itemFilterId, this.priority, this.minSourceCount, this.destinationThreshold, this.blockedReasonToken, this.enabled);
    }

    public static BannerModLogisticsRoute create(UUID sourceEndpointId,
                                                 UUID destinationEndpointId,
                                                 @Nullable String itemFilterId,
                                                 int priority,
                                                 int minSourceCount,
                                                 int destinationThreshold,
                                                 String blockedReasonToken,
                                                 boolean enabled) {
        return fromShared(com.talhanation.bannerlord.shared.logistics.BannerModLogisticsRoute.create(sourceEndpointId, destinationEndpointId, itemFilterId, priority, minSourceCount, destinationThreshold, blockedReasonToken, enabled));
    }

    public boolean matchesItem(ItemStack stack) {
        return this.toShared().matchesItem(stack);
    }

    public int minimumSourceStock() {
        return this.minSourceCount;
    }

    public int desiredDestinationStock() {
        return this.destinationThreshold;
    }

    public CompoundTag toTag() {
        return this.toShared().toTag();
    }

    public static BannerModLogisticsRoute fromTag(CompoundTag tag) {
        return fromShared(com.talhanation.bannerlord.shared.logistics.BannerModLogisticsRoute.fromTag(tag));
    }

    public void toBytes(FriendlyByteBuf buf) {
        this.toShared().toBytes(buf);
    }

    public static BannerModLogisticsRoute fromBytes(FriendlyByteBuf buf) {
        return fromShared(com.talhanation.bannerlord.shared.logistics.BannerModLogisticsRoute.fromBytes(buf));
    }
}
