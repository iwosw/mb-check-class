package com.talhanation.bannermod.settlement.civilian;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Pure rule set for citizen-on-citizen birth. The runtime calls
 * {@link #evaluate} once per claim per tick window with a snapshot of the
 * claim's adult-citizen population, housing slack and birth cooldown.
 *
 * <p>Mirrors {@link WorkerSettlementSpawnRules} so live tests can probe the
 * decision logic without bootstrapping Minecraft.
 */
public final class CitizenBirthRules {

    private CitizenBirthRules() {
    }

    public static Decision evaluate(BannerModSettlementBinding.Status status,
                                    int adultMales,
                                    int adultFemales,
                                    int currentBabies,
                                    int housingSlack,
                                    long elapsedCooldownTicks,
                                    Config config) {
        long requiredCooldownTicks = config == null ? 0L : Math.max(0L, config.cooldownTicks());
        if (config == null || !config.enabled()) {
            return deny(DenialReason.FEATURE_DISABLED, requiredCooldownTicks);
        }
        if (status != BannerModSettlementBinding.Status.FRIENDLY_CLAIM) {
            return deny(DenialReason.NOT_FRIENDLY_CLAIM, requiredCooldownTicks);
        }
        if (adultMales <= 0 || adultFemales <= 0) {
            return deny(DenialReason.NO_PAIRABLE_ADULTS, requiredCooldownTicks);
        }
        if (currentBabies >= config.maxConcurrentBabies()) {
            return deny(DenialReason.BABIES_AT_CAP, requiredCooldownTicks);
        }
        if (elapsedCooldownTicks < requiredCooldownTicks) {
            return deny(DenialReason.COOLDOWN_ACTIVE, requiredCooldownTicks);
        }
        if (housingSlack <= 0) {
            return deny(DenialReason.NO_FREE_HOUSING, requiredCooldownTicks);
        }
        return new Decision(true, null, requiredCooldownTicks);
    }

    private static Decision deny(DenialReason reason, long requiredCooldownTicks) {
        return new Decision(false, reason, requiredCooldownTicks);
    }

    public enum DenialReason {
        FEATURE_DISABLED,
        NOT_FRIENDLY_CLAIM,
        NO_PAIRABLE_ADULTS,
        BABIES_AT_CAP,
        COOLDOWN_ACTIVE,
        NO_FREE_HOUSING
    }

    /**
     * @param enabled whether the birth tick is active
     * @param cooldownTicks minimum ticks between consecutive births in the same claim
     * @param growUpTicks how long a freshly spawned baby remains a baby before becoming an adult
     * @param maxConcurrentBabies hard cap on simultaneous babies in a claim — keeps births bounded even if cooldown is small
     */
    public record Config(boolean enabled,
                         long cooldownTicks,
                         int growUpTicks,
                         int maxConcurrentBabies) {

        public Config {
            cooldownTicks = Math.max(0L, cooldownTicks);
            growUpTicks = Math.max(1, growUpTicks);
            maxConcurrentBabies = Math.max(1, maxConcurrentBabies);
        }
    }

    public record Decision(boolean allowed,
                           @Nullable DenialReason denialReason,
                           long requiredCooldownTicks) {
        public Optional<DenialReason> denialReasonOptional() {
            return Optional.ofNullable(denialReason);
        }
    }
}
