package com.talhanation.bannerlord.entity.civilian;

import com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity;
import com.talhanation.bannerlord.ai.pathfinding.AsyncGroundPathNavigation;
import com.talhanation.workers.init.ModProfessions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class CourierEntity extends AbstractWorkerEntity {

    public CourierEntity(EntityType<? extends AbstractWorkerEntity> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(ForgeMod.SWIM_SPEED.get(), 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
                .add(Attributes.ATTACK_DAMAGE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(ForgeMod.ENTITY_REACH.get(), 0D)
                .add(Attributes.ATTACK_SPEED);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficultyInstance, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag nbt) {
        RandomSource randomsource = world.getRandom();
        SpawnGroupData spawnData = super.finalizeSpawn(world, difficultyInstance, reason, data, nbt);
        ((AsyncGroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        this.populateDefaultEquipmentEnchantments(randomsource, difficultyInstance);
        this.initSpawn();
        return spawnData;
    }

    @Override
    public void initSpawn() {
        this.setCustomName(Component.literal(capitalizeProfessionName()));
        this.setCost(10);
        this.setEquipment();
        this.setDropEquipment();
        this.setRandomSpawnBonus();
        this.setPersistenceRequired();
        AbstractRecruitEntity.applySpawnValues(this);
    }

    @Override
    protected boolean supportsCourierTasks() {
        return true;
    }

    public net.minecraft.world.entity.npc.VillagerProfession getProfession() {
        return ModProfessions.COURIER.get();
    }

    @Override
    public Predicate<ItemEntity> getAllowedItems() {
        return null;
    }

    @Override
    public List<Item> inventoryInputHelp() {
        return null;
    }

    @Override
    public com.talhanation.bannerlord.entity.civilian.workarea.AbstractWorkAreaEntity getCurrentWorkArea() {
        return null;
    }

    private String capitalizeProfessionName() {
        String professionName = this.getProfession().name();
        if (professionName == null || professionName.isBlank()) {
            return "Courier";
        }
        return Character.toUpperCase(professionName.charAt(0)) + professionName.substring(1);
    }
}
