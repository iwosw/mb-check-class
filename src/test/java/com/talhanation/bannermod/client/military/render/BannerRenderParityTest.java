package com.talhanation.bannermod.client.military.render;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerRenderParityTest {
    private static final Path ROOT = Path.of("");

    @Test
    void guiBannerPreviewCarriesBaseColorAndPatternLayersToSharedRenderer() throws IOException {
        String renderer = read("src/main/java/com/talhanation/bannermod/client/military/gui/component/BannerRenderer.java");
        String helper = read("src/main/java/com/talhanation/bannermod/client/military/render/BannerPatternRenderHelper.java");

        assertTrue(renderer.contains("this.baseColor = item.getColor();"));
        assertTrue(renderer.contains("bannerItem.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)"));
        assertTrue(renderer.contains("BannerPatternRenderHelper.render(guiGraphics.pose(), bufferSource, this.flag, 15728880,"));
        assertTrue(renderer.contains("this.baseColor, this.patternLayers"));
        assertTrue(helper.contains("BannerRenderer.renderPatterns("));
        assertTrue(helper.contains("Sheets.BANNER_BASE"));
        assertTrue(helper.contains("baseColor, patternLayers"));
    }

    @Test
    void siegeStandardUsesDynamicBannerClothPoliticalCueAndNoStaticClothDuplicate() throws IOException {
        String renderer = read("src/main/java/com/talhanation/bannermod/client/military/render/SiegeStandardBlockEntityRenderer.java");
        String model = read("src/main/resources/assets/bannermod/models/block/siege_standard.json");

        assertTrue(renderer.contains("resolveColor(sideId)"));
        assertTrue(renderer.contains("BannerPatternRenderHelper.nearestDyeColor(colorArgb)"));
        assertTrue(renderer.contains("BannerPatternRenderHelper.render(poseStack, buffer, this.flag, packedLight,"));
        assertTrue(renderer.contains("baseColor, BannerPatternLayers.EMPTY"));
        assertTrue(renderer.contains("renderColoredOutline(poseStack, consumer, CAP_BOX, colorArgb)"));
        assertTrue(renderer.contains("renderColoredFill(poseStack, fillConsumer, CAP_BOX, colorArgb, packedLight, packedOverlay)"));

        assertFalse(model.contains("\"cloth\""));
        assertFalse(model.contains("#cloth"));
        assertEquals(4, countOccurrences(model, "\"from\""));
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
