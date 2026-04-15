package com.talhanation.recruits.pathfinding;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.config.RecruitsServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class AsyncPathProcessor {

    private static final PathProcessingRuntime RUNTIME = new PathProcessingRuntime();
    private static final ProfilingCounters PROFILING = new ProfilingCounters();
    private static volatile ThreadPoolExecutor pathFindingExecutor = null;

    public static void start() {
        ThreadPoolExecutor existing = pathFindingExecutor;
        if (existing != null && !existing.isShutdown()) {
            return;
        }

        if (existing != null) {
            existing.shutdownNow();
        }

        int workersCount = Math.max(1, RecruitsServerConfig.AsyncPathfindingThreadsCount.get());
        pathFindingExecutor = RUNTIME.createExecutor(workersCount);
    }

    public static void shutdown() {
        ThreadPoolExecutor executor = pathFindingExecutor;
        if (executor == null || executor.isShutdown()) return;

        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            Main.LOGGER.warn("AsyncPathProcessor shutdown interrupted");
        }
    }

    static boolean shouldProcessSynchronously(@Nullable ThreadPoolExecutor executor) {
        return executor == null || executor.isShutdown();
    }

    static void deliverProcessedPath(@Nullable Path path, @Nullable Executor handoffExecutor, Consumer<@Nullable Path> afterProcessing) {
        deliverProcessedPath(path, handoffExecutor, () -> true, afterProcessing);
    }

    static void deliverProcessedPath(@Nullable Path path,
                                     @Nullable Executor handoffExecutor,
                                     BooleanSupplier shouldDeliver,
                                     Consumer<@Nullable Path> afterProcessing) {
        Consumer<@Nullable Path> guardedDelivery = deliveredPath -> {
            if (!shouldDeliver.getAsBoolean()) {
                PROFILING.recordDroppedCallback();
                return;
            }
            PROFILING.recordAwaitDelivery(deliveredPath != null);
            afterProcessing.accept(deliveredPath);
        };
        RUNTIME.deliver(path, handoffExecutor, guardedDelivery);
    }

    protected static void queue(@NotNull AsyncPath path) {
        PROFILING.recordQueueSubmission();
        ThreadPoolExecutor executor = pathFindingExecutor;
        if (shouldProcessSynchronously(executor)) {
            PROFILING.recordSyncFallback();
            path.process();
            return;
        }
        CompletableFuture.runAsync(path::process, executor);
    }
    
    public static void awaitProcessing(@Nullable Path path, @Nullable MinecraftServer server, Consumer<@Nullable Path> afterProcessing) {
        awaitProcessing(path, server, () -> true, afterProcessing);
    }

    public static void awaitProcessing(@Nullable Path path,
                                       @Nullable MinecraftServer server,
                                       BooleanSupplier shouldDeliver,
                                       Consumer<@Nullable Path> afterProcessing) {
        PROFILING.recordAwaitCall();
        Executor handoffExecutor = server == null ? null : server::execute;
        if (path instanceof AsyncPath asyncPath && !asyncPath.isProcessed()) {
            asyncPath.postProcessing(() -> deliverProcessedPath(path, handoffExecutor, shouldDeliver, afterProcessing));
        } else {
            deliverProcessedPath(path, handoffExecutor, shouldDeliver, afterProcessing);
        }
    }

    public static void resetProfiling() {
        PROFILING.reset();
    }

    public static ProfilingSnapshot profilingSnapshot() {
        return PROFILING.snapshot();
    }

    public record ProfilingSnapshot(
            long queueSubmissions,
            long syncFallbacks,
            long awaitCalls,
            long deliveredPaths,
            long deliveredNullPaths,
            long droppedCallbacks
    ) {
        public long totalDeliveryCallbacks() {
            return deliveredPaths + deliveredNullPaths;
        }
    }

    private static final class ProfilingCounters {
        private final LongAdder queueSubmissions = new LongAdder();
        private final LongAdder syncFallbacks = new LongAdder();
        private final LongAdder awaitCalls = new LongAdder();
        private final LongAdder deliveredPaths = new LongAdder();
        private final LongAdder deliveredNullPaths = new LongAdder();
        private final LongAdder droppedCallbacks = new LongAdder();

        private void recordQueueSubmission() {
            queueSubmissions.increment();
        }

        private void recordSyncFallback() {
            syncFallbacks.increment();
        }

        private void recordAwaitCall() {
            awaitCalls.increment();
        }

        private void recordAwaitDelivery(boolean deliveredPath) {
            if (deliveredPath) {
                deliveredPaths.increment();
            }
            else {
                deliveredNullPaths.increment();
            }
        }

        private void recordDroppedCallback() {
            droppedCallbacks.increment();
        }

        private void reset() {
            queueSubmissions.reset();
            syncFallbacks.reset();
            awaitCalls.reset();
            deliveredPaths.reset();
            deliveredNullPaths.reset();
            droppedCallbacks.reset();
        }

        private ProfilingSnapshot snapshot() {
            return new ProfilingSnapshot(
                    queueSubmissions.sum(),
                    syncFallbacks.sum(),
                    awaitCalls.sum(),
                    deliveredPaths.sum(),
                    deliveredNullPaths.sum(),
                    droppedCallbacks.sum()
            );
        }
    }
}
