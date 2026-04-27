package com.talhanation.bannermod.war.registry;

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

public class PoliticalRegistryRuntime {
    private final Map<UUID, PoliticalEntityRecord> recordsById = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> { };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> { } : dirtyListener;
    }

    public PoliticalRegistryValidation.Result canCreate(String name, UUID leaderUuid) {
        return PoliticalRegistryValidation.validateCreate(name, leaderUuid, recordsById.values());
    }

    public Optional<PoliticalEntityRecord> create(String name,
                                                  UUID leaderUuid,
                                                  BlockPos capitalPos,
                                                  String color,
                                                  String charter,
                                                  String ideology,
                                                  String homeRegion,
                                                  long gameTime) {
        PoliticalRegistryValidation.Result validation = canCreate(name, leaderUuid);
        if (!validation.valid() || capitalPos == null) {
            return Optional.empty();
        }
        PoliticalEntityRecord record = new PoliticalEntityRecord(
                UUID.randomUUID(),
                PoliticalRegistryValidation.normalizeName(name),
                PoliticalEntityStatus.SETTLEMENT,
                leaderUuid,
                List.of(),
                capitalPos.immutable(),
                color,
                charter,
                ideology,
                homeRegion,
                gameTime
        );
        recordsById.put(record.id(), record);
        dirtyListener.run();
        return Optional.of(record);
    }

    public Optional<PoliticalEntityRecord> byId(UUID id) {
        return Optional.ofNullable(recordsById.get(id));
    }

    public Optional<PoliticalEntityRecord> byName(String name) {
        String normalized = PoliticalRegistryValidation.normalizeName(name);
        return recordsById.values().stream()
                .filter(record -> record.name().equalsIgnoreCase(normalized))
                .findFirst();
    }

    public Collection<PoliticalEntityRecord> all() {
        return List.copyOf(recordsById.values());
    }

    public boolean updateStatus(UUID id, PoliticalEntityStatus status) {
        PoliticalEntityRecord record = recordsById.get(id);
        if (record == null || status == null) {
            return false;
        }
        recordsById.put(id, record.withStatus(status));
        dirtyListener.run();
        return true;
    }

    public boolean updateCapital(UUID id, BlockPos pos) {
        PoliticalEntityRecord record = recordsById.get(id);
        if (record == null || pos == null) {
            return false;
        }
        recordsById.put(id, record.withCapital(pos));
        dirtyListener.run();
        return true;
    }

    public PoliticalRegistryValidation.Result canRename(UUID id, String newName) {
        if (id == null || !recordsById.containsKey(id)) {
            return PoliticalRegistryValidation.Result.invalid("entity_not_found");
        }
        return PoliticalRegistryValidation.validateRename(newName, id, recordsById.values());
    }

    public boolean updateName(UUID id, String newName) {
        PoliticalEntityRecord record = recordsById.get(id);
        if (record == null) {
            return false;
        }
        PoliticalRegistryValidation.Result validation =
                PoliticalRegistryValidation.validateRename(newName, id, recordsById.values());
        if (!validation.valid()) {
            return false;
        }
        String normalized = PoliticalRegistryValidation.normalizeName(newName);
        if (normalized.equals(record.name())) {
            return true;
        }
        recordsById.put(id, record.withName(normalized));
        dirtyListener.run();
        return true;
    }

    public boolean updateGovernmentForm(UUID id, GovernmentForm newForm) {
        PoliticalEntityRecord record = recordsById.get(id);
        if (record == null || newForm == null) {
            return false;
        }
        if (record.governmentForm() == newForm) {
            return true;
        }
        recordsById.put(id, record.withGovernmentForm(newForm));
        dirtyListener.run();
        return true;
    }

    public boolean addCoLeader(UUID id, UUID coLeaderUuid) {
        PoliticalEntityRecord record = recordsById.get(id);
        if (record == null || coLeaderUuid == null || coLeaderUuid.equals(record.leaderUuid())) {
            return false;
        }
        List<UUID> coLeaders = new ArrayList<>(record.coLeaderUuids());
        if (coLeaders.contains(coLeaderUuid)) {
            return false;
        }
        coLeaders.add(coLeaderUuid);
        recordsById.put(id, record.withCoLeaders(coLeaders));
        dirtyListener.run();
        return true;
    }

    public boolean removeCoLeader(UUID id, UUID coLeaderUuid) {
        PoliticalEntityRecord record = recordsById.get(id);
        if (record == null || coLeaderUuid == null) {
            return false;
        }
        List<UUID> coLeaders = new ArrayList<>(record.coLeaderUuids());
        if (!coLeaders.remove(coLeaderUuid)) {
            return false;
        }
        recordsById.put(id, record.withCoLeaders(coLeaders));
        dirtyListener.run();
        return true;
    }

    /**
     * Update the political entity's color string. Validation runs through
     * {@link PoliticalRegistryValidation#validateColor}; on invalid input the runtime is
     * unchanged and {@code false} is returned. Identical color is a successful no-op so the
     * UI can re-issue safely without spurious dirty churn.
     */
    public boolean updateColor(UUID id, String newColor) {
        PoliticalEntityRecord record = recordsById.get(id);
        if (record == null) {
            return false;
        }
        PoliticalRegistryValidation.Result validation = PoliticalRegistryValidation.validateColor(newColor);
        if (!validation.valid()) {
            return false;
        }
        String normalized = PoliticalRegistryValidation.normalizeColor(newColor);
        if (normalized.equals(record.color())) {
            return true;
        }
        recordsById.put(id, record.withColor(normalized));
        dirtyListener.run();
        return true;
    }

    /**
     * Update the charter free-text. Validation runs through
     * {@link PoliticalRegistryValidation#validateCharter} (length cap only). Identical charter
     * is a no-op.
     */
    public boolean updateCharter(UUID id, String newCharter) {
        PoliticalEntityRecord record = recordsById.get(id);
        if (record == null) {
            return false;
        }
        PoliticalRegistryValidation.Result validation = PoliticalRegistryValidation.validateCharter(newCharter);
        if (!validation.valid()) {
            return false;
        }
        String normalized = PoliticalRegistryValidation.normalizeCharter(newCharter);
        if (normalized.equals(record.charter())) {
            return true;
        }
        recordsById.put(id, record.withCharter(normalized));
        dirtyListener.run();
        return true;
    }

    public Optional<PoliticalEntityRecord> byNameOrIdFragment(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            UUID id = UUID.fromString(token);
            Optional<PoliticalEntityRecord> direct = byId(id);
            if (direct.isPresent()) {
                return direct;
            }
        } catch (IllegalArgumentException ignored) {
            // fall through to fragment / name lookup
        }
        String lower = token.toLowerCase(java.util.Locale.ROOT);
        for (PoliticalEntityRecord record : recordsById.values()) {
            if (record.id().toString().startsWith(lower)) {
                return Optional.of(record);
            }
        }
        return byName(token);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag records = new ListTag();
        for (PoliticalEntityRecord record : recordsById.values()) {
            records.add(record.toTag());
        }
        tag.put("PoliticalEntities", records);
        return tag;
    }

    public static PoliticalRegistryRuntime fromTag(CompoundTag tag) {
        PoliticalRegistryRuntime runtime = new PoliticalRegistryRuntime();
        ListTag records = tag.getList("PoliticalEntities", Tag.TAG_COMPOUND);
        for (int i = 0; i < records.size(); i++) {
            PoliticalEntityRecord record = PoliticalEntityRecord.fromTag(records.getCompound(i));
            runtime.recordsById.put(record.id(), record);
        }
        return runtime;
    }

    public void clearForTest() {
        recordsById.clear();
        dirtyListener.run();
    }
}
