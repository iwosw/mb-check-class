package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.war.registry.PoliticalMembership;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.util.Objects;
import java.util.UUID;

/**
 * Production probe that reads the objective chunk's entities from a {@link ServerLevel} and
 * counts each one against the rebel/occupier political entities. The full chunk is scanned
 * across the entire build height so a defender camped on a rooftop or below an obstruction
 * still counts as holding the objective.
 *
 * <p>Counted actors:
 *
 * <ul>
 *   <li>Online players whose UUID maps to the rebel or occupier political entity through
 *       {@link PoliticalMembership#entityIdFor(PoliticalRegistryRuntime, UUID)} (leader or
 *       co-leader of either side).</li>
 *   <li>Recruits whose {@code getOwnerUUID()} maps to either side via the same membership
 *       resolver.</li>
 * </ul>
 *
 * <p>Other entity types (vanilla mobs, dropped items, work-area entities) do not contribute
 * to either count — only authoritative actors should decide who controls the objective.
 */
public final class ServerLevelObjectivePresenceProbe implements ObjectivePresenceProbe {
    private final ServerLevel level;
    private final PoliticalRegistryRuntime registry;

    public ServerLevelObjectivePresenceProbe(ServerLevel level, PoliticalRegistryRuntime registry) {
        this.level = Objects.requireNonNull(level, "level");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public PresenceCounts countAt(ChunkPos objectiveChunk, UUID rebelEntityId, UUID occupierEntityId) {
        if (objectiveChunk == null || rebelEntityId == null || occupierEntityId == null
                || rebelEntityId.equals(occupierEntityId)) {
            return PresenceCounts.EMPTY;
        }
        AABB chunkBox = new AABB(
                objectiveChunk.getMinBlockX(), level.getMinBuildHeight(), objectiveChunk.getMinBlockZ(),
                objectiveChunk.getMaxBlockX() + 1, level.getMaxBuildHeight(), objectiveChunk.getMaxBlockZ() + 1
        );
        int rebels = 0;
        int occupiers = 0;
        for (Entity entity : level.getEntities((Entity) null, chunkBox, e -> e.isAlive() && (e instanceof Player || e instanceof AbstractRecruitEntity))) {
            UUID actingPlayerId = controllingPlayerOf(entity);
            if (actingPlayerId == null) {
                continue;
            }
            UUID politicalEntity = PoliticalMembership.entityIdFor(registry, actingPlayerId);
            if (politicalEntity == null) {
                continue;
            }
            if (politicalEntity.equals(rebelEntityId)) {
                rebels++;
            } else if (politicalEntity.equals(occupierEntityId)) {
                occupiers++;
            }
        }
        return new PresenceCounts(rebels, occupiers);
    }

    private static UUID controllingPlayerOf(Entity entity) {
        if (entity instanceof Player player) {
            return player.getUUID();
        }
        if (entity instanceof AbstractRecruitEntity recruit) {
            return recruit.getOwnerUUID();
        }
        return null;
    }
}
