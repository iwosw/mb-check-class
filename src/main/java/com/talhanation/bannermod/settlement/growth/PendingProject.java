package com.talhanation.bannermod.settlement.growth;

import com.talhanation.bannermod.settlement.BannerModSettlementBuildingCategory;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingProfileSeed;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Candidate project a settlement could start next. Patterned on Millenaire's
 * {@code Village.PendingProject}; re-implemented to avoid copying GPL source.
 * {@code priorityScore} is clamped to 0..1000, higher is picked sooner.
 */
public record PendingProject(
        UUID projectId,
        ProjectKind kind,
        @Nullable UUID targetBuildingUuid,
        BannerModSettlementBuildingCategory buildingCategory,
        BannerModSettlementBuildingProfileSeed profileSeed,
        int priorityScore,
        long proposedAtGameTime,
        int estimatedTickCost,
        ProjectBlocker blockerReason
) {
    public PendingProject {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId must not be null");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
        if (profileSeed == null) {
            profileSeed = BannerModSettlementBuildingProfileSeed.GENERAL;
        }
        if (buildingCategory == null) {
            buildingCategory = profileSeed.category();
        }
        priorityScore = Math.max(0, Math.min(1000, priorityScore));
        estimatedTickCost = Math.max(0, estimatedTickCost);
        if (blockerReason == null) {
            blockerReason = ProjectBlocker.NONE;
        }
        if (kind == ProjectKind.NEW_BUILDING) {
            targetBuildingUuid = null;
        }
    }
}
