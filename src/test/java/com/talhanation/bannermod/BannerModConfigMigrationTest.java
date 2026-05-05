package com.talhanation.bannermod;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.talhanation.bannermod.config.BannerModConfigMigration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for {@link BannerModConfigMigration}. Verifies the CONFIGMERGE-001 contract:
 * legacy {@code bannermod-recruits-server.toml} + {@code bannermod-workers-server.toml} files
 * are folded into the unified {@code bannermod-server.toml} under {@code recruits.*} /
 * {@code workers.*} sub-paths, the legacy files are renamed to {@code .legacy}, and the
 * helper is a no-op when no legacy file is present.
 */
class BannerModConfigMigrationTest {

    @TempDir
    Path serverConfigDir;

    @Test
    void migratesRecruitsAndWorkersValuesIntoUnifiedFile() throws IOException {
        writeLegacyRecruits(serverConfigDir, """
                [Recruits]
                RecruitCost = 7
                RecruitsMaxXpForLevelUp = 333
                """);
        writeLegacyWorkers(serverConfigDir, """
                [Workers]
                FarmerCost = 11
                BuilderActive = true
                """);

        boolean migrated = BannerModConfigMigration.migrate(serverConfigDir);

        assertTrue(migrated, "migration should report work performed when legacy files are present");
        Path unified = serverConfigDir.resolve(BannerModConfigMigration.UNIFIED_FILENAME);
        assertTrue(Files.isRegularFile(unified), "unified config must be created");

        try (CommentedFileConfig config = CommentedFileConfig.builder(unified).sync().build()) {
            config.load();
            Object recruitCost = config.get("recruits.Recruits.RecruitCost");
            assertNotNull(recruitCost, "recruits.RecruitCost should be present in migrated file");
            assertEquals(7L, ((Number) recruitCost).longValue());
            assertEquals(333L, ((Number) config.<Number>get("recruits.Recruits.RecruitsMaxXpForLevelUp")).longValue());
            assertEquals(11L, ((Number) config.<Number>get("workers.Workers.FarmerCost")).longValue());
            assertEquals(Boolean.TRUE, config.get("workers.Workers.BuilderActive"));
        }

        assertFalse(Files.exists(serverConfigDir.resolve(BannerModConfigMigration.LEGACY_RECRUITS_FILENAME)),
                "legacy recruits file should be moved aside");
        assertFalse(Files.exists(serverConfigDir.resolve(BannerModConfigMigration.LEGACY_WORKERS_FILENAME)),
                "legacy workers file should be moved aside");
        assertTrue(Files.exists(serverConfigDir.resolve(BannerModConfigMigration.LEGACY_RECRUITS_FILENAME + ".legacy")),
                "legacy recruits file should be archived for the operator");
        assertTrue(Files.exists(serverConfigDir.resolve(BannerModConfigMigration.LEGACY_WORKERS_FILENAME + ".legacy")),
                "legacy workers file should be archived for the operator");
    }

    @Test
    void noOpWhenNoLegacyFilesPresent() {
        boolean migrated = BannerModConfigMigration.migrate(serverConfigDir);
        assertFalse(migrated, "migration should be a no-op when no legacy file is present");
        assertFalse(Files.exists(serverConfigDir.resolve(BannerModConfigMigration.UNIFIED_FILENAME)),
                "no unified file should be materialised when there is nothing to migrate");
    }

    @Test
    void preservesExistingUnifiedValuesWhenMergingLegacy() throws IOException {
        Path unified = serverConfigDir.resolve(BannerModConfigMigration.UNIFIED_FILENAME);
        Files.writeString(unified, """
                [war]
                ExistingKey = "preserve-me"
                """);
        writeLegacyRecruits(serverConfigDir, """
                [Recruits]
                RecruitCost = 9
                """);

        boolean migrated = BannerModConfigMigration.migrate(serverConfigDir);
        assertTrue(migrated);

        try (CommentedFileConfig config = CommentedFileConfig.builder(unified).sync().build()) {
            config.load();
            assertEquals("preserve-me", config.get("war.ExistingKey"),
                    "pre-existing keys in unified file must not be wiped by migration");
            assertEquals(9L, ((Number) config.<Number>get("recruits.Recruits.RecruitCost")).longValue());
        }
    }

    private static void writeLegacyRecruits(Path dir, String body) throws IOException {
        Files.writeString(dir.resolve(BannerModConfigMigration.LEGACY_RECRUITS_FILENAME), body);
    }

    private static void writeLegacyWorkers(Path dir, String body) throws IOException {
        Files.writeString(dir.resolve(BannerModConfigMigration.LEGACY_WORKERS_FILENAME), body);
    }
}
