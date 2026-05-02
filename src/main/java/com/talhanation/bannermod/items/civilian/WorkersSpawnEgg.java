package com.talhanation.bannermod.items.civilian;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.items.military.RecruitsSpawnEgg;
import com.talhanation.bannermod.entity.civilian.MerchantEntity;
import com.talhanation.bannermod.persistence.civilian.WorkersMerchantTrade;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Optional;
import java.util.function.Supplier;

public class WorkersSpawnEgg extends RecruitsSpawnEgg {
    public WorkersSpawnEgg(Supplier<? extends EntityType<? extends AbstractRecruitEntity>> entityType, int primaryColor, int secondaryColor, Properties properties) {
        super(entityType, primaryColor, secondaryColor, properties);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack stack = context.getItemInHand();
            BlockPos pos = context.getClickedPos();
            EntityType<?> entitytype = this.getType(stack);
            Entity entity = entitytype.create(world);
            CompoundTag entityTag = readEntityData(stack);
            if (entity instanceof AbstractRecruitEntity) {
                AbstractRecruitEntity recruit = (AbstractRecruitEntity)entity;
                if (entityTag != null) {
                    if(entity instanceof MerchantEntity merchant){
                        fillMerchant(merchant, entityTag, pos);
                    }
                    else fillRecruit(recruit, entityTag, pos);
                    world.addFreshEntity(recruit);
                    if (!context.getPlayer().isCreative()) {
                        stack.shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                }
                if (recruit instanceof AbstractWorkerEntity worker && world instanceof ServerLevel serverLevel) {
                    BlockPos spawnPos = pos.relative(context.getClickedFace());
                    worker.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
                    worker.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWN_EGG, null, null);
                    if (context.getPlayer() != null) {
                        worker.setOwnerUUID(Optional.of(context.getPlayer().getUUID()));
                        worker.setIsOwned(true);
                    }
                    world.addFreshEntity(worker);
                    if (context.getPlayer() != null && context.getPlayer().getTeam() instanceof PlayerTeam playerTeam) {
                        serverLevel.getScoreboard().addPlayerToTeam(worker.getScoreboardName(), playerTeam);
                    }
                    if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
                        stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            return super.useOn(context);
        }
    }

    public void fillMerchant(MerchantEntity merchant, CompoundTag entityTag, BlockPos pos){
        fillRecruit(merchant, entityTag, pos);

        entityTag.put("Trades", WorkersMerchantTrade.listToNbt(merchant.getTrades()));
        entityTag.putBoolean("isCreative", merchant.isCreative());
        entityTag.putInt("TraderProgress", merchant.getTraderProgress());
        entityTag.putInt("TraderLevel", merchant.getTraderLevel());
    }
}
