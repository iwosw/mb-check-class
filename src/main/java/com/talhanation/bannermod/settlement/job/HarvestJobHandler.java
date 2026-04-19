package com.talhanation.bannermod.settlement.job;

import com.talhanation.bannermod.settlement.BannerModSettlementJobHandlerSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentMode;
import net.minecraft.resources.ResourceLocation;

/**
 * Stub harvest/gathering handler.
 *
 * <p>The settlement enum {@link BannerModSettlementJobHandlerSeed} does not currently declare
 * a dedicated {@code HARVEST} value. The closest existing seed is
 * {@link BannerModSettlementJobHandlerSeed#FLOATING_LABOR_POOL}: residents in the floating
 * labor pool are the pool from which gathering/harvesting tasks (farmer, lumberjack, miner,
 * fisherman, animal farmer runtimes in the legacy workers subsystem) are drawn. Binding this
 * handler to that seed lets slice-F start wiring the floating labor pool to a data-driven task
 * catalogue without touching existing worker entities.</p>
 *
 * <p>Real target selection and tick execution remain in the legacy hard-coded
 * {@code AbstractWorkerEntity} subclasses; this handler is a placeholder that future slices
 * will replace with a JSON-loaded behaviour recipe (mirroring Millenaire's
 * {@code GatheringGoal_*} pattern, but written from scratch).</p>
 */
public final class HarvestJobHandler implements JobHandler {

    public static final ResourceLocation ID = new ResourceLocation("bannermod", "harvest");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public BannerModSettlementJobHandlerSeed handles() {
        return BannerModSettlementJobHandlerSeed.FLOATING_LABOR_POOL;
    }

    @Override
    public boolean canHandle(JobExecutionContext ctx) {
        if (ctx == null || ctx.resident() == null) {
            return false;
        }
        return ctx.resident().residentMode() == BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER;
    }

    @Override
    public JobExecutionResult runOneStep(JobExecutionContext ctx) {
        // Intentional stub: the real gathering loop is still owned by the legacy worker entities.
        // Returning COMPLETED lets the scheduler treat each tick as a unit of progress, keeping
        // the contract observable in tests without changing any runtime behaviour.
        return JobExecutionResult.COMPLETED;
    }

    @Override
    public int cooldownTicks() {
        // Gathering in Minecraft is coarse (block breaks, crop pulls); a few ticks between steps
        // is a reasonable default placeholder until slice-F wires real AI timing.
        return 5;
    }
}
