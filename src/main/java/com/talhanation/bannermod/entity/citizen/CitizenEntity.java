package com.talhanation.bannermod.entity.citizen;

import com.talhanation.bannermod.citizen.CitizenCore;
import com.talhanation.bannermod.citizen.CitizenCoreState;
import com.talhanation.bannermod.citizen.CitizenPersistenceBridge;
import com.talhanation.bannermod.citizen.CitizenProfession;
import com.talhanation.bannermod.citizen.CitizenProfessionController;
import com.talhanation.bannermod.citizen.CitizenProfessionRegistry;
import com.talhanation.bannermod.citizen.CitizenProfessionSwitcher;
import com.talhanation.bannermod.citizen.CitizenRoleContext;
import com.talhanation.bannermod.citizen.CitizenStateSnapshot;
import com.talhanation.bannermod.ai.pathfinding.AsyncGroundPathNavigation;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.citizen.AbstractCitizenEntity;
import com.talhanation.bannermod.inventory.civilian.CitizenProfileMenu;
import com.talhanation.bannermod.network.compat.BannerModNetworkHooks;
import com.talhanation.bannermod.registry.civilian.ModEntityTypes;
import com.talhanation.bannermod.settlement.prefab.staffing.PrefabAutoStaffingRuntime;
import com.talhanation.bannermod.util.BannerModCurrencyHelper;
import com.talhanation.bannermod.util.BannerModNpcNamePool;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Single registered NPC entity type for the Manor-Lords-style citizen
 * model: one {@code bannermod:citizen} entity swaps its active
 * {@link CitizenProfessionController} at runtime instead of spawning a
 * per-profession subclass.
 *
 * <p>This class is intentionally thin. State storage lives in a
 * composed {@link CitizenCoreState}; profession switching lives in a
 * composed {@link CitizenProfessionSwitcher}; goal installation is
 * delegated to whichever profession controller is currently active.
 * Per-profession behaviour is owned by Cit-03 controller implementations,
 * not by this class.
 *
 * <p>Cit-02 ships only a stub goal set (look-at-player + random stroll)
 * so the entity is navigable in tests. Real goal sets arrive with the
 * first concrete {@link CitizenProfessionController} in Cit-03.
 */
public class CitizenEntity extends PathfinderMob implements CitizenCore {

    private static final EntityDataAccessor<String> DATA_PROFESSION =
            SynchedEntityData.defineId(CitizenEntity.class, EntityDataSerializers.STRING);

    private static final CitizenProfessionRegistry DEFAULT_REGISTRY = CitizenProfessionRegistry.defaults();

    private final CitizenCoreState state;
    private final CitizenProfessionRegistry registry;
    private CitizenProfessionSwitcher switcher;

    public CitizenEntity(EntityType<? extends CitizenEntity> type, Level level) {
        this(type, level, DEFAULT_REGISTRY);
    }

    public CitizenEntity(EntityType<? extends CitizenEntity> type, Level level, CitizenProfessionRegistry registry) {
        super(type, level);
        this.registry = registry;
        this.state = new CitizenCoreState(27);
        this.switcher = new CitizenProfessionSwitcher(registry, this, CitizenProfession.NONE);
        // Citizens are settlement NPCs the player invests in (named, profession-tagged,
        // assigned to claim work areas). Without this they inherit Mob's vanilla
        // despawn behavior — wander out of the 32-block "no-despawn" radius around
        // a player, accumulate noActionTime, and silently vanish via Mob.checkDespawn.
        // The persistence flag also keeps them across save/load even if the chunk
        // unloads while they're outside the player tracking radius.
        this.setPersistenceRequired();
    }

