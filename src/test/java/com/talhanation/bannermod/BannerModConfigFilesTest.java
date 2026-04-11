package com.talhanation.bannermod;

import com.talhanation.bannermod.config.BannerModConfigFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModConfigFilesTest {

    @TempDir
    Path configDir;

    @Test
    void sharedTaxonomyPublishesBannerModOwnedConfigFileNames() {
        assertEquals("military", BannerModConfigFiles.Surface.MILITARY.taxonomyKey());
        assertEquals("bannermod-military.toml", BannerModConfigFiles.Surface.MILITARY.activeFileName());
        assertEquals("settlement", BannerModConfigFiles.Surface.SETTLEMENT.taxonomyKey());
        assertEquals("bannermod-settlement.toml", BannerModConfigFiles.Surface.SETTLEMENT.activeFileName());
        assertEquals("client", BannerModConfigFiles.Surface.CLIENT.taxonomyKey());
        assertEquals("bannermod-client.toml", BannerModConfigFiles.Surface.CLIENT.activeFileName());
    }

    @Test
    void prepareConfigPathMigratesLegacyWorkersConfigIntoSettlementSurface() throws IOException {
        Path legacyPath = configDir.resolve("workers-server.toml");
        Files.writeString(legacyPath, "legacy-workers=true\n");

        Path activePath = BannerModConfigFiles.prepareConfigPath(configDir, BannerModConfigFiles.Surface.SETTLEMENT);

        assertEquals(configDir.resolve("bannermod-settlement.toml"), activePath);
        assertTrue(Files.exists(activePath));
        assertEquals("legacy-workers=true\n", Files.readString(activePath));
    }

    @Test
    void prepareConfigPathMigratesLegacyBannerModServerConfigIntoMilitarySurface() throws IOException {
        Path legacyPath = configDir.resolve("bannermod-server.toml");
        Files.writeString(legacyPath, "legacy-military=true\n");

        Path activePath = BannerModConfigFiles.prepareConfigPath(configDir, BannerModConfigFiles.Surface.MILITARY);

        assertEquals(configDir.resolve("bannermod-military.toml"), activePath);
        assertTrue(Files.exists(activePath));
        assertEquals("legacy-military=true\n", Files.readString(activePath));
    }

    @Test
    void prepareConfigPathLeavesExistingBannerModFileInPlace() throws IOException {
        Path legacyPath = configDir.resolve("workers-server.toml");
        Files.writeString(legacyPath, "legacy-workers=true\n");
        Path activePath = configDir.resolve("bannermod-settlement.toml");
        Files.writeString(activePath, "active-workers=true\n");

        Path resolvedPath = BannerModConfigFiles.prepareConfigPath(configDir, BannerModConfigFiles.Surface.SETTLEMENT);

        assertEquals(activePath, resolvedPath);
        assertEquals("active-workers=true\n", Files.readString(activePath));
        assertFalse(Files.readString(activePath).contains("legacy-workers"));
    }
}
