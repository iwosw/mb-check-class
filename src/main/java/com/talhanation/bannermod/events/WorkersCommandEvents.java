package com.talhanation.bannermod.events;

import com.talhanation.bannermod.entity.civilian.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class WorkersCommandEvents {
	public void setWorkArea(UUID player_uuid, AbstractWorkerEntity worker, AABB area) {
        LivingEntity owner = worker.getOwner();
    }
}
