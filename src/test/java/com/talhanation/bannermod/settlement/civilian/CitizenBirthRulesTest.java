package com.talhanation.bannermod.settlement.civilian;

import com.talhanation.bannermod.settlement.civilian.CitizenBirthRules.Config;
import com.talhanation.bannermod.settlement.civilian.CitizenBirthRules.Decision;
import com.talhanation.bannermod.settlement.civilian.CitizenBirthRules.DenialReason;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitizenBirthRulesTest {

    private static final int FOOD_MIN = 8;
    private static final int FOOD_OK = 64;
    private static final Config DEFAULT_CONFIG = new Config(true, 24000L, 168000, 2, FOOD_MIN);
    private static final Config DISABLED_CONFIG = new Config(false, 24000L, 168000, 2, FOOD_MIN);

    @Test
    void allowsBirthWhenAllConditionsMet() {
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1, 1, 0, 1, FOOD_OK, Long.MAX_VALUE, DEFAULT_CONFIG);
        assertTrue(decision.allowed());
    }

    @Test
    void deniesWhenFeatureDisabled() {
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1, 1, 0, 1, FOOD_OK, Long.MAX_VALUE, DISABLED_CONFIG);
        assertFalse(decision.allowed());
        assertEquals(DenialReason.FEATURE_DISABLED, decision.denialReason());
    }

    @Test
    void deniesWhenClaimNotFriendly() {
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.HOSTILE_CLAIM,
                1, 1, 0, 1, FOOD_OK, Long.MAX_VALUE, DEFAULT_CONFIG);
        assertFalse(decision.allowed());
        assertEquals(DenialReason.NOT_FRIENDLY_CLAIM, decision.denialReason());
    }

    @Test
    void deniesWhenNoOppositeGenderPair() {
        Decision noFemale = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                3, 0, 0, 1, FOOD_OK, Long.MAX_VALUE, DEFAULT_CONFIG);
        Decision noMale = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                0, 3, 0, 1, FOOD_OK, Long.MAX_VALUE, DEFAULT_CONFIG);
        assertFalse(noFemale.allowed());
        assertEquals(DenialReason.NO_PAIRABLE_ADULTS, noFemale.denialReason());
        assertFalse(noMale.allowed());
        assertEquals(DenialReason.NO_PAIRABLE_ADULTS, noMale.denialReason());
    }

    @Test
    void deniesWhenBabiesAtCap() {
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1, 1, 2, 1, FOOD_OK, Long.MAX_VALUE, DEFAULT_CONFIG);
        assertFalse(decision.allowed());
        assertEquals(DenialReason.BABIES_AT_CAP, decision.denialReason());
    }

    @Test
    void deniesWhenCooldownActive() {
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1, 1, 0, 1, FOOD_OK, 100L, DEFAULT_CONFIG);
        assertFalse(decision.allowed());
        assertEquals(DenialReason.COOLDOWN_ACTIVE, decision.denialReason());
    }

    @Test
    void deniesWhenNoFreeHousing() {
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1, 1, 0, 0, FOOD_OK, Long.MAX_VALUE, DEFAULT_CONFIG);
        assertFalse(decision.allowed());
        assertEquals(DenialReason.NO_FREE_HOUSING, decision.denialReason());
    }

    @Test
    void deniesWhenClaimFoodBelowMinimum() {
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1, 1, 0, 1, FOOD_MIN - 1, Long.MAX_VALUE, DEFAULT_CONFIG);
        assertFalse(decision.allowed());
        assertEquals(DenialReason.NO_FOOD, decision.denialReason());
    }

    @Test
    void allowsBirthAtFoodMinimumBoundary() {
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1, 1, 0, 1, FOOD_MIN, Long.MAX_VALUE, DEFAULT_CONFIG);
        assertTrue(decision.allowed());
    }

    @Test
    void foodGateDisabledWhenMinIsZero() {
        Config zeroFood = new Config(true, 24000L, 168000, 2, 0);
        Decision decision = CitizenBirthRules.evaluate(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1, 1, 0, 1, 0, Long.MAX_VALUE, zeroFood);
        assertTrue(decision.allowed());
    }
}