    @Override
    public boolean removeWhenFarAway(double sqDistanceToClosestPlayer) {
        // See constructor: citizens never despawn from distance. Recruits already do
        // this; we mirror it here so the unified-citizen line behaves consistently.
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROFESSION, CitizenProfession.NONE.name());
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(net.minecraft.world.level.Level level) {
        return new AsyncGroundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Walk to a bound work-area when one is assigned. Sits between FloatGoal (0)
        // and the random stroll (8) so the citizen actually pathfinds to its workplace
        // via AsyncGroundPathNavigation instead of randomly bumping into walls until
        // chance places it within the 3-block conversion window.
        this.goalSelector.addGoal(2, new com.talhanation.bannermod.ai.citizen.CitizenSeekWorkAreaGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        CitizenIndex.instance().onCitizenTick(this);
        if (!this.level().isClientSide() && this.tickCount % 20 == 0) {
            BannerModNpcNamePool.ensureNamed(this);
            PrefabAutoStaffingRuntime.assignCitizenToNearestVacancy((net.minecraft.server.level.ServerLevel) this.level(), this);
            tryConvertIntoPendingWorker();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return super.mobInteract(player, hand);
        }
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!canOpenProfile(player)) {
            return super.mobInteract(player, hand);
        }
        BannerModNpcNamePool.ensureNamed(this);
        openProfileGui(player);
        return InteractionResult.SUCCESS;
    }

    private boolean canOpenProfile(Player player) {
        return player.hasPermissions(2)
                || !this.isOwned()
                || this.getOwnerUUID() != null && this.getOwnerUUID().equals(player.getUUID());
    }

