package com.talhanation.bannermod.citizen;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public record CitizenRoleContext(
        CitizenRole role,
        CitizenCore citizenCore,
        @Nullable LivingEntity entity,
        @Nullable Player requester,
        @Nullable UUID boundWorkAreaUuid
) {
}
