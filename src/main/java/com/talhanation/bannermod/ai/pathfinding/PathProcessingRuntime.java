package com.talhanation.bannermod.ai.pathfinding;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.talhanation.recruits.migration.CompatPathingSeams;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PathProcessingRuntime implements CompatPathingSeams.PathRuntime {

    @Override
    public ThreadPoolExecutor createExecutor(int workersCount) {
        int queueCapacity = workersCount * 8;
        return new ThreadPoolExecutor(
                1,
                workersCount,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactoryBuilder()
                        .setNameFormat("recruits-path-processor-%d")
                        .setDaemon(true)
                        .setPriority(Thread.NORM_PRIORITY - 2)
                        .build(),
                (task, executor) -> {
                    if (!executor.isShutdown()) {
                        task.run();
                    }
                }
        );
    }

    @Override
    public void deliver(@Nullable Path path, @Nullable Executor handoffExecutor, Consumer<@Nullable Path> afterProcessing) {
        Runnable delivery = () -> afterProcessing.accept(path);
        if (handoffExecutor != null) {
            handoffExecutor.execute(delivery);
        }
        else {
            delivery.run();
        }
    }
}
