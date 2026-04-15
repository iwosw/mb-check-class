package com.talhanation.bannermod.authority;

@Deprecated(forRemoval = false)
public final class BannerModAuthorityRules {

    private BannerModAuthorityRules() {
    }

    public enum Relationship {
        OWNER,
        SAME_TEAM,
        ADMIN,
        FORBIDDEN;

        private com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.Relationship toShared() {
            return com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.Relationship.valueOf(this.name());
        }

        private static Relationship fromShared(com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.Relationship relationship) {
            return Relationship.valueOf(relationship.name());
        }
    }

    public enum Decision {
        ALLOW,
        FORBIDDEN,
        TARGET_NOT_FOUND,
        OUTSIDE_FACTION_CLAIM,
        OVERLAPPING,
        INVALID_REQUEST;

        private static Decision fromShared(com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.Decision decision) {
            return Decision.valueOf(decision.name());
        }
    }

    public static Relationship resolveRelationship(boolean owner, boolean sameTeam, boolean admin) {
        return Relationship.fromShared(com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.resolveRelationship(owner, sameTeam, admin));
    }

    public static Decision inspectDecision(boolean targetExists, Relationship relationship) {
        return Decision.fromShared(com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.inspectDecision(targetExists, relationship.toShared()));
    }

    public static Decision modifyDecision(boolean targetExists, Relationship relationship) {
        return Decision.fromShared(com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.modifyDecision(targetExists, relationship.toShared()));
    }

    public static Decision recoverControlDecision(boolean targetExists, Relationship relationship) {
        return Decision.fromShared(com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.recoverControlDecision(targetExists, relationship.toShared()));
    }

    public static Decision createDecision(boolean insideFactionClaim, boolean overlapping) {
        return Decision.fromShared(com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.createDecision(insideFactionClaim, overlapping));
    }

    public static boolean isAllowed(Decision decision) {
        return com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.isAllowed(
                com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules.Decision.valueOf(decision.name())
        );
    }
}
