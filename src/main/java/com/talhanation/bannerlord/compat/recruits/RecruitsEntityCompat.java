package com.talhanation.bannerlord.compat.recruits;

import com.talhanation.bannermod.governance.BannerModGovernorService;
import com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity;

public final class RecruitsEntityCompat {
    private RecruitsEntityCompat() {
    }

    public static BannerModGovernorService.RecruitGovernorTarget toGovernorTarget(AbstractRecruitEntity recruit) {
        return BannerModGovernorService.RecruitGovernorTarget.fromRecruit(recruit);
    }

    public static BannerModGovernorService.RecruitGovernorTarget toGovernorTarget(com.talhanation.recruits.entities.AbstractRecruitEntity recruit) {
        return BannerModGovernorService.RecruitGovernorTarget.fromRecruit(recruit);
    }

    public static com.talhanation.recruits.entities.AbstractRecruitEntity toLegacyRecruit(com.talhanation.recruits.entities.AbstractRecruitEntity recruit) {
        return recruit;
    }

    public static com.talhanation.recruits.entities.AbstractRecruitEntity toLegacyRecruit(AbstractRecruitEntity recruit) {
        Class<com.talhanation.recruits.entities.AbstractRecruitEntity> legacyType = com.talhanation.recruits.entities.AbstractRecruitEntity.class;
        if (legacyType.isInstance(recruit)) {
            return legacyType.cast(recruit);
        }
        throw new IllegalStateException("Bannerlord recruit entity is not backed by a legacy recruit type: " + recruit.getClass().getName());
    }

    public static com.talhanation.recruits.world.RecruitsClaim toLegacyClaim(com.talhanation.recruits.world.RecruitsClaim claim) {
        return claim;
    }

    public static com.talhanation.recruits.world.RecruitsClaim toLegacyClaim(com.talhanation.bannerlord.persistence.military.RecruitsClaim claim) {
        Class<com.talhanation.recruits.world.RecruitsClaim> legacyType = com.talhanation.recruits.world.RecruitsClaim.class;
        if (legacyType.isInstance(claim)) {
            return legacyType.cast(claim);
        }
        throw new IllegalStateException("Bannerlord claim is not backed by a legacy recruits claim: " + claim.getClass().getName());
    }
}
