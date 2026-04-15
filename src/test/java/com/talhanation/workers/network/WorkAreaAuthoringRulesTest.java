package com.talhanation.workers.network;

import com.talhanation.bannermod.authority.BannerModAuthorityRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkAreaAuthoringRulesTest {

    @Test
    void ownerSameTeamAndAdminAreAllowedToInspectAndModify() {
        assertEquals(BannerModAuthorityRules.Relationship.OWNER,
                BannerModAuthorityRules.resolveRelationship(true, false, false));
        assertEquals(BannerModAuthorityRules.Relationship.SAME_TEAM,
                BannerModAuthorityRules.resolveRelationship(false, true, false));
        assertEquals(BannerModAuthorityRules.Relationship.ADMIN,
                BannerModAuthorityRules.resolveRelationship(false, false, true));

        assertEquals(WorkAreaAuthoringRules.AccessLevel.OWNER,
                WorkAreaAuthoringRules.resolveAccess(true, false, false));
        assertEquals(WorkAreaAuthoringRules.AccessLevel.SAME_TEAM,
                WorkAreaAuthoringRules.resolveAccess(false, true, false));
        assertEquals(WorkAreaAuthoringRules.AccessLevel.ADMIN,
                WorkAreaAuthoringRules.resolveAccess(false, false, true));

        assertEquals(WorkAreaAuthoringRules.Decision.ALLOW,
                WorkAreaAuthoringRules.inspectDecision(true, WorkAreaAuthoringRules.AccessLevel.OWNER));
        assertEquals(WorkAreaAuthoringRules.Decision.ALLOW,
                WorkAreaAuthoringRules.modifyDecision(true, WorkAreaAuthoringRules.AccessLevel.SAME_TEAM));
        assertEquals(WorkAreaAuthoringRules.Decision.ALLOW,
                WorkAreaAuthoringRules.modifyDecision(true, WorkAreaAuthoringRules.AccessLevel.ADMIN));
    }

    @Test
    void unrelatedPlayersAreForbiddenToInspectAndModify() {
        assertEquals(BannerModAuthorityRules.Relationship.FORBIDDEN,
                BannerModAuthorityRules.resolveRelationship(false, false, false));

        WorkAreaAuthoringRules.AccessLevel accessLevel = WorkAreaAuthoringRules.resolveAccess(false, false, false);

        assertEquals(WorkAreaAuthoringRules.AccessLevel.FORBIDDEN, accessLevel);
        assertEquals(WorkAreaAuthoringRules.Decision.FORBIDDEN,
                WorkAreaAuthoringRules.inspectDecision(true, accessLevel));
        assertEquals(WorkAreaAuthoringRules.Decision.FORBIDDEN,
                WorkAreaAuthoringRules.modifyDecision(true, accessLevel));
    }

    @Test
    void createRequestsRejectOutsideClaimAndOverlapDistinctly() {
        assertEquals(BannerModAuthorityRules.Decision.OUTSIDE_FACTION_CLAIM,
                BannerModAuthorityRules.createDecision(false, false));
        assertEquals(BannerModAuthorityRules.Decision.OVERLAPPING,
                BannerModAuthorityRules.createDecision(true, true));
        assertEquals(BannerModAuthorityRules.Decision.ALLOW,
                BannerModAuthorityRules.createDecision(true, false));

        assertEquals(WorkAreaAuthoringRules.Decision.OUTSIDE_FACTION_CLAIM,
                WorkAreaAuthoringRules.createDecision(false, false));
        assertEquals(WorkAreaAuthoringRules.Decision.OVERLAPPING,
                WorkAreaAuthoringRules.createDecision(true, true));
        assertEquals(WorkAreaAuthoringRules.Decision.ALLOW,
                WorkAreaAuthoringRules.createDecision(true, false));
    }

    @Test
    void recoverControlKeepsOwnerOrAdminOnlyPolicy() {
        assertEquals(BannerModAuthorityRules.Decision.ALLOW,
                BannerModAuthorityRules.recoverControlDecision(true, BannerModAuthorityRules.Relationship.OWNER));
        assertEquals(BannerModAuthorityRules.Decision.ALLOW,
                BannerModAuthorityRules.recoverControlDecision(true, BannerModAuthorityRules.Relationship.ADMIN));
        assertEquals(BannerModAuthorityRules.Decision.FORBIDDEN,
                BannerModAuthorityRules.recoverControlDecision(true, BannerModAuthorityRules.Relationship.SAME_TEAM));
        assertEquals(BannerModAuthorityRules.Decision.TARGET_NOT_FOUND,
                BannerModAuthorityRules.recoverControlDecision(false, BannerModAuthorityRules.Relationship.OWNER));
    }
}
