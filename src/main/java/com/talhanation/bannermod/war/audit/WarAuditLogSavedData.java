package com.talhanation.bannermod.war.audit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarAuditLogSavedData extends SavedData {
    private static final String FILE_ID = "bannermodWarAuditLog";

    private final List<WarAuditEntry> entries = new ArrayList<>();

    public WarAuditLogSavedData() {
    }

    private WarAuditLogSavedData(List<WarAuditEntry> entries) {
        this.entries.addAll(entries);
    }

    public static WarAuditLogSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                WarAuditLogSavedData::load,
                WarAuditLogSavedData::new,
                FILE_ID
        );
    }

    public static WarAuditLogSavedData load(CompoundTag tag) {
        List<WarAuditEntry> loaded = new ArrayList<>();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            loaded.add(WarAuditEntry.fromTag(list.getCompound(i)));
        }
        return new WarAuditLogSavedData(loaded);
    }

    public WarAuditEntry append(UUID warId, String type, String detail, long gameTime) {
        WarAuditEntry entry = new WarAuditEntry(UUID.randomUUID(), warId, type, detail, gameTime);
        entries.add(entry);
        setDirty();
        return entry;
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

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (WarAuditEntry entry : entries) {
            list.add(entry.toTag());
        }
        tag.put("Entries", list);
        return tag;
    }
}
