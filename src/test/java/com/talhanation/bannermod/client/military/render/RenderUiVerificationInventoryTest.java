package com.talhanation.bannermod.client.military.render;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderUiVerificationInventoryTest {
    private static final Path ROOT = Path.of("");

    @Test
    void verificationSuiteCoversRewrittenRenderAndUiAreas() throws IOException {
        assertContains("src/test/java/com/talhanation/bannermod/client/military/render/RecruitRendererParitySourceTest.java", "humanRendererKeepsPlayerModelTexturesAndLayerStack", "villagerRendererKeepsRecruitModelTextureScaleAndLayerStack");
        assertContains("src/test/java/com/talhanation/bannermod/client/military/render/RecruitCosmeticLayerSourceTest.java", "cosmeticModelLayersUseCurrentArgbRenderColorApi", "customHeadLayersKeepCurrentProfileAndItemRenderingApis");
        assertContains("src/test/java/com/talhanation/bannermod/client/military/render/BannerRenderParityTest.java", "guiBannerPreviewCarriesBaseColorAndPatternLayersToSharedRenderer", "siegeStandardUsesDynamicBannerClothPoliticalCueAndNoStaticClothDuplicate");
        assertContains("src/test/java/com/talhanation/bannermod/client/render/ClientRenderPrimitivesTest.java", "texturedBillboardQuadEmitsVisibleBobberQuad", "lineBoxEmitsAllWorkAreaOutlineEdges", "lineStripVertexEmitsOpaqueFishingLineVertex");
        assertContains("src/test/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapRouteUiVerificationTest.java", "worldMapRouteUiStillWiresRouteDisplayPopupsScrollSelectionAndMoveDispatch");
    }

    @Test
    void hudOverlayCoordinatorKeepsSafeStackingAndOverlapGuards() throws IOException {
        assertContains("src/main/java/com/talhanation/bannermod/client/military/gui/overlay/HudOverlayCoordinator.java",
                "event.registerAbove(VanillaGuiLayers.HOTBAR, HUD_LAYER, HudOverlayCoordinator::render)",
                "TOP_SAFE_MARGIN = 28",
                "RIGHT_SAFE_MARGIN = 6",
                "mc.options.hideGui || mc.getDebugOverlay().showDebugScreen() || mc.options.keyPlayerList.isDown()",
                "y = renderBattleWindow(graphics, mc, y)",
                "y = renderSiegeZone(graphics, mc, y)",
                "renderClaim(graphics, mc, y)",
                "Math.max(6, screenWidth - RIGHT_SAFE_MARGIN - width)",
                "Math.max(6, mc.getWindow().getGuiScaledWidth() - RIGHT_SAFE_MARGIN - CLAIM_PANEL_WIDTH)",
                "WarClientState.hasSnapshot()");
    }

    private static void assertContains(String relativePath, String... snippets) throws IOException {
        String source = Files.readString(ROOT.resolve(relativePath));
        for (String snippet : snippets) {
            assertTrue(source.contains(snippet), relativePath + " missing " + snippet);
        }
    }
}
