package com.talhanation.bannermod.war.registry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PoliticalColorParserTest {

    @Test
    void blankAndNullCollapseToFallback() {
        assertEquals(PoliticalColorParser.FALLBACK_WHITE, PoliticalColorParser.parseArgb(null));
        assertEquals(PoliticalColorParser.FALLBACK_WHITE, PoliticalColorParser.parseArgb(""));
        assertEquals(PoliticalColorParser.FALLBACK_WHITE, PoliticalColorParser.parseArgb("   "));
    }

    @Test
    void sixDigitHexForcesAlphaTo0xFF() {
        assertEquals(0xFFFF0000, PoliticalColorParser.parseArgb("#FF0000"));
        assertEquals(0xFF00FF00, PoliticalColorParser.parseArgb("00FF00"));
        assertEquals(0xFF112233, PoliticalColorParser.parseArgb("#112233"));
    }

    @Test
    void eightDigitHexPreservesAlpha() {
        assertEquals(0x80112233, PoliticalColorParser.parseArgb("#80112233"));
        assertEquals(0xCAFEBABE, PoliticalColorParser.parseArgb("CAFEBABE"));
    }

    @Test
    void unparsableInputReturnsFallback() {
        assertEquals(PoliticalColorParser.FALLBACK_WHITE, PoliticalColorParser.parseArgb("not a color"));
        assertEquals(0x12345678, PoliticalColorParser.parseArgb("ZZZZZZ", 0x12345678));
        assertEquals(0x12345678, PoliticalColorParser.parseArgb("#1234", 0x12345678));
    }

    @Test
    void caseInsensitiveAndWhitespaceTolerant() {
        assertEquals(0xFFAABBCC, PoliticalColorParser.parseArgb("  #aabbcc  "));
        assertEquals(0xFFAABBCC, PoliticalColorParser.parseArgb("AaBbCc"));
    }
}
