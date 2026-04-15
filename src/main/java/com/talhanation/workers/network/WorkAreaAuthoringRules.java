package com.talhanation.workers.network;

import com.talhanation.bannerlord.shared.authority.BannerModAuthorityRules;

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
        return fromRelationship(BannerModAuthorityRules.resolveRelationship(owner, sameTeam, admin));
    }

    public static Decision inspectDecision(boolean areaExists, AccessLevel accessLevel) {
        return fromSharedDecision(BannerModAuthorityRules.inspectDecision(areaExists, toRelationship(accessLevel)));
    }

    public static Decision modifyDecision(boolean areaExists, AccessLevel accessLevel) {
        return fromSharedDecision(BannerModAuthorityRules.modifyDecision(areaExists, toRelationship(accessLevel)));
    }

    public static Decision createDecision(boolean insideFactionClaim, boolean overlapping) {
        return fromSharedDecision(BannerModAuthorityRules.createDecision(insideFactionClaim, overlapping));
    }

    public static boolean isAllowed(Decision decision) {
        return decision == Decision.ALLOW;
    }

    public static String getMessageKey(Decision decision) {
        return switch (decision) {
            case FORBIDDEN -> "gui.bannermod.workers.area.authoring.forbidden";
            case AREA_NOT_FOUND -> "gui.bannermod.workers.area.authoring.not_found";
            case OUTSIDE_FACTION_CLAIM -> "gui.bannermod.workers.area.authoring.outside_claim";
            case OVERLAPPING -> "gui.bannermod.workers.area.authoring.overlapping";
            case INVALID_REQUEST -> "gui.bannermod.workers.area.authoring.invalid";
            case ALLOW -> null;
        };
    }

    private static BannerModAuthorityRules.Relationship toRelationship(AccessLevel accessLevel) {
        return switch (accessLevel) {
            case OWNER -> BannerModAuthorityRules.Relationship.OWNER;
            case SAME_TEAM -> BannerModAuthorityRules.Relationship.SAME_TEAM;
            case ADMIN -> BannerModAuthorityRules.Relationship.ADMIN;
            case FORBIDDEN -> BannerModAuthorityRules.Relationship.FORBIDDEN;
        };
    }

    private static AccessLevel fromRelationship(BannerModAuthorityRules.Relationship relationship) {
        return switch (relationship) {
            case OWNER -> AccessLevel.OWNER;
            case SAME_TEAM -> AccessLevel.SAME_TEAM;
            case ADMIN -> AccessLevel.ADMIN;
            case FORBIDDEN -> AccessLevel.FORBIDDEN;
        };
    }

    private static Decision fromSharedDecision(BannerModAuthorityRules.Decision decision) {
        return switch (decision) {
            case ALLOW -> Decision.ALLOW;
            case FORBIDDEN -> Decision.FORBIDDEN;
            case TARGET_NOT_FOUND -> Decision.AREA_NOT_FOUND;
            case OUTSIDE_FACTION_CLAIM -> Decision.OUTSIDE_FACTION_CLAIM;
            case OVERLAPPING -> Decision.OVERLAPPING;
            case INVALID_REQUEST -> Decision.INVALID_REQUEST;
        };
    }
}
