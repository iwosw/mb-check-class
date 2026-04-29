package com.talhanation.bannermod.shared.logistics;

import com.talhanation.bannermod.ai.civilian.TransportContainerExchange;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BannerModSeaTradeExecutionRuntime {
    private final Map<UUID, BannerModSeaTradeExecutionRecord> routes = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> {
    };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> {
        } : dirtyListener;
    }

    public BannerModSeaTradeExecutionRecord start(BannerModLogisticsRoute route, @Nullable UUID boundCarrierId) {
        BannerModSeaTradeExecutionRecord record = BannerModSeaTradeExecutionRecord.start(route, boundCarrierId);
        this.routes.put(record.routeId(), record);
        markDirty();
        return record;
    }

    public BannerModSeaTradeExecutionRecord update(BannerModSeaTradeExecutionRecord record) {
        this.routes.put(record.routeId(), record);
        markDirty();
        return record;
    }

    public BannerModSeaTradeExecutionRecord loadFromSource(UUID routeId,
                                                           Collection<? extends Container> sourceContainers,
                                                           Container carrierCargo) {
        BannerModSeaTradeExecutionRecord record = route(routeId).orElseThrow();
        if (record.state() != BannerModSeaTradeExecutionState.LOADING) {
            return record;
        }
        int loaded = 0;
        int budget = record.requestedCount();
        if (sourceContainers != null) {
            for (Container source : sourceContainers) {
                if (loaded >= budget) {
                    break;
                }
                loaded += TransportContainerExchange.withdrawInto(source, carrierCargo, record.filter(), budget - loaded);
            }
        }
        BannerModSeaTradeExecutionRecord next = loaded >= record.requestedCount()
                ? record.travelPending(loaded)
                : record.failed(loaded <= 0
                        ? BannerModSeaTradeExecutionRecord.FAILURE_NO_CARGO_LOADED
                        : BannerModSeaTradeExecutionRecord.FAILURE_SOURCE_SHORTAGE, loaded);
        return update(next);
    }

    public BannerModSeaTradeExecutionRecord loadFromSource(UUID routeId,
                                                           StorageArea sourceStorageArea,
                                                           Container carrierCargo) {
        return loadFromSource(routeId, storageContainers(sourceStorageArea), carrierCargo);
    }

    public BannerModSeaTradeExecutionRecord unloadAtDestination(UUID routeId,
                                                                Collection<? extends Container> destinationContainers,
                                                                Container carrierCargo) {
        BannerModSeaTradeExecutionRecord record = route(routeId).orElseThrow();
        if (record.state() != BannerModSeaTradeExecutionState.UNLOADING) {
            return record;
        }
        int unloaded = 0;
        if (destinationContainers != null) {
            for (Container destination : destinationContainers) {
                unloaded += TransportContainerExchange.depositInto(destination, carrierCargo, record.filter());
                if (unloaded >= record.cargoCount()) {
                    break;
                }
            }
        }
        int remaining = Math.max(0, record.cargoCount() - unloaded);
        BannerModSeaTradeExecutionRecord next = remaining == 0
                ? record.unloadComplete()
                : record.failed(BannerModSeaTradeExecutionRecord.FAILURE_DESTINATION_FULL, remaining);
        return update(next);
    }

    public BannerModSeaTradeExecutionRecord unloadAtDestination(UUID routeId,
                                                                StorageArea destinationStorageArea,
                                                                Container carrierCargo) {
        return unloadAtDestination(routeId, storageContainers(destinationStorageArea), carrierCargo);
    }

    public Optional<BannerModSeaTradeExecutionRecord> route(UUID routeId) {
        if (routeId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.routes.get(routeId));
    }

    public List<BannerModSeaTradeExecutionRecord> routes() {
        return Collections.unmodifiableList(new ArrayList<>(this.routes.values()));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag routeList = new ListTag();
        for (BannerModSeaTradeExecutionRecord record : routes()) {
            routeList.add(record.toTag());
        }
        tag.put("Routes", routeList);
        return tag;
    }

    public static BannerModSeaTradeExecutionRuntime fromTag(CompoundTag tag) {
        BannerModSeaTradeExecutionRuntime runtime = new BannerModSeaTradeExecutionRuntime();
        List<BannerModSeaTradeExecutionRecord> records = new ArrayList<>();
        for (Tag entry : tag.getList("Routes", Tag.TAG_COMPOUND)) {
            records.add(BannerModSeaTradeExecutionRecord.fromTag((CompoundTag) entry));
        }
        runtime.restoreSnapshot(records);
        return runtime;
    }

    public void restoreSnapshot(Collection<BannerModSeaTradeExecutionRecord> records) {
        List<BannerModSeaTradeExecutionRecord> before = routes();
        this.routes.clear();
        if (records != null) {
            for (BannerModSeaTradeExecutionRecord record : records) {
                if (record != null) {
                    this.routes.put(record.routeId(), record);
                }
            }
        }
        if (!before.equals(routes())) {
            markDirty();
        }
    }

    private void markDirty() {
        this.dirtyListener.run();
    }

    private static Collection<Container> storageContainers(StorageArea storageArea) {
        if (storageArea == null) {
            return List.of();
        }
        storageArea.scanStorageBlocks();
        return storageArea.storageMap.values();
    }
}
