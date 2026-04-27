package com.talhanation.bannermod;

import com.talhanation.bannermod.config.WorkersServerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkersServerConfigTestOverrideTest {

    @AfterEach
    void clearOverrides() {
        WorkersServerConfig.clearAllTestOverrides();
    }

    @Test
    void longTestOverrideBypassesForgeConfigCacheForGraceTickReads() {
        // Forge's ConfigValue caches the first .get() result and never invalidates that cache
        // when .set() is called afterwards. Without this override seam, parallel/sequential
        // gametests that each call WorkersServerConfig.SettlementHouseGraceTicks.set(value)
        // race: whichever test triggered the first .get() wins, every subsequent test sees the
        // cached value regardless of its own .set() call. The override seam must produce the
        // requested value on read regardless of any .set()/.get() history.
        assertNotNull(WorkersServerConfig.SettlementHouseGraceTicks);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementHouseGraceTicks, 1L);
        assertEquals(1L, WorkersServerConfig.settlementHouseGraceTicks());

        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementHouseGraceTicks, 24_000L);
        assertEquals(24_000L, WorkersServerConfig.settlementHouseGraceTicks());
    }

    @Test
    void longTestOverrideAppliesIndependentlyPerConfigValue() {
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementHouseGraceTicks, 1L);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementFortGraceTicks, 24_000L);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementFortExplosionGraceTicks, 1L);

        assertEquals(1L, WorkersServerConfig.settlementHouseGraceTicks());
        assertEquals(24_000L, WorkersServerConfig.settlementFortGraceTicks());
        assertEquals(1L, WorkersServerConfig.settlementFortExplosionGraceTicks());
    }

    @Test
    void clearAllTestOverridesRestoresSpecBackedRead() {
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementHouseGraceTicks, 7L);
        assertEquals(7L, WorkersServerConfig.settlementHouseGraceTicks());

        WorkersServerConfig.clearAllTestOverrides();

        // Spec is not loaded in the unit-test JVM, so resolveLong's IllegalStateException catch
        // returns the documented fallback of 24000L. The point of this assertion is to lock in
        // that clearAllTestOverrides actually drops the override and we fall through to the
        // spec-backed read path — not the override map.
        assertEquals(24_000L, WorkersServerConfig.settlementHouseGraceTicks());
    }

    @Test
    void nullOverrideRemovesPreviousOverride() {
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementHouseGraceTicks, 7L);
        assertEquals(7L, WorkersServerConfig.settlementHouseGraceTicks());

        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementHouseGraceTicks, null);
        assertEquals(24_000L, WorkersServerConfig.settlementHouseGraceTicks());
    }
}
