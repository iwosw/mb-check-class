package com.talhanation.bannermod.shared.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModRecruitFirearmStatusTest {

    @Test
    void firearmInspectionDistinguishesSupportedMissingAmmoUnsupportedAndIrrelevantLoadouts() {
        BannerModRecruitFirearmStatus.FirearmInspection supported = BannerModRecruitFirearmStatus.firearmInspection(true, true, 6);
        BannerModRecruitFirearmStatus.FirearmInspection missingAmmo = BannerModRecruitFirearmStatus.firearmInspection(true, true, 0);
        BannerModRecruitFirearmStatus.FirearmInspection unsupported = BannerModRecruitFirearmStatus.firearmInspection(true, false, 0);
        BannerModRecruitFirearmStatus.FirearmInspection none = BannerModRecruitFirearmStatus.firearmInspection(false, false, 0);

        assertEquals(BannerModRecruitFirearmStatus.FirearmState.SUPPORTED, supported.state());
        assertEquals(6, supported.ammoCount());
        assertTrue(supported.visible());
        assertTrue(supported.hasAmmo());

        assertEquals(BannerModRecruitFirearmStatus.FirearmState.MISSING_AMMO, missingAmmo.state());
        assertTrue(missingAmmo.visible());
        assertFalse(missingAmmo.hasAmmo());

        assertEquals(BannerModRecruitFirearmStatus.FirearmState.UNSUPPORTED, unsupported.state());
        assertTrue(unsupported.visible());
        assertFalse(unsupported.hasAmmo());

        assertEquals(BannerModRecruitFirearmStatus.FirearmState.NONE, none.state());
        assertFalse(none.visible());
    }
}
