package com.talhanation.bannerlord.entity.civilian;

import com.google.common.collect.ImmutableSet;
import com.talhanation.bannermod.authority.BannerModAuthorityRules;
import com.talhanation.bannermod.citizen.CitizenCore;
import com.talhanation.bannermod.citizen.CitizenPersistenceBridge;
import com.talhanation.bannermod.citizen.CitizenRole;
import com.talhanation.bannermod.citizen.CitizenRoleContext;
import com.talhanation.bannermod.citizen.CitizenRoleController;
import com.talhanation.bannermod.logistics.BannerModLogisticsService;
import com.talhanation.bannermod.logistics.BannerModSupplyStatus;
import com.talhanation.recruits.config.RecruitsClientConfig;
import com.talhanation.bannerlord.entity.military.AbstractChunkLoaderEntity;
import com.talhanation.bannerlord.ai.civilian.CourierWorkGoal;
import com.talhanation.bannerlord.ai.civilian.DepositItemsToStorage;
import com.talhanation.bannerlord.ai.civilian.GetNeededItemsFromStorage;
import com.talhanation.bannerlord.entity.civilian.workarea.BuildArea;
import com.talhanation.bannerlord.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannerlord.persistence.civilian.NeededItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;


public abstract class AbstractWorkerEntity extends AbstractChunkLoaderEntity {

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
    private UUID boundWorkArea;
    private final WorkerControlStatus workStatus = new WorkerControlStatus();
    private final WorkerStorageRequestState storageRequestState = new WorkerStorageRequestState();
    private final CitizenCore citizenCore = new CitizenCore() {
        @Override
        public UUID getOwnerUUID() {
            return AbstractWorkerEntity.this.getOwnerUUID();
        }

        @Override
        public void setOwnerUUID(Optional<UUID> ownerUuid) {
            AbstractWorkerEntity.this.setOwnerUUID(ownerUuid);
        }

        @Override
        public int getFollowState() {
            return AbstractWorkerEntity.this.getFollowState();
        }

        @Override
        public void setFollowState(int state) {
            AbstractWorkerEntity.this.setFollowState(state);
        }

        @Override
        public SimpleContainer getInventory() {
            return AbstractWorkerEntity.this.getInventory();
        }

        @Override
        public String getTeamId() {
            return AbstractWorkerEntity.this.getTeam() == null ? null : AbstractWorkerEntity.this.getTeam().getName();
        }

        @Override
        public Vec3 getHoldPos() {
            return AbstractWorkerEntity.this.getHoldPos();
        }

        @Override
        public void setHoldPos(@Nullable Vec3 holdPos) {
            if (holdPos == null) {
                AbstractWorkerEntity.this.clearHoldPos();
            }
            else {
                AbstractWorkerEntity.this.setHoldPos(holdPos);
            }
        }

        @Override
        public void clearHoldPos() {
            AbstractWorkerEntity.this.clearHoldPos();
        }

        @Override
        public BlockPos getMovePos() {
            return AbstractWorkerEntity.this.getMovePos();
        }

        @Override
        public void setMovePos(@Nullable BlockPos movePos) {
            if (movePos == null) {
                AbstractWorkerEntity.this.clearMovePos();
            }
            else {
                AbstractWorkerEntity.this.setMovePos(movePos);
            }
        }

        @Override
        public void clearMovePos() {
            AbstractWorkerEntity.this.clearMovePos();
        }

        @Override
        public boolean isOwned() {
            return AbstractWorkerEntity.this.getIsOwned();
        }

        @Override
        public void setOwned(boolean owned) {
            AbstractWorkerEntity.this.setIsOwned(owned);
        }

        @Override
        public boolean isWorking() {
            return AbstractWorkerEntity.this.isWorking();
        }

        @Override
        public UUID getBoundWorkAreaUUID() {
            return AbstractWorkerEntity.this.getBoundWorkAreaUUID();
        }

        @Override
        public void setBoundWorkAreaUUID(@Nullable UUID boundWorkAreaUuid) {
            AbstractWorkerEntity.this.boundWorkArea = boundWorkAreaUuid;
        }

        @Override
        public boolean getRuntimeFlag(RuntimeFlag flag) {
            return switch (flag) {
                case SHOULD_FOLLOW -> AbstractWorkerEntity.this.getShouldFollow();
                case SHOULD_HOLD_POS -> AbstractWorkerEntity.this.getShouldHoldPos();
                case SHOULD_MOVE_POS -> AbstractWorkerEntity.this.getShouldMovePos();
                case SHOULD_PROTECT -> AbstractWorkerEntity.this.getShouldProtect();
                case SHOULD_MOUNT -> AbstractWorkerEntity.this.getShouldMount();
                case SHOULD_BLOCK -> AbstractWorkerEntity.this.getShouldBlock();
                case LISTEN -> AbstractWorkerEntity.this.getListen();
                case IS_FOLLOWING -> AbstractWorkerEntity.this.isFollowing();
                case SHOULD_REST -> AbstractWorkerEntity.this.getShouldRest();
                case SHOULD_RANGED -> AbstractWorkerEntity.this.getShouldRanged();
                case IS_IN_FORMATION -> AbstractWorkerEntity.this.isInFormation;
            };
        }

        @Override
        public void setRuntimeFlag(RuntimeFlag flag, boolean value) {
            switch (flag) {
                case SHOULD_FOLLOW -> AbstractWorkerEntity.this.setShouldFollow(value);
                case SHOULD_HOLD_POS -> AbstractWorkerEntity.this.setShouldHoldPos(value);
                case SHOULD_MOVE_POS -> AbstractWorkerEntity.this.setShouldMovePos(value);
                case SHOULD_PROTECT -> AbstractWorkerEntity.this.setShouldProtect(value);
                case SHOULD_MOUNT -> AbstractWorkerEntity.this.setShouldMount(value);
                case SHOULD_BLOCK -> AbstractWorkerEntity.this.setShouldBlock(value);
                case LISTEN -> AbstractWorkerEntity.this.setListen(value);
                case IS_FOLLOWING -> AbstractWorkerEntity.this.setIsFollowing(value);
                case SHOULD_REST -> AbstractWorkerEntity.this.setShouldRest(value);
                case SHOULD_RANGED -> AbstractWorkerEntity.this.setShouldRanged(value);
                case IS_IN_FORMATION -> AbstractWorkerEntity.this.isInFormation = value;
            }
        }
    };
    private CitizenRoleController citizenRoleController = CitizenRoleController.noop(CitizenRole.WORKER);
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new DepositItemsToStorage(this));
        this.goalSelector.addGoal(0, new GetNeededItemsFromStorage(this));
        if (this.supportsCourierTasks()) {
            this.goalSelector.addGoal(1, new CourierWorkGoal(this));
        }

        this.goalSelector.removeGoal(new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
    }

    protected boolean supportsCourierTasks() {
        return false;
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
        return this.boundWorkArea;
    }

    protected void rememberCurrentWorkAreaBinding() {
        AbstractWorkAreaEntity workArea = this.getCurrentWorkArea();
        if (workArea != null) {
            this.boundWorkArea = workArea.getUUID();
            this.getCitizenRoleController().onBoundWorkAreaRemembered(new CitizenRoleContext(
                    this.getCitizenRole(),
                    this.getCitizenCore(),
                    this,
                    null,
                    this.boundWorkArea
            ));
        }
    }

    public void reportBlockedReason(String reasonToken, Component message) {
        reportWorkReason(WorkerControlStatus.Kind.BLOCKED, reasonToken, message);
    }

    public void reportIdleReason(String reasonToken, Component message) {
        reportWorkReason(WorkerControlStatus.Kind.IDLE, reasonToken, message);
    }

    private void reportWorkReason(WorkerControlStatus.Kind kind, String reasonToken, Component message) {
        if (this.level().isClientSide() || message == null || this.getOwner() == null) {
            return;
        }

        if (this.workStatus.shouldNotify(kind, reasonToken)) {
            this.getOwner().sendSystemMessage(message);
        }
    }

    public void clearWorkStatus() {
        this.workStatus.clear();
        this.storageRequestState.clear();
    }

    @Nullable
    public String getReportedWorkReasonToken() {
        return this.workStatus.getReasonToken();
    }

    @Nullable
    public WorkerControlStatus.Kind getReportedWorkKind() {
        return this.workStatus.getKind();
    }

    public boolean recoverControl(Player requester) {
        if (this.level().isClientSide()) {
            return false;
        }

        if (!this.isAlive()) {
            if (requester != null) {
                requester.sendSystemMessage(Component.literal(this.getName().getString() + ": I can't recover right now."));
            }
            return false;
        }

        if (requester != null) {
            BannerModAuthorityRules.Decision decision = BannerModAuthorityRules.recoverControlDecision(
                    true,
                    BannerModAuthorityRules.resolveRelationship(
                            requester.getUUID().equals(this.getOwnerUUID()),
                            false,
                            requester.hasPermissions(2)
                    )
            );

            if (!BannerModAuthorityRules.isAllowed(decision)) {
                requester.sendSystemMessage(Component.literal("You do not control " + this.getName().getString() + "."));
                return false;
            }
        }

        AbstractWorkAreaEntity workArea = this.getCurrentWorkArea();
        if (workArea != null) {
            workArea.setBeingWorkedOn(false);
        }

        this.getNavigation().stop();
        this.clearMovePos();
        this.clearHoldPos();
        this.setTarget(null);
        this.setFollowState(0);
        this.neededItems.clear();
        this.forcedDeposit = false;
        this.lastStorage = null;
        this.clearWorkStatus();
        this.clearCurrentWorkAreaForRecovery();

        if (requester != null) {
            requester.sendSystemMessage(Component.literal(this.getName().getString() + ": control recovered."));
        }

        this.getCitizenRoleController().onRecoveredControl(new CitizenRoleContext(
                this.getCitizenRole(),
                this.getCitizenCore(),
                this,
                requester,
                this.getBoundWorkAreaUUID()
        ));

        return true;
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
        return this.getFollowState() == 6;
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if(this.getCommandSenderWorld().isClientSide()) return;

        this.getCommandSenderWorld().getProfiler().push("looting");
        if (this.canPickUpLoot() && this.isAlive() && !this.dead) {
            List<ItemEntity> nearbyItems = this.getCommandSenderWorld().getEntitiesOfClass(
                ItemEntity.class,
                this.getBoundingBox().inflate(5.5D, 5.5D, 5.5D)
            );

            for (ItemEntity itementity : nearbyItems) {
                if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && !itementity.hasPickUpDelay() && this.wantsToPickUp(itementity.getItem())) {
                    this.pickUpItem(itementity);
                }
            }
        }

        if(tickCount % 20 == 0){
            if(this.getCurrentWorkArea() != null){
                double distance = this.getHorizontalDistanceTo(getCurrentWorkArea().position());
                if(distance >= 1000) this.getCurrentWorkArea().isBeingWorkedOn = false;
            }
        }
    }

    public boolean canAddItem(ItemStack itemToAdd) {
        boolean flag = false;
        List<ItemStack> inventorySlots = new ArrayList<>();
        for(int i = 6; i < this.inventory.getContainerSize(); i++){
            inventorySlots.add(inventory.items.get(i));
        }

        for(ItemStack itemstack : inventorySlots) {
            if (itemstack.isEmpty() || ItemStack.isSameItemSameTags(itemstack, itemToAdd) && itemstack.getCount() < itemstack.getMaxStackSize()) {
                flag = true;
                break;
            }
        }

        return flag;
    }
    public boolean wantsToKeep(ItemStack itemStack) {
        return (itemStack.isEdible() && itemStack.getFoodProperties(this).getNutrition() > 4);
    }
    @Override
    public boolean wantsToPickUp(ItemStack itemStack) {
        if(wantsToKeep(itemStack)) return true;

        List<NeededItem> neededItems = this.neededItems;

        for (NeededItem needed : neededItems) {
            if (needed.matches(itemStack)) {
                return true;
            }
        }
        return super.wantsToPickUp(itemStack);
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemstack = itemEntity.getItem();
        if (this.wantsToPickUp(itemstack)) {
            if (!this.canAddItem(itemstack)) return;

            this.onItemPickup(itemEntity);
            this.take(itemEntity, itemstack.getCount());
            ItemStack itemstack1 = this.addItem(itemstack);

            this.farmedItems += itemstack.getCount() - itemstack1.getCount();
            NeededItem.applyToNeededItems(itemstack, neededItems);
            if (!this.needsToGetItems()) {
                this.clearPendingStorageComplaint();
            }

            if (itemstack1.isEmpty()) {
                itemEntity.remove(RemovalReason.DISCARDED);
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }
    }

    public ItemStack addItem(ItemStack itemStackToAdd) {
        if (itemStackToAdd.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack = itemStackToAdd.copy();
            this.moveItemToOccupiedSlotsWithSameType(itemstack);
            if (itemstack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.moveItemToEmptySlots(itemstack);
                return itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
            }
        }
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack itemStackToMove) {
        for(int i = 6; i < this.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = this.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(itemstack, itemStackToMove)) {
                this.moveItemsBetweenStacks(itemStackToMove, itemstack);
                if (itemStackToMove.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void moveItemToEmptySlots(ItemStack itemStack) {
        for(int i = 6; i < this.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = this.getInventory().getItem(i);
            if (itemstack.isEmpty()) {
                this.getInventory().setItem(i, itemStack.copyAndClear());
                return;
            }
        }

    }

    private void moveItemsBetweenStacks(ItemStack p_19186_, ItemStack p_19187_) {
        int i = Math.min(64, p_19187_.getMaxStackSize());
        int j = Math.min(p_19186_.getCount(), i - p_19187_.getCount());
        if (j > 0) {
            p_19187_.grow(j);
            p_19186_.shrink(j);
            this.getInventory().setChanged();
        }
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
        this.rememberCurrentWorkAreaBinding();

        nbt.putInt("farmedItems", farmedItems);
        if(lastStorage != null) nbt.putUUID("lastStorage", lastStorage);
        persistCitizenStateToLegacy(this.getCitizenCore(), nbt);
    }

    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        this.farmedItems = nbt.getInt("farmedItems");
        if(nbt.contains("lastStorage")) this.lastStorage = nbt.getUUID("lastStorage");
        hydrateCitizenStateFromLegacy(this.getCitizenCore(), nbt);
    }

    @OnlyIn(Dist.CLIENT)
    public SoundEvent getHurtSound(DamageSource ds) {
        if(RecruitsClientConfig.RecruitsLookLikeVillagers.get()){
            return SoundEvents.VILLAGER_HURT;
        }
        else
            return SoundEvents.PLAYER_HURT;
    }

    @OnlyIn(Dist.CLIENT)
    protected SoundEvent getDeathSound() {
        if(RecruitsClientConfig.RecruitsLookLikeVillagers.get()){
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
        return !this.getCommandSenderWorld().isDay();
    }

    public abstract Predicate<ItemEntity> getAllowedItems();

    public void initSpawn(){
        this.setEquipment();
        this.setDropEquipment();
        this.setPersistenceRequired();
        this.setCanPickUpLoot(true);
    }

    public double getDistanceToOwner(){
        return this.getOwner() != null ? this.distanceToSqr(this.getOwner()) : 1D;
    }

    public void tick() {
        super.tick();
        if(this.getCommandSenderWorld().isClientSide()) return;

        this.rememberCurrentWorkAreaBinding();
    }

    public static void hydrateCitizenStateFromLegacy(CitizenCore citizenCore, CompoundTag nbt) {
        citizenCore.apply(CitizenPersistenceBridge.fromWorkerLegacy(nbt));
    }

    public static CompoundTag persistCitizenStateToLegacy(CitizenCore citizenCore, CompoundTag nbt) {
        return CitizenPersistenceBridge.writeWorkerLegacy(citizenCore.snapshot(), nbt);
    }

    public abstract List<Item> inventoryInputHelp();

    public boolean needsToGetToChest() {
        return this.needsToGetFood() || needsToDeposit() || needsToGetItems();
    }

    public boolean needsToDeposit() {
        return forcedDeposit || farmedItems > 128;
    }
    @Nullable
    public ItemStack getMatchingItem(Predicate<ItemStack> predicate) {
        for (ItemStack stack : this.getInventory().items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return stack;
            }
        }
        return null;
    }

    public int countMatchingItems(Predicate<ItemStack> predicate) {
        int count = 0;

        for (ItemStack stack : this.getInventory().items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public int countMatchingStacks(Predicate<ItemStack> predicate) {
        int count = 0;

        for (ItemStack stack : this.getInventory().items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                count++;
            }
        }

        return count;
    }

    int currentTimeBreak;
    int breakingTime;
    int previousTimeBreak;

    public void mineBlock(BlockPos pos) {
        if (!this.isAlive()) return;

        Level level = this.getCommandSenderWorld();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        // Laubblöcke: mit Schere manuell abbauen und droppen
        if (state.getBlock() instanceof LeavesBlock && this.getMainHandItem().getItem() instanceof ShearsItem) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, pos, blockEntity, this, this.getMainHandItem());

            for (ItemStack drop : drops) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
            }

            state.onRemove(level, pos, Blocks.AIR.defaultBlockState(), false); // z.B. für Sounds/Particles
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F); // optionaler Scher-Sound

            return;
        }

        // Normaler Abbau
        if (currentTimeBreak % 5 == 4) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                    state.getSoundType().getHitSound(), SoundSource.BLOCKS, 1F, 0.75F, false);
        }

        if (breakingTime == 0) {
            breakingTime = (int) (state.getDestroySpeed(level, pos) * 30);
        }

        float destroySpeed = this.getUseItem().getDestroySpeed(state) * 2;
        currentTimeBreak += (int) destroySpeed;

        int stage = (int) ((float) currentTimeBreak / breakingTime * 10);
        if (stage != previousTimeBreak) {
            level.destroyBlockProgress(1, pos, stage);
            previousTimeBreak = stage;
        }

        if (currentTimeBreak >= breakingTime) {
            level.destroyBlock(pos, true, this);
            this.damageMainHandItem();
            currentTimeBreak = 0;
            breakingTime = 0;
            previousTimeBreak = 0;
        }

        this.swing(InteractionHand.MAIN_HAND);
    }


    public boolean needsToGetItems() {
        return neededItems.stream().anyMatch(neededItem -> neededItem.required);
    }

    public void addNeededItem(NeededItem neededItem) {
        if(neededItems.contains(neededItem)) return;

        neededItems.add(neededItem);
    }

    public void requestRequiredItem(NeededItem neededItem, String reasonToken, Component message) {
        if (neededItem == null) {
            return;
        }

        if (!neededItem.required) {
            this.addNeededItem(neededItem);
            return;
        }

        this.tryFulfillNeededItemsFromBuildAreaBuffer(neededItem);

        if (this.countMatchingItems(neededItem::matches) >= neededItem.count) {
            this.clearPendingStorageComplaint();
            return;
        }

        this.addNeededItem(neededItem);
        if (message != null) {
            this.storageRequestState.recordPendingComplaint(reasonToken, message.getString());
        }
    }

    public void clearPendingStorageComplaint() {
        this.storageRequestState.clear();
    }

    public void setStorageReservation(UUID reservationId, UUID endpointId, String itemId, int count) {
        this.storageRequestState.setReservation(reservationId, endpointId, itemId, count);
    }

    @Nullable
    public WorkerStorageRequestState.ReservationHandle getStorageReservation() {
        return this.storageRequestState.getReservation();
    }

    public void clearStorageReservation() {
        WorkerStorageRequestState.ReservationHandle reservation = this.storageRequestState.getReservation();
        if (reservation != null) {
            BannerModLogisticsService.shared().release(reservation.reservationId());
        }
        this.storageRequestState.clearReservation();
    }

    @Nullable
    public WorkerStorageRequestState.PendingComplaint releasePendingStorageComplaint() {
        return this.storageRequestState.releasePendingComplaint();
    }

    public BannerModSupplyStatus.WorkerSupplyStatus getSupplyStatus() {
        return BannerModSupplyStatus.workerSupplyStatus(this.storageRequestState.peekPendingComplaint());
    }

    public boolean hasPendingStorageComplaint() {
        return this.storageRequestState.hasPendingComplaint();
    }

    //@Override
    public void onItemStackAdded(ItemStack itemStack1){
        //super.onItemStackAdded(itemStack);
        ItemStack itemStack = itemStack1.copy();
        for(NeededItem neededItem : neededItems){
            if(neededItem.matches(itemStack)){
                NeededItem.applyToNeededItems(itemStack, neededItems);;
                break;
            }
        }

        if (!this.needsToGetItems()) {
            this.clearPendingStorageComplaint();
        }
    }

    public boolean tryFulfillNeededItemsFromBuildAreaBuffer(NeededItem neededItem) {
        if (!(this.getCurrentWorkArea() instanceof BuildArea buildArea) || neededItem == null || neededItem.count <= 0) {
            return false;
        }
        int moved = buildArea.extractBufferedItems(neededItem::matches, neededItem.count, this.getInventory());
        if (moved > 0) {
            for (int i = this.neededItems.size() - 1; i >= 0; i--) {
                NeededItem candidate = this.neededItems.get(i);
                if (candidate == neededItem) {
                    candidate.count -= moved;
                    if (candidate.count <= 0) {
                        this.neededItems.remove(i);
                    }
                    break;
                }
            }
            if (!this.needsToGetItems()) {
                this.clearPendingStorageComplaint();
            }
            return true;
        }
        return false;
    }

    public void switchMainHandItem(Predicate<ItemStack> predicate) {
        if (!this.isAlive() || predicate == null) return;

        SimpleContainer inventory = this.getInventory();
        ItemStack mainHand = this.getMainHandItem();
        if (predicate.test(mainHand)) return;

        for (int i = 6; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (predicate.test(stack)) {

                inventory.setItem(i, mainHand);
                this.setItemInHand(InteractionHand.MAIN_HAND, stack);
                return;
            }
        }
    }

    public double getHorizontalDistanceTo(Vec3 target){
        Vec3 position = new Vec3(position().x, 0, position().z);
        Vec3 toTarget = new Vec3(target.x, 0, target.z);

        return position.distanceToSqr(toTarget);
    }

    @Override
    public void die(DamageSource dmg) {
        super.die(dmg);
        if(this.getCurrentWorkArea() != null) getCurrentWorkArea().setBeingWorkedOn(false);
    }

    public static boolean isPosBroken(BlockPos pos, Level level, boolean allowWater) {
        BlockState state = level.getBlockState(pos);
        if(state.isAir() || UNBREAKABLES.contains(state.getBlock())) return true;
        if(allowWater){
            Fluid fluidState = level.getFluidState(pos).getType();
            return fluidState == Fluids.WATER || fluidState == Fluids.FLOWING_WATER;
        }
        return false;
    }

    public boolean hasFreeInvSlot() {
        for (int i = 6; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void openDepositsGUI(Player player) {

    }

    public boolean shouldWork() {
        return this.isOwned() && (this.getFollowState() == 0 || this.getFollowState() == 6);
    }
}
