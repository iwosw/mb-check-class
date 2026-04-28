package com.talhanation.bannermod.client.military.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalColorParser;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.runtime.SiegeStandardBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Renders the dynamic cloth and political-color cap for a siege standard.
 *
 * <p>The block model carries the static pole/trim geometry. The cloth is emitted through the
 * current banner pattern API so base dye selection and future pattern layers stay on the same
 * rendering path as item banner previews. The cap remains an at-a-glance political cue.</p>
 */
public class SiegeStandardBlockEntityRenderer implements BlockEntityRenderer<SiegeStandardBlockEntity> {

    private static final AABB CAP_BOX = new AABB(
            6.0 / 16.0, 18.0 / 16.0, 6.0 / 16.0,
            10.0 / 16.0, 22.0 / 16.0, 10.0 / 16.0
    );
    private final ModelPart flag;

    public SiegeStandardBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.flag = ctx.bakeLayer(ModelLayers.BANNER).getChild("flag");
    }

    @Override
    public void render(SiegeStandardBlockEntity blockEntity,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight,
                       int packedOverlay) {
        UUID sideId = blockEntity.sidePoliticalEntityId();
        int colorArgb = resolveColor(sideId);
        DyeColor baseColor = BannerPatternRenderHelper.nearestDyeColor(colorArgb);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.84D, 0.53D);
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        BannerPatternRenderHelper.render(poseStack, buffer, this.flag, packedLight,
                baseColor, BannerPatternLayers.EMPTY);
        poseStack.popPose();

        VertexConsumer consumer = buffer.getBuffer(RenderType.LINES);
        renderColoredOutline(poseStack, consumer, CAP_BOX, colorArgb);

        VertexConsumer fillConsumer = buffer.getBuffer(RenderType.translucent());
        renderColoredFill(poseStack, fillConsumer, CAP_BOX, colorArgb, packedLight, packedOverlay);
    }

    private static int resolveColor(@Nullable UUID sideId) {
        if (sideId == null) return PoliticalColorParser.FALLBACK_WHITE;
        PoliticalEntityRecord entity = WarClientState.entityById(sideId);
        if (entity == null) return PoliticalColorParser.FALLBACK_WHITE;
        return PoliticalColorParser.parseArgb(entity.color());
    }

    private static void renderColoredFill(PoseStack poseStack,
                                          VertexConsumer consumer,
                                          AABB box,
                                          int argb,
                                          int packedLight,
                                          int packedOverlay) {
        float a = ((argb >> 24) & 0xFF) / 255.0F;
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        if (a <= 0.0F) a = 1.0F;

        var pose = poseStack.last().pose();
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        // top quad
        consumer.addVertex(pose, x0, y1, z0).setColor(r, g, b, a).setUv(0F, 0F).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
        consumer.addVertex(pose, x1, y1, z0).setColor(r, g, b, a).setUv(1F, 0F).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(1F, 1F).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
        consumer.addVertex(pose, x0, y1, z1).setColor(r, g, b, a).setUv(0F, 1F).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);

        // four side quads
        addSideQuad(consumer, pose, x0, y0, z0, x1, y1, z0, 0F, 0F, -1F, r, g, b, a, packedLight, packedOverlay);
        addSideQuad(consumer, pose, x1, y0, z1, x0, y1, z1, 0F, 0F, 1F, r, g, b, a, packedLight, packedOverlay);
        addSideQuad(consumer, pose, x0, y0, z1, x0, y1, z0, -1F, 0F, 0F, r, g, b, a, packedLight, packedOverlay);
        addSideQuad(consumer, pose, x1, y0, z0, x1, y1, z1, 1F, 0F, 0F, r, g, b, a, packedLight, packedOverlay);
    }

    private static void addSideQuad(VertexConsumer consumer,
                                    org.joml.Matrix4f pose,
                                    float xa, float y0, float za,
                                    float xb, float y1, float zb,
                                    float nx, float ny, float nz,
                                    float r, float g, float b, float a,
                                    int packedLight, int packedOverlay) {
        consumer.addVertex(pose, xa, y0, za).setColor(r, g, b, a).setUv(0F, 0F).setOverlay(packedOverlay).setLight(packedLight).setNormal(nx, ny, nz);
        consumer.addVertex(pose, xb, y0, zb).setColor(r, g, b, a).setUv(1F, 0F).setOverlay(packedOverlay).setLight(packedLight).setNormal(nx, ny, nz);
        consumer.addVertex(pose, xb, y1, zb).setColor(r, g, b, a).setUv(1F, 1F).setOverlay(packedOverlay).setLight(packedLight).setNormal(nx, ny, nz);
        consumer.addVertex(pose, xa, y1, za).setColor(r, g, b, a).setUv(0F, 1F).setOverlay(packedOverlay).setLight(packedLight).setNormal(nx, ny, nz);
    }

    private static void renderColoredOutline(PoseStack poseStack, VertexConsumer consumer, AABB box, int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0F;
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        if (a <= 0.0F) a = 1.0F;

        var pose = poseStack.last().pose();
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        line(consumer, pose, x0, y0, z0, x1, y0, z0, r, g, b, a);
        line(consumer, pose, x1, y0, z0, x1, y0, z1, r, g, b, a);
        line(consumer, pose, x1, y0, z1, x0, y0, z1, r, g, b, a);
        line(consumer, pose, x0, y0, z1, x0, y0, z0, r, g, b, a);
        line(consumer, pose, x0, y1, z0, x1, y1, z0, r, g, b, a);
        line(consumer, pose, x1, y1, z0, x1, y1, z1, r, g, b, a);
        line(consumer, pose, x1, y1, z1, x0, y1, z1, r, g, b, a);
        line(consumer, pose, x0, y1, z1, x0, y1, z0, r, g, b, a);
        line(consumer, pose, x0, y0, z0, x0, y1, z0, r, g, b, a);
        line(consumer, pose, x1, y0, z0, x1, y1, z0, r, g, b, a);
        line(consumer, pose, x1, y0, z1, x1, y1, z1, r, g, b, a);
        line(consumer, pose, x0, y0, z1, x0, y1, z1, r, g, b, a);
    }

    private static void line(VertexConsumer consumer, org.joml.Matrix4f pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(0F, 1F, 0F);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(0F, 1F, 0F);
    }
}
