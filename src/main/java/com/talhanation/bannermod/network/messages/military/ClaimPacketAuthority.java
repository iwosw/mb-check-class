package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;

import javax.annotation.Nullable;
import java.util.UUID;

final class ClaimPacketAuthority {
    private ClaimPacketAuthority() {
    }

    static boolean canEditClaim(UUID actorUuid,
                                boolean admin,
                                RecruitsClaim existingClaim,
                                @Nullable PoliticalEntityRecord politicalOwner) {
        if (actorUuid == null || existingClaim == null) {
            return false;
        }
        if (admin) {
            return true;
        }
        if (existingClaim.getPlayerInfo() != null && actorUuid.equals(existingClaim.getPlayerInfo().getUUID())) {
            return true;
        }
        if (politicalOwner == null) {
            return false;
        }
        return PoliticalEntityAuthority.canAct(actorUuid, false, politicalOwner);
    }
}
