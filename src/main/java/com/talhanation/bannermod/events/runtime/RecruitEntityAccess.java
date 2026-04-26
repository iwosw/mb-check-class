package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

final class RecruitEntityAccess {
    private RecruitEntityAccess() {
    }

    @Nullable
    static AbstractRecruitEntity asRecruit(@Nullable Entity entity) {
        if (!AbstractRecruitEntity.class.isInstance(entity)) {
            return null;
        }
        return AbstractRecruitEntity.class.cast(entity);
    }

    @Nullable
    static AbstractRecruitEntity asRecruit(@Nullable LivingEntity entity) {
        return asRecruit((Entity) entity);
    }
}
