package com.talhanation.bannermod.entity.civilian.workarea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Minimal smoke test for the index's public API shape. Full entity-backed tests need a
 * Minecraft bootstrap that JUnit doesn't provide, so we only exercise the null-safety
 * and pure-Java paths here; behavioural tests live in GameTests.
 */
class WorkAreaIndexTest {

    @BeforeEach
    void reset() {
        WorkAreaIndex.instance().clearAllForTest();
    }

    @Test
    void nullLevelQueryReturnsEmpty() {
        assertEquals(0, WorkAreaIndex.instance().queryInRange(
                null,
                new net.minecraft.world.phys.Vec3(0, 0, 0),
                64,
                AbstractWorkAreaEntity.class
        ).size());
    }

    @Test
    void queryNullTypeReturnsEmpty() {
        assertEquals(0, WorkAreaIndex.instance().queryInRange(
                null, null, 64, null).size());
    }

    @Test
    void clearNullDimensionIsNoOp() {
        WorkAreaIndex.instance().clear(null);
        // No exception is success.
    }
}
