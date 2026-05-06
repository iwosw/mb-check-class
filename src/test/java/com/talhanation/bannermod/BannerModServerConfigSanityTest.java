package com.talhanation.bannermod;

import com.talhanation.bannermod.config.BannerModServerConfig;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.config.WorkersServerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies CONFIGMERGE-001: the unified {@link BannerModServerConfig#SERVER} spec covers
 * every legacy {@code RecruitsServerConfig} / {@code WorkersServerConfig} value and the
 * legacy SERVER fields are mirrored onto the same instance so callsites that historically
 * referenced them keep working.
 */
class BannerModServerConfigSanityTest {

    @Test
    void unifiedServerSpecIsTheSameInstanceMirroredOnLegacyClasses() {
        assertNotNull(BannerModServerConfig.SERVER, "unified SERVER spec must be built");
        assertSame(BannerModServerConfig.SERVER, RecruitsServerConfig.SERVER,
                "legacy RecruitsServerConfig.SERVER must mirror the unified spec to keep external callers working");
        assertSame(BannerModServerConfig.SERVER, WorkersServerConfig.SERVER,
                "legacy WorkersServerConfig.SERVER must mirror the unified spec to keep external callers working");
    }

    @Test
    void everyRecruitsValueHandleIsBoundOnTheUnifiedSpec() {
        assertNotNull(RecruitsServerConfig.RecruitCost);
        assertNotNull(RecruitsServerConfig.RecruitCurrency);
        assertNotNull(RecruitsServerConfig.RecruitsMaxXpForLevelUp);
        assertNotNull(RecruitsServerConfig.UseAsyncPathfinding);
        assertNotNull(RecruitsServerConfig.MaxPlayersInFaction);
        assertNotNull(RecruitsServerConfig.AllowClaiming);
        assertNotNull(RecruitsServerConfig.FogOfWarEnabled);
    }

    @Test
    void everyWorkersValueHandleIsBoundOnTheUnifiedSpec() {
        assertNotNull(WorkersServerConfig.FarmerCost);
        assertNotNull(WorkersServerConfig.LumberjackCost);
        assertNotNull(WorkersServerConfig.MinerCost);
        assertNotNull(WorkersServerConfig.BuilderCost);
        assertNotNull(WorkersServerConfig.MerchantCost);
        assertNotNull(WorkersServerConfig.WorkerBirthEnabled);
        assertNotNull(WorkersServerConfig.SettlementSpawnAllowedProfessions);
        assertNotNull(WorkersServerConfig.CitizenBirthEnabled);
    }

    @Test
    void unifiedSpecPathsAreNamespacedAsRecruitsAndWorkers() {
        // Spec paths reflect the sub-path push() calls we set up in BannerModServerConfig.
        assertTrue(RecruitsServerConfig.RecruitCost.getPath().get(0).equals("recruits"),
                "Recruits values must live under the recruits.* sub-path in the unified TOML");
        assertTrue(WorkersServerConfig.FarmerCost.getPath().get(0).equals("workers"),
                "Workers values must live under the workers.* sub-path in the unified TOML");
    }
}
