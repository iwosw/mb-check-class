package com.talhanation.bannermod.client.civilian.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class WorkerAreaRenderer extends EntityRenderer<AbstractWorkAreaEntity> {
    private static final double STRUCTURE_PREVIEW_MAX_DISTANCE_SQR = 64.0D * 64.0D;

    private UUID cachedPreviewArea;
    private CompoundTag cachedPreviewNbt;
    private List<ScannedBlock> cachedPreviewStructure = List.of();
    private final Map<BlockState, ModelData> previewModelDataCache = new HashMap<>();
    private static boolean renderStructurePreviews = true;

    public static boolean toggleStructurePreviewRendering() {
        renderStructurePreviews = !renderStructurePreviews;
        return renderStructurePreviews;
    }

    public WorkerAreaRenderer(EntityRendererProvider.Context mgr) {
        super(mgr);
        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(AbstractWorkAreaEntity p_115034_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(AbstractWorkAreaEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        if (entity instanceof BuildArea buildArea) {
            Player player = Minecraft.getInstance().player;
            if (renderStructurePreviews
                    && player != null
                    && entity.canPlayerSee(player)
                    && player.distanceToSqr(buildArea) <= STRUCTURE_PREVIEW_MAX_DISTANCE_SQR) {
                return true;
            }
        }
        return super.shouldRender(entity, frustum, camX, camY, camZ);
    }
    //ItemEntityRenderer
    @Override
    public void render(@NotNull AbstractWorkAreaEntity abstractWorkAreaEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!renderStructurePreviews) return;

        if(!abstractWorkAreaEntity.canPlayerSee(player)) return;

        if(abstractWorkAreaEntity instanceof BuildArea buildArea){
            if (player.distanceToSqr(buildArea) <= STRUCTURE_PREVIEW_MAX_DISTANCE_SQR) {
                renderStructurePreview(poseStack, bufferSource, buildArea);
            } else {
                RuntimeProfilingCounters.increment("build_preview.skipped.distance");
            }
        }
    }

    private void renderStructurePreview(PoseStack poseStack, MultiBufferSource bufferSource, BuildArea buildArea) {
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

        poseStack.pushPose();

        for (ScannedBlock scannedBlock : structure) {
            RuntimeProfilingCounters.increment("build_preview.blocks_considered");
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
            } else if (rotatedState.getBlock() instanceof EntityBlock entityBlock) {
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
                    }
                }
            }
        }

        poseStack.popPose();
    }

    private List<ScannedBlock> getCachedPreviewStructure(BuildArea buildArea, CompoundTag nbt) {
        if (buildArea.getUUID().equals(cachedPreviewArea) && nbt == cachedPreviewNbt) {
            return cachedPreviewStructure;
        }

        cachedPreviewArea = buildArea.getUUID();
        cachedPreviewNbt = nbt;
        cachedPreviewStructure = StructureManager.parseStructureFromNBT(nbt);
        RuntimeProfilingCounters.increment("build_preview.parse_cache_misses");
        RuntimeProfilingCounters.add("build_preview.blocks_parsed", cachedPreviewStructure.size());
        return cachedPreviewStructure;
    }

}
