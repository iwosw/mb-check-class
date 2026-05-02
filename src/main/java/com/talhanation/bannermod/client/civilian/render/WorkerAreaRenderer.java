package com.talhanation.bannermod.client.civilian.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.talhanation.bannermod.client.render.ClientRenderPrimitives;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import com.talhanation.bannermod.entity.civilian.workarea.LumberArea;
import com.talhanation.bannermod.entity.civilian.workarea.MiningArea;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import com.talhanation.bannermod.entity.civilian.workarea.MarketArea;
import com.talhanation.bannermod.persistence.civilian.ScannedBlock;
import com.talhanation.bannermod.persistence.civilian.StructureManager;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class WorkerAreaRenderer extends EntityRenderer<AbstractWorkAreaEntity> implements IRenderWorkArea {
    private static final double STRUCTURE_PREVIEW_MAX_DISTANCE_SQR = 64.0D * 64.0D;
    private static final double STRUCTURE_PREVIEW_FULL_DETAIL_DISTANCE_SQR = 24.0D * 24.0D;
    private static final double SETTLEMENT_WORK_AREA_MAX_DISTANCE_SQR = 128.0D * 128.0D;
    private static final int STRUCTURE_PREVIEW_CACHE_ENTRIES = 8;
    private static final int STRUCTURE_PREVIEW_FAR_BLOCK_BUDGET = 512;
    private static final int STRUCTURE_PREVIEW_FAR_BLOCK_ENTITY_BUDGET = 16;

    private final Map<PreviewCacheKey, List<ScannedBlock>> previewStructureCache = new LinkedHashMap<>(STRUCTURE_PREVIEW_CACHE_ENTRIES, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<PreviewCacheKey, List<ScannedBlock>> eldest) {
            return size() > STRUCTURE_PREVIEW_CACHE_ENTRIES;
        }
    };
    private final Map<BlockState, ModelData> previewModelDataCache = new HashMap<>();
    private static boolean renderStructurePreviews = true;
    private static boolean renderSettlementWorkAreas = false;

    public static boolean toggleStructurePreviewRendering() {
        renderStructurePreviews = !renderStructurePreviews;
        return renderStructurePreviews;
    }

    public static boolean toggleSettlementWorkAreaRendering() {
        renderSettlementWorkAreas = !renderSettlementWorkAreas;
        return renderSettlementWorkAreas;
    }

    public WorkerAreaRenderer(EntityRendererProvider.Context mgr) {
        super(mgr);
        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(AbstractWorkAreaEntity workArea) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(AbstractWorkAreaEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        boolean visible = super.shouldRender(entity, frustum, camX, camY, camZ);
        Player player = Minecraft.getInstance().player;
        AABB areaBounds = entity.getArea().inflate(0.5D);
        boolean areaVisible = frustum.isVisible(areaBounds);
        if (player != null) {
            if (entity.showBox && areaVisible) {
                return true;
            }
            if (renderSettlementWorkAreas && shouldRenderSettlementOverlay(entity, player, areaVisible)) {
                return true;
            }
        }
        if (entity instanceof BuildArea buildArea) {
            if (renderStructurePreviews
                    && player != null
                    && (visible || areaVisible)
                    && entity.canPlayerSee(player)
                    && player.distanceToSqr(buildArea) <= STRUCTURE_PREVIEW_MAX_DISTANCE_SQR) {
                return true;
            }
        }
        return visible;
    }
    //ItemEntityRenderer
    @Override
    public void render(@NotNull AbstractWorkAreaEntity abstractWorkAreaEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;

        if(abstractWorkAreaEntity.showBox) {
            renderWorkAreaBox(abstractWorkAreaEntity, poseStack, bufferSource, 0.95F, 0.84F, 0.42F, 0.95F);
        } else if (renderSettlementWorkAreas && shouldRenderSettlementOverlay(abstractWorkAreaEntity, player, true)) {
            float[] color = overlayColor(abstractWorkAreaEntity);
            renderWorkAreaBox(abstractWorkAreaEntity, poseStack, bufferSource, color[0], color[1], color[2], color[3]);
        }

        if(!renderStructurePreviews) return;
        if(!abstractWorkAreaEntity.canPlayerSee(player)) return;

        if(abstractWorkAreaEntity instanceof BuildArea buildArea){
            double distanceSqr = player.distanceToSqr(buildArea);
            if (distanceSqr <= STRUCTURE_PREVIEW_MAX_DISTANCE_SQR) {
                renderStructurePreview(poseStack, bufferSource, buildArea, distanceSqr);
            } else {
                RuntimeProfilingCounters.increment("build_preview.skipped.distance");
            }
        }
    }

    private void renderStructurePreview(PoseStack poseStack, MultiBufferSource bufferSource, BuildArea buildArea, double distanceSqr) {
        CompoundTag nbt = buildArea.getStructureNBT();
        if (nbt == null || nbt.isEmpty()) {
            RuntimeProfilingCounters.increment("build_preview.skipped.empty_nbt");
            return;
        }
        List<ScannedBlock> structure = getCachedPreviewStructure(buildArea, nbt);
        if (structure.isEmpty()) {
            RuntimeProfilingCounters.increment("build_preview.skipped.empty_structure");
            return;
        }

        int width = nbt.getInt("width");
        Direction scanFacing = Direction.byName(nbt.getString("facing"));
        if (scanFacing == null) {
            scanFacing = Direction.SOUTH;
        }
        Direction facing = buildArea.getFacing();
        Direction right = facing.getClockWise();
        int rotationSteps = (4 + facing.get2DDataValue() - scanFacing.get2DDataValue()) % 4;
        BlockPos origin = buildArea.getOriginPos();

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        boolean fullDetail = distanceSqr <= STRUCTURE_PREVIEW_FULL_DETAIL_DISTANCE_SQR;
        int blockBudget = fullDetail ? Integer.MAX_VALUE : STRUCTURE_PREVIEW_FAR_BLOCK_BUDGET;
        int blockEntityBudget = fullDetail ? Integer.MAX_VALUE : STRUCTURE_PREVIEW_FAR_BLOCK_ENTITY_BUDGET;
        int renderedBlocks = 0;
        int renderedBlockEntities = 0;
        double minDx = Double.POSITIVE_INFINITY;
        double minDy = Double.POSITIVE_INFINITY;
        double minDz = Double.POSITIVE_INFINITY;
        double maxDx = Double.NEGATIVE_INFINITY;
        double maxDy = Double.NEGATIVE_INFINITY;
        double maxDz = Double.NEGATIVE_INFINITY;

        poseStack.pushPose();

        for (ScannedBlock scannedBlock : structure) {
            RuntimeProfilingCounters.increment("build_preview.blocks_considered");
            if (renderedBlocks >= blockBudget) {
                RuntimeProfilingCounters.increment("build_preview.skipped.block_budget");
                break;
            }
            BlockPos relPos = scannedBlock.relativePos();
            int relX = relPos.getX();
            int relY = relPos.getY();
            int relZ = relPos.getZ();

            // Compute world block position using the same formula as setStartBuild
            BlockPos worldPos = origin
                    .relative(facing, relZ)
                    .relative(right, width - 1 - relX)
                    .above(relY);

            // Offset relative to entity render origin (poseStack is already at entity position)
            double dx = worldPos.getX() - buildArea.getX();
            double dy = worldPos.getY() - buildArea.getY();
            double dz = worldPos.getZ() - buildArea.getZ();
            minDx = Math.min(minDx, dx);
            minDy = Math.min(minDy, dy);
            minDz = Math.min(minDz, dz);
            maxDx = Math.max(maxDx, dx + 1.0D);
            maxDy = Math.max(maxDy, dy + 1.0D);
            maxDz = Math.max(maxDz, dz + 1.0D);

            BlockState state = scannedBlock.state();
            BlockState rotatedState = BuildArea.rotateBlockState(state, rotationSteps);

            FluidState fluidState = state.getFluidState();
            RenderType renderType = null;
            if (!fluidState.isEmpty()) {
                renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
            }

            ModelData modelData = ModelData.EMPTY;
            if (state.getBlock() instanceof EntityBlock entityBlock) {
                modelData = previewModelDataCache.computeIfAbsent(state, ignored -> {
                    BlockEntity be = entityBlock.newBlockEntity(BlockPos.ZERO, state);
                    return be == null ? ModelData.EMPTY : be.getModelData();
                });
            }

            if (rotatedState.getRenderShape() == RenderShape.MODEL) {
                poseStack.pushPose();
                poseStack.translate(dx, dy, dz);
                dispatcher.renderSingleBlock(rotatedState, poseStack, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, modelData, renderType);
                poseStack.popPose();
                renderedBlocks++;
            } else if (rotatedState.getBlock() instanceof EntityBlock entityBlock) {
                if (renderedBlockEntities >= blockEntityBudget) {
                    RuntimeProfilingCounters.increment("build_preview.skipped.block_entity_budget");
                    continue;
                }
                BlockEntity be = entityBlock.newBlockEntity(worldPos, rotatedState);
                if (be != null) {
                    if (mc.level != null) be.setLevel(mc.level);
                    @SuppressWarnings("unchecked")
                    BlockEntityRenderer<BlockEntity> renderer =
                            (BlockEntityRenderer<BlockEntity>) mc.getBlockEntityRenderDispatcher().getRenderer(be);
                    if (renderer != null) {
                        poseStack.pushPose();
                        poseStack.translate(dx, dy, dz);
                        renderer.render(be, 0f, poseStack, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY);
                        poseStack.popPose();
                        renderedBlocks++;
                        renderedBlockEntities++;
                    }
                }
            }
        }

        if (renderedBlocks > 0 && minDx != Double.POSITIVE_INFINITY) {
            VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
            ClientRenderPrimitives.lineBox(poseStack, lines,
                    new AABB(minDx, minDy, minDz, maxDx, maxDy, maxDz).inflate(0.03D),
                    0.85F, 0.65F, 0.35F, 0.70F);
            double originDx = origin.getX() - buildArea.getX();
            double originDy = origin.getY() - buildArea.getY();
            double originDz = origin.getZ() - buildArea.getZ();
            ClientRenderPrimitives.lineBox(poseStack, lines,
                    new AABB(originDx, originDy, originDz, originDx + 1.0D, originDy + 1.0D, originDz + 1.0D).inflate(0.05D),
                    1.0F, 0.55F, 0.24F, 0.95F);
        }

        poseStack.popPose();
    }

    private void renderWorkAreaBox(AbstractWorkAreaEntity area, PoseStack poseStack, MultiBufferSource bufferSource,
                                   float r, float g, float b, float a) {
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        AABB localArea = area.getArea().inflate(0.03D).move(-area.getX(), -area.getY(), -area.getZ());
        renderWorkArea(poseStack, lines, localArea, r, g, b, a);
        Vec3 center = localArea.getCenter();
        drawLine(poseStack, lines,
                new Vec3(center.x, localArea.minY, center.z),
                new Vec3(center.x, localArea.maxY + 0.45D, center.z),
                r, g, b, a);
    }

    private boolean shouldRenderSettlementOverlay(AbstractWorkAreaEntity area, Player player, boolean areaVisible) {
        return areaVisible
                && area.canPlayerSee(player)
                && player.distanceToSqr(area) <= SETTLEMENT_WORK_AREA_MAX_DISTANCE_SQR
                && isClearlyVisible(area, player);
    }

    private boolean isClearlyVisible(AbstractWorkAreaEntity area, Player player) {
        if (player.distanceToSqr(area) <= 9.0D) {
            return true;
        }
        AABB box = area.getArea().inflate(0.15D);
        Vec3 eye = player.getEyePosition();
        Vec3 center = box.getCenter();
        if (hasLineOfSight(area, player, eye, center)) {
            return true;
        }
        double topY = box.maxY + 0.2D;
        return hasLineOfSight(area, player, eye, new Vec3(box.minX, topY, box.minZ))
                || hasLineOfSight(area, player, eye, new Vec3(box.minX, topY, box.maxZ))
                || hasLineOfSight(area, player, eye, new Vec3(box.maxX, topY, box.minZ))
                || hasLineOfSight(area, player, eye, new Vec3(box.maxX, topY, box.maxZ))
                || hasLineOfSight(area, player, eye, new Vec3(center.x, topY, center.z));
    }

    private boolean hasLineOfSight(AbstractWorkAreaEntity area, Player player, Vec3 from, Vec3 to) {
        HitResult result = player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return result.getType() == HitResult.Type.MISS || area.getArea().inflate(0.2D).contains(result.getLocation());
    }

    private float[] overlayColor(AbstractWorkAreaEntity area) {
        if (area instanceof CropArea) {
            return new float[]{0.49F, 0.76F, 0.31F, 0.82F};
        }
        if (area instanceof LumberArea) {
            return new float[]{0.68F, 0.48F, 0.24F, 0.82F};
        }
        if (area instanceof MiningArea) {
            return new float[]{0.61F, 0.66F, 0.78F, 0.82F};
        }
        if (area instanceof StorageArea) {
            return new float[]{0.78F, 0.58F, 0.28F, 0.82F};
        }
        if (area instanceof MarketArea) {
            return new float[]{0.77F, 0.48F, 0.67F, 0.82F};
        }
        if (area instanceof BuildArea) {
            return new float[]{0.39F, 0.70F, 0.92F, 0.82F};
        }
        return new float[]{0.84F, 0.78F, 0.62F, 0.82F};
    }

    private List<ScannedBlock> getCachedPreviewStructure(BuildArea buildArea, CompoundTag nbt) {
        PreviewCacheKey key = new PreviewCacheKey(buildArea.getUUID(), System.identityHashCode(nbt));
        List<ScannedBlock> cached = previewStructureCache.get(key);
        if (cached != null) {
            RuntimeProfilingCounters.increment("build_preview.parse_cache_hits");
            return cached;
        }

        List<ScannedBlock> parsed = StructureManager.parseStructureFromNBT(nbt);
        previewStructureCache.put(key, parsed);
        RuntimeProfilingCounters.increment("build_preview.parse_cache_misses");
        RuntimeProfilingCounters.add("build_preview.blocks_parsed", parsed.size());
        return parsed;
    }

    private record PreviewCacheKey(UUID areaId, int nbtIdentity) {
        private PreviewCacheKey {
            Objects.requireNonNull(areaId, "areaId");
        }
    }

}
