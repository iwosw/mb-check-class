package com.talhanation.bannermod.shared.authority;

public final class BannerModAuthorityRules {

    private BannerModAuthorityRules() {
    }

    public enum Relationship {
        OWNER,
        SAME_TEAM,
        ADMIN,
        FORBIDDEN
    }

    public enum Decision {
        ALLOW,
        FORBIDDEN,
        TARGET_NOT_FOUND,
        OUTSIDE_FACTION_CLAIM,
        OVERLAPPING,
        INVALID_REQUEST
    }

    public static Relationship resolveRelationship(boolean owner, boolean sameTeam, boolean admin) {
        if (admin) {
            return Relationship.ADMIN;
        }
        if (owner) {
            return Relationship.OWNER;
        }
        if (sameTeam) {
            return Relationship.SAME_TEAM;
        }
        return Relationship.FORBIDDEN;
    }

    public static Decision inspectDecision(boolean targetExists, Relationship relationship) {
        return targetAccessDecision(targetExists, relationship == Relationship.FORBIDDEN);
    }

    public static Decision modifyDecision(boolean targetExists, Relationship relationship) {
        return targetAccessDecision(targetExists, relationship == Relationship.FORBIDDEN);
    }

    public static Decision recoverControlDecision(boolean targetExists, Relationship relationship) {
        if (!targetExists) {
            return Decision.TARGET_NOT_FOUND;
        }
        if (relationship == Relationship.OWNER || relationship == Relationship.ADMIN) {
            return Decision.ALLOW;
        }
        return Decision.FORBIDDEN;
    }

    public static Decision createDecision(boolean insideFactionClaim, boolean overlapping) {
        if (!insideFactionClaim) {
            return Decision.OUTSIDE_FACTION_CLAIM;
        }
        if (overlapping) {
            return Decision.OVERLAPPING;
        }
        return Decision.ALLOW;
    }

    public static boolean isAllowed(Decision decision) {
        return decision == Decision.ALLOW;
    }

    private static Decision targetAccessDecision(boolean targetExists, boolean forbidden) {
        if (!targetExists) {
            return Decision.TARGET_NOT_FOUND;
        }
        if (forbidden) {
            return Decision.FORBIDDEN;
        }
        return Decision.ALLOW;
    }
}
