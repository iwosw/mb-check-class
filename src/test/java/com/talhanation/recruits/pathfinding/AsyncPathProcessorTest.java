package com.talhanation.recruits.pathfinding;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncPathProcessorTest {

    @Test
    void runtimeCreatesUsableExecutor() {
        PathProcessingRuntime runtime = new PathProcessingRuntime();

        ThreadPoolExecutor executor = runtime.createExecutor(2);

        assertFalse(executor.isShutdown());
        executor.shutdownNow();
    }

    @Test
    void deliveryHelperFallsBackDirectlyWithoutHandoffExecutor() {
        PathProcessingRuntime runtime = new PathProcessingRuntime();
        AtomicReference<Object> received = new AtomicReference<>();

        runtime.deliver(null, null, received::set);

        assertEquals(null, received.get());
    }

    @Test
    void deliveryHelperUsesProvidedHandoffExecutor() {
        PathProcessingRuntime runtime = new PathProcessingRuntime();
        AtomicBoolean executorUsed = new AtomicBoolean(false);
        AtomicReference<Object> received = new AtomicReference<>();
        Executor executor = runnable -> {
            executorUsed.set(true);
            runnable.run();
        };

        runtime.deliver(null, executor, received::set);

        assertTrue(executorUsed.get());
        assertEquals(null, received.get());
    }

    @Test
    void deliveryHelperDropsStaleCallbackWithoutInvokingConsumer() {
        AsyncPathProcessor.resetProfiling();
        AtomicBoolean received = new AtomicBoolean(false);

        AsyncPathProcessor.deliverProcessedPath(null, null, () -> false, ignored -> received.set(true));

        AsyncPathProcessor.ProfilingSnapshot snapshot = AsyncPathProcessor.profilingSnapshot();
        assertFalse(received.get());
        assertEquals(0, snapshot.deliveredNullPaths());
        assertEquals(1, snapshot.droppedCallbacks());
    }

    @Test
    void deliveryHelperDeliversAcceptedCallbackExactlyOnce() {
        AsyncPathProcessor.resetProfiling();
        AtomicBoolean executorUsed = new AtomicBoolean(false);
        AtomicInteger deliveries = new AtomicInteger();
        Executor executor = runnable -> {
            executorUsed.set(true);
            runnable.run();
        };

        AsyncPathProcessor.deliverProcessedPath(null, executor, () -> true, ignored -> deliveries.incrementAndGet());

        AsyncPathProcessor.ProfilingSnapshot snapshot = AsyncPathProcessor.profilingSnapshot();
        assertTrue(executorUsed.get());
        assertEquals(1, deliveries.get());
        assertEquals(1, snapshot.deliveredNullPaths());
        assertEquals(0, snapshot.droppedCallbacks());
    }

    @Test
    void fallbackHelperTreatsMissingOrShutdownExecutorAsDirectMode() throws Exception {
        java.lang.reflect.Method helper = AsyncPathProcessor.class.getDeclaredMethod("shouldProcessSynchronously", ThreadPoolExecutor.class);
        helper.setAccessible(true);

        PathProcessingRuntime runtime = new PathProcessingRuntime();
        ThreadPoolExecutor executor = runtime.createExecutor(1);
        executor.shutdown();

        assertTrue((Boolean) helper.invoke(null, new Object[]{null}));
        assertTrue((Boolean) helper.invoke(null, executor));
    }

    @Test
    void profilingSnapshotResetsToZero() {
        AsyncPathProcessor.resetProfiling();

        AsyncPathProcessor.ProfilingSnapshot snapshot = AsyncPathProcessor.profilingSnapshot();

        assertEquals(0, snapshot.queueSubmissions());
        assertEquals(0, snapshot.syncFallbacks());
        assertEquals(0, snapshot.awaitCalls());
        assertEquals(0, snapshot.deliveredPaths());
        assertEquals(0, snapshot.deliveredNullPaths());
        assertEquals(0, snapshot.droppedCallbacks());
    }
}
