package com.talhanation.bannermod.settlement.job;

/**
 * Outcome of a single {@link JobHandler#runOneStep(JobExecutionContext)} invocation.
 *
 * <p>The registry and future executor layer (slice-F onwards) interpret these values to decide
 * whether to re-queue the resident, surface a diagnostic, or release the task back to the pool.</p>
 */
public enum JobExecutionResult {
    /** Handler successfully completed a step. */
    COMPLETED,
    /** Handler cannot progress because the resident lacks a required tool. */
    BLOCKED_NO_TOOL,
    /** Handler cannot progress because no valid target (block/entity/workplace) is reachable. */
    BLOCKED_NO_TARGET,
    /** Handler could not pathfind / made no spatial progress this step. */
    BLOCKED_STUCK,
    /** External code interrupted the step (e.g. combat, schedule change). */
    INTERRUPTED,
    /** Step exceeded its soft time budget without completing. */
    TIMED_OUT
}
