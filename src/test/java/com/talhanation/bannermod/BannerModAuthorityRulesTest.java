package com.talhanation.bannermod;

import com.talhanation.bannermod.authority.BannerModAuthorityRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BannerModAuthorityRulesTest {

    @Test
    void sharedAuthorityVocabularyCoversInspectModifyCreateAndRecoverControl() {
        assertEquals(BannerModAuthorityRules.Relationship.OWNER,
                BannerModAuthorityRules.resolveRelationship(true, false, false));
        assertEquals(BannerModAuthorityRules.Relationship.SAME_TEAM,
                BannerModAuthorityRules.resolveRelationship(false, true, false));
        assertEquals(BannerModAuthorityRules.Relationship.ADMIN,
                BannerModAuthorityRules.resolveRelationship(false, false, true));
        assertEquals(BannerModAuthorityRules.Relationship.FORBIDDEN,
                BannerModAuthorityRules.resolveRelationship(false, false, false));

        assertEquals(BannerModAuthorityRules.Decision.ALLOW,
                BannerModAuthorityRules.inspectDecision(true, BannerModAuthorityRules.Relationship.OWNER));
        assertEquals(BannerModAuthorityRules.Decision.ALLOW,
                BannerModAuthorityRules.modifyDecision(true, BannerModAuthorityRules.Relationship.SAME_TEAM));
        assertEquals(BannerModAuthorityRules.Decision.OUTSIDE_FACTION_CLAIM,
                BannerModAuthorityRules.createDecision(false, false));
        assertEquals(BannerModAuthorityRules.Decision.OVERLAPPING,
                BannerModAuthorityRules.createDecision(true, true));
        assertEquals(BannerModAuthorityRules.Decision.ALLOW,
                BannerModAuthorityRules.recoverControlDecision(true, BannerModAuthorityRules.Relationship.ADMIN));
        assertEquals(BannerModAuthorityRules.Decision.FORBIDDEN,
                BannerModAuthorityRules.recoverControlDecision(true, BannerModAuthorityRules.Relationship.SAME_TEAM));
    }
}
