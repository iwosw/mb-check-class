package com.talhanation.bannermod.util;

import com.talhanation.bannermod.entity.military.RecruitIndex;
import net.minecraft.nbt.CompoundTag;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class RuntimeProfilingCounters {
    private static final ConcurrentHashMap<String, LongAdder> COUNTERS = new ConcurrentHashMap<>();

    private RuntimeProfilingCounters() {
    }

    public static void increment(String key) {
        add(key, 1L);
    }

    public static void add(String key, long amount) {
        if (key == null || key.isBlank() || amount == 0L) return;
        COUNTERS.computeIfAbsent(key, ignored -> new LongAdder()).add(amount);
    }

    public static void recordNbtPacket(String keyPrefix, CompoundTag tag) {
        if (keyPrefix == null || keyPrefix.isBlank()) return;
        increment(keyPrefix + ".executions");
        if (tag != null) {
            add(keyPrefix + ".nbt_chars", tag.toString().length());
        }
    }

    public static void recordBatch(String keyPrefix, int processedItems, int totalItems, long durationNanos, boolean completed) {
        if (keyPrefix == null || keyPrefix.isBlank()) return;
        increment(keyPrefix + ".executions");
        add(keyPrefix + ".items", Math.max(0, processedItems));
        add(keyPrefix + ".total_items", Math.max(0, totalItems));
        add(keyPrefix + ".duration_nanos", Math.max(0L, durationNanos));
        if (completed) {
            increment(keyPrefix + ".completed");
        }
    }

    public static Map<String, Long> snapshot() {
        Map<String, Long> values = new TreeMap<>();
        for (Map.Entry<String, LongAdder> entry : COUNTERS.entrySet()) {
            values.put(entry.getKey(), entry.getValue().sum());
        }
        addRecruitIndexCounters(values, RecruitIndex.instance().snapshot());
        return Collections.unmodifiableMap(values);
    }

    public static int reset() {
        int clearedCounters = snapshot().size();
        COUNTERS.clear();
        RecruitIndex.instance().resetCounters();
        return clearedCounters;
    }

    private static void addRecruitIndexCounters(Map<String, Long> values, RecruitIndex.Snapshot snapshot) {
        putIfNonZero(values, "recruit.index.uuid_hits", snapshot.uuidHits());
        putIfNonZero(values, "recruit.index.uuid_misses", snapshot.uuidMisses());
        putIfNonZero(values, "recruit.index.group_queries", snapshot.groupQueries());
        putIfNonZero(values, "recruit.index.indexed_candidates", snapshot.indexedCandidates());
        putIfNonZero(values, "recruit.index.fallbacks", snapshot.fallbacks());
    }

    private static void putIfNonZero(Map<String, Long> values, String key, long value) {
        if (value != 0L) {
            values.put(key, value);
        }
    }
}
