package com.talhanation.bannermod.config;

import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkersServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final List<String> DEFAULT_SETTLEMENT_WORKER_PROFESSIONS = List.of(
            "farmer",
            "miner",
            "lumberjack",
            "builder"
    );

    public static ForgeConfigSpec SERVER;
    public static ForgeConfigSpec.IntValue FarmerCost;
    public static ForgeConfigSpec.IntValue LumberjackCost;
    public static ForgeConfigSpec.IntValue MinerCost;
    public static ForgeConfigSpec.IntValue BuilderCost;
    public static ForgeConfigSpec.IntValue MerchantCost;
    public static ForgeConfigSpec.BooleanValue BuilderActive;
    public static ForgeConfigSpec.IntValue AnimalPenMaxAnimals;
    public static ForgeConfigSpec.BooleanValue ShouldWorkAreaOnlyBeInFactionClaim;
    public static ForgeConfigSpec.BooleanValue WorkerBirthEnabled;
    public static ForgeConfigSpec.BooleanValue ClaimBasedSettlementSpawnEnabled;
    public static ForgeConfigSpec.IntValue SettlementSpawnMinimumVillagers;
    public static ForgeConfigSpec.IntValue SettlementSpawnWorkerCap;
    public static ForgeConfigSpec.IntValue SettlementSpawnCooldownDays;
    public static ForgeConfigSpec.IntValue SettlementFortBannerMaxDistance;
    public static ForgeConfigSpec.LongValue SettlementFortGraceTicks;
    public static ForgeConfigSpec.LongValue SettlementFortExplosionGraceTicks;
    public static ForgeConfigSpec.LongValue SettlementHouseGraceTicks;
    public static ForgeConfigSpec.LongValue SettlementWorkplaceGraceTicks;
    public static ForgeConfigSpec.IntValue SettlementRevalidationBatchSizePerTick;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> SettlementSpawnAllowedProfessions;
    public static ForgeConfigSpec.BooleanValue EnableClaimWorkerGrowth;
    public static ForgeConfigSpec.LongValue ClaimWorkerGrowthBaseCooldownTicks;
    public static ForgeConfigSpec.IntValue ClaimWorkerMaxPerClaim;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> ClaimWorkerProfessionPool;
    public static ForgeConfigSpec.DoubleValue CitizenMilitiaMobilizationChance;
    public static ArrayList<String> FARMER_PICKUP = new ArrayList<>(
            Arrays.asList(
                    "minecraft:wheat",
                    "minecraft:wheat_seeds",
                    "minecraft:beetroot_seeds",
                    "minecraft:beetroot_seeds",
                    "minecraft:carrot",
                    "minecraft:potato",
                    "minecraft:poisonous_potato",
                    "minecraft:pumpkin",
                    "minecraft:pumpkin_seeds",
                    "minecraft:melon",
                    "minecraft:melon_seeds",
                    "minecraft:melon_slice",
                    "supplementaries:flax",
                    "herbalbrews:green_tea_leaf"

            ));

    public static ArrayList<String> LUMBERMAN_PICKUP = new ArrayList<>(
            Arrays.asList(
                    "minecraft:stick",
                    "minecraft:bee_nest"
            ));

    public static ArrayList<String> MINER_PICKUP = new ArrayList<>(
            Arrays.asList(
                    "minecraft:torch",
                    "minecraft:lantern",
                    "minecraft:redstone",
                    "minecraft:cobblestone",
                    "minecraft:sandstone",
                    "minecraft:sand",
                    "minecraft:gravel",
                    "minecraft:flint",
                    "minecraft:coal",
                    "minecraft:calcite",
                    "minecraft:clay",
                    "minecraft:dripstone_block",
                    "minecraft:pointed_dripstone",
                    "minecraft:netherrack",
                    "minecraft:emerald",
                    "minecraft:lapis_lazuli",
                    "minecraft:diamond"
            ));

    public static ArrayList<String> MINER_IGNORE = new ArrayList<>(
            Arrays.asList(
                    "minecraft:air",
                    "minecraft:cave_air",
                    "minecraft:torch",
                    "minecraft:wall_torch",
                    "minecraft:lantern",
                    "minecraft:lever",
                    "minecraft:redstone_torch",
                    "minecraft:redstone_wall_torch",
                    "minecraft:redstone_wire",
                    "minecraft:redstone_lamp",
                    "minecraft:rail",
                    "minecraft:water",
                    "minecraft:soul_lantern",
                    "minecraft:soul_torch",
                    "minecraft:soul_wall_torch",
                    "minecraft:netherrack"
            ));

    public static ArrayList<String> ANIMAL_FARMER_PICKUP = new ArrayList<>(
            Arrays.asList(
                    "minecraft:feather",
                    "minecraft:leather",
                    "minecraft:milk_bucket",
                    "minecraft:chicken",
                    "minecraft:lamb",
                    "minecraft:egg",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb",
                    "minecraft:lamb"
            ));


    public static ArrayList<String> FISHERMAN_PICKUP = new ArrayList<>(
            Arrays.asList(

            ));

    static {
        BUILDER.comment("Workers Config:").push("Workers");
        FarmerCost = BUILDER.comment("""
                        
                        The amount of currency required to hire a farmer.
                        \t(takes effect after restart)
                        \tdefault: 10""")
                .worldRestart()
                .defineInRange("FarmerCost", 10, 0, 1453);

        LumberjackCost = BUILDER.comment("""
                        
                        The amount of currency required to hire a lumberjack.
                        \t(takes effect after restart)
                        \tdefault: 12""")
                .worldRestart()
                .defineInRange("LumberjackCost", 12, 0, 1453);

        MinerCost = BUILDER.comment("""
                        
                        The amount of currency required to hire a miner.
                        \t(takes effect after restart)
                        \tdefault: 16""")
                .worldRestart()
                .defineInRange("MinerCost", 16, 0, 1453);

        BuilderCost = BUILDER.comment("""
                        
                        The amount of currency required to hire a builder.
                        \t(takes effect after restart)
                        \tdefault: 20""")
                .worldRestart()
                .defineInRange("BuilderCost", 20, 0, 1453);

        MerchantCost = BUILDER.comment("""
                        
                        The amount of currency required to hire a merchant.
                        \t(takes effect after restart)
                        \tdefault: 30""")
                .worldRestart()
                .defineInRange("MerchantCost", 30, 0, 1453);

        ShouldWorkAreaOnlyBeInFactionClaim = BUILDER.comment("""
                        
                        Should placing a work ara or a building only be allowed when in a claim of the own faction.
                        \t(takes effect after restart)
                        \tdefault: false""")
                .worldRestart()
                .define("ShouldWorkAreaOnlyBeInFactionClaim", false);

        BuilderActive = BUILDER.comment("""
                        
                        WIP Builder.
                        \t(takes effect after restart)
                        \tdefault: false""")
                .worldRestart()
                .define("BuilderActive", false);

        AnimalPenMaxAnimals = BUILDER.comment("""
                         
                         The max amount of animals in a pen. After the animal worker will not breed. 
                         \t(takes effect after restart)
                         \tdefault: 32""")
                .worldRestart()
                .defineInRange("LumberjackCost", 32, 0, 1453);

        BUILDER.comment("Phase 30 worker birth and settlement spawn settings").push("SettlementSpawn");

        WorkerBirthEnabled = BUILDER.comment("""
                        
                        Allows villagers in a friendly claim settlement to convert into workers.
                        \t(takes effect after restart)
                        \tdefault: true""")
                .worldRestart()
                .define("WorkerBirthEnabled", true);

        ClaimBasedSettlementSpawnEnabled = BUILDER.comment("""
                        
                        Allows autonomous settlement worker spawning in friendly claims.
                        \t(takes effect after restart)
                        \tdefault: true""")
                .worldRestart()
                .define("ClaimBasedSettlementSpawnEnabled", true);

        SettlementSpawnMinimumVillagers = BUILDER.comment("""
                        
                        Minimum villager population before Phase 30 worker birth or spawn can happen.
                        \t(takes effect after restart)
                        \tdefault: 6""")
                .worldRestart()
                .defineInRange("SettlementSpawnMinimumVillagers", 6, 0, 512);

        SettlementSpawnWorkerCap = BUILDER.comment("""
                        
                        Maximum worker count per friendly claim settlement for Phase 30 spawning.
                        \t(takes effect after restart)
                        \tdefault: 4""")
                .worldRestart()
                .defineInRange("SettlementSpawnWorkerCap", 4, 1, 512);

        SettlementSpawnCooldownDays = BUILDER.comment("""
                        
                        Cooldown in Minecraft days between autonomous settlement worker spawns.
                        \t(takes effect after restart)
                        \tdefault: 3""")
                .worldRestart()
                .defineInRange("SettlementSpawnCooldownDays", 3, 0, 365);

        SettlementFortBannerMaxDistance = BUILDER.comment("""

                        Maximum allowed distance in blocks between fort authority anchor and bound banner during bootstrap validation.
                        \t(takes effect after restart)
                        \tdefault: 8""")
                .worldRestart()
                .defineInRange("SettlementFortBannerMaxDistance", 8, 1, 64);

        SettlementFortGraceTicks = BUILDER.comment("""

                        Grace period in ticks before a failed fort validation becomes INVALID.
                        \t(takes effect after restart)
                        \tdefault: 24000 (1 Minecraft day)""")
                .worldRestart()
                .defineInRange("SettlementFortGraceTicks", 24000L, 0L, Long.MAX_VALUE);

        SettlementFortExplosionGraceTicks = BUILDER.comment("""

                        Grace period in ticks before a fort failure caused by explosion becomes INVALID.
                        \t(takes effect after restart)
                        \tdefault: 12000 (half Minecraft day)""")
                .worldRestart()
                .defineInRange("SettlementFortExplosionGraceTicks", 12000L, 0L, Long.MAX_VALUE);

        SettlementHouseGraceTicks = BUILDER.comment("""

                        Grace period in ticks before a failed house validation becomes INVALID.
                        \t(takes effect after restart)
                        \tdefault: 24000 (1 Minecraft day)""")
                .worldRestart()
                .defineInRange("SettlementHouseGraceTicks", 24000L, 0L, Long.MAX_VALUE);

        SettlementWorkplaceGraceTicks = BUILDER.comment("""

                        Grace period in ticks before a failed workplace/storage validation becomes INVALID.
                        \t(takes effect after restart)
                        \tdefault: 12000 (half Minecraft day)""")
                .worldRestart()
                .defineInRange("SettlementWorkplaceGraceTicks", 12000L, 0L, Long.MAX_VALUE);

        SettlementRevalidationBatchSizePerTick = BUILDER.comment("""

                        Maximum number of queued building revalidations processed per server tick.
                        \t(takes effect after restart)
                        \tdefault: 8""")
                .worldRestart()
                .defineInRange("SettlementRevalidationBatchSizePerTick", 8, 1, 256);

        SettlementSpawnAllowedProfessions = BUILDER.comment("""
                         
                         Ordered worker profession pool used for deterministic settlement worker birth and spawn selection.
                         \t(takes effect after restart)
                         \tdefault: farmer, miner, lumberjack, builder""")
                .worldRestart()
                .defineListAllowEmpty("SettlementSpawnAllowedProfessions",
                        DEFAULT_SETTLEMENT_WORKER_PROFESSIONS,
                        value -> value instanceof String);

        BUILDER.pop();

        BUILDER.comment("Phase 31 claim worker growth settings").push("ClaimWorkerGrowth");

        EnableClaimWorkerGrowth = BUILDER.comment("""
                        
                        Allows worker growth in friendly claims treated as settlement surfaces.
                        \t(takes effect after restart)
                        \tdefault: true""")
                .worldRestart()
                .define("EnableClaimWorkerGrowth", true);

        ClaimWorkerGrowthBaseCooldownTicks = BUILDER.comment("""
                        
                        Base cooldown in ticks for claim worker growth before diminishing scaling by current workers.
                        \t(takes effect after restart)
                        \tdefault: 24000""")
                .worldRestart()
                .defineInRange("ClaimWorkerGrowthBaseCooldownTicks", 24000L, 0L, Long.MAX_VALUE);

        ClaimWorkerMaxPerClaim = BUILDER.comment("""
                        
                        Maximum worker count a friendly claim can grow to through Phase 31 claim worker growth.
                        \t(takes effect after restart)
                        \tdefault: 4""")
                .worldRestart()
                .defineInRange("ClaimWorkerMaxPerClaim", 4, 1, 512);

        ClaimWorkerProfessionPool = BUILDER.comment("""
                        
                        Ordered worker profession pool used as the deterministic whitelist seed for Phase 31 claim worker growth.
                        \t(takes effect after restart)
                        \tdefault: farmer, miner, lumberjack, builder""")
                .worldRestart()
                .defineListAllowEmpty("ClaimWorkerProfessionPool",
                        DEFAULT_SETTLEMENT_WORKER_PROFESSIONS,
                        value -> value instanceof String);

        CitizenMilitiaMobilizationChance = BUILDER.comment("""

                        Chance for an unarmed settlement citizen to mobilize into militia role when its claim is inside an active war's siege-standard zone.
                        \t(takes effect after restart)
                        \tdefault: 0.25""")
                .worldRestart()
                .defineInRange("CitizenMilitiaMobilizationChance", 0.25D, 0.0D, 1.0D);

        BUILDER.pop();

        SERVER = BUILDER.build();
    }

    public static WorkerSettlementSpawnRules.RuleConfig workerBirthRuleConfig() {
        return new WorkerSettlementSpawnRules.RuleConfig(
                resolveBoolean(WorkerBirthEnabled, true),
                resolveInt(SettlementSpawnMinimumVillagers, 6),
                resolveInt(SettlementSpawnWorkerCap, 4),
                resolveAllowedSettlementProfessions()
        );
    }

    public static WorkerSettlementSpawnRules.RuleConfig workerSettlementSpawnRuleConfig() {
        return new WorkerSettlementSpawnRules.RuleConfig(
                resolveBoolean(ClaimBasedSettlementSpawnEnabled, true),
                resolveInt(SettlementSpawnMinimumVillagers, 6),
                resolveInt(SettlementSpawnWorkerCap, 4),
                resolveAllowedSettlementProfessions()
        );
    }

    public static long settlementSpawnCooldownTicks() {
        return Math.max(0, resolveInt(SettlementSpawnCooldownDays, 3)) * 24000L;
    }

    public static WorkerSettlementSpawnRules.ClaimGrowthConfig claimWorkerGrowthConfig() {
        return new WorkerSettlementSpawnRules.ClaimGrowthConfig(
                resolveBoolean(EnableClaimWorkerGrowth, true),
                resolveLong(ClaimWorkerGrowthBaseCooldownTicks, 24000L),
                resolveInt(ClaimWorkerMaxPerClaim, 4),
                resolveAllowedClaimGrowthProfessions()
        );
    }

    public static float citizenMilitiaMobilizationChance() {
        return (float) resolveDouble(CitizenMilitiaMobilizationChance, 0.25D);
    }

    public static int settlementFortBannerMaxDistance() {
        return resolveInt(SettlementFortBannerMaxDistance, 8);
    }

    public static long settlementFortGraceTicks() {
        return resolveLong(SettlementFortGraceTicks, 24000L);
    }

    public static long settlementFortExplosionGraceTicks() {
        return resolveLong(SettlementFortExplosionGraceTicks, 12000L);
    }

    public static long settlementHouseGraceTicks() {
        return resolveLong(SettlementHouseGraceTicks, 24000L);
    }

    public static long settlementWorkplaceGraceTicks() {
        return resolveLong(SettlementWorkplaceGraceTicks, 12000L);
    }

    public static int settlementRevalidationBatchSizePerTick() {
        return resolveInt(SettlementRevalidationBatchSizePerTick, 8);
    }

    private static List<WorkerSettlementSpawnRules.WorkerProfession> resolveAllowedSettlementProfessions() {
        List<WorkerSettlementSpawnRules.WorkerProfession> allowedProfessions = new ArrayList<>();
        for (String configuredValue : resolveConfiguredProfessionIds()) {
            WorkerSettlementSpawnRules.WorkerProfession profession = WorkerSettlementSpawnRules.WorkerProfession.fromConfigValue(configuredValue);
            if (profession != null && !allowedProfessions.contains(profession)) {
                allowedProfessions.add(profession);
            }
        }
        if (allowedProfessions.isEmpty()) {
            for (String fallbackValue : DEFAULT_SETTLEMENT_WORKER_PROFESSIONS) {
                WorkerSettlementSpawnRules.WorkerProfession profession = WorkerSettlementSpawnRules.WorkerProfession.fromConfigValue(fallbackValue);
                if (profession != null) {
                    allowedProfessions.add(profession);
                }
            }
        }
        return List.copyOf(allowedProfessions);
    }

    private static List<? extends String> resolveConfiguredProfessionIds() {
        try {
            return SettlementSpawnAllowedProfessions.get();
        } catch (IllegalStateException exception) {
            return DEFAULT_SETTLEMENT_WORKER_PROFESSIONS;
        }
    }

    private static List<WorkerSettlementSpawnRules.WorkerProfession> resolveAllowedClaimGrowthProfessions() {
        List<WorkerSettlementSpawnRules.WorkerProfession> allowedProfessions = new ArrayList<>();
        for (String configuredValue : resolveConfiguredClaimGrowthProfessionIds()) {
            WorkerSettlementSpawnRules.WorkerProfession profession = WorkerSettlementSpawnRules.WorkerProfession.fromConfigValue(configuredValue);
            if (profession != null && !allowedProfessions.contains(profession)) {
                allowedProfessions.add(profession);
            }
        }
        if (allowedProfessions.isEmpty()) {
            for (String fallbackValue : DEFAULT_SETTLEMENT_WORKER_PROFESSIONS) {
                WorkerSettlementSpawnRules.WorkerProfession profession = WorkerSettlementSpawnRules.WorkerProfession.fromConfigValue(fallbackValue);
                if (profession != null) {
                    allowedProfessions.add(profession);
                }
            }
        }
        return List.copyOf(allowedProfessions);
    }

    private static List<? extends String> resolveConfiguredClaimGrowthProfessionIds() {
        try {
            return ClaimWorkerProfessionPool.get();
        } catch (IllegalStateException exception) {
            return DEFAULT_SETTLEMENT_WORKER_PROFESSIONS;
        }
    }

    // GameTest seam. Forge's ConfigValue.get() caches the first value it reads from the backing
    // child config and never invalidates that cache when set() is called afterwards (see
    // ForgeConfigSpec.ConfigValue#cachedValue). That makes parallel/sequential gametests that
    // each call WorkersServerConfig.X.set(value) race: whichever test triggered the first get()
    // wins, every subsequent test sees the cached value regardless of its own set() call. To
    // keep production reads cheap we layer test overrides on top of the cached spec read instead
    // of touching ConfigValue itself.
    private static final Map<ForgeConfigSpec.ConfigValue<?>, Object> TEST_OVERRIDES = new ConcurrentHashMap<>();

    public static <T> void setTestOverride(ForgeConfigSpec.ConfigValue<T> value, T override) {
        if (value == null) {
            return;
        }
        if (override == null) {
            TEST_OVERRIDES.remove(value);
        } else {
            TEST_OVERRIDES.put(value, override);
        }
    }

    public static void clearAllTestOverrides() {
        TEST_OVERRIDES.clear();
    }

    private static boolean resolveBoolean(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        Object override = TEST_OVERRIDES.get(value);
        if (override instanceof Boolean booleanOverride) {
            return booleanOverride;
        }
        try {
            return value.get();
        } catch (IllegalStateException exception) {
            return fallback;
        }
    }

    private static int resolveInt(ForgeConfigSpec.IntValue value, int fallback) {
        Object override = TEST_OVERRIDES.get(value);
        if (override instanceof Integer intOverride) {
            return intOverride;
        }
        try {
            return value.get();
        } catch (IllegalStateException exception) {
            return fallback;
        }
    }

    private static long resolveLong(ForgeConfigSpec.LongValue value, long fallback) {
        Object override = TEST_OVERRIDES.get(value);
        if (override instanceof Long longOverride) {
            return longOverride;
        }
        try {
            return value.get();
        } catch (IllegalStateException exception) {
            return fallback;
        }
    }

    private static double resolveDouble(ForgeConfigSpec.DoubleValue value, double fallback) {
        Object override = TEST_OVERRIDES.get(value);
        if (override instanceof Double doubleOverride) {
            return doubleOverride;
        }
        try {
            return value.get();
        } catch (IllegalStateException exception) {
            return fallback;
        }
    }
}
