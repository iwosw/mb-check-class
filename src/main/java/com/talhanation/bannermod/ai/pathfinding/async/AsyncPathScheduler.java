package com.talhanation.bannermod.ai.pathfinding.async;

import com.talhanation.bannermod.util.RuntimeProfilingCounters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public final class AsyncPathScheduler implements AutoCloseable {
    private static final String METRICS_PREFIX = "pathfinding.true_async.scheduler";
    private static final AtomicLong THREAD_ID = new AtomicLong();

    private final AsyncPathSolver solver;
    private final ThreadPoolExecutor workerPool;
    private final BlockingQueue<Job> pendingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<PathResult> completedQueue = new LinkedBlockingQueue<>();
    private final int maxQueuedJobs;
    private final Map<PathPriority, Integer> perPriorityBackpressure;
    private final Comparator<Job> jobComparator;
    private final LongAdder outstandingJobs = new LongAdder();
    private final Map<PathPriority, LongAdder> outstandingByPriority = new EnumMap<>(PathPriority.class);

    public AsyncPathScheduler(AsyncPathSolver solver, int workerThreads, int maxQueuedJobs, Map<PathPriority, Integer> perPriorityBackpressure) {
        this.solver = Objects.requireNonNull(solver, "solver");
        this.maxQueuedJobs = Math.max(1, maxQueuedJobs);
        this.perPriorityBackpressure = sanitizeBackpressure(perPriorityBackpressure);
        this.jobComparator = Comparator
                .comparing(Job::priority)
                .thenComparingLong(Job::enqueuedNanos);
        for (PathPriority priority : PathPriority.values()) {
            this.outstandingByPriority.put(priority, new LongAdder());
        }
        this.workerPool = new ThreadPoolExecutor(
                Math.max(1, workerThreads),
                Math.max(1, workerThreads),
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                task -> {
                    Thread thread = new Thread(task);
                    thread.setName("recruits-true-async-path-" + THREAD_ID.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                }
        );
    }

    public boolean submit(PathRequestSnapshot request, RegionSnapshot region, CancellationToken cancellationToken) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(cancellationToken, "cancellationToken");
        RuntimeProfilingCounters.increment(METRICS_PREFIX + ".submit.attempt");

        if (outstandingJobs.sum() >= maxQueuedJobs) {
            RuntimeProfilingCounters.increment(METRICS_PREFIX + ".submit.rejected.max_queue");
            return false;
        }
        if (!canAcceptPriority(request.priority())) {
            RuntimeProfilingCounters.increment(METRICS_PREFIX + ".submit.rejected.priority_cap");
            return false;
        }

        Job job = new Job(request, region, cancellationToken, System.nanoTime());
        if (!pendingQueue.offer(job)) {
            RuntimeProfilingCounters.increment(METRICS_PREFIX + ".submit.rejected.offer_failed");
            return false;
        }
        outstandingJobs.increment();
        outstandingByPriority.get(request.priority()).increment();
        RuntimeProfilingCounters.increment(METRICS_PREFIX + ".submit.accepted");
        dispatch();
        return true;
    }

    public boolean canAccept(PathPriority priority) {
        return outstandingJobs.sum() < maxQueuedJobs && canAcceptPriority(priority);
    }

    private boolean canAcceptPriority(PathPriority priority) {
        PathPriority safePriority = priority == null ? PathPriority.FOLLOW : priority;
        long samePriorityQueued = outstandingByPriority.get(safePriority).sum();
        int limit = perPriorityBackpressure.getOrDefault(safePriority, maxQueuedJobs);
        return samePriorityQueued < limit;
    }

    public List<PathResult> pollCompleted(int maxResults) {
        int capped = Math.max(0, maxResults);
        List<PathResult> results = new ArrayList<>(Math.min(16, capped));
        for (int i = 0; i < capped; i++) {
            PathResult result = completedQueue.poll();
            if (result == null) {
                break;
            }
            results.add(result);
        }
        if (!results.isEmpty()) {
            RuntimeProfilingCounters.add(METRICS_PREFIX + ".completed.polled", results.size());
        }
        return results;
    }

    public int queuedJobs() {
        return Math.toIntExact(Math.min(Integer.MAX_VALUE, outstandingJobs.sum()));
    }

    public int completedJobs() {
        return completedQueue.size();
    }

    @Override
    public void close() {
        workerPool.shutdownNow();
        pendingQueue.clear();
    }

    private void dispatch() {
        while (workerPool.getQueue().remainingCapacity() > 0) {
            Job next = popNextJob();
            if (next == null) {
                break;
            }
            workerPool.execute(() -> runJob(next));
        }
    }

    private Job popNextJob() {
        Job best = null;
        for (Job candidate : pendingQueue) {
            if (best == null || jobComparator.compare(candidate, best) < 0) {
                best = candidate;
            }
        }
        if (best != null && pendingQueue.remove(best)) {
            return best;
        }
        return null;
    }

    private void runJob(Job job) {
        RuntimeProfilingCounters.increment(METRICS_PREFIX + ".solve.started");
        PathResult result;
        try {
            result = solver.solve(job.request(), job.region(), job.cancellationToken());
        } catch (Throwable throwable) {
            RuntimeProfilingCounters.increment(METRICS_PREFIX + ".solve.exception");
            result = new PathResult(
                    job.request().entityUuid(),
                    job.request().requestId(),
                    job.request().epoch(),
                    PathResultStatus.UNSUPPORTED,
                    List.of(),
                    false,
                    0.0D,
                    0,
                    0L,
                    "solver_exception:" + throwable.getClass().getSimpleName()
            );
        }
        completedQueue.offer(result);
        outstandingJobs.add(-1L);
        outstandingByPriority.get(job.priority()).add(-1L);
        RuntimeProfilingCounters.increment(METRICS_PREFIX + ".solve.finished");
    }

    private static Map<PathPriority, Integer> sanitizeBackpressure(Map<PathPriority, Integer> source) {
        EnumMap<PathPriority, Integer> sanitized = new EnumMap<>(PathPriority.class);
        if (source != null) {
            for (Map.Entry<PathPriority, Integer> entry : source.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                    sanitized.put(entry.getKey(), entry.getValue());
                }
            }
        }
        for (PathPriority priority : PathPriority.values()) {
            sanitized.putIfAbsent(priority, Integer.MAX_VALUE);
        }
        return sanitized;
    }

    private record Job(
            PathRequestSnapshot request,
            RegionSnapshot region,
            CancellationToken cancellationToken,
            long enqueuedNanos
    ) {
        private PathPriority priority() {
            return request.priority();
        }
    }
}
