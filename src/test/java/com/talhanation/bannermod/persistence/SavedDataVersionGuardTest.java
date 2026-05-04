package com.talhanation.bannermod.persistence;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedDataVersionGuardTest {

    private static final Path SRC_MAIN_JAVA = Paths.get("src/main/java");
    private static final String EXTENDS_SAVEDDATA = "extends SavedData";
    private static final String CURRENT_VERSION_DECL = "CURRENT_VERSION";
    private static final String PUT_VERSION_CALL = "SavedDataVersioning.putVersion(";
    private static final String MIGRATE_CALL = "SavedDataVersioning.migrate(";
    private static final String DATA_VERSION_KEY = "DataVersion";

    @Test
    void everyConcreteSavedDataDeclaresCurrentVersionAndCallsVersionHelper() throws IOException {
        assertTrue(Files.isDirectory(SRC_MAIN_JAVA),
                "guard test must run from the project root; expected " + SRC_MAIN_JAVA.toAbsolutePath());

        List<Path> savedDataFiles = collectSavedDataFiles();
        assertFalse(savedDataFiles.isEmpty(),
                "guard test failed to find any SavedData subclasses under src/main/java");

        List<String> missing = new ArrayList<>();
        for (Path file : savedDataFiles) {
            String content = Files.readString(file);
            // Skip the helper itself.
            if (file.getFileName().toString().equals("SavedDataVersioning.java")) {
                continue;
            }
            boolean ok = content.contains(CURRENT_VERSION_DECL)
                    && content.contains(PUT_VERSION_CALL)
                    && content.contains(MIGRATE_CALL);
            if (!ok) {
                missing.add(file.toString());
            }
        }
        assertTrue(missing.isEmpty(),
                "SavedData classes missing DataVersion plumbing (CURRENT_VERSION + putVersion + migrate): "
                        + missing);
    }

    @Test
    void dataVersionKeyMatchesHelperConstant() {
        // Anchors the guard string above to the real helper so a rename will be loud.
        assertTrue(SavedDataVersioning.DATA_VERSION_KEY.equals(DATA_VERSION_KEY),
                "guard test DATA_VERSION_KEY constant drifted from helper");
    }

    private static List<Path> collectSavedDataFiles() throws IOException {
        try (Stream<Path> walk = Files.walk(SRC_MAIN_JAVA)) {
            return walk.filter(p -> p.toString().endsWith(".java"))
                    .filter(SavedDataVersionGuardTest::isSavedDataSubclass)
                    .sorted()
                    .toList();
        }
    }

    private static boolean isSavedDataSubclass(Path path) {
        try {
            String content = Files.readString(path);
            return content.contains(EXTENDS_SAVEDDATA);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
