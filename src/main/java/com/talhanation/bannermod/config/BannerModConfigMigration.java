package com.talhanation.bannermod.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One-shot migration of legacy split server configs into the unified
 * {@code bannermod-server.toml}.
 *
 * <p>NeoForge serverconfig folders may contain a {@code bannermod-recruits-server.toml} and/or
 * a {@code bannermod-workers-server.toml} from before {@link BannerModServerConfig} consumed
 * both. On first load against the new spec we:
 * <ol>
 *   <li>Read the legacy values (if either file exists).</li>
 *   <li>Write them under the new {@code recruits.*} / {@code workers.*} sub-paths inside
 *       {@code bannermod-server.toml}, preserving any pre-existing entries already in the
 *       new file.</li>
 *   <li>Rename the legacy files to {@code .legacy.toml} so they are not re-read on the next
 *       boot.</li>
 *   <li>Emit a {@code LOGGER.warn} line so server operators see the deprecation
 *       notice and can clean up the renamed leftovers.</li>
 * </ol>
 *
 * <p>The migration is intentionally side-effect-free if neither legacy file is present, so it
 * is safe to call unconditionally during server startup.
 */
public final class BannerModConfigMigration {
    public static final String LEGACY_RECRUITS_FILENAME = "bannermod-recruits-server.toml";
    public static final String LEGACY_WORKERS_FILENAME = "bannermod-workers-server.toml";
    public static final String UNIFIED_FILENAME = "bannermod-server.toml";

    private static final Logger LOGGER = LogManager.getLogger("bannermod-config-migration");

    private BannerModConfigMigration() {
    }

    /**
     * Run the migration. {@code serverConfigDir} is the per-world serverconfig directory
     * (e.g. {@code <world>/serverconfig/}). Missing legacy files are ignored.
     *
     * @return {@code true} if any legacy file was actually consumed and the unified file was
     *         (re)written; {@code false} if there was nothing to migrate.
     */
    public static boolean migrate(Path serverConfigDir) {
        if (serverConfigDir == null) {
            return false;
        }
        Path legacyRecruits = serverConfigDir.resolve(LEGACY_RECRUITS_FILENAME);
        Path legacyWorkers = serverConfigDir.resolve(LEGACY_WORKERS_FILENAME);
        Path unified = serverConfigDir.resolve(UNIFIED_FILENAME);

        boolean hasRecruits = Files.isRegularFile(legacyRecruits);
        boolean hasWorkers = Files.isRegularFile(legacyWorkers);
        if (!hasRecruits && !hasWorkers) {
            return false;
        }

        try {
            Files.createDirectories(serverConfigDir);
        } catch (IOException ioException) {
            LOGGER.warn("Could not create serverconfig directory {} for legacy bannermod migration: {}",
                    serverConfigDir, ioException.toString());
            return false;
        }

        Map<String, Object> recruitsValues = hasRecruits ? readToml(legacyRecruits) : Map.of();
        Map<String, Object> workersValues = hasWorkers ? readToml(legacyWorkers) : Map.of();

        try (CommentedFileConfig target = CommentedFileConfig.builder(unified)
                .sync()
                .writingMode(WritingMode.REPLACE)
                .build()) {
            if (Files.exists(unified)) {
                target.load();
            }
            if (!recruitsValues.isEmpty()) {
                copyInto(target, "recruits", recruitsValues);
            }
            if (!workersValues.isEmpty()) {
                copyInto(target, "workers", workersValues);
            }
            target.save();
        } catch (RuntimeException runtimeException) {
            LOGGER.warn("Failed to write unified bannermod-server.toml during legacy migration: {}",
                    runtimeException.toString());
            return false;
        }

        if (hasRecruits) {
            archive(legacyRecruits);
            LOGGER.warn("Migrated legacy bannermod config from {} to {}; the legacy file has been"
                    + " renamed to *.legacy.toml — please delete it.",
                    LEGACY_RECRUITS_FILENAME, UNIFIED_FILENAME);
        }
        if (hasWorkers) {
            archive(legacyWorkers);
            LOGGER.warn("Migrated legacy bannermod config from {} to {}; the legacy file has been"
                    + " renamed to *.legacy.toml — please delete it.",
                    LEGACY_WORKERS_FILENAME, UNIFIED_FILENAME);
        }
        return true;
    }

    private static Map<String, Object> readToml(Path source) {
        Map<String, Object> flat = new LinkedHashMap<>();
        try (CommentedFileConfig config = CommentedFileConfig.builder(source).sync().build()) {
            config.load();
            flatten("", config, flat);
        } catch (RuntimeException runtimeException) {
            LOGGER.warn("Failed reading legacy bannermod config {}: {}", source, runtimeException.toString());
        }
        return flat;
    }

    private static void flatten(String prefix, UnmodifiableConfig config, Map<String, Object> out) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof UnmodifiableConfig nested) {
                flatten(key, nested, out);
            } else {
                out.put(key, value);
            }
        }
    }

    private static void copyInto(CommentedFileConfig target, String topLevel, Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String fullPath = topLevel + "." + entry.getKey();
            target.set(fullPath, entry.getValue());
        }
    }

    private static void archive(Path legacyFile) {
        Path archived = legacyFile.resolveSibling(legacyFile.getFileName().toString() + ".legacy");
        try {
            Files.move(legacyFile, archived, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioException) {
            LOGGER.warn("Could not rename legacy config {} to {}: {}", legacyFile, archived, ioException.toString());
        }
    }

    @SuppressWarnings("unused")
    private static CommentedConfig emptyConfig() {
        return CommentedConfig.inMemory();
    }
}
