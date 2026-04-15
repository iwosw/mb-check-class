package com.talhanation.bannermod.bootstrap;

/**
 * Policy decisions for the merged bannermod runtime.
 * Copied from workers.MergedRuntimeCleanupPolicy into the outer repo bootstrap package.
 */
public final class MergedRuntimeCleanupPolicy {

    private MergedRuntimeCleanupPolicy() {
    }

    // The merged bannermod runtime keeps legacy recruits/workers update feeds disabled
    // until one release-facing update contract exists for the combined mod.
    public static boolean enableLegacyUpdateCheckers() {
        return false;
    }
}
