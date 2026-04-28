package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.compat.SmallShips;
import com.talhanation.bannermod.ai.military.UseShield;
import com.talhanation.bannermod.ai.military.controller.SmallShipsController;
import com.talhanation.bannermod.ai.military.controller.CaptainPrepareShipAttackController;
import com.talhanation.bannermod.ai.military.navigation.SailorPathNavigation;
import com.talhanation.bannermod.util.RecruitCommanderUtil;
import com.talhanation.bannermod.util.WaterObstacleScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class CaptainEntity extends AbstractLeaderEntity {
    public final SmallShipsController smallShipsController;
    private static final EntityDataAccessor<Optional<BlockPos>> SAIL_POS = SynchedEntityData.defineId(CaptainEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private final Predicate<ItemEntity> ALLOWED_ITEMS = (item) ->
            (!item.hasPickUpDelay() && item.isAlive() && getInventory().canAddItem(item.getItem()) && this.wantsToPickUp(item.getItem()));
    public CaptainEntity(EntityType<? extends AbstractLeaderEntity> entityType, Level world) {
        super(entityType, world);
        this.attackController = new CaptainPrepareShipAttackController(this);
        this.smallShipsController = new SmallShipsController(this, world);
        this.smallShipsController.tryMountShip(getVehicle());
    }
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SAIL_POS, Optional.empty());
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new UseShield(this));
    }

    @Override
    public double getDistanceToReachWaypoint() {
        return 150D;
    }

    @Override
    @NotNull
    public PathNavigation getNavigation() {
        if (this.getVehicle() instanceof Boat) {
            return new SailorPathNavigation(this, this.getCommandSenderWorld());
        }
        else
            return super.getNavigation();
    }

    @Override
    public void tick() {
        super.tick();
        if(this.level().isClientSide()) return;

        if(this.getVehicle() != null && smallShipsController.ship == null){
            smallShipsController.tryMountShip(this.getVehicle());
        }

        smallShipsController.tick();
        attackController.tick();
    }


    public void setHoldPos(Vec3 holdPos){
        if(SmallShips.isSmallShip(this.getVehicle())){
            if(!WaterObstacleScanner.isWaterBlockPos(this.getCommandSenderWorld(), new BlockPos((int) holdPos.x, (int) holdPos.y, (int) holdPos.z))){
                super.setHoldPos(holdPos);
            }
        }
        super.setHoldPos(holdPos);
    }

    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if(this.getSailPos() != null){
            nbt.putInt("SailPosX", this.getSailPos().getX());
            nbt.putInt("SailPosY", this.getSailPos().getY());
            nbt.putInt("SailPosZ", this.getSailPos().getZ());
        }
    }

    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("SailPosX") && nbt.contains("SailPosY") && nbt.contains("SailPosZ")) {
            this.setSailPos(new BlockPos (
                    nbt.getInt("SailPosX"),
                    nbt.getInt("SailPosY"),
                    nbt.getInt("SailPosZ")));
        }
    }

    //ATTRIBUTES
    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
                .add(Attributes.ATTACK_DAMAGE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 0D)
                .add(Attributes.ATTACK_SPEED);
    }


    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficultyInstance, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag nbt) {
        return finishRecruitLeafSpawn(world, difficultyInstance, super.finalizeSpawn(world, difficultyInstance, reason, data, nbt), true, false);
    }

    @Override
    public void initSpawn() {
        initPersistentNamedSpawn("Captain");
    }

    @Override
    public boolean startRiding(Entity entity) {
        smallShipsController.tryMountShip(entity);
        return super.startRiding(entity);
    }

    @Override
    public void stopRiding() {
        smallShipsController.tryDisMountShip();
        super.stopRiding();
    }

    @Override
    public boolean wantsToPickUp(ItemStack itemStack) {//TODO: add ranged combat
        if(itemStack.getDescriptionId().contains("smallships")) return true;

        if((itemStack.getItem() instanceof SwordItem && this.getMatchingItem(item -> item.getItem() instanceof SwordItem) == null) ||
                (itemStack.getItem() instanceof BowItem && this.getMatchingItem(item -> item.getItem() instanceof BowItem) == null) ||
                (itemStack.getItem() instanceof ShieldItem) && this.getMatchingItem(item -> item.getItem() instanceof ShieldItem) == null)
            return true;

        else return super.wantsToPickUp(itemStack);
    }

    public Predicate<ItemEntity> getAllowedItems(){
        return ALLOWED_ITEMS;
    }

    @Override
    public boolean canHoldItem(ItemStack itemStack){
        return !(itemStack.getItem() instanceof CrossbowItem || itemStack.getItem() instanceof BowItem); //TODO: add ranged combat
    }

    @Override
    public AbstractRecruitEntity get() {
        return this;
    }

    public BlockPos getSailPos() {
        return this.entityData.get(SAIL_POS).orElse(null);
    }

    public float getPrecisionMin(){
        int base = 50;
        if(this.getVehicle() != null && this.getVehicle().getEncodeId().contains("smallships")){
            base = 100;
        }
        return base;
    }

    public float getPrecisionMax(){
        int base = 150;
        if(this.getVehicle() != null && this.getVehicle().getEncodeId().contains("smallships")){
            base = 200;
        }

        return base;
    }

    public void setSailPos(BlockPos pos) {
        if(pos == null) this.setSailPos(Optional.empty());
        else this.setSailPos(Optional.of(pos));
    }

    public void setSailPos(Optional<BlockPos>  pos) {
        this.entityData.set(SAIL_POS, pos);
    }

    @Override
    public void setFollowState(int state){
        super.setFollowState(state);

        this.calculateSailPos(state);
    }
    @Override
    protected void moveToCurrentWaypoint() {
        if(this.getVehicle() != null && this.getVehicle() instanceof Boat){
            // Correct Y to actual water surface so SailorPathNavigation finds a valid target.
            int surfaceY = getCommandSenderWorld().getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                    this.currentWaypoint.getX(), this.currentWaypoint.getZ()) - 1;
            int y = Math.max(surfaceY, getCommandSenderWorld().getMinBuildHeight());
            this.setSailPos(new BlockPos(this.currentWaypoint.getX(), y, this.currentWaypoint.getZ()));
        }
        else super.moveToCurrentWaypoint();
    }

    //0 = wander
    //1 = follow
    //2 = hold position
    //3 = back to position
    //4 = hold my position
    //5 = Protect
    public void calculateSailPos(int state) {

        switch (state){
            case 0 -> {// WANDER
                if(this.getMovePos() != null){
                    BlockPos pos = this.getMovePos();
                    setSailPos(pos);
                }
            }

            case 2,3,4 -> {
                if(this.getHoldPos() != null){
                    Vec3 pos = this.getHoldPos();
                    setSailPos(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
                }
            }

            case 5 -> {// PROTECT
                LivingEntity protect = this.getProtectingMob();
                if(protect != null){
                    BlockPos pos = protect.getOnPos();
                    setSailPos(pos);
                }
            }
        }
        this.smallShipsController.calculatePath();
    }
    @Override
    public boolean canAttackWhilePatrolling() {
        if(enemyArmySpotted() || this.getTarget() != null && this.getTarget().isAlive()) {
            if(this.army != null && !this.army.getRanged().isEmpty()){
                return true;
            }
            else if(this.getVehicle() != null && smallShipsController.ship != null){
                return smallShipsController.ship.hasCannons() && smallShipsController.ship.canShootCannons();
            }
            return true;
        }
        else
            return false;
    }
    @Override
    public void handleUpkeepState() {
        if(army == null) return;

        if(waitForRecruitsUpkeepTime != 0){
            if(smallShipsController.ship != null){
                double speed = smallShipsController.ship.getShipSpeed();
                if(speed < 0.01){
                    this.stopRiding();
                    RecruitCommanderUtil.setRecruitsDismount(army.getAllRecruitUnits());
                }
            }
        }
        else {
            this.shouldMount(true, this.getMountUUID());
            RecruitCommanderUtil.setRecruitsFollow(this.army.getAllRecruitUnits(), this.getUUID());
            if(this.getVehicle() != null && this.getVehicle().getUUID().equals(this.getMountUUID())){
                if(canRepair() && smallShipsController.ship.getDamage() > 10){
                    if(this.tickCount % 20 == 0) smallShipsController.ship.repairShip(this);
                }
                else if(this.getCannonBallCount(this.getInventory()) > 0 && smallShipsController.ship instanceof Container container && this.getCannonBallCount(container) < 128){
                    if(this.tickCount % 20 == 0) refillCannonBalls();
                }
                else{
                    if(isRecruitsInCommandOnBoard()) {
                        waitForRecruitsUpkeepTime = this.getAgainResupplyTime(); // time to resupply again
                        this.setPatrolState(State.PATROLLING);
                    }
                    else{
                        this.setFollowState(2);
                        RecruitCommanderUtil.setRecruitsMount(this.army.getAllRecruitUnits(), this.getVehicle().getUUID());
                    }
                }
            }
        }
    }
    @Override
    public boolean getOtherUpkeepInterruption(){
        if(smallShipsController.ship != null && smallShipsController.ship.getDamage() > 10){
            return true;
        }

        if(smallShipsController.ship != null && smallShipsController.ship.hasCannons() && smallShipsController.ship.getBoat() instanceof Container container && this.getCannonBallCount(container) < 32){
            return true;
        }
        return super.getOtherUpkeepInterruption();
    }

    private boolean isRecruitsInCommandOnBoard() {
        return this.army.getAllRecruitUnits().stream().allMatch(recruit -> recruit.getVehicle() != null && recruit.getVehicle().equals(this.getVehicle()));
    }

    public int getResupplyTime() {
        return 1000;
    }

    public boolean canRepair() {
        return this.getInventory().hasAnyMatching(itemStack -> itemStack.is(Items.IRON_NUGGET)) && this.getInventory().hasAnyMatching(itemStack -> itemStack.is(ItemTags.PLANKS));
    }

    public void refillCannonBalls() {
        if(this.getInventory().hasAnyMatching(itemStack -> itemStack.getDescriptionId().contains("cannon_ball"))){
            if(this.getVehicle() instanceof Container container){

                for(int i = 0; i < this.getInventory().getContainerSize(); i++){
                    ItemStack stack = this.getInventory().getItem(i);

                    if(stack.getDescriptionId().contains("cannon_ball")){
                        ItemStack cannonball = stack.copy();
                        for(int k = 0; k < container.getContainerSize(); k++){
                            if(container.getItem(k).isEmpty()) {
                                container.setItem(k, cannonball);
                                stack.shrink(cannonball.getCount());

                                this.getInventory().setChanged();
                                container.setChanged();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public int getCannonBallCount(Container container) {
        int count = 0;
        for(int i = 0; i < container.getContainerSize(); i++){
            ItemStack stack = container.getItem(i);

            if(stack.getDescriptionId().contains("cannon_ball")){
                    count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public boolean isAtMission() {
        return this.state != State.IDLE && this.state != State.PAUSED && this.state != State.STOPPED;
    }

    @Override
    public boolean hurt(@NotNull DamageSource dmg, float amt) {
        this.commandCooldown = 0;
        return super.hurt(dmg, amt);
    }
}








