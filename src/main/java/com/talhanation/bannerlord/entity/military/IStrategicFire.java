package com.talhanation.bannerlord.entity.military;

import com.talhanation.bannerlord.entity.shared.*;

import net.minecraft.core.BlockPos;

public interface IStrategicFire {
    void setShouldStrategicFire(boolean should);
    void setStrategicFirePos(BlockPos blockpos);

}