    private void openProfileGui(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            BannerModNetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return CitizenEntity.this.getDisplayName();
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player menuPlayer) {
                    return new CitizenProfileMenu(id, CitizenEntity.this, playerInventory);
                }
            }, buffer -> buffer.writeUUID(this.getUUID()));
        }
    }

    private void tryConvertIntoPendingWorker() {
        if (this.activeProfession() != CitizenProfession.NONE) {
            return;
        }
        if (!this.getPersistentData().contains(PrefabAutoStaffingRuntime.TAG_PENDING_WORKER_PROFESSION, Tag.TAG_STRING)) {
            return;
        }
        CitizenProfession targetProfession = CitizenProfession.fromTagName(
                this.getPersistentData().getString(PrefabAutoStaffingRuntime.TAG_PENDING_WORKER_PROFESSION)
        );
        EntityType<? extends AbstractWorkerEntity> workerType = workerTypeFor(targetProfession);
        EntityType<? extends com.talhanation.bannermod.entity.military.AbstractRecruitEntity> recruitType = recruitTypeFor(targetProfession);
        if (workerType == null && recruitType == null) {
            return;
        }
        UUID boundWorkAreaUuid = this.getBoundWorkAreaUUID();
        if (boundWorkAreaUuid == null) {
            return;
        }
        Entity boundWorkArea = ((net.minecraft.server.level.ServerLevel) this.level()).getEntity(boundWorkAreaUuid);
        BlockPos manualAnchor = boundWorkArea == null ? PrefabAutoStaffingRuntime.conversionAnchorPosition(boundWorkAreaUuid) : null;
        if (boundWorkArea == null && manualAnchor == null) {
            return;
        }
        double anchorDistanceSqr = boundWorkArea != null
                ? this.distanceToSqr(boundWorkArea)
                : this.distanceToSqr(Vec3.atCenterOf(manualAnchor));
        if (anchorDistanceSqr > 9.0D) {
            return;
        }
        if (!PrefabAutoStaffingRuntime.hasConversionSlot((net.minecraft.server.level.ServerLevel) this.level(), boundWorkAreaUuid, this.getUUID())) {
            return;
        }
        int hireCost = hireCostFor(targetProfession);
        if (workerType != null) {
            AbstractWorkerEntity worker = workerType.create(this.level());
            if (worker == null) {
                return;
            }
            if (!payHireCost(hireCost)) {
                return;
            }
            worker.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            UUID resolvedOwner = resolveOwnerOrClaimFallback();
            if (resolvedOwner != null) {
                worker.setOwnerUUID(Optional.of(resolvedOwner));
                worker.setIsOwned(true);
            }
            worker.getCitizenCore().setBoundWorkAreaUUID(boundWorkAreaUuid);
            this.level().addFreshEntity(worker);
            this.discard();
            return;
        }
        com.talhanation.bannermod.entity.military.AbstractRecruitEntity recruit = recruitType.create(this.level());
        if (recruit == null) {
            return;
        }
        if (!payHireCost(hireCost)) {
            return;
        }
        recruit.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        UUID resolvedOwner = resolveOwnerOrClaimFallback();
        if (resolvedOwner != null) {
            recruit.setOwnerUUID(Optional.of(resolvedOwner));
            recruit.setIsOwned(true);
        }
        recruit.getCitizenCore().setBoundWorkAreaUUID(boundWorkAreaUuid);
        this.level().addFreshEntity(recruit);
        this.discard();
    }

    /**
     * Resolves the owner UUID for a freshly-spawned worker / recruit derived from this
     * citizen. Prefers the citizen's existing owner, then falls back to the owner of the
     * claim the citizen is standing in. Without this fallback a citizen that was spawned
     * as part of a settlement (no explicit owner) ends up producing an "unowned" worker
     * the first time it converts, which is what the player saw as "сеньор сбрасывается".
     */
    @javax.annotation.Nullable
    private UUID resolveOwnerOrClaimFallback() {
        UUID owner = this.getOwnerUUID();
        if (owner != null) return owner;
        if (com.talhanation.bannermod.events.ClaimEvents.claimManager() == null) return null;
        com.talhanation.bannermod.persistence.military.RecruitsClaim claim = com.talhanation.bannermod.events.ClaimEvents.claimManager()
                .getClaim(new net.minecraft.world.level.ChunkPos(this.blockPosition()));
        if (claim == null || claim.getPlayerInfo() == null) return null;
        return claim.getPlayerInfo().getUUID();
    }

    private boolean payHireCost(int hireCost) {
        if (hireCost <= 0) {
            return true;
        }
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return false;
        }
        UUID ownerUuid = this.getOwnerUUID();
        if (ownerUuid == null) {
            return false;
        }
        net.minecraft.server.level.ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);
        return owner != null && BannerModCurrencyHelper.removeCurrency(owner, hireCost);
    }

    private static int hireCostFor(CitizenProfession profession) {
        return switch (profession == null ? CitizenProfession.NONE : profession) {
            case FARMER -> WorkersServerConfig.FarmerCost.get();
            case LUMBERJACK -> WorkersServerConfig.LumberjackCost.get();
            case MINER -> WorkersServerConfig.MinerCost.get();
            case BUILDER -> WorkersServerConfig.BuilderCost.get();
            case MERCHANT -> WorkersServerConfig.MerchantCost.get();
            case FISHERMAN -> WorkersServerConfig.FarmerCost.get();
            case ANIMAL_FARMER -> WorkersServerConfig.FarmerCost.get();
            case RECRUIT_SPEAR -> RecruitsServerConfig.RecruitCost.get();
            case RECRUIT_BOWMAN -> RecruitsServerConfig.BowmanCost.get();
            case RECRUIT_CROSSBOWMAN -> RecruitsServerConfig.CrossbowmanCost.get();
            case RECRUIT_HORSEMAN -> RecruitsServerConfig.HorsemanCost.get();
            case RECRUIT_SHIELDMAN -> RecruitsServerConfig.ShieldmanCost.get();
            case RECRUIT_NOMAD -> RecruitsServerConfig.NomadCost.get();
            case RECRUIT_SCOUT, NOBLE, NONE -> 0;
        };
    }

    @Nullable
    private static EntityType<? extends AbstractWorkerEntity> workerTypeFor(CitizenProfession profession) {
        return switch (profession) {
            case FARMER -> ModEntityTypes.FARMER.get();
            case LUMBERJACK -> ModEntityTypes.LUMBERJACK.get();
            case MINER -> ModEntityTypes.MINER.get();
            case BUILDER -> ModEntityTypes.BUILDER.get();
            case MERCHANT -> ModEntityTypes.MERCHANT.get();
            case FISHERMAN -> ModEntityTypes.FISHERMAN.get();
            case ANIMAL_FARMER -> ModEntityTypes.ANIMAL_FARMER.get();
            default -> null;
        };
    }

    @Nullable
    private static EntityType<? extends com.talhanation.bannermod.entity.military.AbstractRecruitEntity> recruitTypeFor(CitizenProfession profession) {
        return switch (profession) {
            case RECRUIT_SPEAR -> com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get();
            case RECRUIT_BOWMAN -> com.talhanation.bannermod.registry.military.ModEntityTypes.BOWMAN.get();
            case RECRUIT_CROSSBOWMAN -> com.talhanation.bannermod.registry.military.ModEntityTypes.CROSSBOWMAN.get();
            case RECRUIT_HORSEMAN -> com.talhanation.bannermod.registry.military.ModEntityTypes.HORSEMAN.get();
            case RECRUIT_SHIELDMAN -> com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT_SHIELDMAN.get();
            default -> null;
        };
    }

    // ------------------------------------------------------------------
    // Profession surface
    // ------------------------------------------------------------------

    public CitizenProfession activeProfession() {
        return this.switcher.activeProfession();
    }

    public CitizenProfessionController activeController() {
        return this.switcher.activeController();
    }

    public int professionSwitchCount() {
        return this.switcher.switchCount();
    }

    /**
     * Change profession at runtime without respawning the entity. Calls
     * {@code uninstallGoals} on the old controller then {@code installGoals}
     * on the new one. Returns {@code true} if a switch actually occurred
     * (false means {@code newProfession} matched the active one).
     */
    public boolean switchProfession(CitizenProfession newProfession) {
        CitizenRoleContext ctx = new CitizenRoleContext(
                newProfession.coarseRole(),
                this,
                this,
                null,
                this.state.getBoundWorkAreaUUID()
        );
        boolean changed = this.switcher.switchTo(newProfession, ctx);
        if (changed) {
            this.entityData.set(DATA_PROFESSION, newProfession.name());
        }
        return changed;
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CitizenPersistenceBridge.writeCanonicalRole(tag, this.activeProfession());
        if (this.state.getOwnerUUID() != null) {
            tag.putUUID("CitizenOwner", this.state.getOwnerUUID());
        }
        if (this.state.getTeamId() != null) {
            tag.putString("CitizenTeamId", this.state.getTeamId());
        }
        tag.putInt("CitizenFollowState", this.state.getFollowState());
        tag.putBoolean("CitizenOwned", this.state.isOwned());
        tag.putBoolean("CitizenWorking", this.state.isWorking());
        if (this.state.getBoundWorkAreaUUID() != null) {
            tag.putUUID("CitizenBoundWorkArea", this.state.getBoundWorkAreaUUID());
        }
        Vec3 hold = this.state.getHoldPos();
        if (hold != null) {
            CompoundTag holdTag = new CompoundTag();
            holdTag.putDouble("X", hold.x);
            holdTag.putDouble("Y", hold.y);
            holdTag.putDouble("Z", hold.z);
            tag.put("CitizenHoldPos", holdTag);
        }
        BlockPos move = this.state.getMovePos();
        if (move != null) {
            CompoundTag moveTag = new CompoundTag();
            moveTag.putInt("X", move.getX());
            moveTag.putInt("Y", move.getY());
            moveTag.putInt("Z", move.getZ());
            tag.put("CitizenMovePos", moveTag);
        }
        tag.put("CitizenInventory", CitizenStateSnapshot.copyInventory(this.state.getInventory(), this.registryAccess()));
        CompoundTag flagTag = new CompoundTag();
        for (RuntimeFlag flag : RuntimeFlag.values()) {
            flagTag.putBoolean(flag.name(), this.state.getRuntimeFlag(flag));
        }
        tag.put("CitizenRuntimeFlags", flagTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CitizenProfession profession = tag.contains(CitizenPersistenceBridge.TAG_CITIZEN_PROFESSION, Tag.TAG_STRING)
                ? CitizenProfession.fromTagName(tag.getString(CitizenPersistenceBridge.TAG_CITIZEN_PROFESSION))
                : CitizenProfession.NONE;
        if (profession == CitizenProfession.NONE
                && !tag.contains(CitizenPersistenceBridge.TAG_CITIZEN_PROFESSION, Tag.TAG_STRING)
                && (tag.hasUUID("OwnerUUID") || tag.contains("FollowState", Tag.TAG_INT))) {
            this.state.apply(CitizenPersistenceBridge.fromRecruitLegacy(tag));
        }
        this.switcher = new CitizenProfessionSwitcher(this.registry, this, profession);
        this.entityData.set(DATA_PROFESSION, profession.name());

        this.state.setOwnerUUID(tag.hasUUID("CitizenOwner")
                ? Optional.of(tag.getUUID("CitizenOwner"))
                : Optional.empty());
        this.state.setTeamId(tag.contains("CitizenTeamId", Tag.TAG_STRING) ? tag.getString("CitizenTeamId") : null);
        this.state.setFollowState(tag.getInt("CitizenFollowState"));
        this.state.setOwned(tag.getBoolean("CitizenOwned"));
        this.state.setWorking(tag.getBoolean("CitizenWorking"));
        this.state.setBoundWorkAreaUUID(tag.hasUUID("CitizenBoundWorkArea") ? tag.getUUID("CitizenBoundWorkArea") : null);

        if (tag.contains("CitizenHoldPos", Tag.TAG_COMPOUND)) {
            CompoundTag holdTag = tag.getCompound("CitizenHoldPos");
            this.state.setHoldPos(new Vec3(holdTag.getDouble("X"), holdTag.getDouble("Y"), holdTag.getDouble("Z")));
        }
        else {
            this.state.clearHoldPos();
        }

        if (tag.contains("CitizenMovePos", Tag.TAG_COMPOUND)) {
            CompoundTag moveTag = tag.getCompound("CitizenMovePos");
            this.state.setMovePos(new BlockPos(moveTag.getInt("X"), moveTag.getInt("Y"), moveTag.getInt("Z")));
        }
        else {
            this.state.clearMovePos();
        }

        if (tag.contains("CitizenInventory", Tag.TAG_COMPOUND)) {
            CitizenStateSnapshot.restoreInventory(this.state.getInventory(), tag.getCompound("CitizenInventory"), this.registryAccess());
        }

        if (tag.contains("CitizenRuntimeFlags", Tag.TAG_COMPOUND)) {
            CompoundTag flagTag = tag.getCompound("CitizenRuntimeFlags");
            for (RuntimeFlag flag : RuntimeFlag.values()) {
                this.state.setRuntimeFlag(flag, flagTag.getBoolean(flag.name()));
            }
        }
    }

    // ------------------------------------------------------------------
    // CitizenCore delegate
    // ------------------------------------------------------------------

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.state.getOwnerUUID();
    }

    @Override
    public void setOwnerUUID(Optional<UUID> ownerUuid) {
        this.state.setOwnerUUID(ownerUuid);
    }

    @Override
    public int getFollowState() {
        return this.state.getFollowState();
    }

    @Override
    public void setFollowState(int s) {
        this.state.setFollowState(s);
    }

    @Override
    public SimpleContainer getInventory() {
        return this.state.getInventory();
    }

    @Override
    @Nullable
    public String getTeamId() {
        return this.state.getTeamId();
    }

    @Override
    @Nullable
    public Vec3 getHoldPos() {
        return this.state.getHoldPos();
    }

    @Override
    public void setHoldPos(@Nullable Vec3 holdPos) {
        this.state.setHoldPos(holdPos);
    }

    @Override
    public void clearHoldPos() {
        this.state.clearHoldPos();
    }

    @Override
    @Nullable
    public BlockPos getMovePos() {
        return this.state.getMovePos();
    }

    @Override
    public void setMovePos(@Nullable BlockPos movePos) {
        this.state.setMovePos(movePos);
    }

    @Override
    public void clearMovePos() {
        this.state.clearMovePos();
    }

    @Override
    public boolean isOwned() {
        return this.state.isOwned();
    }

    @Override
    public void setOwned(boolean owned) {
        this.state.setOwned(owned);
    }

    @Override
    public boolean isWorking() {
        return this.state.isWorking();
    }

    @Override
    @Nullable
    public UUID getBoundWorkAreaUUID() {
        return this.state.getBoundWorkAreaUUID();
    }

    @Override
    public void setBoundWorkAreaUUID(@Nullable UUID boundWorkAreaUuid) {
        this.state.setBoundWorkAreaUUID(boundWorkAreaUuid);
    }

    @Override
    public boolean getRuntimeFlag(RuntimeFlag flag) {
        return this.state.getRuntimeFlag(flag);
    }

    @Override
    public void setRuntimeFlag(RuntimeFlag flag, boolean value) {
        this.state.setRuntimeFlag(flag, value);
    }
}
