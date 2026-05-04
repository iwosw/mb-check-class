package com.talhanation.bannermod.society;

import com.talhanation.bannermod.entity.citizen.AbstractCitizenEntity;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class NpcMemoryAccess {
    private static final long ASSAULT_DURATION_TICKS = 24000L * 6L;
    private static final long PROTECTION_DURATION_TICKS = 24000L * 4L;
    private static final long HAMLET_ATTACK_DURATION_TICKS = 24000L * 5L;
    private static final long STARVATION_DURATION_TICKS = 24000L * 3L;
    private static final long HOUSING_DURATION_TICKS = 24000L * 4L;

    private NpcMemoryAccess() {
    }

    public static boolean isSocietyResident(@Nullable Entity entity) {
        return entity instanceof AbstractWorkerEntity
                || entity instanceof AbstractRecruitEntity
                || entity instanceof CitizenEntity
                || entity instanceof AbstractCitizenEntity;
    }

    public static NpcSocietyProfile tickResidentState(ServerLevel level,
                                                      NpcSocietyProfile profile,
                                                      long gameTime) {
        if (level == null || profile == null || profile.residentUuid() == null) {
            throw new IllegalArgumentException("level and profile must not be null");
        }
        rememberConditionPressure(level, profile, gameTime);
        List<NpcMemoryRecord> memories = NpcMemorySavedData.get(level).runtime().memoriesFor(profile.residentUuid(), gameTime);
        DerivedSocialState derived = deriveSocialState(memories, gameTime);
        return NpcSocietyAccess.reconcileSocialState(
                level,
                profile.residentUuid(),
                derived.trust(),
                derived.fear(),
                derived.anger(),
                derived.gratitude(),
                derived.loyalty(),
                gameTime
        );
    }

    public static void rememberAssaultByPlayer(ServerLevel level,
                                               UUID residentUuid,
                                               @Nullable UUID actorUuid,
                                               int intensity,
                                               long gameTime) {
        rememberWithPropagation(level,
                residentUuid,
                actorUuid,
                NpcSocialMemoryType.ASSAULTED_BY_PLAYER,
                intensity,
                ASSAULT_DURATION_TICKS,
                true,
                true,
                gameTime);
    }

    public static void rememberProtectionByPlayer(ServerLevel level,
                                                   UUID residentUuid,
                                                   @Nullable UUID actorUuid,
                                                   int intensity,
                                                   long gameTime) {
        rememberWithPropagation(level,
                residentUuid,
                actorUuid,
                NpcSocialMemoryType.PROTECTED_BY_PLAYER,
                intensity,
                PROTECTION_DURATION_TICKS,
                true,
                true,
                gameTime);
    }

    public static void rememberHamletAttackedByPlayer(ServerLevel level,
                                                      NpcHamletRecord hamlet,
                                                      @Nullable UUID actorUuid,
                                                      int intensity,
                                                      long gameTime) {
        if (level == null || hamlet == null) {
            return;
        }
        for (NpcHamletHouseholdEntry entry : hamlet.householdEntries()) {
            NpcHouseholdRecord household = NpcHouseholdAccess.householdFor(level, entry.householdId()).orElse(null);
            if (household == null) {
                continue;
            }
            for (UUID residentUuid : household.memberResidentUuids()) {
                if (residentUuid != null) {
                    rememberWithPropagation(level,
                            residentUuid,
                            actorUuid,
                            NpcSocialMemoryType.HAMLET_ATTACKED_BY_PLAYER,
                            intensity,
                            HAMLET_ATTACK_DURATION_TICKS,
                            true,
                            true,
                            gameTime);
                }
            }
        }
    }

    public static List<NpcMemorySummarySnapshot> summarySnapshots(ServerLevel level,
                                                                  UUID residentUuid,
                                                                  long gameTime) {
        List<NpcMemorySummarySnapshot> snapshots = new ArrayList<>();
        if (level == null || residentUuid == null) {
            return snapshots;
        }
        List<NpcMemoryRecord> memories = NpcMemorySavedData.get(level).runtime().memoriesFor(residentUuid, gameTime);
        int shown = Math.min(3, memories.size());
        for (int i = 0; i < shown; i++) {
            NpcMemoryRecord memory = memories.get(i);
            snapshots.add(new NpcMemorySummarySnapshot(
                    memory.type().name(),
                    memory.scope().name(),
                    resolveActorLabel(level, memory.actorUuid()),
                    memory.intensity(),
                    memory.type() == NpcSocialMemoryType.PROTECTED_BY_PLAYER
            ));
        }
        return List.copyOf(snapshots);
    }

    public static void onProtectedByPlayer(ServerLevel level,
                                           LivingEntity hostile,
                                           @Nullable UUID actorUuid,
                                           long gameTime) {
        if (level == null || hostile == null) {
            return;
        }
        Set<UUID> protectedResidents = new LinkedHashSet<>();
        if (hostile instanceof Mob mob && isSocietyResident(mob.getTarget())) {
            protectedResidents.add(mob.getTarget().getUUID());
        }
        AABB nearbyBounds = hostile.getBoundingBox().inflate(10.0D);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, nearbyBounds, NpcMemoryAccess::isSocietyResident)) {
            if (nearby.getLastHurtByMob() == hostile || nearby.distanceToSqr(hostile) <= 36.0D) {
                protectedResidents.add(nearby.getUUID());
            }
        }
        for (UUID residentUuid : protectedResidents) {
            rememberProtectionByPlayer(level, residentUuid, actorUuid, 60, gameTime);
        }
    }

    private static void rememberConditionPressure(ServerLevel level, NpcSocietyProfile profile, long gameTime) {
        UUID residentUuid = profile.residentUuid();
        NpcHouseholdRecord household = NpcHouseholdAccess.householdForResident(level, residentUuid).orElse(null);
        if (profile.hungerNeed() >= 85) {
            rememberDirect(level,
                    residentUuid,
                    NpcSocialMemoryType.STARVED,
                    NpcSocialMemoryScope.PERSONAL,
                    null,
                    Math.min(100, 45 + (profile.hungerNeed() - 85) * 3),
                    STARVATION_DURATION_TICKS,
                    gameTime);
        }
        if (household == null) {
            return;
        }
        if (household.housingState() == NpcHouseholdHousingState.HOMELESS) {
            rememberDirect(level,
                    residentUuid,
                    NpcSocialMemoryType.HOMELESS,
                    NpcSocialMemoryScope.HOUSEHOLD,
                    null,
                    72,
                    HOUSING_DURATION_TICKS,
                    gameTime);
        } else if (household.housingState() == NpcHouseholdHousingState.OVERCROWDED) {
            rememberDirect(level,
                    residentUuid,
                    NpcSocialMemoryType.OVERCROWDED,
                    NpcSocialMemoryScope.HOUSEHOLD,
                    null,
                    54,
                    HOUSING_DURATION_TICKS,
                    gameTime);
        }
    }

    private static void rememberWithPropagation(ServerLevel level,
                                                UUID residentUuid,
                                                @Nullable UUID actorUuid,
                                                NpcSocialMemoryType type,
                                                int intensity,
                                                long durationTicks,
                                                boolean propagateFamily,
                                                boolean propagateHousehold,
                                                long gameTime) {
        if (level == null || residentUuid == null) {
            return;
        }
        Set<UUID> impactedResidents = new LinkedHashSet<>();
        rememberDirect(level,
                residentUuid,
                type,
                NpcSocialMemoryScope.PERSONAL,
                actorUuid,
                intensity,
                durationTicks,
                gameTime);
        impactedResidents.add(residentUuid);
        if (propagateFamily) {
            NpcFamilyRecord family = NpcFamilySavedData.get(level).runtime().familyFor(residentUuid).orElse(null);
            if (family != null) {
                for (UUID related : directFamilyMembers(family)) {
                    if (related != null && !related.equals(residentUuid)) {
                        rememberDirect(level,
                                related,
                                type,
                                NpcSocialMemoryScope.FAMILY,
                                actorUuid,
                                scaleIntensity(intensity, 0.72D),
                                durationTicks,
                                gameTime);
                        impactedResidents.add(related);
                    }
                }
            }
        }
        if (propagateHousehold) {
            NpcHouseholdRecord household = NpcHouseholdAccess.householdForResident(level, residentUuid).orElse(null);
            if (household != null) {
                for (UUID member : household.memberResidentUuids()) {
                    if (member != null && !member.equals(residentUuid)) {
                        rememberDirect(level,
                                member,
                                type,
                                NpcSocialMemoryScope.HOUSEHOLD,
                                actorUuid,
                                scaleIntensity(intensity, 0.58D),
                                durationTicks,
                                gameTime);
                        impactedResidents.add(member);
                    }
                }
            }
        }
        for (UUID impactedResident : impactedResidents) {
            NpcSocietyProfile profile = NpcSocietyAccess.ensureResident(level, impactedResident, gameTime);
            tickResidentState(level, profile, gameTime);
        }
    }

    private static void rememberDirect(ServerLevel level,
                                       UUID residentUuid,
                                       NpcSocialMemoryType type,
                                       NpcSocialMemoryScope scope,
                                       @Nullable UUID actorUuid,
                                       int intensity,
                                       long durationTicks,
                                       long gameTime) {
        NpcMemoryRecord memory = NpcMemoryRecord.create(
                residentUuid,
                type,
                scope,
                actorUuid,
                intensity,
                gameTime,
                gameTime + Math.max(20L, durationTicks)
        );
        NpcMemorySavedData.get(level).runtime().remember(memory, gameTime);
    }

    private static Set<UUID> directFamilyMembers(NpcFamilyRecord family) {
        Set<UUID> related = new LinkedHashSet<>();
        if (family.spouseUuid() != null) {
            related.add(family.spouseUuid());
        }
        if (family.motherUuid() != null) {
            related.add(family.motherUuid());
        }
        if (family.fatherUuid() != null) {
            related.add(family.fatherUuid());
        }
        related.addAll(family.childUuids());
        return related;
    }

    private static String resolveActorLabel(ServerLevel level, @Nullable UUID actorUuid) {
        if (actorUuid == null) {
            return null;
        }
        Player player = level.getPlayerByUUID(actorUuid);
        if (player != null) {
            return player.getName().getString();
        }
        Entity entity = level.getEntity(actorUuid);
        if (entity != null) {
            return entity.getName().getString();
        }
        return actorUuid.toString().substring(0, 8);
    }

    private static DerivedSocialState deriveSocialState(List<NpcMemoryRecord> memories, long gameTime) {
        double trust = 50.0D;
        double fear = 0.0D;
        double anger = 0.0D;
        double gratitude = 0.0D;
        double loyalty = 50.0D;
        for (NpcMemoryRecord memory : memories) {
            if (memory == null || memory.isExpired(gameTime)) {
                continue;
            }
            double strength = memory.intensity() / 100.0D;
            double freshness = Math.max(0.1D,
                    (double) (memory.expiresGameTime() - gameTime) / (double) memory.durationTicks());
            double scopeWeight = scopeWeight(memory.scope());
            double weight = strength * freshness * scopeWeight;
            switch (memory.type()) {
                case ASSAULTED_BY_PLAYER -> {
                    trust -= 38.0D * weight;
                    fear += 46.0D * weight;
                    anger += 52.0D * weight;
                    loyalty -= 20.0D * weight;
                }
                case PROTECTED_BY_PLAYER -> {
                    trust += 30.0D * weight;
                    fear -= 18.0D * weight;
                    gratitude += 48.0D * weight;
                    loyalty += 18.0D * weight;
                }
                case HAMLET_ATTACKED_BY_PLAYER -> {
                    trust -= 26.0D * weight;
                    fear += 34.0D * weight;
                    anger += 42.0D * weight;
                    loyalty -= 16.0D * weight;
                }
                case STARVED -> {
                    fear += 12.0D * weight;
                    anger += 10.0D * weight;
                    loyalty -= 28.0D * weight;
                }
                case HOMELESS -> {
                    fear += 18.0D * weight;
                    anger += 14.0D * weight;
                    loyalty -= 34.0D * weight;
                }
                case OVERCROWDED -> {
                    anger += 10.0D * weight;
                    loyalty -= 18.0D * weight;
                }
            }
        }
        return new DerivedSocialState(
                clampAxis(trust),
                clampAxis(fear),
                clampAxis(anger),
                clampAxis(gratitude),
                clampAxis(loyalty)
        );
    }

    private static double scopeWeight(NpcSocialMemoryScope scope) {
        return switch (scope == null ? NpcSocialMemoryScope.PERSONAL : scope) {
            case PERSONAL -> 1.0D;
            case FAMILY -> 0.72D;
            case HOUSEHOLD -> 0.58D;
            case SETTLEMENT -> 0.42D;
        };
    }

    private static int clampAxis(double value) {
        return Math.max(0, Math.min(100, (int) Math.round(value)));
    }

    private static int scaleIntensity(int intensity, double multiplier) {
        return Math.max(0, Math.min(100, (int) Math.round(intensity * multiplier)));
    }

    private record DerivedSocialState(int trust, int fear, int anger, int gratitude, int loyalty) {
    }
}
