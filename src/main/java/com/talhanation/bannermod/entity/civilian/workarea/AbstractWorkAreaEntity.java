package com.talhanation.bannermod.entity.civilian.workarea;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementRefreshSupport;
import com.talhanation.bannermod.network.messages.civilian.MessageToClientOpenWorkAreaScreen;
import com.talhanation.bannermod.network.messages.civilian.WorkAreaAuthoringRules;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import net.minecraft.client.gui.screens.Screen;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.talhanation.bannermod.network.compat.BannerModPacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractWorkAreaEntity extends Entity {
    public static final EntityDataAccessor<String> PLAYER_NAME = SynchedEntityData.defineId(AbstractWorkAreaEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = SynchedEntityData.defineId(AbstractWorkAreaEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<Integer> WIDTH = SynchedEntityData.defineId(AbstractWorkAreaEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DEPTH = SynchedEntityData.defineId(AbstractWorkAreaEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> HEIGHT = SynchedEntityData.defineId(AbstractWorkAreaEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> TEAM_STRING_ID = SynchedEntityData.defineId(AbstractWorkAreaEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Direction> FACING = SynchedEntityData.defineId(AbstractWorkAreaEntity.class, EntityDataSerializers.DIRECTION);
    public boolean isDone;
    public boolean isBeingWorkedOn;
    public static int DONE_TIME =  20*60;
    public boolean showBox;
    public AABB area;
    public AbstractWorkAreaEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.createArea();
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(PLAYER_NAME, "");
        builder.define(PLAYER_UUID, Optional.empty());
        builder.define(WIDTH, 0);
        builder.define(HEIGHT, 0);
        builder.define(DEPTH, 0);
        builder.define(FACING, Direction.SOUTH);
        builder.define(TEAM_STRING_ID, "");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.isDone = tag.getBoolean("isDone");
        this.time = tag.getInt("time");
        this.isBeingWorkedOn = false;
        this.setPlayerUUID(tag.getUUID("playerUUID"));
        this.setWidthSize(tag.getInt("width"));
        this.setHeightSize(tag.getInt("height"));
        this.setDepthSize(tag.getInt("depth"));
        this.setFacing(Direction.from3DDataValue(tag.getInt("facing")));
        if(tag.contains("teamStringID")){
            this.setTeamStringID(tag.getString("teamStringID"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putUUID("playerUUID", getPlayerUUID());
        tag.putBoolean("isDone", isDone);
        tag.putInt("width", getWidthSize());
        tag.putInt("height", getHeightSize());
        tag.putInt("depth", getDepthSize());
        tag.putInt("facing", this.getFacing().get3DDataValue());
        if(!this.getTeamStringID().isEmpty()){
            tag.putString("teamStringID", getTeamStringID());
        }
        tag.putInt("time", time);
    }

    public int time;

    /**
     * Periodicity at which the work-area falls back to the full {@link Entity#baseTick()} as
     * a safety net for lifecycle hooks we don't explicitly handle (chunk tracking edge
     * cases, portal logic if anyone ever stands a work-area in a portal, etc.). Everything
     * in between runs the cheap path only.
     */
    private static final int SAFETY_BASE_TICK_EVERY = 100;

    /**
     * <p>Performance override — work areas are static, invulnerable, weightless server-side
     * markers. For the common 500–1000 work-areas-per-world case, running
     * {@link Entity#baseTick()} every tick (fire/water/lava/walkDist/fluid push/portal
     * tracking/fall damage/inventory/etc.) costs real MSPT even though none of it can
     * apply to us.</p>
     *
     * <p>Cheap path: bump {@code tickCount}, clear {@code firstTick}, advance our own
     * {@code time} counter every 20 ticks. Every {@value #SAFETY_BASE_TICK_EVERY} ticks
     * we still call {@link Entity#baseTick()} as a safety net so chunk-tracking and any
     * other lifecycle invariant we haven't thought of catches up.</p>
     *
     * <p>If this override breaks a rare lifecycle case, lower
     * {@link #SAFETY_BASE_TICK_EVERY} before disabling the override.</p>
     */
    @Override
    public void tick() {
        if (this.tickCount % SAFETY_BASE_TICK_EVERY == 0) {
            super.tick();
        } else {
            this.tickCount++;
            this.firstTick = false;
        }
        if (this.tickCount % 20 == 0) {
            this.time++;
        }
    }
    @Override
    public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand) {
        if (this.getCommandSenderWorld().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        WorkAreaAuthoringRules.Decision decision = WorkAreaAuthoringRules.inspectDecision(true, this.getAuthoringAccess(player));
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            String messageKey = WorkAreaAuthoringRules.getMessageKey(decision);
            if (messageKey != null) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(messageKey));
            }
            return InteractionResult.SUCCESS;
        }

        BannerModMain.SIMPLE_CHANNEL.send(BannerModPacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new MessageToClientOpenWorkAreaScreen(this.getId(), this.getUUID()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float a) {
        if(damageSource.getEntity() instanceof Player player){
            if(player.isCreative() && player.isCrouching() && player.hasPermissions(2)){
                BlockPos discardedAt = this.blockPosition();
                Level level = this.level();
                this.discard();
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel && discardedAt != null) {
                    BannerModSettlementRefreshSupport.refreshSnapshot(serverLevel, discardedAt);
                }
            }
        }

        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean canRiderInteract() {
        return false;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    public boolean canPlayerSee(Player player){
        return this.canPlayerModify(player);
    }

    public boolean canPlayerModify(Player player) {
        return this.getAuthoringAccess(player) != WorkAreaAuthoringRules.AccessLevel.FORBIDDEN;
    }

    public WorkAreaAuthoringRules.AccessLevel getAuthoringAccess(Player player) {
        return WorkAreaAuthoringRules.resolveAccess(this.isOwnedBy(player), this.isSameTeamMember(player), this.isAdminAuthor(player));
    }

    public boolean isOwnedBy(Player player) {
        UUID ownerUuid = this.getPlayerUUID();
        return ownerUuid != null && ownerUuid.equals(player.getUUID());
    }

    public boolean isSameTeamMember(Player player) {
        return player.getTeam() != null && player.getTeam().getName().equals(this.getTeamStringID());
    }

    public boolean isAdminAuthor(Player player) {
        return player.isCreative() && player.hasPermissions(2);
    }

    @Override
    public boolean isEffectiveAi() {
        return false;
    }

    public boolean canWorkHere(AbstractWorkerEntity worker) {
        if (!worker.isOwned()) {
            return false;
        }

        UUID ownerUuid = worker.getOwnerUUID();
        boolean ownerMatch = ownerUuid != null && ownerUuid.equals(this.getPlayerUUID());
        boolean sameTeam = worker.getTeam() != null && this.getTeamStringID() != null && this.getTeamStringID().equals(worker.getTeam().getName());
        if (!ownerMatch && !sameTeam) {
            return false;
        }

        String settlementFactionId = resolveSettlementPoliticalEntityId(this.getTeamStringID());
        if (settlementFactionId == null || settlementFactionId.isBlank()) {
            return true;
        }

        BannerModSettlementBinding.Binding binding = BannerModSettlementBinding.resolveSettlementStatus(
                ClaimEvents.recruitsClaimManager,
                this.blockPosition(),
                settlementFactionId
        );
        return BannerModSettlementBinding.allowsSettlementOperation(binding);
    }

    private String resolveSettlementPoliticalEntityId(String teamStringId) {
        if (teamStringId == null || teamStringId.isBlank() || !(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return teamStringId;
        }
        return WarRuntimeContext.registry(serverLevel)
                .byName(teamStringId)
                .map(PoliticalEntityRecord::id)
                .map(UUID::toString)
                .orElse(teamStringId);
    }

    public void setDone(boolean b) {
        this.isDone = b;
    }

    public boolean isDone(){
        return this.isDone;
    }

    public void setBeingWorkedOn(boolean b) {
        this.isBeingWorkedOn = b;
    }

    public int getTime(){
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public AABB getArea() {
        return createArea();
    }

    public AABB createArea() {
        Direction facing = getFacing();
        int width = getWidthSize() - 1;
        int depth = getDepthSize() - 1;
        int height = getHeightSize();

        BlockPos start = this.getOnPos();
        BlockPos end;
        switch (facing) {
            case NORTH -> end = start.offset(width, height, -depth);
            case SOUTH -> end = start.offset(-width, height, depth);
            case EAST  -> end = start.offset(depth, height, width);   // depth entlang +X, width entlang +Z
            default    -> end = start.offset(-depth, height, -width); // WEST: depth entlang -X, width entlang -Z
        }
        return new AABB(Vec3.atLowerCornerOf(start), Vec3.atLowerCornerOf(end));
    }
    public static List<AbstractWorkAreaEntity> getNearbyAreas(Level level, BlockPos center, int radius) {
        List<AbstractWorkAreaEntity> nearby = new ArrayList<>();

        AABB queryBox = new AABB(center).inflate(radius);
        double queryRadius = halfDiagonal(queryBox);
        List<AbstractWorkAreaEntity> scannedAreas = WorkAreaIndex.instance().queryInRange(
                level,
                queryBox.getCenter(),
                queryRadius,
                AbstractWorkAreaEntity.class
        );
        RuntimeProfilingCounters.add("work_area.nearby_seen", scannedAreas.size());
        for (AbstractWorkAreaEntity area : scannedAreas) {
            if (!area.getOnPos().equals(center)) {
                nearby.add(area);
            }
        }

        return nearby;
    }

    public static boolean isAreaOverlapping(Level level, AbstractWorkAreaEntity currentArea, AABB targetBox) {
        AABB queryBox = targetBox.inflate(64);
        List<AbstractWorkAreaEntity> scannedAreas = WorkAreaIndex.instance().queryInRange(
                level,
                queryBox.getCenter(),
                halfDiagonal(queryBox),
                AbstractWorkAreaEntity.class
        );
        RuntimeProfilingCounters.add("work_area.overlap_seen", scannedAreas.size());
        for (AbstractWorkAreaEntity other : scannedAreas) {
            if (other == currentArea) continue;
            if (other instanceof BuildArea) continue;  // BuildAreas are excluded
            if (other.getArea().intersects(targetBox)) return true;
        }
        return false;
    }

    private static double halfDiagonal(AABB box) {
        double x = box.getXsize();
        double y = box.getYsize();
        double z = box.getZsize();
        return Math.sqrt(x * x + y * y + z * z) / 2.0D;
    }

    public boolean isBeingWorkedOn(){
        return this.isBeingWorkedOn;
    }
    public void setWidthSize(int size) {
        this.entityData.set(WIDTH, size);
        this.area = null;
    }
    public void setHeightSize(int height) {
        this.entityData.set(HEIGHT, height);
        this.area = null;
    }
    public void setDepthSize(int size) {
        this.entityData.set(DEPTH, size);
        this.area = null;
    }

    public void setFacing(Direction direction) {
        this.entityData.set(FACING, direction);
        this.area = null;
    }

    public void setPlayerName(String playerName) {
        this.entityData.set(PLAYER_NAME, playerName);
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.entityData.set(PLAYER_UUID, Optional.of(playerUUID));
    }

    public void setTeamStringID(String teamStringID) {
        this.entityData.set(TEAM_STRING_ID, teamStringID);
    }

    public int getHeightSize() {
        return this.entityData.get(HEIGHT);
    }

    public int getWidthSize() {
        return this.entityData.get(WIDTH);
    }
    public int getDepthSize() {
        return this.entityData.get(DEPTH);
    }

    public Direction getFacing() {
        return this.entityData.get(FACING);
    }

    public String getTeamStringID(){
        return this.entityData.get(TEAM_STRING_ID);
    }

    public String getPlayerName(){
        return this.entityData.get(PLAYER_NAME);
    }

    public UUID getPlayerUUID(){
        return this.entityData.get(PLAYER_UUID).orElse(null);
    }

    public abstract Item getRenderItem();
    @OnlyIn(Dist.CLIENT)
    public abstract Screen getScreen(Player player);

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        super.moveTo(x, y, z, yRot, xRot);
        this.area = null;
    }

    public BlockPos getOriginPos() {
        return this.getOnPos();
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }
}
