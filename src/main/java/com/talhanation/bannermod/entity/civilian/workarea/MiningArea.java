package com.talhanation.bannermod.entity.civilian.workarea;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.ai.civilian.MiningClaimExcavationRules;
import com.talhanation.bannermod.ai.civilian.MiningPatternPlanner;
import com.talhanation.bannermod.entity.civilian.MinerEntity;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementRefreshSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Stack;
import java.util.stream.Stream;

public class MiningArea extends AbstractWorkAreaEntity {
    public static final int DEFAULT_PATTERN_SEGMENTS = MiningPatternContract.DEFAULT_PATTERN_SEGMENTS;
    public static final EntityDataAccessor<Integer> HEIGHT_OFFSET = SynchedEntityData.defineId(MiningArea.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> CLOSE_FLOOR = SynchedEntityData.defineId(MiningArea.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> MINING_MODE = SynchedEntityData.defineId(MiningArea.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> BRANCH_SPACING = SynchedEntityData.defineId(MiningArea.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> BRANCH_LENGTH = SynchedEntityData.defineId(MiningArea.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DESCENT_STEP = SynchedEntityData.defineId(MiningArea.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> CURRENT_SEGMENT = SynchedEntityData.defineId(MiningArea.class, EntityDataSerializers.INT);
    public Stack<BlockPos> stackToPlace = new Stack<>();
    public Stack<BlockPos> stackToBreak = new Stack<>();
    private static final MiningPatternPlanner PATTERN_PLANNER = new MiningPatternPlanner();

    public MiningArea(EntityType<?> type, Level level) {
        super(type, level);
    }
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEIGHT_OFFSET, 1);
        builder.define(CLOSE_FLOOR, true);
        builder.define(MINING_MODE, MiningMode.TUNNEL.getIndex());
        builder.define(BRANCH_SPACING, 3);
        builder.define(BRANCH_LENGTH, 8);
        builder.define(DESCENT_STEP, 1);
        builder.define(CURRENT_SEGMENT, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.applyPatternSettings(readPatternSettings(tag));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writePatternSettings(tag, this.getPatternSettings());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isDone() && !this.isRemoved()) {
            if (!this.level().isClientSide() && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                BannerModSettlementRefreshSupport.refreshSnapshot(serverLevel, this.blockPosition());
            }
            this.remove(RemovalReason.DISCARDED);
        }
    }

    public Item getRenderItem()  {
        return Items.IRON_PICKAXE;
    }

    @Override
    public AABB createArea() {
        Direction facing = getFacing();
        int width = getWidthSize() - 1;
        int depth = getDepthSize() - 1;
        int height = getHeightSize();

        BlockPos start = this.getOnPos().offset(0, this.getHeightOffset(), 0);
        BlockPos end;
        switch (facing) {
            case NORTH -> end = start.offset(width, height, -depth);
            case SOUTH -> end = start.offset(-width, height, depth);
            case WEST  -> end = start.offset(-width, height, -depth);
            default  -> end = start.offset(width, height, depth);//EAST
        }
        return new AABB(Vec3.atLowerCornerOf(start), Vec3.atLowerCornerOf(end));
    }

    public int getHeightOffset() {
        return this.entityData.get(HEIGHT_OFFSET);
    }
    public void setHeightOffset(int offset) {
        this.entityData.set(HEIGHT_OFFSET, offset);
        area = this.createArea();
    }

    public boolean getCloseFloor() {
        return this.entityData.get(CLOSE_FLOOR);
    }
    public void setCloseFloor(boolean close) {
        this.entityData.set(CLOSE_FLOOR, close);
    }

    public MiningMode getMiningMode() {
        return MiningMode.fromIndex(this.entityData.get(MINING_MODE));
    }

    public void setMiningMode(MiningMode mode) {
        this.entityData.set(MINING_MODE, mode.getIndex());
    }

    public int getBranchSpacing() {
        return this.entityData.get(BRANCH_SPACING);
    }

    public void setBranchSpacing(int spacing) {
        this.entityData.set(BRANCH_SPACING, Math.max(1, spacing));
    }

    public int getBranchLength() {
        return this.entityData.get(BRANCH_LENGTH);
    }

    public void setBranchLength(int length) {
        this.entityData.set(BRANCH_LENGTH, Math.max(1, length));
    }

    public int getDescentStep() {
        return this.entityData.get(DESCENT_STEP);
    }

    public void setDescentStep(int step) {
        this.entityData.set(DESCENT_STEP, Math.max(1, step));
    }

    public int getCurrentSegment() {
        return this.entityData.get(CURRENT_SEGMENT);
    }

    public void setCurrentSegment(int segment) {
        this.entityData.set(CURRENT_SEGMENT, Math.max(0, segment));
    }

    public int getTotalSegments() {
        return MiningPatternContract.resolveTotalSegments(MiningPatternContract.fromMiningMode(this.getMiningMode()), this.getDepthSize());
    }

    public static int resolveTotalSegments(MiningMode mode, int depthSize) {
        return MiningPatternContract.resolveTotalSegments(MiningPatternContract.fromMiningMode(mode), depthSize);
    }

    public boolean hasRemainingPatternSegments() {
        return this.getCurrentSegment() < this.getTotalSegments();
    }

    public void resetPatternProgress() {
        this.setCurrentSegment(0);
    }

    public boolean advancePatternSegment() {
        if (this.getCurrentSegment() + 1 >= this.getTotalSegments()) {
            this.setCurrentSegment(this.getTotalSegments());
            return false;
        }
        this.setCurrentSegment(this.getCurrentSegment() + 1);
        return true;
    }

    public MiningPatternSettings getPatternSettings() {
        return new MiningPatternSettings(
                switch (this.getMiningMode()) {
                    case CUSTOM -> MiningPatternSettings.Mode.CUSTOM;
                    case MINE -> MiningPatternSettings.Mode.MINE;
                    case BRANCH -> MiningPatternSettings.Mode.BRANCH;
                    default -> MiningPatternSettings.Mode.TUNNEL;
                },
                this.getWidthSize(),
                this.getHeightSize(),
                this.getHeightOffset(),
                this.getCloseFloor(),
                this.getBranchSpacing(),
                this.getBranchLength(),
                this.getDescentStep()
        );
    }

    public void applyPatternSettings(MiningPatternSettings settings) {
        PatternApplication appliedSettings = projectPatternSettings(settings, this.getDepthSize());
        this.setWidthSize(appliedSettings.widthSize());
        this.setHeightSize(appliedSettings.heightSize());
        this.setDepthSize(appliedSettings.depthSize());
        this.setHeightOffset(appliedSettings.heightOffset());
        this.setCloseFloor(appliedSettings.closeFloor());
        this.setBranchSpacing(appliedSettings.branchSpacing());
        this.setBranchLength(appliedSettings.branchLength());
        this.setDescentStep(appliedSettings.descentStep());
        this.setMiningMode(appliedSettings.miningMode());
    }

    public static PatternApplication projectPatternSettings(MiningPatternSettings settings, int currentDepthSize) {
        MiningPatternContract.PatternApplication appliedSettings = MiningPatternContract.projectPatternSettings(settings, currentDepthSize);
        return new PatternApplication(
                appliedSettings.widthSize(),
                appliedSettings.heightSize(),
                appliedSettings.depthSize(),
                appliedSettings.heightOffset(),
                appliedSettings.closeFloor(),
                appliedSettings.branchSpacing(),
                appliedSettings.branchLength(),
                appliedSettings.descentStep(),
                switch (appliedSettings.miningMode()) {
                    case CUSTOM -> MiningMode.CUSTOM;
                    case MINE -> MiningMode.MINE;
                    case BRANCH -> MiningMode.BRANCH;
                    default -> MiningMode.TUNNEL;
                }
        );
    }

    public record PatternApplication(
            int widthSize,
            int heightSize,
            int depthSize,
            int heightOffset,
            boolean closeFloor,
            int branchSpacing,
            int branchLength,
            int descentStep,
            MiningMode miningMode
    ) {
    }

    public static void writePatternSettings(CompoundTag tag, MiningPatternSettings settings) {
        settings.writeToRoot(tag);
    }

    public static MiningPatternSettings readPatternSettings(CompoundTag tag) {
        return MiningPatternSettings.fromRoot(tag);
    }


    public void scanBreakArea() {
        if (this.getMiningMode() == MiningMode.TUNNEL || this.getMiningMode() == MiningMode.BRANCH) {
            this.scanPatternBreakArea();
            return;
        }
        if (area == null) area = this.getArea();
        stackToBreak.clear();

        Level level = this.getCommandSenderWorld();

        BlockPos.betweenClosedStream(area).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            FluidState fluidState = level.getFluidState(pos);
            if (pos.getY() != area.maxY && !state.isAir() && fluidState.isEmpty()) {
                if (!isAir(state) && !shouldIgnore(state) && this.allowsExcavation(pos)) {
                    stackToBreak.push(pos.immutable());
                }
            }
        });
    }

    public boolean shouldIgnore(BlockState state){
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if(id == null) return false;
        return WorkersServerConfig.MINER_IGNORE.contains(id.toString());
    }

    public void scanForOresOnWalls() {
        if (area == null) area = this.getArea();
        stackToBreak.clear();

        Level level = this.getCommandSenderWorld();

        getWallBlocks(area, 3).forEach(pos -> {
            BlockState state = level.getBlockState(pos);

            if (isOre(state) && hasAirNeighbor(pos, level) && this.allowsExcavation(pos)) {
                stackToBreak.push(pos.immutable());
            }
        });
    }

    public void scanFloorArea() {
        if (this.getMiningMode() == MiningMode.TUNNEL || this.getMiningMode() == MiningMode.BRANCH) {
            this.scanPatternFloorArea();
            return;
        }
        if (area == null) area = this.getArea();
        stackToPlace.clear();

        Level level = this.getCommandSenderWorld();

        getWallBlocks(area, 1).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            FluidState fluidState = level.getFluidState(pos);

            if(!fluidState.isEmpty()) {
                stackToPlace.push(pos.immutable());
            }

            if (pos.getY() == area.minY - 1 && (state.isAir() || !fluidState.isEmpty())) {
                stackToPlace.push(pos.immutable());
            }
        });

        BlockPos.betweenClosedStream(area).forEach(pos -> {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.isSource()) {
                stackToPlace.push(pos.immutable());
            }
        });
    }


    private Stream<BlockPos> getWallBlocks(AABB base, int inflate) {
        AABB expanded = base.inflate(inflate);

        return BlockPos.betweenClosedStream(expanded)
                .filter(pos -> !base.contains(Vec3.atCenterOf(pos)));
    }

    public boolean isWorkerPerfectCandidate(MinerEntity miner) {
        if (miner.getMatchingItem(stack -> stack.getItem() instanceof PickaxeItem) == ItemStack.EMPTY) {
            return false;
        }

        return true;
    }

    public boolean hasAirNeighbor(BlockPos pos, Level level){
        for(Direction direction: Direction.values()){
            BlockState state = level.getBlockState(pos.relative(direction,1));
            if(isAir(state)) return true;
        }
        return false;
    }

    public boolean isOre(BlockState state){
        return state.is(Tags.Blocks.ORES);
    }

    public boolean isAir(BlockState state){
        return state.isAir() || state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR);
    }

    public enum MiningMode {
        CUSTOM(0),
        MINE(1),
        TUNNEL(2),
        BRANCH(3);

        private final int index;
        MiningMode(int index){
            this.index = index;
        }
        public int getIndex(){
            return this.index;
        }

        public static MiningMode fromIndex(int index) {
            for (MiningMode messengerState : MiningMode.values()) {
                if (messengerState.getIndex() == index) {
                    return messengerState;
                }
            }
            throw new IllegalArgumentException("Invalid State index: " + index);
        }


    }

    private void scanPatternBreakArea() {
        stackToBreak.clear();
        if (!this.hasRemainingPatternSegments()) {
            return;
        }
        MiningPatternPlanner.SegmentPlan segmentPlan = this.getSegmentPlan(this.getCurrentSegment());
        Level level = this.getCommandSenderWorld();
        for (BlockPos pos : segmentPlan.breakTargets()) {
            BlockState state = level.getBlockState(pos);
            FluidState fluidState = level.getFluidState(pos);
            if (!state.isAir() && fluidState.isEmpty() && !shouldIgnore(state) && this.allowsExcavation(pos)) {
                stackToBreak.push(pos.immutable());
            }
        }
    }

    private boolean allowsExcavation(BlockPos pos) {
        BannerModSettlementBinding.Binding binding = BannerModSettlementBinding.resolveFactionStatus(
                ClaimEvents.recruitsClaimManager,
                pos,
                this.getTeamStringID()
        );
        return MiningClaimExcavationRules.allowsExcavation(binding.status());
    }

    private void scanPatternFloorArea() {
        stackToPlace.clear();
        if (!this.hasRemainingPatternSegments() || !this.getCloseFloor()) {
            return;
        }
        Level level = this.getCommandSenderWorld();
        for (BlockPos pos : this.getSegmentPlan(this.getCurrentSegment()).floorTargets()) {
            BlockState state = level.getBlockState(pos);
            FluidState fluidState = level.getFluidState(pos);
            if (state.isAir() || !fluidState.isEmpty()) {
                stackToPlace.push(pos.immutable());
            }
        }
    }

    private MiningPatternPlanner.SegmentPlan getSegmentPlan(int segmentIndex) {
        BlockPos origin = this.getOnPos();
        MiningPatternSettings settings = this.getPatternSettings();
        if (this.getMiningMode() == MiningMode.BRANCH) {
            return PATTERN_PLANNER.planBranchSegment(settings, origin, this.getFacing(), segmentIndex);
        }
        return PATTERN_PLANNER.planTunnelSegment(settings, origin, this.getFacing(), segmentIndex);
    }
}
