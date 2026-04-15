package com.talhanation.bannermod.migration;

import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * Contract-only optional-compat and async-path runtime seams for Phase 7 migration prep.
 *
 * <p>Source anchors are documented in
 * {@code .planning/phases/07-migration-ready-internal-seams/07-SEAM-INVENTORY.md} and currently
 * live in the reflection-heavy compat helpers plus {@code AsyncPathProcessor} executor and callback
 * delivery logic.</p>
 */
public final class CompatPathingSeams {

    private CompatPathingSeams() {
    }

    /**
     * Shared optional-class lookup contract for reflection-backed compatibility helpers.
     */
    public interface ReflectiveLookup {
        Optional<Class<?>> findClass(String className);
    }

    /**
     * Shared runtime contract for async path executor creation and processed-path handoff.
     */
    public interface PathRuntime {
        ThreadPoolExecutor createExecutor(int workersCount);

        void deliver(@Nullable Path path, @Nullable Executor handoffExecutor, Consumer<@Nullable Path> afterProcessing);
    }
}
