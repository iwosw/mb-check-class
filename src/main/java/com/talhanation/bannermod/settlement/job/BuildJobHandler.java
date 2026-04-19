package com.talhanation.bannermod.settlement.job;

import com.talhanation.bannermod.settlement.BannerModSettlementJobHandlerSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentMode;
import net.minecraft.resources.ResourceLocation;

/**
 * Stub build/construction handler.
 *
 * <p>The settlement enum {@link BannerModSettlementJobHandlerSeed} does not declare a
 * dedicated {@code BUILD} value. The closest existing seed is
 * {@link BannerModSettlementJobHandlerSeed#LOCAL_BUILDING_LABOR}: that seed is already used by
 * {@code BannerModSettlementResidentJobDefinition.defaultFor(...)} for controlled workers bound
 * to a local building service contract, which is precisely the population from which a
 * future builder-style handler will pull. Binding this handler there keeps the contract
 * aligned with the existing default-resolution code without modifying it.</p>
 *
 * <p>As with {@link HarvestJobHandler}, the real per-block construction logic still lives in
 * the legacy builder worker entity. This placeholder gives slice-F a stable hook point.</p>
 */
public final class BuildJobHandler implements JobHandler {

    public static final ResourceLocation ID = new ResourceLocation("bannermod", "build");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public BannerModSettlementJobHandlerSeed handles() {
        return BannerModSettlementJobHandlerSeed.LOCAL_BUILDING_LABOR;
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
        // Intentional stub; see HarvestJobHandler for rationale.
        return JobExecutionResult.COMPLETED;
    }

    @Override
    public int cooldownTicks() {
        // Construction ticks are heavier than gathering ticks; give builders a slightly longer
        // cadence placeholder so scheduler tuning later has a sensible starting point.
        return 10;
    }
}
