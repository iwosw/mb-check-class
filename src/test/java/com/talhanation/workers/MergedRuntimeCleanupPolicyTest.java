package com.talhanation.workers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MergedRuntimeCleanupPolicyTest {

    @Test
    void legacyUpdateCheckersStayDisabledInMergedRuntime() {
        assertFalse(MergedRuntimeCleanupPolicy.enableLegacyUpdateCheckers());
    }
}
