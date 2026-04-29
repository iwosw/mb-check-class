package com.talhanation.bannermod.client.military.render;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitCosmeticLayerSourceTest {
    private static final Path LAYER_PACKAGE = Path.of("src/main/java/com/talhanation/bannermod/client/military/render/layer");
    private static final Pattern LEGACY_FLOAT_RENDER_COLOR = Pattern.compile("renderToBuffer\\([^;]*(?:1\\.0F|0\\.0F|p_\\d+_)", Pattern.DOTALL);

    @Test
    void cosmeticModelLayersUseCurrentArgbRenderColorApi() throws IOException {
        for (String fileName : modelLayerFiles()) {
            String source = read(fileName);

            assertTrue(source.contains("FULL_WHITE = 0xFFFFFFFF"), fileName);
            assertTrue(source.contains("OverlayTexture.NO_OVERLAY, FULL_WHITE"), fileName);
            assertFalse(LEGACY_FLOAT_RENDER_COLOR.matcher(source).find(), fileName);
        }
    }

    @Test
    void cosmeticModelLayersKeepTextureAndVisibilityContracts() throws IOException {
        assertContains("RecruitHumanTeamColorLayer.java", "human_team_white.png", "human_team_gold.png", "human_base_cloth.png", "TEXTURE[recruit.getColor()]", "RecruitRenderLod.shouldRenderCosmeticModelLayer(recruit)");
        assertContains("RecruitVillagerTeamColorLayer.java", "villager_team_white.png", "villager_team_gold.png", "villager_base_cloth.png", "TEXTURE[recruit.getColor()]", "RecruitRenderLod.shouldRenderCosmeticModelLayer(recruit)");
        assertContains("RecruitHumanBiomeLayer.java", "human_desert.png", "human_taiga.png", "TEXTURE[recruit.getBiome()]", "RecruitRenderLod.shouldRenderCosmeticModelLayer(recruit)");
        assertContains("RecruitVillagerBiomeLayer.java", "villager_desert.png", "villager_taiga.png", "BIOME_TEXTURE[recruit.getBiome()]", "RecruitRenderLod.shouldRenderCosmeticModelLayer(recruit)");
        assertContains("RecruitHumanCompanionLayer.java", "human_assassin_cloth.png", "recruit instanceof ICompanion", "RenderType.entityCutoutNoCull(LOCATION)");
        assertContains("RecruitVillagerCompanionLayer.java", "villager_assassin_cloth.png", "recruit instanceof ICompanion", "RenderType.entityCutoutNoCull(LOCATION)");
    }

    @Test
    void customHeadLayersKeepCurrentProfileAndItemRenderingApis() throws IOException {
        assertContains("RecruitLodCustomHeadLayer.java", "extends CustomHeadLayer", "super.render", "RecruitRenderLod.shouldRenderCustomHead(recruit)");
        assertContains("VillagerRecruitCustomHeadLayer.java", "ResolvableProfile", "DataComponents.PROFILE", "SkullBlockRenderer.getRenderType(skullblock$type, profile)", "ItemDisplayContext.HEAD");
    }

    private static List<String> modelLayerFiles() {
        return List.of(
                "RecruitHumanTeamColorLayer.java",
                "RecruitVillagerTeamColorLayer.java",
                "RecruitHumanBiomeLayer.java",
                "RecruitVillagerBiomeLayer.java",
                "RecruitHumanCompanionLayer.java",
                "RecruitVillagerCompanionLayer.java"
        );
    }

    private static void assertContains(String fileName, String... snippets) throws IOException {
        String source = read(fileName);
        for (String snippet : snippets) {
            assertTrue(source.contains(snippet), fileName + " missing " + snippet);
        }
    }

    private static String read(String fileName) throws IOException {
        return Files.readString(LAYER_PACKAGE.resolve(fileName));
    }
}
