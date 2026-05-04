package com.talhanation.bannermod.society;

import net.minecraft.core.BlockPos;
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

public final class NpcHamletRuntime {
    private static final double JOIN_RADIUS_SQR = 48.0D * 48.0D;
    private static final String[] BASE_NAMES = {
            "Berezki",
            "Dubrava",
            "Sosnovka",
            "Kamenka",
            "Ozerki",
            "Ruchey",
            "Yarovo",
            "Lugovoe",
            "Prudki",
            "Borovik",
            "Veresk",
            "Polyanka",
            "Zalesye",
            "Khmel",
            "Rassvet",
            "Klyuchi"
    };

    private final Map<UUID, NpcHamletRecord> hamletsById = new LinkedHashMap<>();
    private final Map<UUID, UUID> hamletByHousehold = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> {
    };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> {
        } : dirtyListener;
    }

    public Optional<NpcHamletRecord> hamletFor(UUID hamletId) {
        if (hamletId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.hamletsById.get(hamletId));
    }

    public Optional<NpcHamletRecord> hamletForHousehold(UUID householdId) {
        if (householdId == null) {
            return Optional.empty();
        }
        return hamletFor(this.hamletByHousehold.get(householdId));
    }

    public List<NpcHamletRecord> hamletsForClaim(UUID claimUuid) {
        if (claimUuid == null) {
            return List.of();
        }
        List<NpcHamletRecord> matches = new ArrayList<>();
        for (NpcHamletRecord record : this.hamletsById.values()) {
            if (record != null && claimUuid.equals(record.claimUuid())) {
                matches.add(record);
            }
        }
        return matches;
    }

    public @Nullable NpcHamletRecord reconcileHousehold(UUID claimUuid,
                                                        UUID householdId,
                                                        @Nullable UUID homeBuildingUuid,
                                                        @Nullable BlockPos plotPos,
                                                        long gameTime) {
        if (householdId == null) {
            return null;
        }
        if (claimUuid == null || plotPos == null || homeBuildingUuid == null) {
            removeHousehold(householdId, gameTime);
            return null;
        }
        NpcHamletRecord current = hamletForHousehold(householdId).orElse(null);
        if (current != null && !claimUuid.equals(current.claimUuid())) {
            current = removeHouseholdInternal(householdId, gameTime);
        }

        NpcHamletRecord target = current;
        if (target == null) {
            target = findJoinTarget(claimUuid, plotPos, householdId).orElse(null);
        }

        boolean created = false;
        if (target == null) {
            target = NpcHamletRecord.create(
                    UUID.randomUUID(),
                    claimUuid,
                    householdId,
                    uniqueNameFor(claimUuid, generatedBaseName(claimUuid, householdId, plotPos), null),
                    plotPos,
                    homeBuildingUuid,
                    gameTime
            );
            created = true;
        } else {
            target = target.withHousehold(householdId, plotPos, homeBuildingUuid, gameTime);
        }

        NpcHamletRecord existing = this.hamletsById.get(target.hamletId());
        if (created || !target.equals(existing)) {
            store(target);
            markDirty();
        }
        return target;
    }

    public @Nullable NpcHamletRecord removeHousehold(UUID householdId, long gameTime) {
        if (householdId == null) {
            return null;
        }
        NpcHamletRecord updated = removeHouseholdInternal(householdId, gameTime);
        if (updated != null) {
            markDirty();
        }
        return updated;
    }

    public NpcHamletRecord register(UUID hamletId, long gameTime) {
        NpcHamletRecord existing = require(hamletId);
        NpcHamletRecord updated = existing.withStatus(NpcHamletStatus.REGISTERED, gameTime);
        if (!updated.equals(existing)) {
            store(updated);
            markDirty();
        }
        return updated;
    }

    public NpcHamletRecord rename(UUID hamletId, String name, long gameTime) {
        NpcHamletRecord existing = require(hamletId);
        String normalized = validateAndNormalizeName(name);
        ensureUniqueName(existing.claimUuid(), normalized, hamletId);
        NpcHamletRecord updated = existing.rename(normalized, gameTime);
        if (!updated.equals(existing)) {
            store(updated);
            markDirty();
        }
        return updated;
    }

    public @Nullable NpcHamletRecord nearestHamlet(UUID claimUuid,
                                                   BlockPos pos,
                                                   double maxDistance,
                                                   boolean includeAbandoned) {
        if (claimUuid == null || pos == null) {
            return null;
        }
        double bestDistSqr = Math.max(1.0D, maxDistance * maxDistance);
        NpcHamletRecord best = null;
        for (NpcHamletRecord record : this.hamletsById.values()) {
            if (record == null || !claimUuid.equals(record.claimUuid())) {
                continue;
            }
            if (!includeAbandoned && record.status() == NpcHamletStatus.ABANDONED) {
                continue;
            }
            double distSqr = record.nearestPlotDistanceSqr(pos);
            if (distSqr <= bestDistSqr) {
                bestDistSqr = distSqr;
                best = record;
            }
        }
        return best;
    }

    public boolean noteHostileAction(UUID hamletId, long gameTime, long cooldownTicks) {
        NpcHamletRecord existing = this.hamletsById.get(hamletId);
        if (existing == null) {
            return false;
        }
        long cooldown = Math.max(0L, cooldownTicks);
        if (cooldown > 0L && existing.lastHostileActionGameTime() > 0L
                && gameTime - existing.lastHostileActionGameTime() < cooldown) {
            return false;
        }
        NpcHamletRecord updated = existing.noteHostileAction(gameTime);
        if (!updated.equals(existing)) {
            store(updated);
            markDirty();
        }
        return true;
    }

    public List<NpcHamletRecord> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(this.hamletsById.values()));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag hamlets = new ListTag();
        for (NpcHamletRecord hamlet : snapshot()) {
            hamlets.add(hamlet.toTag());
        }
        tag.put("Hamlets", hamlets);
        return tag;
    }

    public static NpcHamletRuntime fromTag(CompoundTag tag) {
        NpcHamletRuntime runtime = new NpcHamletRuntime();
        List<NpcHamletRecord> hamlets = new ArrayList<>();
        for (Tag entry : tag.getList("Hamlets", Tag.TAG_COMPOUND)) {
            hamlets.add(NpcHamletRecord.fromTag((CompoundTag) entry));
        }
        runtime.restoreSnapshot(hamlets);
        return runtime;
    }

    public void restoreSnapshot(@Nullable Collection<NpcHamletRecord> hamlets) {
        this.hamletsById.clear();
        this.hamletByHousehold.clear();
        if (hamlets != null) {
            for (NpcHamletRecord hamlet : hamlets) {
                if (hamlet != null && hamlet.hamletId() != null) {
                    store(hamlet);
                }
            }
        }
    }

    private Optional<NpcHamletRecord> findJoinTarget(UUID claimUuid, BlockPos plotPos, UUID householdId) {
        NpcHamletRecord best = null;
        double bestDistSqr = JOIN_RADIUS_SQR;
        for (NpcHamletRecord record : this.hamletsById.values()) {
            if (record == null
                    || !claimUuid.equals(record.claimUuid())
                    || record.status() == NpcHamletStatus.ABANDONED
                    || record.hasHousehold(householdId)) {
                continue;
            }
            double distSqr = record.nearestPlotDistanceSqr(plotPos);
            if (distSqr <= bestDistSqr) {
                bestDistSqr = distSqr;
                best = record;
            }
        }
        return Optional.ofNullable(best);
    }

    private @Nullable NpcHamletRecord removeHouseholdInternal(UUID householdId, long gameTime) {
        UUID hamletId = this.hamletByHousehold.remove(householdId);
        if (hamletId == null) {
            return null;
        }
        NpcHamletRecord existing = this.hamletsById.get(hamletId);
        if (existing == null) {
            return null;
        }
        NpcHamletRecord updated = existing.withoutHousehold(householdId, gameTime);
        store(updated);
        return updated;
    }

    private NpcHamletRecord require(UUID hamletId) {
        return hamletFor(hamletId).orElseThrow(() -> new IllegalArgumentException("No hamlet exists for id " + hamletId));
    }

    private void ensureUniqueName(UUID claimUuid, String name, @Nullable UUID currentHamletId) {
        for (NpcHamletRecord record : this.hamletsById.values()) {
            if (record == null || !claimUuid.equals(record.claimUuid())) {
                continue;
            }
            if (currentHamletId != null && currentHamletId.equals(record.hamletId())) {
                continue;
            }
            if (record.name().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("duplicate_name");
            }
        }
    }

    private String uniqueNameFor(UUID claimUuid, String preferred, @Nullable UUID currentHamletId) {
        String base = validateAndNormalizeName(preferred);
        String candidate = base;
        int suffix = 2;
        while (hasName(claimUuid, candidate, currentHamletId)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean hasName(UUID claimUuid, String name, @Nullable UUID currentHamletId) {
        for (NpcHamletRecord record : this.hamletsById.values()) {
            if (record == null || !claimUuid.equals(record.claimUuid())) {
                continue;
            }
            if (currentHamletId != null && currentHamletId.equals(record.hamletId())) {
                continue;
            }
            if (record.name().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String generatedBaseName(UUID claimUuid, UUID householdId, BlockPos plotPos) {
        int hash = 17;
        hash = 31 * hash + claimUuid.hashCode();
        hash = 31 * hash + householdId.hashCode();
        hash = 31 * hash + plotPos.hashCode();
        return BASE_NAMES[Math.floorMod(hash, BASE_NAMES.length)];
    }

    public static String validateAndNormalizeName(String rawName) {
        String normalized = rawName == null ? "" : rawName.trim().replaceAll("\\s+", " ");
        if (normalized.length() < 3) {
            throw new IllegalArgumentException("name_too_short");
        }
        if (normalized.length() > 32) {
            throw new IllegalArgumentException("name_too_long");
        }
        return normalized;
    }

    private void store(NpcHamletRecord hamlet) {
        this.hamletsById.put(hamlet.hamletId(), hamlet);
        this.hamletByHousehold.entrySet().removeIf(entry -> entry.getValue().equals(hamlet.hamletId()));
        for (NpcHamletHouseholdEntry entry : hamlet.householdEntries()) {
            this.hamletByHousehold.put(entry.householdId(), hamlet.hamletId());
        }
    }

    private void markDirty() {
        this.dirtyListener.run();
    }
}
