package com.talhanation.bannermod.entity.civilian;

import com.google.common.collect.ImmutableSet;
import com.talhanation.bannermod.citizen.CitizenCore;
import com.talhanation.bannermod.citizen.CitizenRole;
import com.talhanation.bannermod.citizen.CitizenRoleController;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRuntime;
import com.talhanation.bannermod.config.RecruitsClientConfig;
import com.talhanation.bannermod.entity.military.AbstractChunkLoaderEntity;
import com.talhanation.bannermod.ai.civilian.DepositItemsToStorage;
import com.talhanation.bannermod.ai.civilian.GetNeededItemsFromStorage;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.persistence.civilian.NeededItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;


public abstract class AbstractWorkerEntity extends AbstractChunkLoaderEntity implements WorkerLogisticsAccess {

    public static final Set<Block> UNBREAKABLES = ImmutableSet.of(
            Blocks.BEDROCK,
            Blocks.BARRIER);

    public AbstractWorkerEntity(EntityType<? extends AbstractWorkerEntity> entityType, Level world) {
        super(entityType, world);
    }
    public List<NeededItem> neededItems = new ArrayList<>();
    public int farmedItems;
    public boolean forcedDeposit;
    public UUID lastStorage;
    private final WorkerCourierService courierService = new WorkerCourierService(this);
    private final WorkerInventoryService inventoryService = new WorkerInventoryService(this);
    private final WorkerControlAccess controlAccess = new WorkerControlAccess(this);
    private final WorkerSupplyRuntime supplyRuntime = new WorkerSupplyRuntime(this);
    private final WorkerBlockBreakService blockBreakService = new WorkerBlockBreakService(this);
    private final WorkerStateAccess stateAccess = new WorkerStateAccess(this);
    private final CitizenCore citizenCore = WorkerCitizenBridge.createCore(this);
    private CitizenRoleController citizenRoleController = CitizenRoleController.noop(CitizenRole.WORKER);
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new DepositItemsToStorage(this));
        this.goalSelector.addGoal(0, new GetNeededItemsFromStorage(this));

        this.goalSelector.removeGoal(new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
    }

    public abstract AbstractWorkAreaEntity getCurrentWorkArea();

    @Override
    public CitizenCore getCitizenCore() {
        return this.citizenCore;
    }

    @Override
    public CitizenRole getCitizenRole() {
        return CitizenRole.WORKER;
    }

    @Override
    public CitizenRoleController getCitizenRoleController() {
        return this.citizenRoleController;
    }

    @Override
    public void setCitizenRoleController(CitizenRoleController controller) {
        this.citizenRoleController = controller == null ? CitizenRoleController.noop(this.getCitizenRole()) : controller;
    }

    protected void clearCurrentWorkAreaForRecovery() {
    }

    @Nullable
    public UUID getBoundWorkAreaUUID() {
        return this.controlAccess.getBoundWorkAreaUUID();
    }

    WorkerControlAccess controlAccess() {
        return this.controlAccess;
    }

    public void reportBlockedReason(String reasonToken, Component message) {
        this.controlAccess.reportBlockedReason(reasonToken, message);
    }

    public void reportIdleReason(String reasonToken, Component message) {
        this.controlAccess.reportIdleReason(reasonToken, message);
    }

    public void clearWorkStatus() {
        this.controlAccess.clearWorkStatus();
    }

    WorkerCourierService courierService() {
        return this.courierService;
    }

    WorkerInventoryService inventoryService() {
        return this.inventoryService;
    }

    WorkerSupplyRuntime supplyRuntime() {
        return this.supplyRuntime;
    }

    public boolean recoverControl(Player requester) {
        return this.controlAccess.recoverControl(requester);
    }

    /////////////////////////////////// TICK/////////////////////////////////////////
    /*
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new DebugSyncWorkerPathNavigation(this, level);//TODO ONLY TO TEST NODE EVALUATOR
    }

    public @NotNull PathNavigation getNavigation() {
        return this.navigation;//TODO REMOVE)
    }
    */

    public boolean isWorking(){
        return this.stateAccess.isWorking();
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if(this.getCommandSenderWorld().isClientSide()) return;

        this.getCommandSenderWorld().getProfiler().push("looting");
        WorkerRuntimeLoop.aiStep(this);
        this.getCommandSenderWorld().getProfiler().pop();
    }

    boolean isAliveForLooting() {
        return this.isAlive() && !this.dead;
    }

    @Override
    public boolean wantsToPickUp(ItemStack itemStack) {
        if (this.wantsToPickUpWorkerItem(itemStack)) return true;
        return super.wantsToPickUp(itemStack);
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        this.pickUpWorkerItem(itemEntity);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance diff, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag nbt) {
        return spawnData;
    }
    public void setDropEquipment()  {
        this.dropEquipment();
    }


    //////////////////////////////////// REGISTER////////////////////////////////////

    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        WorkerPersistenceBridge.writeWorkerData(this, nbt);
    }

    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        WorkerPersistenceBridge.readWorkerData(this, nbt);
    }

    private static boolean recruitsLookLikeVillagers() {
        try {
            return RecruitsClientConfig.RecruitsLookLikeVillagers.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public SoundEvent getHurtSound(DamageSource ds) {
        if(recruitsLookLikeVillagers()){
            return SoundEvents.VILLAGER_HURT;
        }
        else
            return SoundEvents.PLAYER_HURT;
    }

    @OnlyIn(Dist.CLIENT)
    protected SoundEvent getDeathSound() {
        if(recruitsLookLikeVillagers()){
            return SoundEvents.VILLAGER_DEATH;
        }
        else
            return SoundEvents.PLAYER_DEATH;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    //////////////////////////////////// SET////////////////////////////////////

    public void setEquipment() {
    }

    public boolean needsToSleep() {
        return this.stateAccess.needsToSleep();
    }

    public abstract Predicate<ItemEntity> getAllowedItems();

    public void initSpawn(){
        this.setEquipment();
        this.setDropEquipment();
        this.setPersistenceRequired();
        this.setCanPickUpLoot(true);
    }

    public double getDistanceToOwner(){
        return this.stateAccess.getDistanceToOwner();
    }

    public void tick() {
        super.tick();
        if(this.getCommandSenderWorld().isClientSide()) return;

        WorkerRuntimeLoop.tick(this);
    }

    public abstract List<Item> inventoryInputHelp();

    public void mineBlock(BlockPos pos) {
        this.blockBreakService.mineBlock(pos);
    }


    public void switchMainHandItem(Predicate<ItemStack> predicate) {
        this.stateAccess.switchMainHandItem(predicate);
    }

    public double getHorizontalDistanceTo(Vec3 target){
        return this.stateAccess.getHorizontalDistanceTo(target);
    }

    @Override
    public void die(DamageSource dmg) {
        super.die(dmg);
        if(this.getCurrentWorkArea() != null) getCurrentWorkArea().setBeingWorkedOn(false);
    }

    public static boolean isPosBroken(BlockPos pos, Level level, boolean allowWater) {
        return WorkerBlockBreakService.isPosBroken(pos, level, allowWater);
    }

    public void openDepositsGUI(Player player) {

    }

    public boolean shouldWork() {
        return this.stateAccess.shouldWork();
    }
}
