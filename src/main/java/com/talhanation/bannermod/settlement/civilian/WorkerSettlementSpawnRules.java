package com.talhanation.bannermod.settlement.civilian;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class WorkerSettlementSpawnRules {

    private WorkerSettlementSpawnRules() {
    }

    public static Decision evaluateBirth(BannerModSettlementBinding.Binding binding,
                                         int villagerCount,
                                         int currentWorkerCount,
                                         boolean cooldownActive,
                                         RuleConfig config,
                                         Map<WorkerProfession, Integer> currentByProfession) {
        return evaluate(binding, villagerCount, currentWorkerCount, cooldownActive, config, currentByProfession);
    }

    public static Decision evaluateSettlementSpawn(BannerModSettlementBinding.Binding binding,
                                                    int villagerCount,
                                                    int currentWorkerCount,
                                                    boolean cooldownActive,
                                                    RuleConfig config,
                                                    Map<WorkerProfession, Integer> currentByProfession) {
        return evaluate(binding, villagerCount, currentWorkerCount, cooldownActive, config, currentByProfession);
    }

    public static Decision evaluateClaimWorkerGrowth(BannerModSettlementBinding.Status status,
                                                     int currentWorkerCount,
                                                     long elapsedCooldownTicks,
                                                     ClaimGrowthConfig config,
                                                     Map<WorkerProfession, Integer> currentByProfession) {
        long requiredCooldownTicks = config == null ? 0L : config.requiredCooldownTicks(currentWorkerCount);
        if (config == null || !config.enabled()) {
            return deny(DenialReason.FEATURE_DISABLED, requiredCooldownTicks);
        }
        if (status != BannerModSettlementBinding.Status.FRIENDLY_CLAIM) {
            return deny(DenialReason.NOT_FRIENDLY_CLAIM, requiredCooldownTicks);
        }
        if (currentWorkerCount >= config.workerCap()) {
            return deny(DenialReason.WORKER_CAP_REACHED, requiredCooldownTicks);
        }
        if (elapsedCooldownTicks < requiredCooldownTicks) {
            return deny(DenialReason.COOLDOWN_ACTIVE, requiredCooldownTicks);
        }
        if (config.allowedProfessions().isEmpty()) {
            return deny(DenialReason.NO_ALLOWED_PROFESSIONS, requiredCooldownTicks);
        }

        return allow(pickByDeficit(config.allowedProfessions(), currentByProfession), requiredCooldownTicks);
    }

    private static Decision evaluate(BannerModSettlementBinding.Binding binding,
                                     int villagerCount,
                                     int currentWorkerCount,
                                     boolean cooldownActive,
                                     RuleConfig config,
                                     Map<WorkerProfession, Integer> currentByProfession) {
        if (config == null || !config.enabled()) {
            return deny(DenialReason.FEATURE_DISABLED, 0L);
        }
        if (binding == null || !BannerModSettlementBinding.allowsSettlementOperation(binding)) {
            return deny(DenialReason.NOT_FRIENDLY_CLAIM, 0L);
        }
        if (villagerCount < config.minimumVillagers()) {
            return deny(DenialReason.INSUFFICIENT_VILLAGERS, 0L);
        }
        if (currentWorkerCount >= config.workerCap()) {
            return deny(DenialReason.WORKER_CAP_REACHED, 0L);
        }
        if (cooldownActive) {
            return deny(DenialReason.COOLDOWN_ACTIVE, 0L);
        }
        if (config.allowedProfessions().isEmpty()) {
            return deny(DenialReason.NO_ALLOWED_PROFESSIONS, 0L);
        }

        return allow(pickByDeficit(config.allowedProfessions(), currentByProfession), 0L);
    }

    /**
     * Pick the allowed profession with the lowest current headcount in the claim,
     * tiebroken by declaration order. A missing key defaults to 0 — so a profession
     * the settlement has never staffed always wins over one that already exists.
     */
    static WorkerProfession pickByDeficit(List<WorkerProfession> allowed,
                                          @Nullable Map<WorkerProfession, Integer> currentByProfession) {
        WorkerProfession best = allowed.get(0);
        int bestCount = currentCount(currentByProfession, best);
        for (int i = 1; i < allowed.size(); i++) {
            WorkerProfession candidate = allowed.get(i);
            int candidateCount = currentCount(currentByProfession, candidate);
            if (candidateCount < bestCount) {
                best = candidate;
                bestCount = candidateCount;
            }
        }
        return best;
    }

    private static int currentCount(@Nullable Map<WorkerProfession, Integer> map, WorkerProfession profession) {
        if (map == null) {
            return 0;
        }
        Integer value = map.get(profession);
        return value == null ? 0 : Math.max(0, value);
    }

    private static Decision allow(WorkerProfession profession, long requiredCooldownTicks) {
        return new Decision(true, profession, null, requiredCooldownTicks);
    }

    private static Decision deny(DenialReason denialReason, long requiredCooldownTicks) {
        return new Decision(false, null, denialReason, requiredCooldownTicks);
    }

    public enum DenialReason {
        FEATURE_DISABLED,
        NOT_FRIENDLY_CLAIM,
        INSUFFICIENT_VILLAGERS,
        WORKER_CAP_REACHED,
        COOLDOWN_ACTIVE,
        NO_ALLOWED_PROFESSIONS
    }

    public enum WorkerProfession {
        FARMER,
        LUMBERJACK,
        MINER,
        BUILDER,
        MERCHANT,
        FISHERMAN,
        ANIMAL_FARMER;

        @Nullable
        public static WorkerProfession fromConfigValue(@Nullable String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
            if (normalized.isEmpty()) {
                return null;
            }
            try {
                return WorkerProfession.valueOf(normalized);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
    }

    public record RuleConfig(boolean enabled,
                             int minimumVillagers,
                             int workerCap,
                             List<WorkerProfession> allowedProfessions) {

        public RuleConfig {
            minimumVillagers = Math.max(0, minimumVillagers);
            workerCap = Math.max(1, workerCap);
            allowedProfessions = allowedProfessions == null ? List.of() : allowedProfessions.stream().filter(Objects::nonNull).distinct().toList();
        }

        public RuleConfig withEnabled(boolean enabled) {
            return new RuleConfig(enabled, minimumVillagers, workerCap, allowedProfessions);
        }
    }

    public record ClaimGrowthConfig(boolean enabled,
                                    long baseCooldownTicks,
                                    int workerCap,
                                    List<WorkerProfession> allowedProfessions) {

        public ClaimGrowthConfig {
            baseCooldownTicks = Math.max(0L, baseCooldownTicks);
            workerCap = Math.max(1, workerCap);
            allowedProfessions = allowedProfessions == null ? List.of() : allowedProfessions.stream().filter(Objects::nonNull).distinct().toList();
        }

        public long requiredCooldownTicks(int currentWorkerCount) {
            return baseCooldownTicks * Math.max(1, currentWorkerCount + 1L);
        }

        public ClaimGrowthConfig withEnabled(boolean enabled) {
            return new ClaimGrowthConfig(enabled, baseCooldownTicks, workerCap, allowedProfessions);
        }
    }

    public record Decision(boolean allowed,
                           @Nullable WorkerProfession profession,
                           @Nullable DenialReason denialReason,
                           long requiredCooldownTicks) {

        public Optional<WorkerProfession> professionOptional() {
            return Optional.ofNullable(profession);
        }

        public Optional<DenialReason> denialReasonOptional() {
            return Optional.ofNullable(denialReason);
        }
    }
}
