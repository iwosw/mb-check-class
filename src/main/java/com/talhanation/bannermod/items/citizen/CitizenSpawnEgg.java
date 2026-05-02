package com.talhanation.bannermod.items.citizen;

import com.talhanation.bannermod.citizen.CitizenProfession;
import com.talhanation.bannermod.entity.civilian.WorkerCitizenConversionService;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.items.military.RecruitsSpawnEgg;
import com.talhanation.bannermod.settlement.prefab.staffing.PrefabAutoStaffingRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Single spawn egg for the unified {@link CitizenEntity}. Profession data is
 * written into the egg's entity NBT so the normal spawn-egg placement path
 * can restore it while still preserving vanilla placement/finalization.
 */
public class CitizenSpawnEgg extends DeferredSpawnEggItem {

    public CitizenSpawnEgg(Supplier<? extends EntityType<? extends CitizenEntity>> entityType,
                           int primaryColor,
                           int secondaryColor,
                           Properties properties) {
        super(entityType, primaryColor, secondaryColor, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        Entity entity = this.getType(stack).create(serverLevel);
        if (!(entity instanceof CitizenEntity citizen)) {
            return super.useOn(context);
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        citizen.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        citizen.getPersistentData().putLong(
                PrefabAutoStaffingRuntime.TAG_ASSIGNMENT_PAUSE_UNTIL,
                serverLevel.getGameTime() + WorkerCitizenConversionService.MANUAL_ASSIGNMENT_PAUSE_TICKS
        );

        if (context.getPlayer() != null) {
            citizen.setOwnerUUID(Optional.of(context.getPlayer().getUUID()));
            citizen.setOwned(true);
        }

        CompoundTag entityTag = readEntityData(stack);
        if (entityTag != null && !entityTag.isEmpty()) {
            applyEntityTag(serverLevel, citizen, entityTag);
        }

        serverLevel.addFreshEntity(citizen);
        if (citizen.getTeam() instanceof PlayerTeam team) {
            serverLevel.getScoreboard().addPlayerToTeam(citizen.getScoreboardName(), team);
        } else if (context.getPlayer() != null && context.getPlayer().getTeam() instanceof PlayerTeam playerTeam) {
            serverLevel.getScoreboard().addPlayerToTeam(citizen.getScoreboardName(), playerTeam);
        }
        if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static CompoundTag readEntityData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.ENTITY_DATA);
        return data == null ? null : data.copyTag();
    }

    private static void applyEntityTag(ServerLevel level, CitizenEntity citizen, CompoundTag entityTag) {
        if (entityTag.contains("Name")) {
            citizen.setCustomName(Component.literal(entityTag.getString("Name")));
        }
        if (entityTag.hasUUID("OwnerUUID")) {
            citizen.setOwnerUUID(Optional.of(entityTag.getUUID("OwnerUUID")));
            citizen.setOwned(true);
        }
        if (entityTag.contains("Team")) {
            PlayerTeam playerTeam = level.getScoreboard().getPlayerTeam(entityTag.getString("Team"));
            if (playerTeam != null) {
                level.getScoreboard().addPlayerToTeam(citizen.getScoreboardName(), playerTeam);
            }
        }
        if (entityTag.contains("CitizenProfession")) {
            citizen.switchProfession(CitizenProfession.fromTagName(entityTag.getString("CitizenProfession")));
        }
    }
}
