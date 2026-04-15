package com.talhanation.bannermod.logistics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

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
    public static BannerModLogisticsRoute create(UUID sourceEndpointId,
                                                 UUID destinationEndpointId,
                                                 @Nullable String itemFilterId,
                                                 int priority,
                                                 int minSourceCount,
                                                 int destinationThreshold,
                                                 String blockedReasonToken,
                                                 boolean enabled) {
        return new BannerModLogisticsRoute(
                UUID.randomUUID(),
                sourceEndpointId,
                destinationEndpointId,
                normalizeItemFilter(itemFilterId),
                priority,
                minSourceCount,
                destinationThreshold,
                blockedReasonToken,
                enabled
        );
    }

    public boolean matchesItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (this.itemFilterId == null) {
            return true;
        }
        return Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(), this.itemFilterId);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("RouteId", this.routeId);
        tag.putUUID("SourceEndpointId", this.sourceEndpointId);
        tag.putUUID("DestinationEndpointId", this.destinationEndpointId);
        if (this.itemFilterId != null) {
            tag.putString("ItemFilterId", this.itemFilterId);
        }
        tag.putInt("Priority", this.priority);
        tag.putInt("MinSourceCount", this.minSourceCount);
        tag.putInt("DestinationThreshold", this.destinationThreshold);
        tag.putString("BlockedReasonToken", this.blockedReasonToken);
        tag.putBoolean("Enabled", this.enabled);
        return tag;
    }

    public static BannerModLogisticsRoute fromTag(CompoundTag tag) {
        return new BannerModLogisticsRoute(
                tag.hasUUID("RouteId") ? tag.getUUID("RouteId") : UUID.randomUUID(),
                tag.getUUID("SourceEndpointId"),
                tag.getUUID("DestinationEndpointId"),
                normalizeItemFilter(tag.contains("ItemFilterId") ? tag.getString("ItemFilterId") : null),
                tag.getInt("Priority"),
                tag.getInt("MinSourceCount"),
                tag.getInt("DestinationThreshold"),
                tag.getString("BlockedReasonToken"),
                !tag.contains("Enabled") || tag.getBoolean("Enabled")
        );
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.routeId);
        buf.writeUUID(this.sourceEndpointId);
        buf.writeUUID(this.destinationEndpointId);
        buf.writeBoolean(this.itemFilterId != null);
        if (this.itemFilterId != null) {
            buf.writeUtf(this.itemFilterId);
        }
        buf.writeVarInt(this.priority);
        buf.writeVarInt(this.minSourceCount);
        buf.writeVarInt(this.destinationThreshold);
        buf.writeUtf(this.blockedReasonToken);
        buf.writeBoolean(this.enabled);
    }

    public static BannerModLogisticsRoute fromBytes(FriendlyByteBuf buf) {
        return new BannerModLogisticsRoute(
                buf.readUUID(),
                buf.readUUID(),
                buf.readUUID(),
                normalizeItemFilter(buf.readBoolean() ? buf.readUtf() : null),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readUtf(),
                buf.readBoolean()
        );
    }

    @Nullable
    private static String normalizeItemFilter(@Nullable String itemFilterId) {
        if (itemFilterId == null || itemFilterId.isBlank()) {
            return null;
        }
        return itemFilterId;
    }
}
