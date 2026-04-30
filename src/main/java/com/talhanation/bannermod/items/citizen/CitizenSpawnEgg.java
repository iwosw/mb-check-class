package com.talhanation.bannermod.items.citizen;

import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

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
        return super.useOn(context);
    }
}
