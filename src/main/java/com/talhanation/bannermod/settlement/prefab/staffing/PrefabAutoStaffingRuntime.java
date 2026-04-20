package com.talhanation.bannermod.settlement.prefab.staffing;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.registry.civilian.ModEntityTypes;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabProfession;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabRegistry;
import com.talhanation.bannermod.settlement.prefab.PrefabBuildAreaTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Auto-staffing hook for prefab-backed BuildAreas. When a BuildArea that was spawned via a
 * {@link BuildingPrefab} completes, this runtime:
 *
 * <ol>
 *   <li>Consumes the tracker entry so the hook only fires once.</li>
 *   <li>Looks up the prefab's declared {@link BuildingPrefabProfession}.</li>
 *   <li>Spawns the matching worker or recruit entity on top of the BuildArea.</li>
 *   <li>Binds the worker to the embedded work-area entity (if the prefab embedded one).</li>
 *   <li>Transfers the BuildArea's owner UUID and team onto the new staffer.</li>
 * </ol>
 *
 * <p>Server-only, in-memory. Not persisted.</p>
 */
public final class PrefabAutoStaffingRuntime {

    private PrefabAutoStaffingRuntime() {
    }

    /**
     * Hook called from {@code BuildArea} once it transitions to {@code isDone == true}.
     * Safe to call multiple times: the tracker entry is consumed on the first call so
     * subsequent invocations are no-ops.
     */
    public static void onBuildAreaCompleted(ServerLevel level, BuildArea buildArea) {
        if (level == null || buildArea == null) {
            return;
        }

        ResourceLocation prefabId = PrefabBuildAreaTracker.consume(buildArea.getUUID()).orElse(null);
        if (prefabId == null) {
            return;
        }

        Optional<BuildingPrefab> prefabOpt = BuildingPrefabRegistry.instance().lookup(prefabId);
        if (prefabOpt.isEmpty()) {
            System.err.println("[PrefabAutoStaffing] Unknown prefab id=" + prefabId + " for BuildArea " + buildArea.getUUID());
            return;
        }

        BuildingPrefabProfession profession = prefabOpt.get().descriptor().profession();
        if (profession == null || profession == BuildingPrefabProfession.NONE) {
            return;
        }

        EntityType<?> entityType = entityTypeFor(profession);
        if (entityType == null) {
            System.err.println("[PrefabAutoStaffing] No entity type mapping for profession=" + profession + " (prefab=" + prefabId + ")");
            return;
        }

        AbstractWorkAreaEntity workArea = findEmbeddedWorkArea(level, buildArea);

        spawnAndBind(level, buildArea, entityType, profession, workArea);
    }

    /**
     * Maps a {@link BuildingPrefabProfession} to the corresponding {@link EntityType} to spawn.
     * {@code null} is returned for {@link BuildingPrefabProfession#NONE}.
     *
     * <p>Visible for unit tests — must stay pure (no Level/World access).</p>
     */
    @Nullable
    public static EntityType<?> entityTypeFor(BuildingPrefabProfession profession) {
        if (profession == null) {
            return null;
        }
        return switch (profession) {
            case NONE -> null;
            case FARMER -> ModEntityTypes.FARMER.get();
            case LUMBERJACK -> ModEntityTypes.LUMBERJACK.get();
            case MINER -> ModEntityTypes.MINER.get();
            case BUILDER -> ModEntityTypes.BUILDER.get();
            case MERCHANT -> ModEntityTypes.MERCHANT.get();
            case FISHERMAN -> ModEntityTypes.FISHERMAN.get();
            case ANIMAL_FARMER -> ModEntityTypes.ANIMAL_FARMER.get();
            // SHEPHERD reuses animal farmer for now — we don't have a dedicated shepherd type yet.
            case SHEPHERD -> ModEntityTypes.ANIMAL_FARMER.get();
            case RECRUIT_SWORDSMAN -> com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get();
            case RECRUIT_ARCHER -> com.talhanation.bannermod.registry.military.ModEntityTypes.BOWMAN.get();
            case RECRUIT_PIKEMAN -> com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT_SHIELDMAN.get();
            case RECRUIT_CROSSBOW -> com.talhanation.bannermod.registry.military.ModEntityTypes.CROSSBOWMAN.get();
            case RECRUIT_CAVALRY -> com.talhanation.bannermod.registry.military.ModEntityTypes.HORSEMAN.get();
        };
    }

