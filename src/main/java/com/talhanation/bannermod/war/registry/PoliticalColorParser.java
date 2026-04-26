package com.talhanation.bannermod.war.registry;

/**
 * Parses {@link PoliticalEntityRecord#color} strings into ARGB ints suitable for renderers.
 *
 * <p>Accepted forms (case-insensitive):</p>
 * <ul>
 *   <li>{@code "#RRGGBB"} or {@code "RRGGBB"} — six hex digits, alpha forced to 0xFF.</li>
 *   <li>{@code "#AARRGGBB"} or {@code "AARRGGBB"} — eight hex digits, alpha preserved.</li>
 *   <li>blank, null, or any unparsable value — returns the supplied fallback.</li>
 * </ul>
 *
 * <p>Pure: no Minecraft types, fully unit-testable from regular JUnit.</p>
 */
public final class PoliticalColorParser {

    public static final int FALLBACK_WHITE = 0xFFFFFFFF;

    private PoliticalColorParser() {
    }

    public static int parseArgb(String input, int fallback) {
        if (input == null) {
            return fallback;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }
        if (trimmed.charAt(0) == '#') {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.length() != 6 && trimmed.length() != 8) {
            return fallback;
        }
        try {
            long parsed = Long.parseUnsignedLong(trimmed, 16);
            if (trimmed.length() == 6) {
                return 0xFF000000 | (int) (parsed & 0x00FFFFFFL);
            }
            return (int) (parsed & 0xFFFFFFFFL);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static int parseArgb(String input) {
        return parseArgb(input, FALLBACK_WHITE);
    }
}
