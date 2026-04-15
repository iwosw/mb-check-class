package com.talhanation.bannermod.ai.military.controller;

import net.minecraft.world.phys.Vec3;

public interface IAttackController {
    void tick();
    void setInitPos(Vec3 pos);
    boolean isTargetInRange();
}
