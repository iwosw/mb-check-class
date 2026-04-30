package com.talhanation.bannermod.army.command;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandIntentQueueParitySourceTest {
    private static final Path COMMAND_PACKAGE = Path.of("src/main/java/com/talhanation/bannermod/army/command");

    @Test
    void queuedHeadUsesSameLegacyCommandEventsAsImmediateDispatch() throws IOException {
        String dispatcher = Files.readString(COMMAND_PACKAGE.resolve("CommandIntentDispatcher.java"));
        String runtime = Files.readString(COMMAND_PACKAGE.resolve("CommandIntentQueueRuntime.java"));

        assertContainsBoth(dispatcher, runtime, "CommandEvents.onMovementCommand(");
        assertContainsBoth(dispatcher, runtime, "CommandEvents.onFaceCommand(");
        assertContainsBoth(dispatcher, runtime, "CommandEvents.onAttackCommand(");
        assertContainsBoth(dispatcher, runtime, "CommandEvents.onStrategicFireCommand(");
        assertContainsBoth(dispatcher, runtime, "CommandEvents.onAggroCommand(");
        assertContainsBoth(dispatcher, runtime, "CommandEvents.onCombatStanceCommand(");
        assertContainsBoth(dispatcher, runtime, "CommandEvents.onMountButton(");
    }

    @Test
    void queuedRuntimeKeepsPendingHeadsFromAutoCompletingBeforeTheyStart() throws IOException {
        String runtime = Files.readString(COMMAND_PACKAGE.resolve("CommandIntentQueueRuntime.java"));

        assertTrue(runtime.contains("if (startedAt == null) {"));
        assertTrue(runtime.contains("startHead(recruitUuid, server, recruit, head.get(), gameTime);"));
        assertTrue(runtime.contains("continue;"));
        assertTrue(runtime.contains("startedAtByRecruit.remove(recruitUuid);"));
        assertTrue(runtime.contains("if (requiresLiveIssuer(intent) && issuer == null) {"));
    }

    private static void assertContainsBoth(String first, String second, String needle) {
        assertTrue(first.contains(needle), "Missing expected dispatcher call: " + needle);
        assertTrue(second.contains(needle), "Missing expected queue runtime call: " + needle);
    }
}
