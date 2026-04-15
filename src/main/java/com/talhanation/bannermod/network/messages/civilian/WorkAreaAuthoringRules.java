package com.talhanation.bannermod.network.messages.civilian;

public final class WorkAreaAuthoringRules {

    private WorkAreaAuthoringRules() {
    }

    public enum AccessLevel {
        OWNER,
        SAME_TEAM,
        ADMIN,
        FORBIDDEN
    }

    public enum Decision {
        ALLOW,
        FORBIDDEN,
        AREA_NOT_FOUND,
        OUTSIDE_FACTION_CLAIM,
        OVERLAPPING,
        INVALID_REQUEST
    }

    public static AccessLevel resolveAccess(boolean owner, boolean sameTeam, boolean admin) {
        if (admin) {
            return AccessLevel.ADMIN;
        }
        if (owner) {
            return AccessLevel.OWNER;
        }
        if (sameTeam) {
            return AccessLevel.SAME_TEAM;
        }
        return AccessLevel.FORBIDDEN;
    }

    public static Decision inspectDecision(boolean areaExists, AccessLevel accessLevel) {
        return accessDecision(areaExists, accessLevel);
    }

    public static Decision modifyDecision(boolean areaExists, AccessLevel accessLevel) {
        return accessDecision(areaExists, accessLevel);
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

    public static String getMessageKey(Decision decision) {
        return switch (decision) {
            case FORBIDDEN -> "gui.workers.area.authoring.forbidden";
            case AREA_NOT_FOUND -> "gui.workers.area.authoring.not_found";
            case OUTSIDE_FACTION_CLAIM -> "gui.workers.area.authoring.outside_claim";
            case OVERLAPPING -> "gui.workers.area.authoring.overlapping";
            case INVALID_REQUEST -> "gui.workers.area.authoring.invalid";
            case ALLOW -> null;
        };
    }

    private static Decision accessDecision(boolean areaExists, AccessLevel accessLevel) {
        if (!areaExists) {
            return Decision.AREA_NOT_FOUND;
        }
        if (accessLevel == AccessLevel.FORBIDDEN) {
            return Decision.FORBIDDEN;
        }
        return Decision.ALLOW;
    }
}
