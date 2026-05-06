package com.talhanation.bannermod.network;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ENQUEUE-001 regression guard: every BannerModMessage subclass that
 * overrides {@code executeServerSide(BannerModNetworkContext context)} MUST
 * either dispatch its body through {@code context.enqueueWork(() -> ...)} so
 * NeoForge runs it on the main thread, or carry an explicit
 * {@code // @safe-network-thread (reason)} comment justifying network-thread
 * execution.
 *
 * <p>Empty / no-op bodies are allowed because they cannot mutate state.
 */
class NetworkThreadGuardTest {

    private static final Path SRC_MAIN_JAVA = Paths.get("src/main/java");
    private static final Pattern SIGNATURE = Pattern.compile(
            "(^|\\s)public\\s+void\\s+executeServerSide\\s*\\(\\s*BannerModNetworkContext\\s+\\w+\\s*\\)\\s*\\{",
            Pattern.MULTILINE
    );
    private static final String SAFE_MARKER = "@safe-network-thread";
    private static final String ENQUEUE_TOKEN = "enqueueWork(";
    private static final String INTERFACE_FILE = "BannerModMessage.java";

    @Test
    void everyServerSideHandlerEnqueuesOrIsExplicitlySafe() throws IOException {
        assertTrue(Files.isDirectory(SRC_MAIN_JAVA),
                "guard test must run from project root; expected " + SRC_MAIN_JAVA.toAbsolutePath());

        List<String> offenders = new ArrayList<>();
        int handlersChecked = 0;
        try (Stream<Path> walk = Files.walk(SRC_MAIN_JAVA)) {
            for (Path file : (Iterable<Path>) walk.filter(p -> p.toString().endsWith(".java"))::iterator) {
                if (file.getFileName().toString().equals(INTERFACE_FILE)) {
                    continue;
                }
                String text;
                try {
                    text = Files.readString(file);
                } catch (UncheckedIOException | IOException e) {
                    continue;
                }
                Matcher m = SIGNATURE.matcher(text);
                while (m.find()) {
                    int openBrace = m.end();
                    int closeBrace = matchingClose(text, openBrace);
                    if (closeBrace < 0) {
                        offenders.add(file + " :: unbalanced braces in executeServerSide");
                        continue;
                    }
                    String body = text.substring(openBrace, closeBrace);
                    handlersChecked++;
                    if (body.trim().isEmpty()) {
                        continue;
                    }
                    if (body.contains(SAFE_MARKER)) {
                        continue;
                    }
                    if (body.contains(ENQUEUE_TOKEN)) {
                        continue;
                    }
                    offenders.add(file.toString());
                }
            }
        }
        assertTrue(handlersChecked > 0,
                "guard test failed to find any executeServerSide overrides — pattern drifted?");
        assertTrue(offenders.isEmpty(),
                "executeServerSide handlers missing context.enqueueWork(...) wrap and lacking "
                        + SAFE_MARKER + " escape: " + offenders);
    }

    @Test
    void setPacketHandledNoOpHasBeenRemoved() throws IOException {
        Path ctx = Paths.get("src/main/java/com/talhanation/bannermod/network/compat/BannerModNetworkContext.java");
        assertTrue(Files.isRegularFile(ctx), "BannerModNetworkContext.java must exist");
        String text = Files.readString(ctx);
        assertFalse(text.contains("setPacketHandled"),
                "BannerModNetworkContext.setPacketHandled no-op must stay removed (ENQUEUE-001)");
    }

    private static int matchingClose(String text, int openBracePos) {
        int depth = 1;
        int i = openBracePos;
        int n = text.length();
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inChar = false;
        while (i < n) {
            char c = text.charAt(i);
            char nxt = i + 1 < n ? text.charAt(i + 1) : '\0';
            if (inLineComment) {
                if (c == '\n') inLineComment = false;
                i++;
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && nxt == '/') {
                    inBlockComment = false;
                    i += 2;
                    continue;
                }
                i++;
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    i += 2;
                    continue;
                }
                if (c == '"') inString = false;
                i++;
                continue;
            }
            if (inChar) {
                if (c == '\\') {
                    i += 2;
                    continue;
                }
                if (c == '\'') inChar = false;
                i++;
                continue;
            }
            if (c == '/' && nxt == '/') {
                inLineComment = true;
                i += 2;
                continue;
            }
            if (c == '/' && nxt == '*') {
                inBlockComment = true;
                i += 2;
                continue;
            }
            if (c == '"') {
                inString = true;
                i++;
                continue;
            }
            if (c == '\'') {
                inChar = true;
                i++;
                continue;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
            i++;
        }
        return -1;
    }
}
