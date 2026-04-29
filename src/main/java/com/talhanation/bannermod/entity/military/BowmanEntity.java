package com.talhanation.bannermod.entity.military;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.ai.military.RecruitMoveTowardsTargetGoal;
import com.talhanation.bannermod.ai.military.RecruitStrategicFire;
import com.talhanation.bannermod.ai.military.RecruitRangedBowAttackGoal;
import com.talhanation.bannermod.persistence.military.RecruitsPatrolSpawn;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class BowmanEntity extends AbstractStrategicFireRecruitEntity implements IRangedRecruit {

    private final Predicate<ItemEntity> ALLOWED_ITEMS = (item) ->
            (!item.hasPickUpDelay() && item.isAlive() && getInventory().canAddItem(item.getItem()) && this.wantsToPickUp(item.getItem()));

    public BowmanEntity(EntityType<? extends AbstractRecruitEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new com.talhanation.bannermod.ai.military.RecruitRangedSpacingGoal(this));
        this.goalSelector.addGoal(2, new RecruitStrategicFire(this, 10, 20));
        this.goalSelector.addGoal(4, new RecruitRangedBowAttackGoal<>(this, 1.15D, 10, 20, 44.0F, getMeleeStartRange()));
        this.goalSelector.addGoal(8, new RecruitMoveTowardsTargetGoal(this, 1.15D, (float) this.getMeleeStartRange()));
    }
    @Override
    public double getMeleeStartRange() {
        return 5D;
    }


    //ATTRIBUTES
    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(NeoForgeMod.SWIM_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.05D)
                .add(Attributes.ATTACK_DAMAGE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 64.0D) //do not change as ranged ai dependants on it
                .add(Attributes.ENTITY_INTERACTION_RANGE, 0D)
                .add(Attributes.ATTACK_SPEED);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficultyInstance, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag nbt) {
        return finishRecruitLeafSpawn(world, difficultyInstance, super.finalizeSpawn(world, difficultyInstance, reason, data, nbt), false, true);
    }


    @Override
    public void initSpawn() {
        initStandardRecruitSpawn("Bowman", RecruitsServerConfig.BowmanCost.get());

        if(RecruitsServerConfig.RangedRecruitsNeedArrowsToShoot.get()){
            RecruitsPatrolSpawn.setRangedArrows(this);
        }
    }

    @Override
    public boolean canHoldItem(ItemStack itemStack){
        return !(itemStack.getItem() instanceof SwordItem || itemStack.getItem() instanceof ShieldItem || itemStack.getItem() instanceof CrossbowItem);
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float v) {
        if(this.level().isClientSide()) return;
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            RecruitRangedCombatService.fireBowAtTarget(this, target, v);
        }
    }

    public double arrowDamageModifier() {
        return 1.0D;
    }

    public void performRangedAttackXYZ(double x, double y, double z, float v, float angle, float force) {
        if(this.level().isClientSide()) return;
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            RecruitRangedCombatService.fireBowAtPosition(this, x, y, z, v, angle, force);
        }
    }

    public void fleeEntity(LivingEntity target) {
        if (target != null) {
            double fleeDistance = 10.0D;
            Vec3 vecTarget = new Vec3(target.getX(), target.getY(), target.getZ());
            Vec3 vecBowman = new Vec3(this.getX(), this.getY(), this.getZ());
            Vec3 fleeDir = vecBowman.subtract(vecTarget);
            fleeDir = fleeDir.normalize();
            double rnd = this.getRandom().nextGaussian() * 1.2;
            Vec3 fleePos = new Vec3(vecBowman.x + rnd + fleeDir.x * fleeDistance, vecBowman.y + fleeDir.y * fleeDistance, vecBowman.z + rnd + fleeDir.z * fleeDistance);
            this.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, 1.1D);
        }
    }

    @Override
    public boolean wantsToPickUp(ItemStack itemStack) {
        if ((itemStack.getItem() instanceof BowItem || itemStack.getItem() instanceof ProjectileWeaponItem || itemStack.getItem() instanceof SwordItem) && this.getMainHandItem().isEmpty()){
            return !hasSameTypeOfItem(itemStack);
        }
        else if(itemStack.is(ItemTags.ARROWS) && RecruitsServerConfig.RangedRecruitsNeedArrowsToShoot.get())
            return true;

        else
            return super.wantsToPickUp(itemStack);
    }

    @Override
    public Predicate<ItemEntity> getAllowedItems() {
        return ALLOWED_ITEMS;
    }

    public List<List<String>> getEquipment(){
        return RecruitsServerConfig.BowmanStartEquipments.get();
    }

    @Override
    public Predicate<ItemStack> getWeaponType() {
        return itemStack -> itemStack.getItem() instanceof BowItem;
    }
}
