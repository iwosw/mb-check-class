package com.talhanation.bannermod.settlement.goal;

import net.minecraft.resources.ResourceLocation;

public interface ResidentGoal {

    ResourceLocation id();

    /**
     * Return >0 if this goal wants to run right now for the resident in ctx;
     * 0 means skip. Higher = more urgent. Deterministic — no RNG.
     */
    int computePriority(ResidentGoalContext ctx);

    /**
     * Gate: even with high priority, refuse if preconditions fail (e.g. no
     * workplace bound, role mismatch, cooldown still active).
     */
    boolean canStart(ResidentGoalContext ctx);

    /**
     * Build the task representing one execution of this goal. The scheduler
     * owns the task lifetime; impls must NOT retain mutable state across calls.
     */
    ResidentTask start(ResidentGoalContext ctx);

    /**
     * Cooldown in ticks after this goal's task ends successfully before the
     * same goal may be picked again for the same resident. Default 0 = no
     * cooldown.
     */
    default int cooldownTicks() {
        return 0;
    }
}