    /**
     * Returns true if the profession represents a civilian worker (bindable to a work-area).
     * Visible for tests.
     */
    public static boolean isWorkerProfession(BuildingPrefabProfession profession) {
        if (profession == null) {
            return false;
        }
        return switch (profession) {
            case FARMER, LUMBERJACK, MINER, BUILDER, MERCHANT, FISHERMAN, ANIMAL_FARMER, SHEPHERD -> true;
            default -> false;
        };
    }

    @Nullable
    private static AbstractWorkAreaEntity findEmbeddedWorkArea(ServerLevel level, BuildArea buildArea) {
        List<AbstractWorkAreaEntity> candidates = level.getEntitiesOfClass(
                AbstractWorkAreaEntity.class,
                buildArea.getBoundingBox().inflate(4)
        );
        AbstractWorkAreaEntity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (AbstractWorkAreaEntity wa : candidates) {
            if (wa == null || !wa.isAlive()) continue;
            if (wa == buildArea) continue;
            if (wa instanceof BuildArea) continue;
            double dsq = wa.distanceToSqr(buildArea);
            if (dsq < nearestDistSq) {
                nearestDistSq = dsq;
                nearest = wa;
            }
        }
        return nearest;
    }

    private static void spawnAndBind(ServerLevel level,
                                     BuildArea buildArea,
                                     EntityType<?> entityType,
                                     BuildingPrefabProfession profession,
                                     @Nullable AbstractWorkAreaEntity workArea) {
        Entity entity = entityType.create(level);
        if (entity == null) {
            System.err.println("[PrefabAutoStaffing] Failed to create entity of type " + entityType + " for profession " + profession);
            return;
        }

        double spawnX = buildArea.getX();
        double spawnY = buildArea.getY();
        double spawnZ = buildArea.getZ();
        entity.moveTo(spawnX, spawnY, spawnZ, 0.0F, 0.0F);

        UUID ownerUuid = buildArea.getPlayerUUID();
        String teamStringId = buildArea.getTeamStringID();

        if (entity instanceof AbstractWorkerEntity worker) {
            if (ownerUuid != null) {
                worker.setOwnerUUID(Optional.of(ownerUuid));
                worker.setIsOwned(true);
            }
            applyTeam(level, entity, teamStringId);
            level.addFreshEntity(worker);
            if (workArea != null && isWorkerProfession(profession)) {
                worker.getCitizenCore().setBoundWorkAreaUUID(workArea.getUUID());
            }
            return;
        }

        if (entity instanceof AbstractRecruitEntity recruit) {
            if (ownerUuid != null) {
                recruit.setOwnerUUID(Optional.of(ownerUuid));
                recruit.setIsOwned(true);
            }
            applyTeam(level, entity, teamStringId);
            level.addFreshEntity(recruit);
            return;
        }

        // Generic fallback — just add the entity.
        level.addFreshEntity(entity);
    }

    private static void applyTeam(ServerLevel level, Entity entity, @Nullable String teamStringId) {
        if (teamStringId == null || teamStringId.isEmpty()) {
            return;
        }
        PlayerTeam team = level.getScoreboard().getPlayerTeam(teamStringId);
        if (team == null) {
            return;
        }
        // Workers and recruits share the AbstractRecruitEntity hierarchy — both route through the
        // faction team service so team mirrors the BuildArea's faction owner.
        if (entity instanceof AbstractRecruitEntity recruit) {
            FactionEvents.addRecruitToTeam(recruit, team, level);
            return;
        }
        level.getScoreboard().addPlayerToTeam(entity.getScoreboardName(), team);
    }
}
