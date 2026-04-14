package com.talhanation.bannerlord.ai.military.controller;

import com.talhanation.bannerlord.entity.shared.*;

import net.minecraft.world.phys.Vec3;

public interface IAttackController {
    void tick();
    void setInitPos(Vec3 pos);
    boolean isTargetInRange();
}
