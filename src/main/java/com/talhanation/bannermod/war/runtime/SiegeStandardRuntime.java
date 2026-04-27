package com.talhanation.bannermod.war.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SiegeStandardRuntime {
    private final Map<UUID, SiegeStandardRecord> standardsById = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> { };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> { } : dirtyListener;
    }

    public Optional<SiegeStandardRecord> place(UUID warId,
                                               UUID sidePoliticalEntityId,
                                               BlockPos pos,
                                               int radius,
                                               long gameTime) {
        if (warId == null || sidePoliticalEntityId == null || pos == null) {
            return Optional.empty();
        }
        SiegeStandardRecord record = new SiegeStandardRecord(
                UUID.randomUUID(),
                warId,
                sidePoliticalEntityId,
                pos.immutable(),
                radius,
                gameTime
        );
        standardsById.put(record.id(), record);
        dirtyListener.run();
        return Optional.of(record);
    }

    public boolean remove(UUID id) {
        boolean removed = standardsById.remove(id) != null;
        if (removed) {
            dirtyListener.run();
        }
        return removed;
    }

    /**
     * Apply a single damage event to the standard's control pool. Returns the policy's
     * {@link com.talhanation.bannermod.combat.SiegeObjectivePolicy.DamageOutcome} so the
     * caller can write exactly one destruction-audit row when the pool drops to zero.
     *
     * <p>The runtime updates the stored record in place; persistence is the
     * {@link SiegeStandardSavedData} layer's job and runs on the dirty listener tick.</p>
     */
    public java.util.Optional<com.talhanation.bannermod.combat.SiegeObjectivePolicy.DamageOutcome>
    applyDamage(UUID id, int damage) {
        SiegeStandardRecord existing = standardsById.get(id);
        if (existing == null) {
            return java.util.Optional.empty();
        }
        com.talhanation.bannermod.combat.SiegeObjectivePolicy.DamageOutcome outcome =
                com.talhanation.bannermod.combat.SiegeObjectivePolicy.applyDamage(
                        existing.controlPool(), damage, existing.maxControlPool());
        SiegeStandardRecord updated = existing.withControlPool(outcome.controlAfter());
        standardsById.put(id, updated);
        dirtyListener.run();
        return java.util.Optional.of(outcome);
    }

    /** Return the standard whose pos matches {@code pos}, or empty if none. */
    public java.util.Optional<SiegeStandardRecord> byPos(BlockPos pos) {
        if (pos == null) return java.util.Optional.empty();
        for (SiegeStandardRecord record : standardsById.values()) {
            if (record.pos().equals(pos)) {
                return java.util.Optional.of(record);
            }
        }
        return java.util.Optional.empty();
    }

    public Optional<SiegeStandardRecord> byId(UUID id) {
        return Optional.ofNullable(standardsById.get(id));
    }

    public Collection<SiegeStandardRecord> all() {
        return List.copyOf(standardsById.values());
    }

    public Collection<SiegeStandardRecord> forWar(UUID warId) {
        if (warId == null) {
            return List.of();
        }
        List<SiegeStandardRecord> matching = new ArrayList<>();
        for (SiegeStandardRecord record : standardsById.values()) {
            if (warId.equals(record.warId())) {
                matching.add(record);
            }
        }
        return matching;
    }

    public boolean isInsideAnyZone(BlockPos point, Collection<UUID> activeWarIds) {
        if (point == null || activeWarIds == null || activeWarIds.isEmpty()) {
            return false;
        }
        for (SiegeStandardRecord record : standardsById.values()) {
            if (activeWarIds.contains(record.warId()) && record.contains(point)) {
                return true;
            }
        }
        return false;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (SiegeStandardRecord record : standardsById.values()) {
            list.add(record.toTag());
        }
        tag.put("SiegeStandards", list);
        return tag;
    }

    public static SiegeStandardRuntime fromTag(CompoundTag tag) {
        SiegeStandardRuntime runtime = new SiegeStandardRuntime();
        ListTag list = tag.getList("SiegeStandards", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            SiegeStandardRecord record = SiegeStandardRecord.fromTag(list.getCompound(i));
            runtime.standardsById.put(record.id(), record);
        }
        return runtime;
    }
}
