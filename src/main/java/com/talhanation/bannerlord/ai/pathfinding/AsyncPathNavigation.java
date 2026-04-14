package com.talhanation.bannerlord.ai.pathfinding;

import com.google.common.collect.ImmutableSet;
import com.talhanation.recruits.config.RecruitsServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AsyncPathNavigation extends PathNavigation {
    private static final int PATH_REUSE_REQUESTER_DISTANCE = 4;
    private static final int PATH_REUSE_TARGET_DISTANCE = 2;
    private static final int PATH_REUSE_MAX_AGE_TICKS = 20;
    @Nullable
    private BlockPos targetPos;
    private int reachRange;
    private final PathFinder pathFinder;
    private boolean isStuck;
    @Nullable
    private DeferredPathState deferredPathState;
    @Nullable
    private AsyncPathRequest activeAsyncPathRequest;
    private PathRequestOutcome lastPathRequestOutcome = PathRequestOutcome.EXECUTED;
    private long nextAsyncPathRequestId = 1L;

    public AsyncPathNavigation(PathfinderMob p_26515_, Level p_26516_) {
        super(p_26515_, p_26516_);
        int i = Mth.floor(p_26515_.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0D);
        this.pathFinder = this.createPathFinder(i);
    }

    protected @NotNull PathFinder createPathFinder(int p_26531_) {
        if (RecruitsServerConfig.UseAsyncPathfinding.get()) {
            return new AsyncPathfinder(this.nodeEvaluator, p_26531_, this.level);
        }
        return new PathFinder(this.nodeEvaluator, p_26531_);
    }

    @Nullable
    public final Path createPathAsync(double p_26525_, double p_26526_, double p_26527_, int p_26528_) {
        return this.createPath(new BlockPos((int) p_26525_, (int) p_26526_, (int) p_26527_), p_26528_);
    }

    @Nullable
    public Path createPath(Stream<BlockPos> p_26557_, int p_26558_) {
        return this.createPath(p_26557_.collect(Collectors.toSet()), 8, false, p_26558_);
    }

    
    @Nullable
    public Path createPath(Set<BlockPos> p_26549_, int p_26550_) {
        return this.createPath(p_26549_, 8, false, p_26550_);
    }

    
    @Nullable
    public Path createPath(BlockPos p_26546_, int p_26547_) {
        return this.createPath(p_26546_, p_26547_, null);
    }

    @Nullable
    protected Path createPath(BlockPos p_26546_, int p_26547_, @Nullable GlobalPathfindingController.FlowFieldPrototypeRequest flowFieldPrototypeRequest) {
        return this.createPath(ImmutableSet.of(p_26546_), 8, false, p_26547_, (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), flowFieldPrototypeRequest);
    }

    
    @Nullable
    public Path createPath(BlockPos p_148219_, int p_148220_, int p_148221_) {
        return this.createPath(ImmutableSet.of(p_148219_), 8, false, p_148220_, (float) p_148221_);
    }

    
    @Nullable
    public Path createPath(Entity p_26534_, int p_26535_) {
        BlockPos targetPos = p_26534_.blockPosition();
        GlobalPathfindingController.PathRequestResult<Path> result = GlobalPathfindingController.requestPath(
                new GlobalPathfindingController.PathRequest(
                        GlobalPathfindingController.RequestKind.ENTITY_TARGET,
                        RecruitsServerConfig.UseAsyncPathfinding.get(),
                        1,
                        this.level.getGameTime(),
                        new GlobalPathfindingController.ReuseContext(
                                this.mob.blockPosition(),
                                targetPos,
                                this.level.getGameTime(),
                                PATH_REUSE_REQUESTER_DISTANCE,
                                PATH_REUSE_TARGET_DISTANCE,
                                PATH_REUSE_MAX_AGE_TICKS
                        )
                ),
                this.deferredTicketFor(GlobalPathfindingController.RequestKind.ENTITY_TARGET),
                () -> this.doCreatePath(ImmutableSet.of(targetPos), 16, true, p_26535_, (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE))
        );
        return this.applyPathRequestResult(
                result,
                () -> DeferredPathState.forEntity(
                        Objects.requireNonNull(result.deferredTicket(), "deferredTicket"),
                        this.mob.blockPosition(),
                        p_26534_,
                        p_26535_,
                        this.speedModifier
                )
        );
    }


    @Nullable
    protected Path createPath(Set<BlockPos> p_26552_, int p_26553_, boolean p_26554_, int p_26555_) {
        return this.createPath(p_26552_, p_26553_, p_26554_, p_26555_, (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), null);
    }


    @Nullable
    protected Path createPath(Set<BlockPos> p_148223_, int p_148224_, boolean p_148225_, int p_148226_, float p_148227_) {
        return this.createPath(p_148223_, p_148224_, p_148225_, p_148226_, p_148227_, null);
    }


    @Nullable
    protected Path createPath(Set<BlockPos> p_148223_, int p_148224_, boolean p_148225_, int p_148226_, float p_148227_,
                              @Nullable GlobalPathfindingController.FlowFieldPrototypeRequest flowFieldPrototypeRequest) {
        BlockPos primaryTargetPos = p_148223_.isEmpty() ? this.mob.blockPosition() : p_148223_.iterator().next();
        GlobalPathfindingController.PathRequestResult<Path> result = GlobalPathfindingController.requestPath(
                new GlobalPathfindingController.PathRequest(
                        GlobalPathfindingController.RequestKind.BLOCK_TARGETS,
                        RecruitsServerConfig.UseAsyncPathfinding.get(),
                        p_148223_.size(),
                        this.level.getGameTime(),
                        new GlobalPathfindingController.ReuseContext(
                                this.mob.blockPosition(),
                                primaryTargetPos,
                                this.level.getGameTime(),
                                PATH_REUSE_REQUESTER_DISTANCE,
                                PATH_REUSE_TARGET_DISTANCE,
                                PATH_REUSE_MAX_AGE_TICKS
                        ),
                        flowFieldPrototypeRequest
                ),
                this.deferredTicketFor(GlobalPathfindingController.RequestKind.BLOCK_TARGETS),
                () -> this.doCreatePath(p_148223_, p_148224_, p_148225_, p_148226_, p_148227_)
        );
        return this.applyPathRequestResult(
                result,
                () -> DeferredPathState.forBlocks(
                        Objects.requireNonNull(result.deferredTicket(), "deferredTicket"),
                        this.mob.blockPosition(),
                        p_148223_,
                        p_148224_,
                        p_148225_,
                        p_148226_,
                        p_148227_,
                        this.speedModifier
                )
        );
    }

    @Nullable
    private GlobalPathfindingController.DeferredPathTicket deferredTicketFor(GlobalPathfindingController.RequestKind requestKind) {
        if (this.deferredPathState == null || this.deferredPathState.requestKind() != requestKind) {
            return null;
        }
        return this.deferredPathState.deferredTicket();
    }

    @Nullable
    private Path applyPathRequestResult(GlobalPathfindingController.PathRequestResult<Path> result,
                                        Supplier<DeferredPathState> deferredStateOnDeferral) {
        this.lastPathRequestOutcome = switch (result.status()) {
            case EXECUTED -> PathRequestOutcome.EXECUTED;
            case DEFERRED -> PathRequestOutcome.DEFERRED;
            case DROPPED -> PathRequestOutcome.DROPPED;
        };

        if (result.status() == GlobalPathfindingController.RequestStatus.EXECUTED) {
            this.deferredPathState = null;
            return result.result();
        }

        if (result.status() == GlobalPathfindingController.RequestStatus.DEFERRED) {
            this.replaceDeferredPathState(deferredStateOnDeferral.get());
            return null;
        }

        this.deferredPathState = null;
        return null;
    }

    private void replaceDeferredPathState(DeferredPathState nextDeferredPathState) {
        if (this.deferredPathState != null && this.deferredPathState.deferredTicket().id() != nextDeferredPathState.deferredTicket().id()) {
            GlobalPathfindingController.discardDeferred(
                    this.deferredPathState.deferredTicket(),
                    this.level.getGameTime(),
                    GlobalPathfindingController.DeferredDropReason.INVALIDATED
            );
        }
        this.deferredPathState = nextDeferredPathState;
    }

    private void replaceActiveAsyncPathRequest(AsyncPathRequest nextRequest) {
        AsyncPathRequest currentRequest = this.activeAsyncPathRequest;
        if (currentRequest != null && currentRequest.requestId() != nextRequest.requestId()) {
            this.invalidateAsyncPathRequest(currentRequest);
        }
        this.activeAsyncPathRequest = nextRequest;
    }

    private void clearActiveAsyncPathRequest() {
        AsyncPathRequest currentRequest = this.activeAsyncPathRequest;
        if (currentRequest != null) {
            this.invalidateAsyncPathRequest(currentRequest);
            this.activeAsyncPathRequest = null;
        }
    }

    private void invalidateAsyncPathRequest(AsyncPathRequest request) {
        request.path().invalidate();
        if (this.path == request.path() && !request.path().isProcessed()) {
            this.path = null;
        }
    }

    private boolean ownsAsyncPathRequest(AsyncPathRequest request) {
        return this.activeAsyncPathRequest != null
                && this.activeAsyncPathRequest.requestId() == request.requestId()
                && !request.path().isInvalidated();
    }

    private void onAsyncPathProcessed(AsyncPathRequest request, @Nullable Path processedPath) {
        if (processedPath == this.path) {
            this.acceptProcessedPath(processedPath, request.reachRange());
        }
    }

    private void acceptProcessedPath(@Nullable Path processedPath, int reachRange) {
        if (processedPath == null || processedPath.isDone()) {
            return;
        }
        this.targetPos = processedPath.getTarget();
        this.reachRange = reachRange;
        this.resetStuckTimeout();
    }

    private boolean consumeDeferredPathRequest() {
        boolean deferred = this.lastPathRequestOutcome == PathRequestOutcome.DEFERRED;
        this.lastPathRequestOutcome = PathRequestOutcome.EXECUTED;
        return deferred;
    }

    private void updateDeferredSpeed(double speedModifier) {
        if (this.deferredPathState != null) {
            this.deferredPathState = this.deferredPathState.withMoveSpeed(speedModifier);
        }
    }

    private void retryDeferredPathIfNeeded() {
        DeferredPathState deferredState = this.deferredPathState;
        if (deferredState == null) {
            return;
        }
        if (!deferredState.isStillValid(this.mob)) {
            GlobalPathfindingController.discardDeferred(
                    deferredState.deferredTicket(),
                    this.level.getGameTime(),
                    GlobalPathfindingController.DeferredDropReason.INVALIDATED
            );
            this.deferredPathState = null;
            this.lastPathRequestOutcome = PathRequestOutcome.DROPPED;
            return;
        }

        Path resumedPath = deferredState.requestKind() == GlobalPathfindingController.RequestKind.ENTITY_TARGET
                ? this.createPath(Objects.requireNonNull(deferredState.entityTarget(), "entityTarget"), deferredState.reachRange())
                : this.createPath(deferredState.blockTargets(), deferredState.regionOffset(), deferredState.offsetUp(), deferredState.reachRange(), deferredState.followRange());

        if (this.lastPathRequestOutcome == PathRequestOutcome.DEFERRED) {
            return;
        }

        if (resumedPath != null) {
            this.moveTo(resumedPath, deferredState.moveSpeed());
        }
    }

    @Nullable
    private Path doCreatePath(Set<BlockPos> p_148223_, int p_148224_, boolean p_148225_, int p_148226_, float p_148227_) {
        if(Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) return null;
        if (p_148223_.isEmpty()) {
            return null;
        } else if (this.mob.getY() < (double) this.level.getMinBuildHeight()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && (path instanceof AsyncPath asyncPath && !asyncPath.isProcessed() && asyncPath.hasSameProcessingPositions(p_148223_))) { // petal start - catch early if it's still processing these positions let it keep processing
            return this.path;
        } else if (this.path != null && !this.path.isDone() && p_148223_.contains(this.targetPos)) {
            return this.path;
        } else {
            BlockPos blockpos = p_148225_ ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int i = (int)(p_148227_ + (float)p_148224_);
            PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.level, blockpos.offset(-i, -i, -i), blockpos.offset(i, i, i));
            float maxVisitedNodesMultiplier = 1.0F;
            Path path = this.pathFinder.findPath(pathnavigationregion, this.mob, p_148223_, p_148227_, p_148226_, maxVisitedNodesMultiplier);

            if (!p_148223_.isEmpty())
                this.targetPos = p_148223_.iterator().next(); // petal - assign early a target position. most calls will only have 1 position

            if (path instanceof AsyncPath asyncPath) {
                AsyncPathRequest request = new AsyncPathRequest(this.nextAsyncPathRequestId++, asyncPath, p_148226_);
                this.replaceActiveAsyncPathRequest(request);
                AsyncPathProcessor.awaitProcessing(path, this.level.getServer(), () -> this.ownsAsyncPathRequest(request), processedPath -> this.onAsyncPathProcessed(request, processedPath));
            } else {
                this.acceptProcessedPath(path, p_148226_);
            }
            return path;
        }
    }

    @Override
    public boolean moveTo(double p_26520_, double p_26521_, double p_26522_, double p_26523_) {
        Path path = this.createPath(new BlockPos((int) p_26520_, (int) p_26521_, (int) p_26522_), 1);
        if (this.consumeDeferredPathRequest()) {
            this.updateDeferredSpeed(p_26523_);
            return true;
        }
        return this.moveTo(path, p_26523_);
    }

    // Paper start - optimise pathfinding
    private long pathfindFailures = 0;
    private long lastFailure = 0;
    // Paper end

    @Override
    public boolean moveTo(@NotNull Entity p_26532_, double p_26533_) {
        if(Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) return false;
        long currentTick = this.level.getGameTime();
        // Paper start - Pathfinding optimizations
        if (this.pathfindFailures > 10 && this.path == null && currentTick < this.lastFailure + 40) {
            return false;
        }
        // Paper end
        Path path = this.createPath(p_26532_, 1);
        if (this.consumeDeferredPathRequest()) {
            this.updateDeferredSpeed(p_26533_);
            return true;
        }
        // Paper start - Pathfinding optimizations
        if (path != null && this.moveTo(path, p_26533_)) {
            this.lastFailure = 0;
            this.pathfindFailures = 0;
            return true;
        } else {
            this.pathfindFailures++;
            this.lastFailure = currentTick;
            return false;
        }
        // Paper end
    }

    @Override
    public boolean moveTo(@Nullable Path p_26537_, double p_26538_) {
        if(Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) return false;
        if (p_26537_ == null) {
            this.clearActiveAsyncPathRequest();
            this.path = null;
            return false;
        }
        if (this.activeAsyncPathRequest != null && this.activeAsyncPathRequest.path() != p_26537_) {
            this.clearActiveAsyncPathRequest();
        }
        if (!p_26537_.sameAs(this.path)) {
            this.path = p_26537_;
        }

        if (this.isDone()) {
            return false;
        }

        boolean isProcessed = (this.path instanceof AsyncPath asyncPath && asyncPath.isProcessed()) || (!(this.path instanceof AsyncPath asyncPath));

        if (isProcessed) {
            if (this.activeAsyncPathRequest != null && this.activeAsyncPathRequest.path() == this.path) {
                this.acceptProcessedPath(this.path, this.activeAsyncPathRequest.reachRange());
            }
            this.trimPath();
            if(this.path.getNodeCount() <= 0) return false;
        }

        this.speedModifier = p_26538_;
        Vec3 vec3 = this.getTempMobPos();
        this.lastStuckCheck = this.tick;
        this.lastStuckCheckPos = vec3;

        return true;
    }


    protected void trimPath() {
        if (this.path == null) return;

        for (int i = 0; i < this.path.getNodeCount(); ++i) {
            Node node = this.path.getNode(i);
            Node node1 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
            BlockState blockstate = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));
            if (blockstate.is(BlockTags.CAULDRONS)) {
                this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
                if (node1 != null && node.y >= node1.y) {
                    this.path.replaceNode(i + 1, node.cloneAndMove(node1.x, node.y + 1, node1.z));
                }
            }
        }
    }

    @Override
    public void tick() {
        ++this.tick;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        this.retryDeferredPathIfNeeded();

        if (this.path instanceof AsyncPath asyncPath && !asyncPath.isProcessed()) return;

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vec3 vec3 = this.getTempMobPos();
                Vec3 vec31 = this.path.getNextEntityPos(this.mob);
                if (vec3.y > vec31.y && !this.mob.onGround() && Mth.floor(vec3.x) == Mth.floor(vec31.x) && Mth.floor(vec3.z) == Mth.floor(vec31.z)) {
                    this.path.advance();
                }
            }

            DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
            if (!this.isDone()) {
                Vec3 vec32 = this.path.getNextEntityPos(this.mob);
                this.mob.getMoveControl().setWantedPosition(vec32.x, this.getGroundY(vec32), vec32.z, this.speedModifier);
            }
        }
    }

    protected void followThePath() {
        if ((this.path instanceof AsyncPath asyncPath && !asyncPath.isProcessed())) return;
        Vec3 vec3 = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        Vec3i vec3i = this.path.getNextNodePos();
        double d0 = Math.abs(this.mob.getX() - ((double)vec3i.getX() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
        double d1 = Math.abs(this.mob.getY() - (double)vec3i.getY());
        double d2 = Math.abs(this.mob.getZ() - ((double)vec3i.getZ() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
        boolean flag = d0 <= (double)this.maxDistanceToWaypoint && d2 <= (double)this.maxDistanceToWaypoint && d1 < 1.0D; //Forge: Fix MC-94054
        if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
            this.path.advance();
        }

        this.doStuckDetection(vec3);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 p_26560_) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec3 vec3 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (!p_26560_.closerThan(vec3, 2.0D)) {
                return false;
            } else if (this.canMoveDirectly(p_26560_, this.path.getNextEntityPos(this.mob))) {
                return true;
            } else {
                Vec3 vec31 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3 vec32 = vec31.subtract(vec3);
                Vec3 vec33 = p_26560_.subtract(vec3);
                return vec32.dot(vec33) > 0.0D;
            }
        }
    }

    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
            if (this.targetPos != null) {
                Path recomputedPath = this.createPath(this.targetPos, this.reachRange);
                if (!this.consumeDeferredPathRequest()) {
                    this.path = recomputedPath;
                }
                this.timeLastRecompute = this.level.getGameTime();
                this.hasDelayedRecomputation = false;
            }
        } else {
            this.hasDelayedRecomputation = true;
        }
    }

    private void resetStuckTimeout() {
        this.timeoutCachedNode = Vec3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0D;
        this.isStuck = false;
    }

    @Override
    public boolean isStuck() {
        return this.isStuck;
    }

    private enum PathRequestOutcome {
        EXECUTED,
        DEFERRED,
        DROPPED
    }

    private record DeferredPathState(
            GlobalPathfindingController.DeferredPathTicket deferredTicket,
            GlobalPathfindingController.RequestKind requestKind,
            BlockPos requesterPos,
            @Nullable Entity entityTarget,
            Set<BlockPos> blockTargets,
            int regionOffset,
            boolean offsetUp,
            int reachRange,
            float followRange,
            double moveSpeed
    ) {
        private static DeferredPathState forEntity(GlobalPathfindingController.DeferredPathTicket deferredTicket, BlockPos requesterPos,
                                                   Entity entityTarget, int reachRange, double moveSpeed) {
            return new DeferredPathState(
                    deferredTicket,
                    GlobalPathfindingController.RequestKind.ENTITY_TARGET,
                    requesterPos,
                    entityTarget,
                    Set.of(),
                    16,
                    true,
                    reachRange,
                    0.0F,
                    moveSpeed
            );
        }

        private static DeferredPathState forBlocks(GlobalPathfindingController.DeferredPathTicket deferredTicket, BlockPos requesterPos,
                                                   Set<BlockPos> blockTargets, int regionOffset, boolean offsetUp,
                                                   int reachRange, float followRange, double moveSpeed) {
            return new DeferredPathState(
                    deferredTicket,
                    GlobalPathfindingController.RequestKind.BLOCK_TARGETS,
                    requesterPos,
                    null,
                    Set.copyOf(new LinkedHashSet<>(blockTargets)),
                    regionOffset,
                    offsetUp,
                    reachRange,
                    followRange,
                    moveSpeed
            );
        }

        private DeferredPathState withMoveSpeed(double moveSpeed) {
            return new DeferredPathState(
                    deferredTicket,
                    requestKind,
                    requesterPos,
                    entityTarget,
                    blockTargets,
                    regionOffset,
                    offsetUp,
                    reachRange,
                    followRange,
                    moveSpeed
            );
        }

        private boolean isStillValid(Entity mob) {
            if (!mob.blockPosition().closerThan(this.requesterPos, PATH_REUSE_REQUESTER_DISTANCE + 1.0D)) {
                return false;
            }
            if (this.requestKind == GlobalPathfindingController.RequestKind.ENTITY_TARGET) {
                return this.entityTarget != null && this.entityTarget.isAlive() && !this.entityTarget.isRemoved();
            }
            return !this.blockTargets.isEmpty();
        }
    }

    private record AsyncPathRequest(long requestId, AsyncPath path, int reachRange) {
    }
}
