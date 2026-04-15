package com.talhanation.workers;

import com.talhanation.bannermod.settlement.BannerModSettlementBinding;
import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.bannerlord.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannerlord.ai.civilian.animals.WorkerTemptGoal;
import com.talhanation.workers.settlement.WorkerSettlementSpawnRules;
import com.talhanation.workers.settlement.WorkerSettlementSpawner;
import com.talhanation.bannerlord.entity.civilian.workarea.MarketArea;
import com.talhanation.workers.init.ModEntityTypes;
import com.talhanation.bannerlord.persistence.military.RecruitsHireTradesRegistry;
import com.talhanation.workers.network.MessageToClientUpdateConfig;
import com.talhanation.bannerlord.persistence.military.RecruitsHireTrade;
import com.talhanation.workers.config.WorkersServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VillagerEvents {
    private static final Map<UUID, Long> SETTLEMENT_SPAWN_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> CLAIM_WORKER_GROWTH_SPAWN_TIMES = new HashMap<>();
    private static final Set<UUID> KNOWN_ADULT_VILLAGERS = new HashSet<>();
    public static final Component TITLE_FARMER = Component.translatable("description.bannermod.workers.title.farmer");
    public static final Component TITLE_MINER = Component.translatable("description.bannermod.workers.title.miner");
    public static final Component TITLE_LUMBERJACK = Component.translatable("description.bannermod.workers.title.lumberjack");
    public static final Component TITLE_BUILDER = Component.translatable("description.bannermod.workers.title.builder");
    public static final Component TITLE_MERCHANT = Component.translatable("description.bannermod.workers.title.merchant");
    public static final Component TITLE_FISHERMAN = Component.translatable("description.bannermod.workers.title.fisherman");
    public static final Component TITLE_ANIMAL_FARMER = Component.translatable("description.bannermod.workers.title.animalFarmer");
    public static final Component DESCRIPTION_FARMER = Component.translatable("description.bannermod.workers.farmer");
    public static final Component DESCRIPTION_MINER = Component.translatable("description.bannermod.workers.miner");
    public static final Component DESCRIPTION_LUMBERJACK = Component.translatable("description.bannermod.workers.lumberjack");
    public static final Component DESCRIPTION_BUILDER = Component.translatable("description.bannermod.workers.builder");
    public static final Component DESCRIPTION_MERCHANT = Component.translatable("description.bannermod.workers.merchant");
    public static final Component DESCRIPTION_FISHERMAN = Component.translatable("description.bannermod.workers.fisherman");
    public static final Component DESCRIPTION_ANIMAL_FARMER = Component.translatable("description.bannermod.workers.animalFarmer");

    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide()) return;

        if(event.getEntity() instanceof ServerPlayer player){
                WorkersRuntime.channel().send(PacketDistributor.PLAYER.with(() -> player),
                        new MessageToClientUpdateConfig(WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.get()));
        }
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        SETTLEMENT_SPAWN_COOLDOWNS.clear();
        CLAIM_WORKER_GROWTH_SPAWN_TIMES.clear();
        KNOWN_ADULT_VILLAGERS.clear();

        RecruitsHireTrade FARMER = new RecruitsHireTrade(ModEntityTypes.FARMER.getId(), WorkersServerConfig.FarmerCost.get(), TITLE_FARMER, DESCRIPTION_FARMER);
        RecruitsHireTrade LUMBERJACK = new RecruitsHireTrade(ModEntityTypes.LUMBERJACK.getId(), WorkersServerConfig.LumberjackCost.get(), TITLE_LUMBERJACK, DESCRIPTION_LUMBERJACK);
        RecruitsHireTrade MINER = new RecruitsHireTrade(ModEntityTypes.MINER.getId(), WorkersServerConfig.MinerCost.get(), TITLE_MINER, DESCRIPTION_MINER);
        RecruitsHireTrade MERCHANT = new RecruitsHireTrade(ModEntityTypes.MERCHANT.getId(), WorkersServerConfig.MerchantCost.get(), TITLE_MERCHANT, DESCRIPTION_MERCHANT);
        RecruitsHireTrade BUILDER = new RecruitsHireTrade(ModEntityTypes.BUILDER.getId(), WorkersServerConfig.BuilderCost.get(), TITLE_BUILDER, DESCRIPTION_BUILDER);

        RecruitsHireTrade FISHERMAN = new RecruitsHireTrade(ModEntityTypes.FISHERMAN.getId(), WorkersServerConfig.BuilderCost.get(), TITLE_FISHERMAN, DESCRIPTION_FISHERMAN);

        RecruitsHireTrade ANIMAL_FARMER = new RecruitsHireTrade(ModEntityTypes.ANIMAL_FARMER.getId(), WorkersServerConfig.BuilderCost.get(), TITLE_ANIMAL_FARMER, DESCRIPTION_ANIMAL_FARMER);


        RecruitsHireTradesRegistry.addTrade("workers", 1, FARMER, LUMBERJACK);
        RecruitsHireTradesRegistry.addTrade("workers", 2, ANIMAL_FARMER);
        RecruitsHireTradesRegistry.addTrade("workers", 3, BUILDER);

        RecruitsHireTradesRegistry.addTrade("workers2", 1, FARMER, MINER);
        RecruitsHireTradesRegistry.addTrade("workers2", 2, ANIMAL_FARMER);
        RecruitsHireTradesRegistry.addTrade("workers2", 3, BUILDER);

        RecruitsHireTradesRegistry.addTrade("workers3", 1, FARMER, FISHERMAN);
        RecruitsHireTradesRegistry.addTrade("workers3", 2, ANIMAL_FARMER);
        RecruitsHireTradesRegistry.addTrade("workers3", 3, BUILDER);

        /*
        RecruitsHireTradesRegistry.addTrade("herd", 1, ANIMAL_FARMER, FISHERMAN);
        RecruitsHireTradesRegistry.addTrade("herd", 2, MERCHANT);
        RecruitsHireTradesRegistry.addTrade("herd", 3, CHEF);

        RecruitsHireTradesRegistry.addTrade("herd", 1, ANIMAL_FARMER, BEE_KEEPER);
        RecruitsHireTradesRegistry.addTrade("herd", 2, MERCHANT);
        RecruitsHireTradesRegistry.addTrade("herd", 3, CHEF);
        */
    }

    @SubscribeEvent
    public void onVillagerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Villager villager)) {
            return;
        }

        if (!villager.isBaby()) {
            KNOWN_ADULT_VILLAGERS.add(villager.getUUID());
        }
    }

    @SubscribeEvent
    public void onVillagerLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Villager villager) || villager.level().isClientSide() || !(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (villager.isBaby()) {
            return;
        }

        if (KNOWN_ADULT_VILLAGERS.add(villager.getUUID())) {
            attemptBirthWorkerSpawn(serverLevel, villager);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        ServerLevel level = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer() == null
                ? null
                : net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().overworld();
        if (level == null || level.getGameTime() % 200L != 0L) {
            return;
        }

        runClaimWorkerGrowthPass(level);
    }

    @SubscribeEvent
    public void onAnimalJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Chicken chicken) {
            chicken.goalSelector.addGoal(3, new WorkerTemptGoal(chicken,1.0,
                    Ingredient.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.PUMPKIN_SEEDS,
                            Items.MELON_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD)));
        }
        else if(entity instanceof Cow cow) {
            cow.goalSelector.addGoal(3, new WorkerTemptGoal(cow,1.0, Ingredient.of(Items.WHEAT)));
        }
        else if(entity instanceof Sheep sheep) {
            sheep.goalSelector.addGoal(3, new WorkerTemptGoal(sheep,1.0, Ingredient.of(Items.WHEAT)));
        }
        else if(entity instanceof Pig pig) {
            pig.goalSelector.addGoal(3, new WorkerTemptGoal(pig,1.0,
                    Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT)));
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getLevel() == null || event.getEntity() == null) return;

        if(!(event instanceof PlayerInteractEvent.RightClickBlock) && !(event instanceof PlayerInteractEvent.LeftClickBlock)) {
            return;
        }

        if(disableInteractionInMarketArea(event.getEntity(), event.getLevel(), event.getPos())){
            event.setCanceled(true);
        }

    }

    public static boolean disableInteractionInMarketArea(Player player, Level level, BlockPos pos) {
        List<MarketArea> markets = level.getEntitiesOfClass(MarketArea.class, new AABB(pos).inflate(8));
        if (markets.isEmpty()) return false;

        markets.removeIf(marketArea -> {
            AABB area = marketArea.getArea();
            return !(pos.getX() >= area.minX && pos.getX() <= area.maxX
                    && pos.getY() >= area.minY && pos.getY() <= area.maxY
                    && pos.getZ() >= area.minZ && pos.getZ() <= area.maxZ);
        });
        if (markets.isEmpty()) return false;

        boolean isContainer = level.getBlockEntity(pos) instanceof Container;
        if (!isContainer) return false;

        MarketArea market = markets.get(0);
        UUID ownerUUID = market.getPlayerUUID();

        boolean isOwner  = ownerUUID != null && player.getUUID().equals(ownerUUID);
        boolean isAdmin  = player.isCreative() && player.hasPermissions(2);

        return !isOwner && !isAdmin;
    }

    public static AbstractWorkerEntity attemptBirthWorkerSpawn(ServerLevel level, Villager villager) {
        RecruitsClaim claim = resolveClaim(villager.blockPosition());
        if (claim == null) {
            return null;
        }

        int villagerCount = Math.max(1, countEntitiesInClaim(level, claim, Villager.class));

        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateBirth(
                resolveSettlementBinding(villager, claim),
                villagerCount,
                countEntitiesInClaim(level, claim, AbstractWorkerEntity.class),
                false,
                WorkersServerConfig.workerBirthRuleConfig()
        );

        return WorkerSettlementSpawner.spawnWorkerFromVillager(level, villager, decision, claim);
    }

    public static AbstractWorkerEntity attemptSettlementWorkerSpawn(ServerLevel level, Villager villager) {
        RecruitsClaim claim = resolveClaim(villager.blockPosition());
        if (claim == null) {
            return null;
        }

        long now = level.getGameTime();
        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateSettlementSpawn(
                resolveSettlementBinding(villager, claim),
                countEntitiesInClaim(level, claim, Villager.class),
                countEntitiesInClaim(level, claim, AbstractWorkerEntity.class),
                isSettlementSpawnOnCooldown(claim, now),
                WorkersServerConfig.workerSettlementSpawnRuleConfig()
        );

        AbstractWorkerEntity worker = WorkerSettlementSpawner.spawnWorkerFromVillager(level, villager, decision, claim);
        if (worker != null) {
            long cooldownTicks = WorkersServerConfig.settlementSpawnCooldownTicks();
            if (cooldownTicks > 0L) {
                SETTLEMENT_SPAWN_COOLDOWNS.put(claim.getUUID(), now + cooldownTicks);
            }
        }
        return worker;
    }

    public static void runClaimWorkerGrowthPass(ServerLevel level) {
        if (level == null || ClaimEvents.recruitsClaimManager == null) {
            return;
        }

        for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
            if (claim == null || claim.getOwnerFaction() == null) {
                continue;
            }
            attemptClaimWorkerGrowth(level, claim, claim.getOwnerFactionStringID(), level.getGameTime());
        }
    }

    public static AbstractWorkerEntity attemptClaimWorkerGrowth(ServerLevel level,
                                                                RecruitsClaim claim,
                                                                BannerModSettlementBinding.Binding binding,
                                                                long gameTime) {
        if (level == null || claim == null || binding == null) {
            return null;
        }

        int currentWorkerCount = countEntitiesInClaim(level, claim, AbstractWorkerEntity.class);
        long elapsedCooldownTicks = resolveClaimGrowthElapsedTicks(claim, gameTime);
        WorkerSettlementSpawnRules.ClaimGrowthConfig config = WorkersServerConfig.claimWorkerGrowthConfig();
        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                binding.status(),
                currentWorkerCount,
                elapsedCooldownTicks,
                config
        );
        WorkerSettlementSpawnRules.Decision deterministicDecision = applyClaimGrowthProfessionSeed(claim, currentWorkerCount, config, decision);
        if (!deterministicDecision.allowed()) {
            return null;
        }

        BlockPos spawnPos = resolveClaimGrowthSpawnPos(level, claim);
        AbstractWorkerEntity worker = WorkerSettlementSpawner.spawnClaimWorker(level, spawnPos, deterministicDecision, claim);
        if (worker != null) {
            CLAIM_WORKER_GROWTH_SPAWN_TIMES.put(claim.getUUID(), gameTime);
        }
        return worker;
    }

    public static AbstractWorkerEntity attemptClaimWorkerGrowth(ServerLevel level,
                                                                RecruitsClaim claim,
                                                                String settlementFactionId,
                                                                long gameTime) {
        return attemptClaimWorkerGrowth(level, claim, resolveClaimGrowthBinding(claim, settlementFactionId), gameTime);
    }

    private static boolean isSettlementSpawnOnCooldown(RecruitsClaim claim, long gameTime) {
        Long cooldownUntil = SETTLEMENT_SPAWN_COOLDOWNS.get(claim.getUUID());
        return cooldownUntil != null && cooldownUntil > gameTime;
    }

    private static long resolveClaimGrowthElapsedTicks(RecruitsClaim claim, long gameTime) {
        Long lastSpawnTime = CLAIM_WORKER_GROWTH_SPAWN_TIMES.get(claim.getUUID());
        if (lastSpawnTime == null) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, gameTime - lastSpawnTime);
    }

    private static WorkerSettlementSpawnRules.Decision applyClaimGrowthProfessionSeed(RecruitsClaim claim,
                                                                                       int currentWorkerCount,
                                                                                       WorkerSettlementSpawnRules.ClaimGrowthConfig config,
                                                                                       WorkerSettlementSpawnRules.Decision decision) {
        if (claim == null || config == null || decision == null || !decision.allowed() || config.allowedProfessions().isEmpty()) {
            return decision;
        }

        ChunkPos anchorChunk = resolveClaimAnchorChunk(claim);
        int professionIndex = Math.floorMod(anchorChunk.x * 31 + anchorChunk.z * 17 + currentWorkerCount, config.allowedProfessions().size());
        WorkerSettlementSpawnRules.WorkerProfession profession = config.allowedProfessions().get(professionIndex);
        return new WorkerSettlementSpawnRules.Decision(true, profession, null, decision.requiredCooldownTicks());
    }

    private static BannerModSettlementBinding.Binding resolveClaimGrowthBinding(RecruitsClaim claim, String settlementFactionId) {
        ChunkPos anchorChunk = resolveClaimAnchorChunk(claim);
        return BannerModSettlementBinding.resolveSettlementStatus(claim, anchorChunk, settlementFactionId);
    }

    private static ChunkPos resolveClaimAnchorChunk(RecruitsClaim claim) {
        if (claim.getCenter() != null) {
            return claim.getCenter();
        }
        if (!claim.getClaimedChunks().isEmpty()) {
            return claim.getClaimedChunks().get(0);
        }
        return new ChunkPos(0, 0);
    }

    private static BlockPos resolveClaimGrowthSpawnPos(ServerLevel level, RecruitsClaim claim) {
        ChunkPos anchorChunk = resolveClaimAnchorChunk(claim);
        BlockPos chunkCenter = new BlockPos(anchorChunk.getMiddleBlockX(), level.getSeaLevel(), anchorChunk.getMiddleBlockZ());
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, chunkCenter);
    }

    private static RecruitsClaim resolveClaim(BlockPos pos) {
        if (ClaimEvents.recruitsClaimManager == null) {
            return null;
        }
        return ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(pos));
    }

    private static BannerModSettlementBinding.Binding resolveSettlementBinding(Villager villager, RecruitsClaim claim) {
        String factionId = claim.getOwnerFaction() != null ? claim.getOwnerFactionStringID() : null;
        if (villager.getTeam() != null) {
            factionId = villager.getTeam().getName();
        }
        return BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, villager.blockPosition(), factionId);
    }

    private static <T extends Entity> int countEntitiesInClaim(ServerLevel level, RecruitsClaim claim, Class<T> entityType) {
        AABB claimBounds = getClaimBounds(level, claim);
        return level.getEntitiesOfClass(entityType, claimBounds, entity -> entity.isAlive() && claim.containsChunk(entity.chunkPosition())).size();
    }

    private static AABB getClaimBounds(ServerLevel level, RecruitsClaim claim) {
        int minChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).min().orElse(claim.getCenter().x);
        int maxChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).max().orElse(claim.getCenter().x);
        int minChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).min().orElse(claim.getCenter().z);
        int maxChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).max().orElse(claim.getCenter().z);
        return new AABB(
                minChunkX * 16.0D,
                level.getMinBuildHeight(),
                minChunkZ * 16.0D,
                (maxChunkX + 1) * 16.0D,
                level.getMaxBuildHeight(),
                (maxChunkZ + 1) * 16.0D
        );
    }
}
