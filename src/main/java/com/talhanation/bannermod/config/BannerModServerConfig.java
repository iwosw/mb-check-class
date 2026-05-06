package com.talhanation.bannermod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Unified BannerMod SERVER config that owns the single {@link ModConfigSpec} for both the
 * historical {@code bannermod-recruits-server.toml} and {@code bannermod-workers-server.toml}
 * specs. The legacy classes ({@link RecruitsServerConfig}, {@link WorkersServerConfig}) keep
 * their public {@code static} value handles for callsite backwards-compatibility, but the
 * spec they live in is built once here and registered once by
 * {@link com.talhanation.bannermod.bootstrap.BannerModMain} as
 * {@code bannermod-server.toml}.
 *
 * <p>Sub-path layout in the generated TOML:
 * <ul>
 *   <li>{@code recruits.*} — every key historically owned by {@link RecruitsServerConfig}.</li>
 *   <li>{@code workers.*} — every key historically owned by {@link WorkersServerConfig}.</li>
 * </ul>
 *
 * <p>Pre-existing {@code bannermod-recruits-server.toml} / {@code bannermod-workers-server.toml}
 * files on disk are migrated automatically into the new file by
 * {@link BannerModConfigMigration} before NeoForge loads the spec.
 */
public final class BannerModServerConfig {

    public static final ModConfigSpec SERVER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Recruits configuration (formerly bannermod-recruits-server.toml).").push("recruits");
        RecruitsServerConfig.populate(builder);
        builder.pop();

        builder.comment("Workers configuration (formerly bannermod-workers-server.toml).").push("workers");
        WorkersServerConfig.populate(builder);
        builder.pop();

        SERVER = builder.build();
        // Mirror the unified spec on the legacy classes so any external accessor that still
        // reads RecruitsServerConfig.SERVER / WorkersServerConfig.SERVER (e.g. third-party
        // tooling, addon mods) keeps observing a non-null spec rather than a NPE.
        RecruitsServerConfig.SERVER = SERVER;
        WorkersServerConfig.SERVER = SERVER;
    }

    private BannerModServerConfig() {
        // utility holder
    }
}
