package com.talhanation.bannermod.ai.pathfinding;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class AsyncPathProcessor {

    private static final PathProcessingRuntime RUNTIME = new PathProcessingRuntime();
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
            BannerModMain.LOGGER.warn("AsyncPathProcessor shutdown interrupted");
        }
    }

    static boolean shouldProcessSynchronously(@Nullable ThreadPoolExecutor executor) {
        return executor == null || executor.isShutdown();
    }

    static void deliverProcessedPath(@Nullable Path path, @Nullable Executor handoffExecutor, Consumer<@Nullable Path> afterProcessing) {
        RUNTIME.deliver(path, handoffExecutor, afterProcessing);
    }

    protected static void queue(@NotNull AsyncPath path) {
        ThreadPoolExecutor executor = pathFindingExecutor;
        if (shouldProcessSynchronously(executor)) {
            path.process();
            return;
        }
        CompletableFuture.runAsync(path::process, executor);
    }
    
    public static void awaitProcessing(@Nullable Path path, @Nullable MinecraftServer server, Consumer<@Nullable Path> afterProcessing) {
        Executor handoffExecutor = server == null ? null : server::execute;
        if (path instanceof AsyncPath asyncPath && !asyncPath.isProcessed()) {
            asyncPath.postProcessing(() -> deliverProcessedPath(path, handoffExecutor, afterProcessing));
        } else {
            deliverProcessedPath(path, handoffExecutor, afterProcessing);
        }
    }
}
