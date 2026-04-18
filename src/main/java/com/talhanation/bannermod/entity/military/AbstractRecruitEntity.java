package com.talhanation.bannermod.entity.military;
import com.talhanation.bannermod.bootstrap.BannerModMain;
//ezgi&talha kantar

import com.talhanation.bannermod.citizen.CitizenCore;
import com.talhanation.bannermod.citizen.CitizenPersistenceBridge;
import com.talhanation.bannermod.citizen.CitizenRole;
import com.talhanation.bannermod.citizen.CitizenRoleController;
import com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus;
import com.talhanation.bannermod.events.*;
import com.talhanation.bannermod.events.RecruitEvent;
import com.talhanation.bannermod.compat.IWeapon;
import com.talhanation.bannermod.config.RecruitsClientConfig;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.ai.military.*;
import com.talhanation.bannermod.ai.military.async.AsyncManager;
import com.talhanation.bannermod.ai.military.async.AsyncTaskWithCallback;
import com.talhanation.bannermod.ai.military.controller.RecruitCommandStateTransitions;
import com.talhanation.bannermod.ai.military.compat.BlockWithWeapon;
import com.talhanation.bannermod.ai.military.navigation.RecruitPathNavigation;
import com.talhanation.bannermod.ai.military.navigation.RecruitsOpenDoorGoal;
import com.talhanation.bannermod.registry.military.ModItems;
import com.talhanation.bannermod.inventory.military.DebugInvMenu;
import com.talhanation.bannermod.inventory.military.RecruitHireMenu;
import com.talhanation.bannermod.inventory.military.RecruitInventoryMenu;
import com.talhanation.bannermod.network.messages.military.*;
import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractRecruitEntity extends AbstractInventoryEntity{
    private static final int TARGET_STICKINESS_RANK_WINDOW = 3;
    private static final double TARGET_STICKINESS_DISTANCE_BUFFER_SQR = 36.0D;
    private static final TargetSearchProfilingCounters TARGET_SEARCH_PROFILING = new TargetSearchProfilingCounters();
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOLLOW_STATE = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHOULD_FOLLOW = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_BLOCK = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_MOUNT = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_PROTECT = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_HOLD_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_MOVE_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> HOLD_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> MOVE_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> UPKEEP_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> LISTEN = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FOLLOWING = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> MOUNT_ID = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> PROTECT_ID = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> GROUP = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> KILLS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLEEING = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MORAL = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> OWNED = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COST = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> UPKEEP_ID = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> BIOME = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> SHOULD_REST = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_RANGED = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    public int blockCoolDown;
    public boolean needsTeamUpdate = true;
    public boolean needsGroupUpdate = true;
    public boolean forcedUpkeep;
    public int dismount = 0;
    public int upkeepTimer = 0;
    public int mountTimer = 0;
    public int despawnTimer = -1;
    public boolean reachedMovePos;
    public int attackCooldown = 0;
    public int paymentTimer;
    public boolean rotate;
    public float ownerRot;
    public int rotateTicks;
    public int formationPos = -1;
    private int maxFallDistance;
    private final int tickOffset = (int)(System.nanoTime() % 20);
    public Vec3 holdPosVec;
    public boolean isInFormation;
    public boolean needsColorUpdate = true;
    public float moveSpeed = 1;
    public TargetingConditions targetingConditions;
    private final CitizenCore citizenCore = RecruitCitizenBridge.createCore(this);
    private CitizenRoleController citizenRoleController = CitizenRoleController.noop(CitizenRole.RECRUIT);

    public AbstractRecruitEntity(EntityType<? extends AbstractInventoryEntity> entityType, Level world) {
        super(entityType, world);
        this.xpReward = 6;
        this.navigation = this.createNavigation(world);
        this.targetingConditions = TargetingConditions.forCombat().ignoreInvisibilityTesting().selector(this::shouldAttack);
        this.setMaxUpStep(1F);
        this.setMaxFallDistance(1);
    }

    ///////////////////////////////////NAVIGATION/////////////////////////////////////////
    @NotNull
    protected PathNavigation createNavigation(@NotNull Level level) {
        return new RecruitPathNavigation(this, level);
    }

    public @NotNull PathNavigation getNavigation() {
        return super.getNavigation();
    }

    public CitizenCore getCitizenCore() {
        return this.citizenCore;
    }

    public CitizenRole getCitizenRole() {
        return CitizenRole.RECRUIT;
    }

    public CitizenRoleController getCitizenRoleController() {
        return this.citizenRoleController;
    }

    public void setCitizenRoleController(CitizenRoleController controller) {
        this.citizenRoleController = controller == null ? CitizenRoleController.noop(this.getCitizenRole()) : controller;
    }

    public void rideTick() {
        super.rideTick();
    }

    public double getMyRidingOffset() {
        return -0.35D;
    }

    public int getMaxFallDistance() {
        return maxFallDistance;
    }

    public void setMaxFallDistance(int x){
        this.maxFallDistance = x;
    }

    ///////////////////////////////////TICK/////////////////////////////////////////

    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        if(this.rotateTicks > 0 && this.getNavigation().isDone()) {
            this.yBodyRot = this.ownerRot;
            this.yHeadRot = this.ownerRot;
            return 0;
        }
        return super.tickHeadTurn(yRot, animStep);
    }

    // @Override
    public void aiStep(){
        super.aiStep();
        updateSwingTime();
        updateShield();

        if (this.getCommandSenderWorld().isClientSide()) return;

        if(needsColorUpdate && this.getTeam() != null) updateColor(this.getTeam().getName());
        if(this instanceof IRangedRecruit  && (this.tickCount + this.tickOffset) % 20 == 0) pickUpArrows();
        if(needsTeamUpdate) updateTeam();
        if(needsGroupUpdate) updateGroup();

    }
    public void tick() {
        super.tick();
        if(this.level().isClientSide()) return;

        if(despawnTimer > 0) despawnTimer--;
        if(despawnTimer == 0) recruitCheckDespawn();

        if(RecruitsServerConfig.RecruitsPayment.get()){
            if(paymentTimer > 0) paymentTimer--;
            if(paymentTimer == 0) {
                if(getUpkeepPos() != null || getUpkeepUUID() != null) forcedUpkeep = true;
                else checkPayment(this.getInventory());
            }
        }

        if(getMountTimer() > 0) setMountTimer(getMountTimer() - 1);
        if(getUpkeepTimer() > 0) setUpkeepTimer(getUpkeepTimer() - 1);
        if(getHunger() >=  70F && getHealth() < getMaxHealth()){
            this.heal(1.0F/50F);// 1 hp in 2.5s
        }

        int resolvedFollowState = RecruitCommandStateTransitions.afterMoveArrival(this.reachedMovePos, this.getFollowState());
        if(this.reachedMovePos){
            this.setFollowState(resolvedFollowState);
            this.reachedMovePos = false;
        }

        if(this.attackCooldown > 0) this.attackCooldown--;


        if(this.isAlive() && this.getState() != 3){
            RecruitAiLodPolicy.Evaluation targetSearchLod = evaluateTargetSearchLod();
            TARGET_SEARCH_PROFILING.recordLodTier(targetSearchLod.tier());

            if (isBaseTargetSearchTick()) {
                TARGET_SEARCH_PROFILING.recordSearchOpportunity();
                if (targetSearchLod.shouldRunSearch()) {
                    this.searchForTargets();
                }
                else {
                    TARGET_SEARCH_PROFILING.recordLodSkip();
                }
            }
        }

        LivingEntity currentTarget = this.getTarget();
        if(currentTarget != null && (currentTarget.isDeadOrDying() || currentTarget.isRemoved())) this.setTarget(null);

        // Handle face rotation command
        if(this.rotateTicks > 0) {
            if(this.getNavigation().isDone()) {
                this.setYRot(this.ownerRot);
                this.yRotO = this.ownerRot;
                this.rotateTicks--;
            }
        }

    }

    public void searchForTargets() {
        if (!(this.getCommandSenderWorld() instanceof ServerLevel serverLevel)) return;

        TARGET_SEARCH_PROFILING.recordSearch();
        if(RecruitsServerConfig.UseAsyncTargetFinding.get()) searchForTargetsAsync(serverLevel);
        else searchForTargetsSync(serverLevel);
    }

    private boolean isBaseTargetSearchTick() {
        return (this.tickCount + this.tickOffset) % RecruitAiLodPolicy.DEFAULT_FULL_SEARCH_INTERVAL == 0;
    }

    private RecruitAiLodPolicy.Evaluation evaluateTargetSearchLod() {
        RecruitAiLodPolicy.Settings settings = RecruitAiLodPolicy.settingsFromConfig();
        LivingEntity currentTarget = this.getTarget();
        boolean hasLiveTarget = currentTarget != null && currentTarget.isAlive() && !currentTarget.isRemoved();
        double liveTargetDistanceSqr = hasLiveTarget ? this.distanceToSqr(currentTarget) : Double.POSITIVE_INFINITY;
        double maxRelevantDistance = settings.reducedDistance();
        Player nearbyPlayer = maxRelevantDistance > 0 ? this.level().getNearestPlayer(this, maxRelevantDistance) : null;
        double nearestPlayerDistanceSqr = nearbyPlayer != null ? this.distanceToSqr(nearbyPlayer) : Double.POSITIVE_INFINITY;

        return RecruitAiLodPolicy.evaluate(new RecruitAiLodPolicy.Context(
                this.hurtTime > 0,
                hasLiveTarget,
                liveTargetDistanceSqr,
                nearestPlayerDistanceSqr,
                this.tickCount,
                this.tickOffset
        ), settings);
    }

    private void searchForTargetsAsync(ServerLevel serverLevel) {
        FormationTargetSelectionController.RuntimeSelectionRequest selectionRequest = createFormationSelectionRequest();
        FormationTargetSelectionController.Decision<LivingEntity> selectionDecision = FormationTargetSelectionController.beginRuntimeSelection(
                selectionRequest,
                this::isValidSharedTarget
        );

        if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.REUSED_SHARED_SELECTION) {
            if (selectionDecision.target() != null) {
                TARGET_SEARCH_PROFILING.recordTargetAssigned();
            }
            applyCombatTarget(selectionDecision.target());
            return;
        }

        NearbyCombatCandidates scan = scanNearbyCombatCandidates(serverLevel, 40D);
        List<LivingEntity> nearby = scan.candidates();
        TARGET_SEARCH_PROFILING.recordAsyncSearch(scan.observedCount());

        if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION) {
            List<LivingEntity> candidates = filterCombatCandidates(nearby, this::isValidSharedTarget, true);

            LivingEntity target = FormationTargetSelectionController.completeRuntimeSelection(
                    selectionRequest,
                    resolveCombatTargetFromCandidates(candidates)
            );

            if (target != null) {
                TARGET_SEARCH_PROFILING.recordTargetAssigned();
            }
            applyCombatTarget(target);
            return;
        }

        // MULTI THREADED
        Supplier<List<LivingEntity>> findTargetsTask = () -> {
            return filterCombatCandidates(nearby, this::isValidSharedTarget, true);
        };

        Consumer<List<LivingEntity>> handleTargets = targets -> {
            if (!this.isAlive() || this.isRemoved()) {
                return;
            }

            LivingEntity target = resolveCombatTargetFromCandidates(targets);
            if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION) {
                target = FormationTargetSelectionController.completeRuntimeSelection(selectionRequest, target);
            }

            if (target != null) {
                TARGET_SEARCH_PROFILING.recordTargetAssigned();
            }
            applyCombatTarget(target);
        };

        AsyncManager.executor.execute(new AsyncTaskWithCallback<>(findTargetsTask, handleTargets, serverLevel));
    }

    private void searchForTargetsSync(ServerLevel serverLevel) {
        FormationTargetSelectionController.RuntimeSelectionRequest selectionRequest = createFormationSelectionRequest();
        FormationTargetSelectionController.Decision<LivingEntity> selectionDecision = FormationTargetSelectionController.beginRuntimeSelection(
                selectionRequest,
                this::isValidSharedTarget
        );

        if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.REUSED_SHARED_SELECTION) {
            if (selectionDecision.target() != null) {
                TARGET_SEARCH_PROFILING.recordTargetAssigned();
            }
            applyCombatTarget(selectionDecision.target());
            return;
        }

        NearbyCombatCandidates scan = scanNearbyCombatCandidates(serverLevel, 40D);
        TARGET_SEARCH_PROFILING.recordSyncSearch(scan.observedCount());

        List<LivingEntity> nearby = filterCombatCandidates(scan.candidates(), this::isValidSharedTarget, true);

        LivingEntity target = resolveCombatTargetFromCandidates(nearby);
        if (selectionDecision.type() == FormationTargetSelectionController.DecisionType.COMPUTE_SHARED_SELECTION) {
            target = FormationTargetSelectionController.completeRuntimeSelection(selectionRequest, target);
        }

        if (target != null) {
            TARGET_SEARCH_PROFILING.recordTargetAssigned();
        }
        applyCombatTarget(target);
    }

    private FormationTargetSelectionController.RuntimeSelectionRequest createFormationSelectionRequest() {
        clearInvalidTargetForSelection();
        UUID ownerId = this.getOwnerUUID();
        UUID groupId = this.getGroup();
        return new FormationTargetSelectionController.RuntimeSelectionRequest(
                ownerId,
                groupId,
                isEligibleForFormationTargetSelection(ownerId, groupId),
                this.level().getGameTime()
        );
    }

    private boolean isEligibleForFormationTargetSelection(@Nullable UUID ownerId, @Nullable UUID groupId) {
        if (ownerId == null || groupId == null) {
            return false;
        }
        if (this.getState() != 1 || !this.isAlive()) {
            return false;
        }
        return this.isInFormation || this.getFollowState() == 2 || this.getFollowState() == 3 || this.getFollowState() == 5;
    }

    private void clearInvalidTargetForSelection() {
        RecruitCombatTargeting.clearInvalidTargetForSelection(this, this::isValidSharedTarget);
    }

    private boolean isValidSharedTarget(@Nullable LivingEntity target) {
        return target != null
                && target.isAlive()
                && !target.isRemoved()
                && this.targetingConditions.test(this, target);
    }

    protected NearbyCombatCandidates scanNearbyCombatCandidates(ServerLevel serverLevel, double radius) {
        return RecruitCombatTargeting.scanNearbyCombatCandidates(this, serverLevel, radius);
    }

    protected List<LivingEntity> filterCombatCandidates(List<LivingEntity> candidates, Predicate<LivingEntity> filter, boolean sortByDistance) {
        return RecruitCombatTargeting.filterCombatCandidates(this, candidates, filter, sortByDistance);
    }

    private @Nullable LivingEntity resolveCombatTargetFromCandidates(List<LivingEntity> targets) {
        return RecruitCombatTargeting.resolveCombatTargetFromCandidates(this, targets, this::isValidSharedTarget);
    }

    public boolean canAssignCombatTarget(@Nullable LivingEntity target) {
        return RecruitCombatTargeting.canAssignCombatTarget(this, target);
    }

    public boolean assignOrderedCombatTarget(@Nullable LivingEntity target) {
        return RecruitCombatTargeting.assignOrderedCombatTarget(this, target);
    }

    public boolean assignReactiveCombatTarget(@Nullable LivingEntity target) {
        return RecruitCombatTargeting.assignReactiveCombatTarget(this, target);
    }

    private void applyCombatTarget(@Nullable LivingEntity target) {
        RecruitCombatTargeting.applyCombatTarget(this, target);
    }

    public static void resetTargetSearchProfiling() {
        TARGET_SEARCH_PROFILING.reset();
    }

    protected static record NearbyCombatCandidates(int observedCount, List<LivingEntity> candidates) {
    }

    public static TargetSearchProfilingSnapshot targetSearchProfilingSnapshot() {
        return TARGET_SEARCH_PROFILING.snapshot();
    }

    public record TargetSearchProfilingSnapshot(
            long searchOpportunities,
            long totalSearches,
            long asyncSearches,
            long syncSearches,
            long candidateEntitiesObserved,
            long targetsAssigned,
            long lodSkippedSearches,
            long lodFullTierTicks,
            long lodReducedTierTicks,
            long lodShedTierTicks
    ) {
    }

    private static final class TargetSearchProfilingCounters {
        private final LongAdder searchOpportunities = new LongAdder();
        private final LongAdder totalSearches = new LongAdder();
        private final LongAdder asyncSearches = new LongAdder();
        private final LongAdder syncSearches = new LongAdder();
        private final LongAdder candidateEntitiesObserved = new LongAdder();
        private final LongAdder targetsAssigned = new LongAdder();
        private final LongAdder lodSkippedSearches = new LongAdder();
        private final LongAdder lodFullTierTicks = new LongAdder();
        private final LongAdder lodReducedTierTicks = new LongAdder();
        private final LongAdder lodShedTierTicks = new LongAdder();

        private void recordSearchOpportunity() {
            searchOpportunities.increment();
        }

        private void recordSearch() {
            totalSearches.increment();
        }

        private void recordAsyncSearch(int candidates) {
            asyncSearches.increment();
            candidateEntitiesObserved.add(candidates);
        }

        private void recordSyncSearch(int candidates) {
            syncSearches.increment();
            candidateEntitiesObserved.add(candidates);
        }

        private void recordTargetAssigned() {
            targetsAssigned.increment();
        }

        private void recordLodSkip() {
            lodSkippedSearches.increment();
        }

        private void recordLodTier(RecruitAiLodPolicy.LodTier tier) {
            switch (tier) {
                case FULL -> lodFullTierTicks.increment();
                case REDUCED -> lodReducedTierTicks.increment();
                case SHED -> lodShedTierTicks.increment();
            }
        }

        private void reset() {
            searchOpportunities.reset();
            totalSearches.reset();
            asyncSearches.reset();
            syncSearches.reset();
            candidateEntitiesObserved.reset();
            targetsAssigned.reset();
            lodSkippedSearches.reset();
            lodFullTierTicks.reset();
            lodReducedTierTicks.reset();
            lodShedTierTicks.reset();
        }

        private TargetSearchProfilingSnapshot snapshot() {
            return new TargetSearchProfilingSnapshot(
                    searchOpportunities.sum(),
                    totalSearches.sum(),
                    asyncSearches.sum(),
                    syncSearches.sum(),
                    candidateEntitiesObserved.sum(),
                    targetsAssigned.sum(),
                    lodSkippedSearches.sum(),
                    lodFullTierTicks.sum(),
                    lodReducedTierTicks.sum(),
                    lodShedTierTicks.sum()
            );
        }
    }

    private void recruitCheckDespawn() {
        if(this.isOwned()) return;
        Entity entity = this.getCommandSenderWorld().getNearestPlayer(this, -1.0D);

        if (entity != null) {
            double d0 = entity.distanceToSqr(this);
            int k = this.getType().getCategory().getNoDespawnDistance();
            int l = k * k;

            if (this.random.nextInt(800) == 0 && d0 > (double) l) {
                if(this.getVehicle() instanceof LivingEntity livingMount) livingMount.discard();
                this.discard();
            }
        }
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag nbt) {
        return RecruitSpawnService.prepareBaseRecruitSpawn(this, world, spawnData);
    }

    protected final SpawnGroupData finishRecruitLeafSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, SpawnGroupData spawnData, boolean canOpenDoors, boolean enchantEquipment) {
        return RecruitSpawnService.finishLeafRecruitSpawn(this, world, difficulty, spawnData, canOpenDoors, enchantEquipment);
    }

    void rebuildSpawnNavigation(ServerLevelAccessor world) {
        this.createNavigation(world.getLevel());
    }

    void enableRecruitSpawnDoors() {
        if (this.getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(true);
        }
    }

    void applyRecruitSpawnEnchantments(ServerLevelAccessor world, DifficultyInstance difficulty) {
        this.populateDefaultEquipmentEnchantments(world.getRandom(), difficulty);
    }

    protected final void initStandardRecruitSpawn(String defaultName, int cost) {
        RecruitSpawnService.initStandardRecruitSpawn(this, defaultName, cost);
    }

    protected final void initPersistentNamedSpawn(String defaultName) {
        RecruitSpawnService.initPersistentNamedSpawn(this, defaultName);
    }

    public void setRandomSpawnBonus(){
        RecruitSpawnService.setRandomSpawnBonus(this);
    }

    ////////////////////////////////////REGISTER////////////////////////////////////

    protected void registerGoals() {
        this.goalSelector.addGoal(4, new BlockWithWeapon(this));
        this.goalSelector.addGoal(0, new RecruitFloatGoal(this));
        this.goalSelector.addGoal(1, new RecruitQuaffGoal(this));
        this.goalSelector.addGoal(1, new FleeTNT(this));
        this.goalSelector.addGoal(1, new FleeFire(this));
        this.goalSelector.addGoal(6, new RecruitsOpenDoorGoal(this, true) {});
        this.goalSelector.addGoal(1, new RecruitProtectEntityGoal(this));
        this.goalSelector.addGoal(0, new RecruitEatGoal(this));
        this.goalSelector.addGoal(5, new RecruitUpkeepPosGoal(this));
        this.goalSelector.addGoal(6, new RecruitUpkeepEntityGoal(this));
        this.goalSelector.addGoal(3, new RecruitMountEntity(this));
        this.goalSelector.addGoal(3, new RecruitDismountEntity(this));
        this.goalSelector.addGoal(3, new RecruitMoveToPosGoal(this, 1.05D));
        this.goalSelector.addGoal(2, new RecruitFollowOwnerGoal(this, 1.05D, 300, 100));
        this.goalSelector.addGoal(2, new RecruitMeleeAttackGoal(this, 1.05D, this.getMeleeStartRange()));
        this.goalSelector.addGoal(3, new RecruitHoldPosGoal(this, 32.0F));
        //this.goalSelector.addGoal(7, new RecruitDodgeGoal(this));
        this.goalSelector.addGoal(4, new RestGoal(this));
        this.goalSelector.addGoal(10, new RecruitWanderGoal(this));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));
        //this.goalSelector.addGoal(13, new RecruitPickupWantedItemGoal(this));

        this.targetSelector.addGoal(1, new RecruitProtectHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new RecruitOwnerHurtByTargetGoal(this));

        this.targetSelector.addGoal(3, (new RecruitHurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(4, new RecruitOwnerHurtTargetGoal(this));

        this.targetSelector.addGoal(7, new RecruitDefendVillageFromPlayerGoal(this));
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
        this.entityData.define(GROUP, Optional.empty());
        this.entityData.define(SHOULD_FOLLOW, false);
        this.entityData.define(SHOULD_BLOCK, false);
        this.entityData.define(SHOULD_MOUNT, false);
        this.entityData.define(SHOULD_PROTECT, false);
        this.entityData.define(SHOULD_HOLD_POS, false);
        this.entityData.define(SHOULD_MOVE_POS, false);
        this.entityData.define(FLEEING, false);
        this.entityData.define(STATE, 0);
        this.entityData.define(VARIANT, 0);
        this.entityData.define(XP, 0);
        this.entityData.define(KILLS, 0);
        this.entityData.define(LEVEL, 1);
        this.entityData.define(FOLLOW_STATE, 0);
        this.entityData.define(HOLD_POS, Optional.empty());
        this.entityData.define(UPKEEP_POS, Optional.empty());
        this.entityData.define(MOVE_POS, Optional.empty());
        this.entityData.define(LISTEN, true);
        this.entityData.define(MOUNT_ID, Optional.empty());
        this.entityData.define(PROTECT_ID, Optional.empty());
        this.entityData.define(IS_FOLLOWING, false);
        this.entityData.define(HUNGER, 50F);
        this.entityData.define(MORAL, 50F);
        this.entityData.define(OWNER_ID, Optional.empty());
        this.entityData.define(UPKEEP_ID, Optional.empty());
        this.entityData.define(OWNED, false);
        this.entityData.define(COST, 1);
        this.entityData.define(COLOR, (byte) 0);
        this.entityData.define(BIOME, (byte) 0);
        this.entityData.define(SHOULD_REST, false);
        this.entityData.define(SHOULD_RANGED, true);
        //STATE
        // 0 = NEUTRAL
        // 1 = AGGRESSIVE
        // 2 = RAID
        // 3 = PASSIVE

        //FOLLOW
        //0 = wander
        //1 = follow
        //2 = hold position
        //3 = back to position
        //4 = hold my position
        //5 = Protect
        //6 = Work

    }
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        RecruitPersistenceBridge.writeRecruitData(this, nbt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        RecruitPersistenceBridge.readRecruitData(this, nbt);
    }

    ////////////////////////////////////GET////////////////////////////////////

    public int getUpkeepTimer(){
        return this.upkeepTimer;
    }

    public int getVariant() {
        return entityData.get(VARIANT);
    }
    public int getBlockCoolDown(){
        return 200;
    }
    public UUID getUpkeepUUID(){
        return  this.entityData.get(UPKEEP_ID).orElse(null);
    }
    public BlockPos getUpkeepPos(){
        return entityData.get(UPKEEP_POS).orElse(null);
    }

    @Nullable
    public Player getOwner(){
        if (this.getOwnerUUID() != null){
            UUID ownerID = this.getOwnerUUID();
            return this.getCommandSenderWorld().getPlayerByUUID(ownerID);
        }
        else
            return null;
    }

    public UUID getOwnerUUID(){
        return  this.entityData.get(OWNER_ID).orElse(null);
    }

    public UUID getProtectUUID(){
        return  this.entityData.get(PROTECT_ID).orElse(null);
    }

    public UUID getMountUUID(){
        return  this.entityData.get(MOUNT_ID).orElse(null);
    }

    public boolean getIsOwned() {
        return entityData.get(OWNED);
    }

    public float getMorale() {
        return this.entityData.get(MORAL);
    }

    public float getHunger() {
        return this.entityData.get(HUNGER);
    }
    public float getAttackDamage(){
        return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public float getMovementSpeed(){
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    public boolean getFleeing() {
        return entityData.get(FLEEING);
    }

    public int getKills() {
        return entityData.get(KILLS);
    }

    public int getXpLevel() {
        return entityData.get(LEVEL);
    }

    public int getXp() {
        return entityData.get(XP);
    }


    public boolean getShouldMovePos() {
        return entityData.get(SHOULD_MOVE_POS);
    }
    public boolean getShouldHoldPos() {
        return entityData.get(SHOULD_HOLD_POS);
    }

    public boolean getShouldMount() {
        return entityData.get(SHOULD_MOUNT);
    }

    public boolean getShouldProtect() {
        return entityData.get(SHOULD_PROTECT);
    }

    public boolean getShouldFollow() {
        return entityData.get(SHOULD_FOLLOW);
    }

    public boolean getShouldBlock() {
        return entityData.get(SHOULD_BLOCK);
    }

    public boolean isFollowing(){
        return entityData.get(IS_FOLLOWING);
    }
    public boolean getShouldRest() {
        return entityData.get(SHOULD_REST);
    }

    public boolean getShouldRanged() {
        return entityData.get(SHOULD_RANGED);
    }
    public int getState() {
        return entityData.get(STATE);
    }
    //STATE
    // 0 = NEUTRAL
    // 1 = AGGRESSIVE
    // 2 = RAID
    // 3 = PASSIVE
    public UUID getGroup(){
        return getGroupUUID().isPresent() ? getGroupUUID().get() : null;
    }
    public Optional<UUID> getGroupUUID() {
        return this.entityData.get(GROUP);
    }


    //FOLLOW
    //0 = wander
    //1 = follow
    //2 = hold your position
    //3 = back to position
    //4 = hold my position
    //5 = Protect
    //6 = Work
    public int getFollowState(){
        return entityData.get(FOLLOW_STATE);
    }

    /**
     * Safe accessor for {@code RecruitsClientConfig.RecruitsLookLikeVillagers} that
     * returns the config default ({@code true}) when Forge config has not loaded yet
     * (e.g. gametest harness early-tick).
     */
    static boolean recruitsLookLikeVillagers() {
        try {
            return RecruitsClientConfig.RecruitsLookLikeVillagers.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public SoundEvent getHurtSound(@NotNull DamageSource ds) {
        if (this.isBlocking())
            return SoundEvents.SHIELD_BLOCK;
        return recruitsLookLikeVillagers() ? SoundEvents.VILLAGER_HURT : SoundEvents.GENERIC_HURT;
    }

    protected SoundEvent getDeathSound() {
        return recruitsLookLikeVillagers() ? SoundEvents.VILLAGER_DEATH : SoundEvents.GENERIC_DEATH;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    protected float getStandingEyeHeight(@NotNull Pose pos, EntityDimensions size) {
        return size.height * 0.98F;
    }

    public int getMaxHeadXRot() {
        return super.getMaxHeadXRot();
    }

    public int getMaxSpawnClusterSize() {
        return 8;
    }

    public Vec3 getHoldPos(){
        return this.holdPosVec;
        //return entityData.get(HOLD_POS).orElse(null);
    }

    @Nullable
    public BlockPos getMovePos(){
        return entityData.get(MOVE_POS).orElse(null);
    }

    public boolean getListen() {
        return entityData.get(LISTEN);
    }

    @Nullable
    public LivingEntity getProtectingMob(){
        List<LivingEntity> list = this.getCommandSenderWorld().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(64D),
                (living) -> this.getProtectUUID() != null && living.getUUID().equals(this.getProtectUUID()) && living.isAlive()
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    public int getBiome() {
        return entityData.get(BIOME);
    }
    public DyeColor getDyeColor() {
        return DyeColor.byId(getColor());
    }

    ////////////////////////////////////SET////////////////////////////////////

    public void setUpkeepTimer(int x){
        this.upkeepTimer =  x;
    }
    public void setVariant(int variant){
        entityData.set(VARIANT, variant);
    }
    public void setColor(byte color){
        entityData.set(COLOR, color);
    }
    public void setBiome(byte biome){
        entityData.set(BIOME, biome);
    }
    public void setUpkeepUUID(Optional<UUID> id) {
        this.entityData.set(UPKEEP_ID, id);
    }
    public void setCost(int cost){
        entityData.set(COST, cost);
    }
    public void setUpkeepPos(BlockPos pos){
        this.entityData.set(UPKEEP_POS, Optional.of(pos));
    }

    public void setIsOwned(boolean bool){
        entityData.set(OWNED, bool);
    }

    public void setOwnerUUID(Optional<UUID> id) {
        this.entityData.set(OWNER_ID,id);
    }

    public void setProtectUUID(Optional<UUID> id) {
        this.entityData.set(PROTECT_ID, id);
    }

    public void setMountUUID(Optional<UUID> id) {
        this.entityData.set(MOUNT_ID, id);
    }

    public void setMoral(float value) {
        this.entityData.set(MORAL, value);
        this.applyMoralEffects();
    }

    public void setHunger(float value) {
        float currentHunger = getHunger();
        if(value < 0 && currentHunger - value <= 0)
            this.entityData.set(HUNGER, 0F);
        else
            this.entityData.set(HUNGER, value);
    }

    public void setFleeing(boolean bool){
        entityData.set(FLEEING, bool);
    }
    public void setMountTimer(int x){
        this.mountTimer = x;
    }

    public void disband(@Nullable Player player, boolean keepTeam, boolean increaseCost){
        RecruitLifecycleService.disband(this, player, keepTeam, increaseCost, player == null ? null : TEXT_DISBAND(this.getName().getString()));
    }

    public void addXpLevel(int level){
        RecruitProgressionService.addXpLevel(this, level);
    }

    public void setKills(int kills){
        this.entityData.set(KILLS, kills);
    }

    public void setXpLevel(int XpLevel){
        this.entityData.set(LEVEL, XpLevel);
    }

    public void setXp(int xp){
        this.entityData.set(XP, xp);
    }

    public void addXp(int xp){
        int currentXp = this.getXp();
        int newXp = currentXp + xp;

        this.entityData.set(XP, newXp);
    }

    public void setShouldHoldPos(boolean bool){
        entityData.set(SHOULD_HOLD_POS, bool);
    }

    public void setShouldMovePos(boolean bool){
        entityData.set(SHOULD_MOVE_POS, bool);
    }
    public void setShouldProtect(boolean bool){
        entityData.set(SHOULD_PROTECT, bool);
    }

    public void setShouldMount(boolean bool){
        entityData.set(SHOULD_MOUNT, bool);
    }

    public void setShouldFollow(boolean bool){
        entityData.set(SHOULD_FOLLOW, bool);
    }

    public void setShouldBlock(boolean bool){
        entityData.set(SHOULD_BLOCK, bool);
    }

    public void setIsFollowing(boolean bool){
        entityData.set(IS_FOLLOWING, bool);
    }

    public void setGroupUUID(UUID uuid){
        entityData.set(GROUP, uuid == null ? Optional.empty() : Optional.of(uuid));
    }
    public void setShouldRest(boolean bool){
        if(bool) setFollowState(0);
        entityData.set(SHOULD_REST, bool);
    }

    public void setShouldRanged(boolean should) {
        entityData.set(SHOULD_RANGED, should);
    }

    public void setAggroState(int state) {
        RecruitCommandStateService.setAggroState(this, state);
    }

    //STATE
    // 0 = NEUTRAL
    // 1 = AGGRESSIVE
    // 2 = RAID
    // 3 = PASSIVE

    //FOLLOW
    //0 = wander
    //1 = follow
    //2 = hold position
    //3 = back to position
    //4 = hold my position
    //5 = Protect
    //6 = Work
    public void setFollowState(int state){
        RecruitCommandStateService.setFollowState(this, state);
    }

    public void setHoldPos(Vec3 holdPos){
        RecruitCommandStateService.setHoldPos(this, holdPos);
    }
    public void setMovePos(BlockPos holdPos){
        RecruitCommandStateService.setMovePos(this, holdPos);
    }

    public void clearHoldPos(){
        RecruitCommandStateService.clearHoldPos(this);
    }

    public void clearMovePos(){
        RecruitCommandStateService.clearMovePos(this);
    }

    public static void hydrateCitizenStateFromLegacy(CitizenCore citizenCore, CompoundTag nbt) {
        citizenCore.apply(CitizenPersistenceBridge.fromRecruitLegacy(nbt));
    }

    public static CompoundTag persistCitizenStateToLegacy(CitizenCore citizenCore, CompoundTag nbt) {
        return CitizenPersistenceBridge.writeRecruitLegacy(citizenCore.snapshot(), nbt);
    }

    public void setListen(boolean bool) {
        entityData.set(LISTEN, bool);
    }

    public void setEquipment(){
        RecruitEquipmentLoadoutService.applyRandomEquipmentSet(this);
    }

    @Override
    public void setTarget(@Nullable LivingEntity p_21544_) {
        super.setTarget(p_21544_);

        this.setUpkeepTimer(500);
    }

    public List<List<String>> getEquipment() {
        return null;
    }

    public double getMeleeStartRange() {
        return 32D;
    }

    public abstract void initSpawn();

    public static void applySpawnValues(AbstractRecruitEntity recruit){
        RecruitSpawnService.applySpawnValues(recruit);
    }

    public static void applyBiomeAndVariant(AbstractRecruitEntity recruit){
        RecruitSpawnService.applyBiomeAndVariant(recruit);
    }

    ////////////////////////////////////is FUNCTIONS////////////////////////////////////

    public boolean isEffectedByCommand(UUID player_uuid) {
        return isEffectedByCommand(player_uuid, null);
    }

    public boolean isEffectedByCommand(UUID player_uuid, UUID group) {
        if (!this.isOwned() || !this.isAlive() || !this.getListen()) return false;

        if (!this.getOwnerUUID().equals(player_uuid)) return false;

        if (group == null) {
            return true;
        }

        return this.getGroup() != null && this.getGroup().equals(group);
    }
    public boolean isOwned(){
        return getIsOwned();
    }

    public boolean isOwnedBy(Player player){
       return player.getUUID() == this.getOwnerUUID() || player == this.getOwner();
    }

    ////////////////////////////////////ON FUNCTIONS////////////////////////////////////

    public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        return RecruitInteractionService.mobInteract(this, player, hand);
    }

    public boolean hire(Player player, RecruitsGroup group, boolean message) {
        String name = this.getName().getString() + ": ";
        return RecruitLifecycleService.hire(this, player, group, message, INFO_RECRUITING_MAX(name), List.of(TEXT_RECRUITED1(name), TEXT_RECRUITED2(name), TEXT_RECRUITED3(name)));
    }

    public void dialogue(String name, Player player) {
        int i = this.random.nextInt(4);
        switch (i) {
            case 1 -> {
                player.sendSystemMessage(TEXT_HELLO_1(name));
            }
            case 2 -> {
                player.sendSystemMessage(TEXT_HELLO_2(name));
            }
            case 3 -> {
                player.sendSystemMessage(TEXT_HELLO_3(name));
            }
        }
    }

    ////////////////////////////////////ATTACK FUNCTIONS////////////////////////////////////

    public boolean hurt(@NotNull DamageSource dmg, float amt) {
        if (this.isInvulnerableTo(dmg)) {
            return false;
        } else {
            amt = RecruitCombatOverrideService.prepareIncomingDamage(this, dmg, amt);
            return super.hurt(dmg, amt);
        }
    }

    public boolean doHurtTarget(@NotNull Entity entity) {
        return RecruitCombatDecisions.doHurtTarget(this, entity);
    }

    public void addLevelBuffs(){
        RecruitProgressionService.applyLevelBuffs(this);
    }

    public void addLevelBuffsForLevel(int level){
        RecruitProgressionService.applyLevelBuffsForLevel(this, level);
    }

    /*
           .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    */
    /**
        Important for mod compat: See smallships or siege weapons mod
    **/
    public boolean isAlliedTo(@NotNull Team team) {
        if(!this.getCommandSenderWorld().isClientSide() && this.getTeam() != null){
            RecruitsDiplomacyManager.DiplomacyStatus status = FactionEvents.recruitsDiplomacyManager.getRelation(this.getTeam().getName(), team.getName());
            return status == RecruitsDiplomacyManager.DiplomacyStatus.ALLY;
        }
        return super.isAlliedTo(team);
    }

    public void die(DamageSource dmg) {
        RecruitLifecycleService.onDeath(this, dmg, this.getCombatTracker().getDeathMessage());
    }

    ////////////////////////////////////OTHER FUNCTIONS////////////////////////////////////

    public void updateMorale(){
        RecruitUpkeepService.updateMorale(this);
    }

    public void applyMoralEffects(){
        RecruitUpkeepService.applyMoralEffects(this);
    }

    public void updateHunger(){
        RecruitUpkeepService.updateHunger(this);
    }

    public boolean needsToGetFood(){
        return RecruitUpkeepService.needsToGetFood(this);
    }

    public boolean hasFoodInInv(){
        return RecruitUpkeepService.hasFoodInInv(this);
    }

    public boolean needsToEat(){
        return RecruitUpkeepService.needsToEat(this);
    }

    public boolean needsToPotion(){
        LivingEntity target = this.getTarget();
        if(target != null){
            return getHealth() <= (getMaxHealth() * 0.60) || target.getHealth() > this.getHealth();
        }
        return false;
    }

    public boolean isStarving(){
        return RecruitUpkeepService.isStarving(this);
    }

    public boolean isSaturated(){
        return RecruitUpkeepService.isSaturated(this);
    }

    public void checkLevel(){
        RecruitProgressionService.checkLevel(this);
    }

    void recalculateCost() {
        RecruitProgressionService.recalculateCost(this);
    }

    public void makeLevelUpSound() {
        this.getCommandSenderWorld().playSound(null, this.getX(), this.getY() + 1 , this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());

        if(recruitsLookLikeVillagers())
            this.getCommandSenderWorld().playSound(null, this.getX(), this.getY() + 1 , this.getZ(), SoundEvents.VILLAGER_CELEBRATE, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
    }

    public void makeHireSound() {
        if(recruitsLookLikeVillagers())
            this.playSound(SoundEvents.VILLAGER_AMBIENT, 1.0F, 0.8F + 0.4F * this.random.nextFloat());
    }

    @Override
    public boolean canBeLeashed(@NotNull Player player) {
        return false;
    }

    public int getCost(){
        return entityData.get(COST);
    }

    protected void hurtArmor(@NotNull DamageSource damageSource, float damage) {
        RecruitEquipmentService.hurtArmor(this, damageSource, damage);
    }

    public void damageMainHandItem() {
        RecruitEquipmentService.damageMainHandItem(this);
    }

    public void tryToReequip(EquipmentSlot equipmentSlot){
        RecruitEquipmentService.tryToReequip(this, equipmentSlot);
    }

    public void tryToReequipShield(){
        RecruitEquipmentService.tryToReequipShield(this);
    }

    @Override
    protected void hurtCurrentlyUsedShield(float damage) {
        RecruitEquipmentService.hurtCurrentlyUsedShield(this, damage);
    }

    @Override
    public boolean killedEntity(@NotNull ServerLevel level, @NotNull LivingEntity living) {
        super.killedEntity(level, living);
        return RecruitCombatOverrideService.handleKillRewards(this, living);
    }

    @Override
    protected void blockUsingShield(@NotNull LivingEntity living) {
        super.blockUsingShield(living);
        if (living.getMainHandItem().canDisableShield(this.useItem, this, living))
            this.disableShield();
    }

    public void disableShield() {
            this.blockCoolDown = this.getBlockCoolDown();
            this.stopUsingItem();
            this.getCommandSenderWorld().broadcastEntityEvent(this, (byte) 30);
    }

    public boolean canBlock(){
        return this.blockCoolDown == 0;
    }

    public void updateShield(){
        if(this.blockCoolDown > 0){
            this.blockCoolDown--;
        }
    }

    public int getMountTimer() {
        return this.mountTimer;
    }

    @Override
    public void openGUI(Player player) {
        RecruitInteractionService.openGUI(this, player);
    }

    public void openDebugScreen(Player player) {
        RecruitInteractionService.openDebugScreen(this, player);
    }

    public static void openTakeOverGUI(Player player) {

    }
    public boolean canBeHired(){
        return true;
    }
    @Override
    public boolean canAttack(@Nonnull LivingEntity target) {
        if(target instanceof MessengerEntity messenger && messenger.isAtMission()) return false;
        if(RecruitsServerConfig.TargetBlackList.get().contains(target.getEncodeId())) return false;
        return RecruitEvents.canAttack(this, target);
    }
    // 0 = NEUTRAL
    // 1 = AGGRESSIVE
    // 2 = RAID
    // 3 = PASSIVE
    public boolean shouldAttack(LivingEntity target) {
        return RecruitCombatDecisions.shouldAttack(this, target);
    }

    private boolean shouldAttackOnNeutral(LivingEntity target){
        return RecruitCombatDecisions.shouldAttackOnNeutral(this, target);
    }

    private boolean shouldAttackOnAggressive(LivingEntity target){
        return RecruitCombatDecisions.shouldAttackOnAggressive(this, target);
    }

    private boolean isMonster(LivingEntity target) {
        return RecruitCombatDecisions.isMonster(target);
    }

    private boolean isAttackingOwnerOrSelf(AbstractRecruitEntity recruit, LivingEntity target) {
        return RecruitCombatDecisions.isAttackingOwnerOrSelf(recruit, target);
    }

    public boolean isAlliedTo(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            return !RecruitEvents.canHarmTeam(this, livingTarget);
        } else {
            return super.isAlliedTo(target);
        }
    }

    //
    /*********************************************************
     * Update the current team of the recruit in following conditions:
     * - If recruit team is not the same team as the owner
     * - If recruit team is null but owner team != null
     * - If recruit team is != null but owner team is null
     *********************************************************/
    public void updateTeam(){
        RecruitLifecycleService.updateTeam(this);
    }

    void updateColor(String name) {
        if(!this.getCommandSenderWorld().isClientSide()){
            RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(name);
            if(recruitsFaction != null && recruitsFaction.getUnitColor() != this.getColor()){
                this.setColor(recruitsFaction.getUnitColor());
                this.needsColorUpdate = false;
            }
        }
    }

    public void updateGroup() {
        RecruitLifecycleService.updateGroup(this);
    }


    public void openHireGUI(Player player) {
        RecruitInteractionService.openHireGUI(this, player);
    }

    public void assignToPlayer(UUID newOwner, UUID newGroupUUID){
        RecruitLifecycleService.assignToPlayer(this, newOwner, newGroupUUID);
    }

    public void shouldMount(boolean should, UUID mount_uuid) {
        RecruitCommandStateService.shouldMount(this, should, mount_uuid);
    }

    public void shouldProtect(boolean should, UUID protect_uuid) {
        RecruitCommandStateService.shouldProtect(this, should, protect_uuid);
    }

    public void clearUpkeepPos() {
        this.entityData.set(UPKEEP_POS, Optional.empty());
    }

    public void clearUpkeepEntity() {
        this.entityData.set(UPKEEP_ID, Optional.empty());
    }

    public boolean hasUpkeep(){
        return this.getUpkeepPos() != null || this.getUpkeepUUID() != null;
    }

    public BannerModSupplyStatus.RecruitSupplyStatus getSupplyStatus(@Nullable Container upkeepContainer) {
        return RecruitUpkeepService.getSupplyStatus(this, upkeepContainer);
    }

    public void upkeepReequip(@NotNull Container container) {
        RecruitUpkeepService.upkeepReequip(this, container);
    }

    public static enum ArmPose {
        ATTACKING,
        BLOCKING,
        BOW_AND_ARROW,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        CELEBRATING,
        NEUTRAL;
    }

    public int getUpkeepCooldown() {
        return 3000;
    }

    public AbstractRecruitEntity.ArmPose getArmPose() {
        return AbstractRecruitEntity.ArmPose.NEUTRAL;
    }

    private MutableComponent TEXT_RECRUITED1(String name) {
        return Component.translatable("chat.bannermod.text.recruited1", name);
    }

    private MutableComponent TEXT_RECRUITED2(String name) {
        return Component.translatable("chat.bannermod.text.recruited2", name);
    }

    private MutableComponent TEXT_RECRUITED3(String name) {
        return Component.translatable("chat.bannermod.text.recruited3", name);
    }

    private Component INFO_RECRUITING_MAX(String name) {
        return Component.translatable("chat.bannermod.info.reached_max", name);
    }

    private MutableComponent TEXT_DISBAND(String name) {
        return Component.translatable("chat.bannermod.text.disband", name);
    }

    private MutableComponent TEXT_WANDER(String name) {
        return Component.translatable("chat.bannermod.text.wander", name);
    }

    private MutableComponent TEXT_HOLD_YOUR_POS(String name) {
        return Component.translatable("chat.bannermod.text.holdPos", name);
    }

    private MutableComponent TEXT_FOLLOW(String name) {
        return Component.translatable("chat.bannermod.text.follow", name);
    }

    private MutableComponent TEXT_HELLO_1(String name) {
        return Component.translatable("chat.bannermod.text.hello_1", name);
    }

    private MutableComponent TEXT_HELLO_2(String name) {
        return Component.translatable("chat.bannermod.text.hello_2", name);
    }

    private MutableComponent TEXT_HELLO_3(String name) {
        return Component.translatable("chat.bannermod.text.hello_3", name);
    }

    private MutableComponent TEXT_NO_PAYMENT(String name) {
        return Component.translatable("chat.bannermod.text.noPaymentInUpkeep", name);
    }

    InteractionResult superMobInteract(Player player, InteractionHand hand) {
        return super.mobInteract(player, hand);
    }

    MutableComponent textWander(String name) {
        return TEXT_WANDER(name);
    }

    MutableComponent textHoldYourPos(String name) {
        return TEXT_HOLD_YOUR_POS(name);
    }

    MutableComponent textFollow(String name) {
        return TEXT_FOLLOW(name);
    }

    private void pickUpArrows() {
        this.getCommandSenderWorld().getEntitiesOfClass(
                AbstractArrow.class,
                this.getBoundingBox().inflate(7D),
                (arrow) -> arrow.inGround &&
                        arrow.pickup == AbstractArrow.Pickup.ALLOWED &&
                        this.getInventory().canAddItem(Items.ARROW.getDefaultInstance())
        ).forEach((arrow) -> {
            this.getInventory().addItem(Items.ARROW.getDefaultInstance());
            arrow.moveTo(this.position());
            arrow.discard();
        });
    }

    @Override
    public boolean startRiding(Entity entity) {
        this.setMountUUID(Optional.of(entity.getUUID()));
        return super.startRiding(entity);
    }

    @Override
    public boolean removeWhenFarAway(double p_21542_) {
        return false;
    }


    public boolean canEatItemStack(ItemStack stack){
        ResourceLocation location = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if(RecruitsServerConfig.FoodBlackList.get().contains(location.toString())){
            return false;
        }
        return stack.isEdible();
    }

    private boolean hasFoodInContainer(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (this.canEatItemStack(container.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public void checkPayment(Container container) {
        RecruitUpkeepService.checkPayment(this, container, this.getOwner() != null ? TEXT_NO_PAYMENT(this.getName().getString()) : null);
    }

    public void doNoPaymentAction(){
        RecruitUpkeepService.doNoPaymentAction(this);
    }

    public void resetPaymentTimer(){
        RecruitUpkeepService.resetPaymentTimer(this);
    }

    void writeAggroState(int state) {
        this.entityData.set(STATE, state);
    }

    void writeFollowState(int state) {
        this.entityData.set(FOLLOW_STATE, state);
    }

    void writeHoldPos(Optional<BlockPos> holdPos) {
        this.entityData.set(HOLD_POS, holdPos);
    }

    void writeMovePos(Optional<BlockPos> movePos) {
        this.entityData.set(MOVE_POS, movePos);
    }

    void stopNavigation() {
        this.navigation.stop();
    }

    void superDie(DamageSource dmg) {
        super.die(dmg);
    }

    public enum NoPaymentAction{
        MORALE_LOSS,
        DISBAND,
        DISBAND_KEEP_TEAM,
        DESPAWN;

        public static NoPaymentAction fromString(String name) {
            try {
                return NoPaymentAction.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return MORALE_LOSS;
            }
        }
    }
}
