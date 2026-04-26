package com.talhanation.bannermod.entity.citizen;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Per-level chunk-indexed registry of loaded {@link CitizenEntity} UUIDs. */
public final class CitizenIndex {
    private static final CitizenIndex INSTANCE = new CitizenIndex();

    private final Map<ResourceKey<Level>, Map<ChunkPos, Set<UUID>>> byLevel = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Map<UUID, ChunkPos>> chunksByCitizen = new ConcurrentHashMap<>();

    private CitizenIndex() {
    }

    public static CitizenIndex instance() {
        return INSTANCE;
    }

    public void onEntityJoin(Entity entity) {
        if (!(entity instanceof CitizenEntity)) return;
        if (entity.level() == null || entity.level().isClientSide()) return;
        addOrMove(entity);
    }

    public void onCitizenTick(CitizenEntity citizen) {
        if (citizen == null || citizen.level() == null || citizen.level().isClientSide()) return;
        addOrMove(citizen);
    }

    public void onEntityLeave(Entity entity) {
        if (!(entity instanceof CitizenEntity)) return;
        if (entity.level() == null || entity.level().isClientSide()) return;
        ResourceKey<Level> dimension = entity.level().dimension();
        Map<ChunkPos, Set<UUID>> chunks = byLevel.get(dimension);
        if (chunks == null) return;

        ChunkPos oldChunk = null;
        Map<UUID, ChunkPos> citizenChunks = chunksByCitizen.get(dimension);
        if (citizenChunks != null) {
            oldChunk = citizenChunks.remove(entity.getUUID());
        }
        removeFromChunk(chunks, oldChunk == null ? entity.chunkPosition() : oldChunk, entity.getUUID());
        for (Set<UUID> other : chunks.values()) {
            other.remove(entity.getUUID());
        }
    }

    private void addOrMove(Entity entity) {
        ResourceKey<Level> dimension = entity.level().dimension();
        ChunkPos chunkPosition = entity.chunkPosition();
        UUID uuid = entity.getUUID();
        Map<ChunkPos, Set<UUID>> chunks = byLevel.computeIfAbsent(dimension, key -> new ConcurrentHashMap<>());
        Map<UUID, ChunkPos> citizenChunks = chunksByCitizen.computeIfAbsent(dimension, key -> new ConcurrentHashMap<>());
        ChunkPos previousChunk = citizenChunks.put(uuid, chunkPosition);
        if (previousChunk != null && !previousChunk.equals(chunkPosition)) {
            removeFromChunk(chunks, previousChunk, uuid);
        }
        chunks.computeIfAbsent(chunkPosition, chunk -> ConcurrentHashMap.newKeySet()).add(uuid);
    }

    private static void removeFromChunk(Map<ChunkPos, Set<UUID>> chunks, ChunkPos chunkPosition, UUID uuid) {
        if (chunkPosition == null) return;
        Set<UUID> uuids = chunks.get(chunkPosition);
        if (uuids == null) return;
        uuids.remove(uuid);
        if (uuids.isEmpty()) {
            chunks.remove(chunkPosition);
        }
    }

    public Optional<List<CitizenEntity>> queryInClaim(ServerLevel level, RecruitsClaim claim) {
        RuntimeProfilingCounters.increment("citizen.index.claim_queries");
        if (level == null || claim == null) {
            RuntimeProfilingCounters.increment("citizen.index.fallbacks");
            return Optional.empty();
        }
        Map<ChunkPos, Set<UUID>> chunks = byLevel.get(level.dimension());
        if (chunks == null) {
            RuntimeProfilingCounters.increment("citizen.index.misses");
            RuntimeProfilingCounters.increment("citizen.index.fallbacks");
            return Optional.empty();
        }

        List<CitizenEntity> citizens = new ArrayList<>();
        for (ChunkPos chunkPosition : claim.getClaimedChunks()) {
            Set<UUID> uuids = chunks.get(chunkPosition);
            if (uuids == null) {
                RuntimeProfilingCounters.increment("citizen.index.misses");
                continue;
            }
            RuntimeProfilingCounters.increment("citizen.index.hits");
            for (UUID uuid : uuids) {
                Entity entity = level.getEntity(uuid);
                if (!(entity instanceof CitizenEntity citizen)) continue;
                if (!citizen.isAlive() || !claim.containsChunk(citizen.chunkPosition())) continue;
                citizens.add(citizen);
            }
        }
        RuntimeProfilingCounters.add("citizen.index.indexed_candidates", citizens.size());
        return Optional.of(List.copyOf(citizens));
    }

    public void clear(ResourceKey<Level> dimension) {
        if (dimension == null) return;
        byLevel.remove(dimension);
        chunksByCitizen.remove(dimension);
    }

    public void clearAllForTest() {
        byLevel.clear();
        chunksByCitizen.clear();
    }
}
