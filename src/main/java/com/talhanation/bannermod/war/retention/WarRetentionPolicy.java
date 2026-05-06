package com.talhanation.bannermod.war.retention;

/**
 * Centralized retention limits for war SavedData containers.
 *
 * <p>Long-running servers historically accumulated unbounded audit entries, occupations,
 * and revolts — each save serialized the full list, so worlds bloated linearly with play
 * time. WARRETENTION-001 caps each container and ages out resolved-war records via a
 * server-tick sweeper.</p>
 *
 * <p>Constants are intentionally generous so normal play never hits them; the cap is a
 * runaway-growth guardrail, not a gameplay limit. {@link #ticksFromGameDays(int)} converts
 * the resolved-war retention window to ticks for sweeper code.</p>
 */
public final class WarRetentionPolicy {
    private WarRetentionPolicy() {
    }

    /** Hard cap on entries kept in {@link com.talhanation.bannermod.war.audit.WarAuditLogSavedData}. */
    public static final int MAX_AUDIT_ENTRIES = 4096;

    /** Hard cap on rows kept in {@link com.talhanation.bannermod.war.runtime.OccupationRuntime}. */
    public static final int MAX_OCCUPATIONS = 1024;

    /** Hard cap on revolts kept per single war in {@link com.talhanation.bannermod.war.runtime.RevoltRuntime}. */
    public static final int MAX_REVOLTS_PER_WAR = 64;

    /** Game-days to retain audit + occupation entries belonging to resolved/cancelled wars. */
    public static final int RESOLVED_WAR_RETENTION_GAME_DAYS = 30;

    /** Sweeper polling interval (server ticks). 1200 ticks = ~1 game-minute. */
    public static final int SWEEPER_INTERVAL_TICKS = 1200;

    private static final long TICKS_PER_GAME_DAY = 24000L;

    public static long ticksFromGameDays(int days) {
        return Math.max(0L, (long) days) * TICKS_PER_GAME_DAY;
    }

    /** Convenience: retention window for resolved wars expressed in ticks. */
    public static long resolvedWarRetentionTicks() {
        return ticksFromGameDays(RESOLVED_WAR_RETENTION_GAME_DAYS);
    }
}
