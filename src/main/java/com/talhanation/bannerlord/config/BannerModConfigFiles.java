package com.talhanation.bannerlord.config;

public final class BannerModConfigFiles {

    private BannerModConfigFiles() {
    }

    public enum Surface {
        MILITARY(com.talhanation.bannermod.config.BannerModConfigFiles.Surface.MILITARY),
        SETTLEMENT(com.talhanation.bannermod.config.BannerModConfigFiles.Surface.SETTLEMENT),
        CLIENT(com.talhanation.bannermod.config.BannerModConfigFiles.Surface.CLIENT);

        private final com.talhanation.bannermod.config.BannerModConfigFiles.Surface delegate;

        Surface(com.talhanation.bannermod.config.BannerModConfigFiles.Surface delegate) {
            this.delegate = delegate;
        }
    }

    public static java.nio.file.Path prepareConfigPath(java.nio.file.Path configDir, Surface surface) {
        return com.talhanation.bannermod.config.BannerModConfigFiles.prepareConfigPath(configDir, surface.delegate);
    }

    public static java.nio.file.Path legacyPath(java.nio.file.Path configDir, Surface surface) {
        return com.talhanation.bannermod.config.BannerModConfigFiles.legacyPath(configDir, surface.delegate);
    }
}
