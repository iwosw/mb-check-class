package com.talhanation.bannermod.settlement.project;

/**
 * Lifecycle phase of a {@link ProjectAssignment}. Slice C only produces the
 * {@link #SEARCHING_BUILDER} and {@link #MATERIALS_PENDING} phases; later slices
 * will advance assignments through {@link #IN_PROGRESS} and terminal states.
 */
public enum AssignmentPhase {
    /** Sitting in the scheduler queue, not yet paired with a BuildArea. */
    QUEUED,
    /** Bound to a BuildArea but no builder has been located yet. */
    SEARCHING_BUILDER,
    /** Builder resolved, but structure template or materials have not loaded. */
    MATERIALS_PENDING,
    /** Builder is actively placing/breaking blocks for this assignment. */
    IN_PROGRESS,
    /** Assignment finished and should be reaped on the next tick. */
    COMPLETED,
    /** Assignment terminated without completion. */
    CANCELLED
}
