package com.talhanation.bannermod.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class BannerModConfigFiles {

    private static final Logger LOGGER = LogManager.getLogger();

    private BannerModConfigFiles() {
    }

    public enum Surface {
        MILITARY("military", "bannermod-military.toml", "bannermod-server.toml"),
        SETTLEMENT("settlement", "bannermod-settlement.toml", "workers-server.toml"),
        CLIENT("client", "bannermod-client.toml", null);

        private final String taxonomyKey;
        private final String activeFileName;
        private final String legacyFileName;

        Surface(String taxonomyKey, String activeFileName, String legacyFileName) {
            this.taxonomyKey = taxonomyKey;
            this.activeFileName = activeFileName;
            this.legacyFileName = legacyFileName;
        }

        public String taxonomyKey() {
            return taxonomyKey;
        }

        public String activeFileName() {
            return activeFileName;
        }

        public String legacyFileName() {
            return legacyFileName;
        }
    }

    public static Path prepareConfigPath(Path configDir, Surface surface) {
        Path activePath = configDir.resolve(surface.activeFileName());
        Path legacyPath = legacyPath(configDir, surface);
        if (legacyPath == null || Files.exists(activePath) || !Files.exists(legacyPath)) {
            return activePath;
        }

        try {
            Files.copy(legacyPath, activePath, StandardCopyOption.COPY_ATTRIBUTES);
            LOGGER.info("Migrated BannerMod {} config from {} to {}", surface.taxonomyKey(), legacyPath.getFileName(), activePath.getFileName());
        } catch (IOException e) {
            LOGGER.warn("Failed to migrate BannerMod {} config from {} to {}", surface.taxonomyKey(), legacyPath, activePath, e);
        }

        return activePath;
    }

    public static Path legacyPath(Path configDir, Surface surface) {
        if (surface.legacyFileName() == null) {
            return null;
        }
        return configDir.resolve(surface.legacyFileName());
    }
}
