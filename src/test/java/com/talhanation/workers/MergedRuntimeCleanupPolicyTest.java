package com.talhanation.workers;

import com.talhanation.bannermod.bootstrap.MergedRuntimeCleanupPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MergedRuntimeCleanupPolicyTest {

    @Test
    void legacyUpdateCheckersStayDisabledInMergedRuntime() {
        assertFalse(MergedRuntimeCleanupPolicy.enableLegacyUpdateCheckers());
    }
}
