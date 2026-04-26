package com.talhanation.bannermod.citizen;

import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

public final class CitizenRoleSelectors {
    private CitizenRoleSelectors() {
    }

    public static boolean isRecruitProfession(CitizenProfession profession) {
        return profession == CitizenProfession.RECRUIT_SPEAR
                || profession == CitizenProfession.RECRUIT_SHIELDMAN
                || profession == CitizenProfession.RECRUIT_BOWMAN
                || profession == CitizenProfession.RECRUIT_CROSSBOWMAN
                || profession == CitizenProfession.RECRUIT_HORSEMAN
                || profession == CitizenProfession.RECRUIT_NOMAD
                || profession == CitizenProfession.RECRUIT_SCOUT;
    }

    public static boolean isOwnedCommandableRecruitUnit(@Nullable Entity entity, UUID ownerUuid) {
        if (entity instanceof AbstractRecruitEntity recruit) {
            return recruit.isAlive()
                    && recruit.isOwned()
                    && recruit.getOwnerUUID() != null
                    && recruit.getOwnerUUID().equals(ownerUuid);
        }
        if (!(entity instanceof CitizenEntity citizen)) {
            return false;
        }
        return citizen.isAlive()
                && citizen.isOwned()
                && citizen.getOwnerUUID() != null
                && citizen.getOwnerUUID().equals(ownerUuid)
                && isRecruitProfession(citizen.activeProfession());
    }
}
