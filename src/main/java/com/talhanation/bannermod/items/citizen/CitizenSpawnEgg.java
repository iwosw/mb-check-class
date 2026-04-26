package com.talhanation.bannermod.items.citizen;

import com.talhanation.bannermod.citizen.CitizenProfession;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.function.Supplier;

/**
 * Single spawn egg for the unified {@link CitizenEntity}. Reads an optional
 * {@code EntityTag.CitizenProfession} string from the stack NBT and applies
 * it via {@link CitizenEntity#switchProfession(CitizenProfession)} so a
 * picked-up citizen retains their role when re-spawned.
 */
public class CitizenSpawnEgg extends ForgeSpawnEggItem {

    public CitizenSpawnEgg(Supplier<? extends EntityType<? extends CitizenEntity>> entityType,
                           int primaryColor,
                           int secondaryColor,
                           Properties properties) {
        super(entityType, primaryColor, secondaryColor, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        EntityType<?> type = this.getType(stack.getTag());
        Entity entity = type.create(level);
        if (!(entity instanceof CitizenEntity citizen)) {
            return super.useOn(context);
        }

        citizen.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0F, 0F);
        level.addFreshEntity(citizen);

        CompoundTag entityTag = readEntityTag(stack);
        if (entityTag != null && entityTag.contains("CitizenProfession")) {
            CitizenProfession profession = CitizenProfession.fromTagName(entityTag.getString("CitizenProfession"));
            if (profession != CitizenProfession.NONE) {
                citizen.switchProfession(profession);
            }
        }

        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static CompoundTag readEntityTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("EntityTag")) return null;
        return tag.getCompound("EntityTag");
    }
}
