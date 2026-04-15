package com.talhanation.bannermod.authority;

/**
 * @deprecated Use {@link com.talhanation.bannermod.shared.authority.BannerModAuthorityRules} instead.
 * Forwarder retained for staged migration per Phase 21 D-05 -- legacy shared-package overlap is
 * documented in MERGE_NOTES.md and is intentionally NOT deduplicated during Phase 21.
 *
 * <p>The nested {@link Relationship} and {@link Decision} enums are literal mirrors of the
 * canonical enums and delegate all decisions through mapping helpers. Do not add new members
 * here -- add them to the canonical class and extend the mappers below.
 */
@Deprecated
public final class BannerModAuthorityRules {

    private BannerModAuthorityRules() {
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.Relationship}. */
    @Deprecated
    public enum Relationship {
        OWNER,
        SAME_TEAM,
        ADMIN,
        FORBIDDEN
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.Decision}. */
    @Deprecated
    public enum Decision {
        ALLOW,
        FORBIDDEN,
        TARGET_NOT_FOUND,
        OUTSIDE_FACTION_CLAIM,
        OVERLAPPING,
        INVALID_REQUEST
    }

    public static Relationship resolveRelationship(boolean owner, boolean sameTeam, boolean admin) {
        return fromShared(com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.resolveRelationship(owner, sameTeam, admin));
    }

    public static Decision inspectDecision(boolean targetExists, Relationship relationship) {
        return fromShared(com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.inspectDecision(targetExists, toShared(relationship)));
    }

    public static Decision modifyDecision(boolean targetExists, Relationship relationship) {
        return fromShared(com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.modifyDecision(targetExists, toShared(relationship)));
    }

    public static Decision recoverControlDecision(boolean targetExists, Relationship relationship) {
        return fromShared(com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.recoverControlDecision(targetExists, toShared(relationship)));
    }

    public static Decision createDecision(boolean insideFactionClaim, boolean overlapping) {
        return fromShared(com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.createDecision(insideFactionClaim, overlapping));
    }

    public static boolean isAllowed(Decision decision) {
        return com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.isAllowed(toShared(decision));
    }

    private static com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.Relationship toShared(Relationship relationship) {
        if (relationship == null) {
            return null;
        }
        return com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.Relationship.valueOf(relationship.name());
    }

    private static Relationship fromShared(com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.Relationship relationship) {
        if (relationship == null) {
            return null;
        }
        return Relationship.valueOf(relationship.name());
    }

    private static com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.Decision toShared(Decision decision) {
        if (decision == null) {
            return null;
        }
        return com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.Decision.valueOf(decision.name());
    }

    private static Decision fromShared(com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.Decision decision) {
        if (decision == null) {
            return null;
        }
        return Decision.valueOf(decision.name());
    }
}
