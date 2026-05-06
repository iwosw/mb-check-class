package com.talhanation.bannermod.war.audit;

import com.talhanation.bannermod.persistence.SavedDataVersioning;
import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WarAuditLogSavedData extends SavedData {
    private static final String FILE_ID = "bannermodWarAuditLog";
    private static final SavedData.Factory<WarAuditLogSavedData> FACTORY = new SavedData.Factory<>(WarAuditLogSavedData::new, WarAuditLogSavedData::load);

    private static final int CURRENT_VERSION = 1;
    /**
     * Backed by an {@link ArrayDeque} so oldest-first eviction in {@link #append} is O(1)
     * and full-list iteration / NBT serialization stays linear in surviving entries only.
     */
    private final Deque<WarAuditEntry> entries = new ArrayDeque<>();

    public WarAuditLogSavedData() {
    }

    private WarAuditLogSavedData(List<WarAuditEntry> entries) {
        this.entries.addAll(entries);
        enforceCap();
    }

    public static WarAuditLogSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static WarAuditLogSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.migrate(tag, CURRENT_VERSION, "WarAuditLogSavedData");
        List<WarAuditEntry> loaded = new ArrayList<>();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            loaded.add(WarAuditEntry.fromTag(list.getCompound(i)));
        }
        return new WarAuditLogSavedData(loaded);
    }

    public WarAuditEntry append(UUID warId, String type, String detail, long gameTime) {
        WarAuditEntry entry = new WarAuditEntry(UUID.randomUUID(), warId, type, detail, gameTime);
        entries.addLast(entry);
        enforceCap();
        setDirty();
        return entry;
    }

    /**
     * Drop entries belonging to {@code resolvedWarIds} that are older than
     * {@code currentGameTime - retentionTicks}. Entries with a {@code null} warId are kept
     * (system events). Returns the number of entries removed.
     */
    public int pruneResolved(Collection<UUID> resolvedWarIds, long currentGameTime, long retentionTicks) {
        if (resolvedWarIds == null || resolvedWarIds.isEmpty() || retentionTicks <= 0L) {
            return 0;
        }
        Set<UUID> resolvedSet = Set.copyOf(resolvedWarIds);
        long cutoff = currentGameTime - retentionTicks;
        int removed = 0;
        var iter = entries.iterator();
        while (iter.hasNext()) {
            WarAuditEntry entry = iter.next();
            UUID warId = entry.warId();
            if (warId != null && resolvedSet.contains(warId) && entry.gameTime() < cutoff) {
                iter.remove();
                removed++;
            }
        }
        if (removed > 0) {
            setDirty();
        }
        return removed;
    }

    public List<WarAuditEntry> all() {
        return List.copyOf(entries);
    }

    public List<WarAuditEntry> forWar(UUID warId) {
        if (warId == null) {
            return List.of();
        }
        List<WarAuditEntry> filtered = new ArrayList<>();
        for (WarAuditEntry entry : entries) {
            if (warId.equals(entry.warId())) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public int size() {
        return entries.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.putVersion(tag, CURRENT_VERSION);
        ListTag list = new ListTag();
        for (WarAuditEntry entry : entries) {
            list.add(entry.toTag());
        }
        tag.put("Entries", list);
        return tag;
    }

    private void enforceCap() {
        while (entries.size() > WarRetentionPolicy.MAX_AUDIT_ENTRIES) {
            entries.pollFirst();
        }
    }
}
