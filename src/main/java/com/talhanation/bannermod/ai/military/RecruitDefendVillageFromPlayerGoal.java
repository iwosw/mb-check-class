package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class RecruitDefendVillageFromPlayerGoal extends TargetGoal {
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final int SCAN_INTERVAL_JITTER_TICKS = 10;

    private final AbstractRecruitEntity recruit;
    @Nullable
    private LivingEntity potentialTarget;
    private long nextScanTick;
    private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D).ignoreLineOfSight();

    public RecruitDefendVillageFromPlayerGoal(AbstractRecruitEntity p_26029_) {
        super(p_26029_, false, true);
        this.recruit = p_26029_;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }
    public boolean canUse() {
        long gameTime = this.recruit.getCommandSenderWorld().getGameTime();
        if (gameTime < this.nextScanTick) {
            return false;
        }

        this.nextScanTick = gameTime + SCAN_INTERVAL_TICKS + this.recruit.getRandom().nextInt(SCAN_INTERVAL_JITTER_TICKS + 1);
        this.potentialTarget = null;

        AABB aabb = this.recruit.getBoundingBox().inflate(30.0D, 8.0D, 30.0D);
        List<Villager> list = this.recruit.getCommandSenderWorld().getEntitiesOfClass(Villager.class, aabb, (livingEntity) -> !this.attackTargeting.test(this.recruit, livingEntity));

        List<Player> list1 = this.recruit.getCommandSenderWorld().getEntitiesOfClass(Player.class, aabb);

        for(Villager villager : list) {
            for(Player player : list1) {
                int i = villager.getPlayerReputation(player);
                if (i <= -100) {
                    this.potentialTarget = player;
                }
            }
        }

        if (this.potentialTarget == null) {
            return false;
        } else {
            return !(this.potentialTarget instanceof Player) || !this.potentialTarget.isSpectator() && !((Player)this.potentialTarget).isCreative();
        }
    }

    public void start() {
        this.recruit.assignReactiveCombatTarget(this.potentialTarget);
        super.start();
    }
}
