package com.talhanation.bannermod.shared.logistics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record BannerModSeaTradeExecutionRecord(
        UUID routeId,
        @Nullable UUID boundCarrierId,
        UUID sourceStorageAreaId,
        UUID destinationStorageAreaId,
        BannerModLogisticsItemFilter filter,
        int requestedCount,
        int cargoCount,
        BannerModSeaTradeExecutionState state,
        String failureReason
) {
    public static final String FAILURE_NO_CARRIER = "NO_CARRIER";
    public static final String FAILURE_NO_CARGO_LOADED = "NO_CARGO_LOADED";

    public BannerModSeaTradeExecutionRecord {
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(sourceStorageAreaId, "sourceStorageAreaId");
        Objects.requireNonNull(destinationStorageAreaId, "destinationStorageAreaId");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(state, "state");
        if (requestedCount <= 0) {
            throw new IllegalArgumentException("requestedCount must be > 0");
        }
        cargoCount = Math.max(0, Math.min(cargoCount, requestedCount));
        failureReason = failureReason == null ? "" : failureReason;
        if (state != BannerModSeaTradeExecutionState.FAILED) {
            failureReason = "";
        }
    }

    public static BannerModSeaTradeExecutionRecord start(BannerModLogisticsRoute route, @Nullable UUID boundCarrierId) {
        Objects.requireNonNull(route, "route");
        BannerModSeaTradeExecutionRecord record = new BannerModSeaTradeExecutionRecord(
                route.routeId(),
                boundCarrierId,
                route.source().storageAreaId(),
                route.destination().storageAreaId(),
                route.filter(),
                route.requestedCount(),
                0,
                BannerModSeaTradeExecutionState.LOADING,
                ""
        );
        return boundCarrierId == null ? record.failed(FAILURE_NO_CARRIER) : record;
    }

    public BannerModSeaTradeExecutionRecord loadStarted() {
        if (this.state == BannerModSeaTradeExecutionState.FAILED) {
            return this;
        }
        return new BannerModSeaTradeExecutionRecord(
                this.routeId,
                this.boundCarrierId,
                this.sourceStorageAreaId,
                this.destinationStorageAreaId,
                this.filter,
                this.requestedCount,
                this.cargoCount,
                BannerModSeaTradeExecutionState.LOADING,
                ""
        );
    }

    public BannerModSeaTradeExecutionRecord travelPending(int loadedCargoCount) {
        if (this.state == BannerModSeaTradeExecutionState.FAILED) {
            return this;
        }
        if (loadedCargoCount <= 0) {
            return failed(FAILURE_NO_CARGO_LOADED);
        }
        return new BannerModSeaTradeExecutionRecord(
                this.routeId,
                this.boundCarrierId,
                this.sourceStorageAreaId,
                this.destinationStorageAreaId,
                this.filter,
                this.requestedCount,
                loadedCargoCount,
                BannerModSeaTradeExecutionState.TRAVELLING,
                ""
        );
    }

    public BannerModSeaTradeExecutionRecord arrivalReady() {
        if (this.state != BannerModSeaTradeExecutionState.TRAVELLING) {
            return this;
        }
        return new BannerModSeaTradeExecutionRecord(
                this.routeId,
                this.boundCarrierId,
                this.sourceStorageAreaId,
                this.destinationStorageAreaId,
                this.filter,
                this.requestedCount,
                this.cargoCount,
                BannerModSeaTradeExecutionState.UNLOADING,
                ""
        );
    }

    public BannerModSeaTradeExecutionRecord unloadComplete() {
        if (this.state != BannerModSeaTradeExecutionState.UNLOADING) {
            return this;
        }
        return new BannerModSeaTradeExecutionRecord(
                this.routeId,
                this.boundCarrierId,
                this.sourceStorageAreaId,
                this.destinationStorageAreaId,
                this.filter,
                this.requestedCount,
                0,
                BannerModSeaTradeExecutionState.COMPLETE,
                ""
        );
    }

    public BannerModSeaTradeExecutionRecord failed(String reason) {
        String normalizedReason = reason == null || reason.isBlank() ? "UNKNOWN" : reason;
        return new BannerModSeaTradeExecutionRecord(
                this.routeId,
                this.boundCarrierId,
                this.sourceStorageAreaId,
                this.destinationStorageAreaId,
                this.filter,
                this.requestedCount,
                this.cargoCount,
                BannerModSeaTradeExecutionState.FAILED,
                normalizedReason
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("RouteId", this.routeId);
        if (this.boundCarrierId != null) {
            tag.putUUID("BoundCarrierId", this.boundCarrierId);
        }
        tag.putUUID("SourceStorageAreaId", this.sourceStorageAreaId);
        tag.putUUID("DestinationStorageAreaId", this.destinationStorageAreaId);
        ListTag itemIds = new ListTag();
        for (ResourceLocation itemId : this.filter.itemIds()) {
            itemIds.add(StringTag.valueOf(itemId.toString()));
        }
        tag.put("ItemFilter", itemIds);
        tag.putInt("RequestedCount", this.requestedCount);
        tag.putInt("CargoCount", this.cargoCount);
        tag.putString("State", this.state.name());
        if (!this.failureReason.isBlank()) {
            tag.putString("FailureReason", this.failureReason);
        }
        return tag;
    }

    public static BannerModSeaTradeExecutionRecord fromTag(CompoundTag tag) {
        return new BannerModSeaTradeExecutionRecord(
                tag.getUUID("RouteId"),
                tag.hasUUID("BoundCarrierId") ? tag.getUUID("BoundCarrierId") : null,
                tag.getUUID("SourceStorageAreaId"),
                tag.getUUID("DestinationStorageAreaId"),
                BannerModLogisticsItemFilter.ofItemIds(readItemIds(tag.getList("ItemFilter", Tag.TAG_STRING))),
                Math.max(1, tag.getInt("RequestedCount")),
                tag.getInt("CargoCount"),
                stateFromTagName(tag.getString("State")),
                tag.contains("FailureReason", Tag.TAG_STRING) ? tag.getString("FailureReason") : ""
        );
    }

    private static Set<ResourceLocation> readItemIds(ListTag list) {
        LinkedHashSet<ResourceLocation> itemIds = new LinkedHashSet<>();
        for (Tag entry : list) {
            ResourceLocation itemId = ResourceLocation.tryParse(entry.getAsString());
            if (itemId != null) {
                itemIds.add(itemId);
            }
        }
        return itemIds;
    }

    private static BannerModSeaTradeExecutionState stateFromTagName(String name) {
        try {
            return BannerModSeaTradeExecutionState.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return BannerModSeaTradeExecutionState.FAILED;
        }
    }
}
