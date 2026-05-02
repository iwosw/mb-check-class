package com.talhanation.bannermod.settlement.prefab.staffing;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PrefabAutoStaffingRuntimeRegressionTest {
    private static final Path ROOT = Path.of("");

    @Test
    void occupancyScanUsesIndexedRecruitIterationInsteadOfFullWorldBoxSweep() throws IOException {
        String source = Files.readString(ROOT.resolve("src/main/java/com/talhanation/bannermod/settlement/prefab/staffing/PrefabAutoStaffingRuntime.java"));

        assertTrue(source.contains(".instance().all(level, true)"),
                "occupancy scan must iterate indexed recruits directly to avoid world-size chunk sweeps");
        assertTrue(!source.contains(".instance().allInBox(level, search, true)"),
                "occupancy scan must not use allInBox with the full-world search AABB");
    }
}
