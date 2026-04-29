package com.talhanation.bannermod.shared.logistics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

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
}
