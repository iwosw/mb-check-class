package com.talhanation.bannermod.governance;

import com.talhanation.bannermod.authority.BannerModAuthorityRules;

import javax.annotation.Nullable;
import java.util.UUID;

public final class BannerModGovernorAuthority {

    private BannerModGovernorAuthority() {
    }

    public enum Decision {
        ALLOW,
        TARGET_NOT_FOUND,
        FORBIDDEN
    }

    public record ActorContext(@Nullable UUID actorUuid, @Nullable String actorTeamId, boolean admin) {
    }

    public static Decision assignmentDecision(ActorContext actor, @Nullable UUID recruitOwnerUuid, @Nullable String recruitTeamId) {
        return fromAuthorityDecision(resolveAuthorityDecision(actor, recruitOwnerUuid, recruitTeamId));
    }

    public static Decision revokeDecision(ActorContext actor, @Nullable UUID governorOwnerUuid, @Nullable String governorTeamId) {
        return fromAuthorityDecision(resolveAuthorityDecision(actor, governorOwnerUuid, governorTeamId));
    }

    public static boolean isAllowed(Decision decision) {
        return decision == Decision.ALLOW;
    }

    public static BannerModAuthorityRules.Relationship resolveRelationship(ActorContext actor,
                                                                           @Nullable UUID ownerUuid,
                                                                           @Nullable String teamId) {
        boolean owner = actor != null && actor.actorUuid() != null && actor.actorUuid().equals(ownerUuid);
        boolean sameTeam = actor != null
                && actor.actorTeamId() != null
                && teamId != null
                && actor.actorTeamId().equals(teamId)
                && !owner;
        boolean admin = actor != null && actor.admin();
        return BannerModAuthorityRules.resolveRelationship(owner, sameTeam, admin);
    }

    private static BannerModAuthorityRules.Decision resolveAuthorityDecision(ActorContext actor,
                                                                             @Nullable UUID ownerUuid,
                                                                             @Nullable String teamId) {
        BannerModAuthorityRules.Relationship relationship = resolveRelationship(actor, ownerUuid, teamId);
        return BannerModAuthorityRules.recoverControlDecision(ownerUuid != null, relationship);
    }

    private static Decision fromAuthorityDecision(BannerModAuthorityRules.Decision decision) {
        return switch (decision) {
            case ALLOW -> Decision.ALLOW;
            case TARGET_NOT_FOUND -> Decision.TARGET_NOT_FOUND;
            default -> Decision.FORBIDDEN;
        };
    }
}
