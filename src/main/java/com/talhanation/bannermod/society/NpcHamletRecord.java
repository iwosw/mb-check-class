package com.talhanation.bannermod.society;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record NpcHamletRecord(
        UUID hamletId,
        UUID claimUuid,
        UUID founderHouseholdId,
        String name,
        BlockPos anchorPos,
        NpcHamletStatus status,
        List<NpcHamletHouseholdEntry> householdEntries,
        long lastHostileActionGameTime,
        long version,
        long lastUpdatedGameTime
) {
    public NpcHamletRecord {
        if (hamletId == null) {
            throw new IllegalArgumentException("hamletId must not be null");
        }
        if (claimUuid == null) {
            throw new IllegalArgumentException("claimUuid must not be null");
        }
        if (founderHouseholdId == null) {
            throw new IllegalArgumentException("founderHouseholdId must not be null");
        }
        name = sanitizeName(name);
        householdEntries = sanitizeEntries(householdEntries);
        anchorPos = anchorPos == null ? computeAnchor(householdEntries, BlockPos.ZERO) : anchorPos;
        status = status == null ? NpcHamletStatus.INFORMAL : status;
        version = Math.max(1L, version);
    }

    public static NpcHamletRecord create(UUID hamletId,
                                         UUID claimUuid,
                                         UUID founderHouseholdId,
                                         String name,
                                         BlockPos plotPos,
                                         @javax.annotation.Nullable UUID homeBuildingUuid,
                                         long gameTime) {
        return new NpcHamletRecord(
                hamletId,
                claimUuid,
                founderHouseholdId,
                name,
                plotPos,
                NpcHamletStatus.INFORMAL,
                List.of(new NpcHamletHouseholdEntry(founderHouseholdId, plotPos, homeBuildingUuid)),
                0L,
                1L,
                gameTime
        );
    }

    public boolean hasHousehold(UUID householdId) {
        return householdId != null && householdEntry(householdId) != null;
    }

    public boolean isInhabited() {
        return !this.householdEntries.isEmpty() && this.status != NpcHamletStatus.ABANDONED;
    }

    public int householdCount() {
        return this.householdEntries.size();
    }

    public double distanceToAnchorSqr(BlockPos pos) {
        return pos == null ? Double.POSITIVE_INFINITY : this.anchorPos.distSqr(pos);
    }

    public double nearestPlotDistanceSqr(BlockPos pos) {
        if (pos == null || this.householdEntries.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        double best = Double.POSITIVE_INFINITY;
        for (NpcHamletHouseholdEntry entry : this.householdEntries) {
            best = Math.min(best, entry.plotPos().distSqr(pos));
        }
        return best;
    }

    public NpcHamletRecord withHousehold(UUID householdId,
                                         BlockPos plotPos,
                                         @javax.annotation.Nullable UUID homeBuildingUuid,
                                         long gameTime) {
        if (householdId == null || plotPos == null) {
            return this;
        }
        List<NpcHamletHouseholdEntry> updatedEntries = new ArrayList<>(this.householdEntries);
        NpcHamletHouseholdEntry candidate = new NpcHamletHouseholdEntry(householdId, plotPos, homeBuildingUuid);
        boolean replaced = false;
        for (int i = 0; i < updatedEntries.size(); i++) {
            NpcHamletHouseholdEntry existing = updatedEntries.get(i);
            if (existing.householdId().equals(householdId)) {
                if (existing.equals(candidate)) {
                    return this;
                }
                updatedEntries.set(i, candidate);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            updatedEntries.add(candidate);
        }
        return new NpcHamletRecord(
                this.hamletId,
                this.claimUuid,
                this.founderHouseholdId,
                this.name,
                computeAnchor(updatedEntries, plotPos),
                this.status == NpcHamletStatus.ABANDONED ? NpcHamletStatus.INFORMAL : this.status,
                updatedEntries,
                this.lastHostileActionGameTime,
                this.version + 1L,
                gameTime
        );
    }

    public NpcHamletRecord withoutHousehold(UUID householdId, long gameTime) {
        if (householdId == null || !hasHousehold(householdId)) {
            return this;
        }
        List<NpcHamletHouseholdEntry> updatedEntries = new ArrayList<>(this.householdEntries);
        updatedEntries.removeIf(entry -> entry.householdId().equals(householdId));
        UUID updatedFounder = this.founderHouseholdId.equals(householdId) && !updatedEntries.isEmpty()
                ? updatedEntries.getFirst().householdId()
                : this.founderHouseholdId;
        NpcHamletStatus updatedStatus = updatedEntries.isEmpty() ? NpcHamletStatus.ABANDONED : this.status;
        return new NpcHamletRecord(
                this.hamletId,
                this.claimUuid,
                updatedFounder,
                this.name,
                computeAnchor(updatedEntries, this.anchorPos),
                updatedStatus,
                updatedEntries,
                this.lastHostileActionGameTime,
                this.version + 1L,
                gameTime
        );
    }

    public NpcHamletRecord withStatus(NpcHamletStatus status, long gameTime) {
        NpcHamletStatus normalized = status == null ? NpcHamletStatus.INFORMAL : status;
        if (this.status == normalized) {
            return this;
        }
        return new NpcHamletRecord(
                this.hamletId,
                this.claimUuid,
                this.founderHouseholdId,
                this.name,
                this.anchorPos,
                normalized,
                this.householdEntries,
                this.lastHostileActionGameTime,
                this.version + 1L,
                gameTime
        );
    }

    public NpcHamletRecord rename(String name, long gameTime) {
        String normalized = sanitizeName(name);
        if (this.name.equals(normalized)) {
            return this;
        }
        return new NpcHamletRecord(
                this.hamletId,
                this.claimUuid,
                this.founderHouseholdId,
                normalized,
                this.anchorPos,
                this.status,
                this.householdEntries,
                this.lastHostileActionGameTime,
                this.version + 1L,
                gameTime
        );
    }

    public NpcHamletRecord noteHostileAction(long gameTime) {
        if (this.lastHostileActionGameTime == gameTime) {
            return this;
        }
        return new NpcHamletRecord(
                this.hamletId,
                this.claimUuid,
                this.founderHouseholdId,
                this.name,
                this.anchorPos,
                this.status,
                this.householdEntries,
                gameTime,
                this.version + 1L,
                gameTime
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("HamletId", this.hamletId);
        tag.putUUID("ClaimUuid", this.claimUuid);
        tag.putUUID("FounderHouseholdId", this.founderHouseholdId);
        tag.putString("Name", this.name);
        tag.putLong("AnchorPos", this.anchorPos.asLong());
        tag.putString("Status", this.status.name());
        ListTag households = new ListTag();
        for (NpcHamletHouseholdEntry entry : this.householdEntries) {
            households.add(entry.toTag());
        }
        tag.put("Households", households);
        tag.putLong("LastHostileActionGameTime", this.lastHostileActionGameTime);
        tag.putLong("Version", this.version);
        tag.putLong("LastUpdatedGameTime", this.lastUpdatedGameTime);
        return tag;
    }

    public static NpcHamletRecord fromTag(CompoundTag tag) {
        List<NpcHamletHouseholdEntry> entries = new ArrayList<>();
        for (Tag entryTag : tag.getList("Households", Tag.TAG_COMPOUND)) {
            entries.add(NpcHamletHouseholdEntry.fromTag((CompoundTag) entryTag));
        }
        UUID founderHouseholdId = tag.contains("FounderHouseholdId")
                ? tag.getUUID("FounderHouseholdId")
                : (entries.isEmpty() ? new UUID(0L, 0L) : entries.getFirst().householdId());
        return new NpcHamletRecord(
                tag.getUUID("HamletId"),
                tag.getUUID("ClaimUuid"),
                founderHouseholdId,
                tag.getString("Name"),
                tag.contains("AnchorPos") ? BlockPos.of(tag.getLong("AnchorPos")) : computeAnchor(entries, BlockPos.ZERO),
                NpcHamletStatus.fromName(tag.getString("Status")),
                entries,
                tag.getLong("LastHostileActionGameTime"),
                Math.max(1L, tag.getLong("Version")),
                tag.getLong("LastUpdatedGameTime")
        );
    }

    private NpcHamletHouseholdEntry householdEntry(UUID householdId) {
        if (householdId == null) {
            return null;
        }
        for (NpcHamletHouseholdEntry entry : this.householdEntries) {
            if (householdId.equals(entry.householdId())) {
                return entry;
            }
        }
        return null;
    }

    private static String sanitizeName(String name) {
        String normalized = name == null ? "" : name.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? "Outfield" : normalized;
    }

    private static List<NpcHamletHouseholdEntry> sanitizeEntries(Collection<NpcHamletHouseholdEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        Map<UUID, NpcHamletHouseholdEntry> ordered = new LinkedHashMap<>();
        for (NpcHamletHouseholdEntry entry : entries) {
            if (entry != null && entry.householdId() != null && entry.plotPos() != null) {
                ordered.put(entry.householdId(), entry);
            }
        }
        return List.copyOf(ordered.values());
    }

    private static BlockPos computeAnchor(Collection<NpcHamletHouseholdEntry> entries, BlockPos fallback) {
        if (entries == null || entries.isEmpty()) {
            return fallback == null ? BlockPos.ZERO : fallback;
        }
        long sumX = 0L;
        long sumY = 0L;
        long sumZ = 0L;
        int count = 0;
        for (NpcHamletHouseholdEntry entry : entries) {
            if (entry == null || entry.plotPos() == null) {
                continue;
            }
            BlockPos pos = entry.plotPos();
            sumX += pos.getX();
            sumY += pos.getY();
            sumZ += pos.getZ();
            count++;
        }
        if (count <= 0) {
            return fallback == null ? BlockPos.ZERO : fallback;
        }
        return new BlockPos(
                Math.toIntExact(Math.round(sumX / (double) count)),
                Math.toIntExact(Math.round(sumY / (double) count)),
                Math.toIntExact(Math.round(sumZ / (double) count))
        );
    }
}
