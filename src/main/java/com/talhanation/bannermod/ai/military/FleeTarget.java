package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AssassinEntity;
import com.talhanation.bannermod.ai.pathfinding.AsyncPathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class FleeTarget extends Goal {

    AsyncPathfinderMob entity;
    private int fleePathCooldown;

    public FleeTarget(AsyncPathfinderMob creatureEntity) {
    this.entity = creatureEntity;
    }

    @Override
    public boolean canUse() {
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();


        return (currentHealth <  maxHealth - maxHealth / 2.25);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.entity.getTarget();
        if (target != null && target.isAlive() && entity.distanceToSqr(target) <= 1024D) {
            if (fleePathCooldown-- > 0) return;
            fleePathCooldown = 6 + entity.getRandom().nextInt(4);

            double fleeDistance = 64.0D;
            Vec3 vecTarget = new Vec3(target.getX(), target.getY(), target.getZ());
            Vec3 vecRec = new Vec3(entity.getX(), entity.getY(), entity.getZ());
            Vec3 fleeDir = vecRec.subtract(vecTarget);
            fleeDir = fleeDir.normalize();
            Vec3 fleePos = new Vec3(vecRec.x + fleeDir.x * fleeDistance, vecRec.y + fleeDir.y * fleeDistance, vecRec.z + fleeDir.z * fleeDistance);
            entity.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, 1.25D);
            if (entity instanceof AssassinEntity recruit) {
                recruit.setFleeing(true);
            }
        } else if (entity instanceof AssassinEntity recruit) {
            recruit.setFleeing(false);
        }
    }
}
