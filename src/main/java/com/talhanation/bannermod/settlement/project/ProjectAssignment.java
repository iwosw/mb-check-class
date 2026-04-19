package com.talhanation.bannermod.settlement.project;

import com.talhanation.bannermod.settlement.growth.PendingProject;

import java.util.UUID;

/**
 * Snapshot of a {@link PendingProject} that has been bound to a concrete
 * {@link com.talhanation.bannermod.entity.civilian.workarea.BuildArea} entity.
 * Immutable; re-issued when phases change.
 */
public record ProjectAssignment(
        UUID projectId,
        UUID claimUuid,
        UUID buildAreaUuid,
        PendingProject project,
        long assignedAtGameTime,
        AssignmentPhase phase
) {
    public ProjectAssignment {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId must not be null");
        }
        if (claimUuid == null) {
            throw new IllegalArgumentException("claimUuid must not be null");
        }
        if (buildAreaUuid == null) {
            throw new IllegalArgumentException("buildAreaUuid must not be null");
        }
        if (project == null) {
            throw new IllegalArgumentException("project must not be null");
        }
        if (phase == null) {
            throw new IllegalArgumentException("phase must not be null");
        }
    }

    /** Returns a copy advanced to a new {@link AssignmentPhase}, keeping all other fields. */
    public ProjectAssignment withPhase(AssignmentPhase newPhase) {
        return new ProjectAssignment(projectId, claimUuid, buildAreaUuid, project, assignedAtGameTime, newPhase);
    }
}
