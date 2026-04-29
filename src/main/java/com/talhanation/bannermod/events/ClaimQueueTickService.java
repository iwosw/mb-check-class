package com.talhanation.bannermod.events;

import com.talhanation.bannermod.army.command.CommandIntentQueueRuntime;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.settlement.validation.BuildingInvalidationRuntime;
import com.talhanation.bannermod.util.AdaptiveRuntimeBudgets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

final class ClaimQueueTickService {
    private long serverTickStartedAtNanos;

    void onServerTickStart() {
        serverTickStartedAtNanos = System.nanoTime();
    }

    void recordServerTickDuration() {
        if (serverTickStartedAtNanos > 0L) {
            AdaptiveRuntimeBudgets.recordServerTickNanos(System.nanoTime() - serverTickStartedAtNanos);
        }
    }

    void tickCommandQueue(MinecraftServer server, ServerLevel level) {
        // Command-intent queue advancement runs every tick; the runtime no-ops when idle.
        CommandIntentQueueRuntime.instance().tick(server, level.getGameTime());
    }

    void tickBuildingInvalidationQueue(ServerLevel level) {
        int revalidationBudget = AdaptiveRuntimeBudgets.intBudget(
                "settlement.revalidation.batch",
                WorkersServerConfig.settlementRevalidationBatchSizePerTick(),
                1
        );
        BuildingInvalidationRuntime.tickBatch(level, revalidationBudget);
    }
}
