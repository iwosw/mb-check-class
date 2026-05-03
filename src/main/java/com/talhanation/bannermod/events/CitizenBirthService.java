package com.talhanation.bannermod.events;

import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.citizen.CitizenIndex;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.registry.citizen.ModCitizenEntityTypes;
import com.talhanation.bannermod.settlement.civilian.CitizenBirthRules;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-tick birth pass: scans each claim for an opposite-gender adult pair,
 * checks cooldown + housing slack via {@link CitizenBirthRules}, and spawns
 * a baby {@link CitizenEntity} that grows into an adult after the configured
 * grow-up ticks.
 *
 * <p>Cooldown state is per-claim and lives in a static map for the lifetime
 * of the server JVM. Persisting cooldowns across restarts is intentionally
 * out of scope for this slice — a server restart resets the cooldown, which
 * is acceptable for a 24000-tick (1 day) default.
 */
final class CitizenBirthService {
    private static final Map<UUID, Long> LAST_BIRTH_GAME_TIME = new HashMap<>();

    private CitizenBirthService() {
    }

    static void resetRuntimeState() {
        LAST_BIRTH_GAME_TIME.clear();
    }

    static void runCitizenBirthPass(ServerLevel level) {
        if (level == null || ClaimEvents.claimManager() == null) {
            return;
        }
        CitizenBirthRules.Config config = WorkersServerConfig.citizenBirthConfig();
        if (!config.enabled()) {
            return;
        }
        long gameTime = level.getGameTime();
        for (RecruitsClaim claim : ClaimEvents.claimManager().getAllClaims()) {
            if (claim == null || claim.getOwnerPoliticalEntityId() == null) {
                continue;
            }
            attemptBirthInClaim(level, claim, config, gameTime);
        }
    }

    @Nullable
    private static CitizenEntity attemptBirthInClaim(ServerLevel level, RecruitsClaim claim, CitizenBirthRules.Config config, long gameTime) {
        List<CitizenEntity> citizens = CitizenIndex.instance().queryInClaim(level, claim).orElse(List.of());
        int males = 0;
        int females = 0;
        int babies = 0;
        CitizenEntity firstAdultMale = null;
        CitizenEntity firstAdultFemale = null;
        for (CitizenEntity citizen : citizens) {
            if (!citizen.isAlive()) {
                continue;
            }
            if (citizen.isBaby()) {
                babies++;
                continue;
            }
            if (citizen.isFemale()) {
                females++;
                if (firstAdultFemale == null) firstAdultFemale = citizen;
            } else {
                males++;
                if (firstAdultMale == null) firstAdultMale = citizen;
            }
        }

        BannerModSettlementBinding.Status status =
                WorkerSettlementClaimPolicy.resolveClaimGrowthBinding(claim, claim.getOwnerPoliticalEntityId().toString()).status();
        long elapsedCooldownTicks = elapsedSince(LAST_BIRTH_GAME_TIME.get(claim.getUUID()), gameTime);
        int housingSlack = WorkerSettlementClaimPolicy.housingSlackForClaim(level, claim);

        CitizenBirthRules.Decision decision = CitizenBirthRules.evaluate(
                status, males, females, babies, housingSlack, elapsedCooldownTicks, config);
        if (!decision.allowed()) {
            return null;
        }

        CitizenEntity baby = ModCitizenEntityTypes.CITIZEN.get().create(level);
        if (baby == null) {
            return null;
        }
        Vec3 spawnPos = firstAdultFemale.position();
        baby.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        baby.setBabyForBirth(config.growUpTicks());
        baby.finalizeSpawn(level, level.getCurrentDifficultyAt(baby.blockPosition()), MobSpawnType.BREEDING, null);
        if (!level.addFreshEntity(baby)) {
            return null;
        }
        LAST_BIRTH_GAME_TIME.put(claim.getUUID(), gameTime);
        WorkerSettlementClaimPolicy.assignHomeIfAvailable(level, claim, baby.getUUID(), gameTime);
        return baby;
    }

    private static long elapsedSince(@Nullable Long lastGameTime, long now) {
        if (lastGameTime == null) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, now - lastGameTime);
    }
}
