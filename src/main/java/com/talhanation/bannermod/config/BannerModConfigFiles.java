package com.talhanation.bannermod.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Deprecated(forRemoval = false)
public final class BannerModConfigFiles {

    private static final Logger LOGGER = LogManager.getLogger();

    private BannerModConfigFiles() {
    }

    public enum Surface {
        MILITARY,
        SETTLEMENT,
        CLIENT;

        private com.talhanation.bannerlord.config.BannerModConfigFiles.Surface toShared() {
            return com.talhanation.bannerlord.config.BannerModConfigFiles.Surface.valueOf(this.name());
        }
    }

    public static Path prepareConfigPath(Path configDir, Surface surface) {
        return com.talhanation.bannerlord.config.BannerModConfigFiles.prepareConfigPath(configDir, surface.toShared());
    }

    public static Path legacyPath(Path configDir, Surface surface) {
        return com.talhanation.bannerlord.config.BannerModConfigFiles.legacyPath(configDir, surface.toShared());
    }
}
