package com.talhanation.bannermod.client.military.render;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitRendererParitySourceTest {
    private static final Path RENDER_PACKAGE = Path.of("src/main/java/com/talhanation/bannermod/client/military/render");

    @Test
    void humanRendererKeepsPlayerModelTexturesAndLayerStack() throws IOException {
        String source = read("RecruitHumanRenderer.java");

        assertTrue(source.contains("extends AbstractRecruitRenderer<HumanoidModel<AbstractRecruitEntity>>"));
        assertTrue(source.contains("new RecruitHumanModel(mgr.bakeLayer(ModelLayers.PLAYER))"));
        assertTrue(source.contains("TEXTURE[recruit.getVariant()]"));
        assertTrue(source.contains("textures/entity/human/human_0.png"));
        assertTrue(source.contains("textures/entity/human/human_19.png"));
        assertTrue(source.contains("RecruitRenderProfiling.textureStateSwitch(\"base_model\")"));
        assertTrue(source.contains("new RecruitLodArmorLayer"));
        assertTrue(source.contains("new RecruitHumanTeamColorLayer"));
        assertTrue(source.contains("new RecruitHumanBiomeLayer"));
        assertTrue(source.contains("new RecruitHumanCompanionLayer"));
        assertTrue(source.contains("new RecruitLodItemInHandLayer"));
        assertTrue(source.contains("new RecruitLodCustomHeadLayer"));
        assertTrue(source.contains("IClientItemExtensions.of(itemStack).getArmPose"));
    }

    @Test
    void villagerRendererKeepsRecruitModelTextureScaleAndLayerStack() throws IOException {
        String source = read("RecruitVillagerRenderer.java");

        assertTrue(source.contains("extends AbstractRecruitRenderer<HumanoidModel<AbstractRecruitEntity>>"));
        assertTrue(source.contains("new RecruitVillagerModel(context.bakeLayer(ClientEvent.RECRUIT))"));
        assertTrue(source.contains("TEXTURE[0]"));
        assertTrue(source.contains("textures/entity/villager/villager_1.png"));
        assertTrue(source.contains("RecruitRenderProfiling.textureStateSwitch(\"base_model\")"));
        assertTrue(source.contains("new RecruitLodArmorLayer"));
        assertTrue(source.contains("new RecruitVillagerTeamColorLayer"));
        assertTrue(source.contains("new RecruitVillagerBiomeLayer"));
        assertTrue(source.contains("new RecruitVillagerCompanionLayer"));
        assertTrue(source.contains("new RecruitLodItemInHandLayer"));
        assertTrue(source.contains("new VillagerRecruitCustomHeadLayer"));
        assertTrue(source.contains("matrixStackIn.scale(0.9375F, 0.9375F, 0.9375F)"));
    }

    @Test
    void abstractRendererKeepsNameplateAndProfilingHooks() throws IOException {
        String source = read("AbstractRecruitRenderer.java");

        assertTrue(source.contains("RecruitRenderProfiling.duration(\"animation_pose\", poseStart)"));
        assertTrue(source.contains("RecruitRenderProfiling.beginNormalRender()"));
        assertTrue(source.contains("super.render(recruit, entityYaw, partialTicks, poseStack, bufferSource, packedLight)"));
        assertTrue(source.contains("RecruitRenderProfiling.endNormalRender(renderStart)"));
        assertTrue(source.contains("RecruitRenderLod.shouldRenderName(recruit) && super.shouldShowName(recruit)"));
        assertTrue(source.contains("RecruitRenderProfiling.increment(\"nameplates.visible\")"));
        assertTrue(source.contains("RecruitRenderProfiling.skipped(\"nameplates\")"));
        assertTrue(source.contains("super.renderNameTag(recruit, displayName, poseStack, bufferSource, packedLight, partialTick)"));
        assertTrue(source.contains("RecruitRenderProfiling.duration(\"nameplates\", start)"));
    }

    @Test
    void profilingBucketsKeepRuntimeCounterNames() throws IOException {
        String source = read("RecruitRenderProfiling.java");

        assertTrue(source.contains("PREFIX = \"recruit.render.\""));
        assertTrue(source.contains("PREFIX + bucket + \".calls\""));
        assertTrue(source.contains("PREFIX + bucket + \".nanos\""));
        assertTrue(source.contains("bucket.equals(\"nameplates\") || bucket.startsWith(\"layers.\")"));
        assertTrue(source.contains("duration(\"layers.\" + layerBucket, startNanos)"));
        assertTrue(source.contains("PREFIX + \"texture_state_switches\""));
        assertTrue(source.contains("PREFIX + \"texture_state.\" + bucket"));
        assertTrue(source.contains("PREFIX + \"normal_total.calls\""));
        assertTrue(source.contains("PREFIX + \"normal_total.nanos\""));
        assertTrue(source.contains("PREFIX + \"base_model.calls\""));
        assertTrue(source.contains("PREFIX + \"base_model.nanos\""));
    }

    private static String read(String fileName) throws IOException {
        return Files.readString(RENDER_PACKAGE.resolve(fileName));
    }
}
