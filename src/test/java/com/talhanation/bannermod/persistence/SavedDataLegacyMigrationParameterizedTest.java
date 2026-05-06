package com.talhanation.bannermod.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TESTMIGRATE-001: parameterized v0->v1 migration round-trip for every {@link SavedData}
 * subclass under {@code com.talhanation.bannermod}.
 *
 * <p>Strategy mirrors {@code BannerModMessageFuzzHarnessTest} (TESTFUZZ-001):
 * <ul>
 *   <li>Walk the test classpath, finding every {@code .class} under the BannerMod root package
 *       that extends {@link SavedData}.</li>
 *   <li>For each class, reflectively invoke its public {@code static load(CompoundTag,
 *       HolderLookup.Provider)} factory with a v0-shape tag (an empty {@link CompoundTag}: no
 *       payload, and crucially no {@code DataVersion} key).</li>
 *   <li>Assert the loaded instance is non-null (load survived the legacy shape).</li>
 *   <li>Re-save it via {@link SavedData#save(CompoundTag, HolderLookup.Provider)} and assert the
 *       resulting tag carries {@code DataVersion=1} (the v1 stamp).</li>
 * </ul>
 *
 * <p>Acceptance for DATAVERSION-001 is "pre-versioning saves load cleanly into versioned form
 * for every SavedData". The empty-payload CompoundTag is the strictest legacy shape: it
 * exercises the migration switch (no DataVersion -> getVersion returns 0) and the load path's
 * tolerance for missing top-level keys. Production-payload coverage for the three biggest
 * SavedData classes (WarCooldown, Treasury, WarAuditLog) lives in
 * {@link SavedDataLegacyMigrationRoundTripTest}; this test extends the contract to the long
 * tail.
 *
 * <p>Floor invariant: {@link #EXPECTED_MIN_SAVEDDATA_CLASSES} matches the count from
 * DATAVERSION-001's commit (24 SavedData subclasses). The scan MUST find at least that many or
 * the harness wiring is broken.
 *
 * <p>Some classes' {@code load} cannot be unit-tested cleanly (require non-null
 * {@link HolderLookup.Provider} or live world refs). Such classes are listed in
 * {@link #EXPECTED_LOAD_SKIPS} with a {@code TODO(TESTMIGRATE-002)} marker; their migration
 * contract is verified via the in-process gametest harness instead (see
 * {@code SavedDataLegacyMigrationGameTests}).
 */
class SavedDataLegacyMigrationParameterizedTest {

    /** Root package to scan for SavedData subclasses. */
    private static final String SCAN_ROOT_PACKAGE = "com.talhanation.bannermod";

    /**
     * Lower bound matching the count of SavedData subclasses patched by DATAVERSION-001.
     * Source of truth: DATAVERSION-001 commit message ("24 SavedData classes").
     */
    private static final int EXPECTED_MIN_SAVEDDATA_CLASSES = 24;

    /**
     * Classes whose {@code load} cannot be exercised with {@code (emptyTag, null)} in a plain
     * JUnit harness. Each entry MUST carry a TODO marker pointing at the follow-up backlog
     * task that hardens the load path or moves the contract into a gametest.
     */
    private static final Set<String> EXPECTED_LOAD_SKIPS = new HashSet<>();

    static {
        // Populated empirically by running this test once on the DATAVERSION-001 tip; any
        // SavedData class added here MUST be tracked under TESTMIGRATE-002 (filed as a follow-up
        // backlog task) so the migration contract is still verified somewhere — typically by
        // exercising the production save/load entry point inside the GameTestServer harness in
        // src/gametest/java (see SavedDataLegacyMigrationGameTests).
        // The set starts empty so the test discovers any actually-failing classes loudly.
    }

    @TestFactory
    Stream<DynamicTest> v0EmptyTagLoadsCleanlyAndResavesAtCurrentVersion() throws Exception {
        List<Class<? extends SavedData>> subclasses = enumerateSavedDataSubclasses();

        assertTrue(subclasses.size() >= EXPECTED_MIN_SAVEDDATA_CLASSES,
                "Classpath scan must find at least " + EXPECTED_MIN_SAVEDDATA_CLASSES
                        + " SavedData subclasses; found " + subclasses.size()
                        + ". Did the SCAN_ROOT_PACKAGE change?");

        return subclasses.stream().map(cls -> DynamicTest.dynamicTest(
                cls.getSimpleName(),
                () -> verifyV0RoundTrip(cls)));
    }

    private static void verifyV0RoundTrip(Class<? extends SavedData> cls) throws Exception {
        if (EXPECTED_LOAD_SKIPS.contains(cls.getName())) {
            // Documented skip — see EXPECTED_LOAD_SKIPS comment for the follow-up task.
            return;
        }

        Method loadMethod = findLoadMethod(cls);
        assertNotNull(loadMethod,
                cls.getName() + " must declare a public static "
                        + "load(CompoundTag, HolderLookup.Provider) factory; the SavedData "
                        + "convention used by BannerModMain.* registries depends on it.");

        // v0 shape: no DataVersion key, empty payload. The strictest legacy tag.
        CompoundTag legacyTag = new CompoundTag();
        assertEquals(0, SavedDataVersioning.getVersion(legacyTag),
                "precondition: v0 tag must report version 0");

        Object loaded;
        try {
            loaded = loadMethod.invoke(null, legacyTag, /* registries */ null);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
            fail(cls.getName() + ".load(emptyV0Tag, null) threw "
                    + cause.getClass().getSimpleName()
                    + (cause.getMessage() == null ? "" : (": " + cause.getMessage()))
                    + ". If load legitimately needs live world refs, add the FQCN to "
                    + "EXPECTED_LOAD_SKIPS with a TODO(TESTMIGRATE-002) and cover it via "
                    + "SavedDataLegacyMigrationGameTests.",
                    cause);
            return;
        }

        assertNotNull(loaded, cls.getName() + ".load returned null on v0 tag");
        assertTrue(SavedData.class.isInstance(loaded),
                cls.getName() + ".load returned non-SavedData: " + loaded.getClass().getName());

        SavedData instance = (SavedData) loaded;
        Method saveMethod = findSaveMethod(cls);
        assertNotNull(saveMethod,
                cls.getName() + " must declare a public "
                        + "CompoundTag save(CompoundTag, HolderLookup.Provider) method.");

        CompoundTag saved;
        try {
            Object result = saveMethod.invoke(instance, new CompoundTag(), /* registries */ null);
            assertNotNull(result, cls.getName() + ".save returned null");
            saved = (CompoundTag) result;
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
            fail(cls.getName() + ".save threw " + cause.getClass().getSimpleName()
                    + (cause.getMessage() == null ? "" : (": " + cause.getMessage())),
                    cause);
            return;
        }

        int actualVersion = SavedDataVersioning.getVersion(saved);
        assertEquals(1, actualVersion,
                cls.getName() + ".save() must stamp DataVersion=1 (v1) when re-saving a "
                        + "freshly-loaded v0 record. Got version=" + actualVersion + ". "
                        + "Either the save() forgot SavedDataVersioning.putVersion(...), or "
                        + "CURRENT_VERSION drifted from 1 without updating this test.");
    }

    // ---------------------------------------------------------------------------------
    // Enumeration (mirrors BannerModMessageFuzzHarnessTest's classpath scan)
    // ---------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Class<? extends SavedData>> enumerateSavedDataSubclasses() throws Exception {
        Set<String> classNames = new TreeSet<>();
        String pkgPath = SCAN_ROOT_PACKAGE.replace('.', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        java.util.Enumeration<URL> resources = cl.getResources(pkgPath);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if ("file".equals(url.getProtocol())) {
                Path root = Path.of(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
                if (Files.isDirectory(root)) {
                    collectClassFilesRecursive(root, SCAN_ROOT_PACKAGE, classNames);
                }
            }
            // jar: protocol intentionally skipped — gradle test puts compiled classes in a
            // directory output. The EXPECTED_MIN_SAVEDDATA_CLASSES floor below detects regressions.
        }

        List<Class<? extends SavedData>> result = new ArrayList<>();
        for (String name : classNames) {
            Class<?> cls;
            try {
                cls = Class.forName(name, false, cl);
            } catch (Throwable t) {
                // Skip classes that fail to load (e.g. optional-mod hard refs). Mirror behaviour
                // of the fuzz harness — such classes are not reachable in the test classpath.
                continue;
            }
            if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
                continue;
            }
            if (!SavedData.class.isAssignableFrom(cls)) {
                continue;
            }
            if (cls == SavedData.class) {
                continue;
            }
            result.add((Class<? extends SavedData>) cls);
        }
        return result;
    }

    private static void collectClassFilesRecursive(Path dir, String pkg, Set<String> sink)
            throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            for (Path entry : (Iterable<Path>) entries::iterator) {
                String name = entry.getFileName().toString();
                if (Files.isDirectory(entry)) {
                    collectClassFilesRecursive(entry, pkg + "." + name, sink);
                } else if (name.endsWith(".class") && !name.contains("$")) {
                    sink.add(pkg + "." + name.substring(0, name.length() - ".class".length()));
                }
            }
        } catch (UncheckedIOException e) {
            throw e.getCause() != null ? new IOException(e.getCause()) : new IOException(e);
        }
    }

    private static Method findLoadMethod(Class<? extends SavedData> cls) {
        for (Method m : cls.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) continue;
            if (!Modifier.isPublic(m.getModifiers())) continue;
            if (!"load".equals(m.getName())) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 2) continue;
            if (params[0] != CompoundTag.class) continue;
            if (params[1] != HolderLookup.Provider.class) continue;
            if (!SavedData.class.isAssignableFrom(m.getReturnType())) continue;
            return m;
        }
        return null;
    }

    private static Method findSaveMethod(Class<? extends SavedData> cls) {
        // save() may be declared on the class or inherited. Use getMethod to walk the hierarchy.
        try {
            Method m = cls.getMethod("save", CompoundTag.class, HolderLookup.Provider.class);
            if (m.getReturnType() == CompoundTag.class) {
                return m;
            }
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

}
