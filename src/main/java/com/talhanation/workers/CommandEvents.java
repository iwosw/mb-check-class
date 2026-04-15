package com.talhanation.workers;

import com.talhanation.bannerlord.entity.civilian.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class CommandEvents {
	public void setWorkArea(UUID player_uuid, AbstractWorkerEntity worker, AABB area) {
        LivingEntity owner = worker.getOwner();
    }
}
