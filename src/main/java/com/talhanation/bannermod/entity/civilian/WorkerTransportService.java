package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import com.talhanation.bannermod.shared.logistics.BannerModCourierTask;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.UUID;

final class WorkerTransportService {
    private static final double MIN_SUPPORTED_ROUTE_DISTANCE_SQR = 16.0D * 16.0D;
    private static final double MOUNT_SEARCH_RADIUS = 12.0D;
    private static final double MOUNT_NAVIGATION_SPEED = 1.35D;
    private static final int REMOUNT_COOLDOWN_TICKS = 40;

    private final AbstractWorkerEntity worker;
    private int remountCooldown;

    WorkerTransportService(AbstractWorkerEntity worker) {
        this.worker = worker;
    }

    void tick() {
        if (!(this.worker.level() instanceof ServerLevel level)) {
            return;
        }

        BannerModCourierTask task = this.worker.getActiveCourierTask();
        if (!isSupportedCourierRoute(level, task)) {
            this.releaseTransport();
            return;
        }

        if (this.worker.isPassenger()) {
            Entity vehicle = this.worker.getVehicle();
            if (isApprovedTransport(vehicle) && isOwnerCompatible(vehicle)) {
                this.driveMountedTransport(level, vehicle);
                return;
            }
            this.releaseTransport();
        }

        if (this.remountCooldown > 0) {
            this.remountCooldown--;
            return;
        }

        Entity transport = findAvailableTransport(level);
        if (transport == null) {
            return;
        }

        this.worker.startRiding(transport);
        if (this.worker.isPassenger()) {
            this.driveMountedTransport(level, transport);
        }
        if (!this.worker.isPassenger()) {
            this.remountCooldown = REMOUNT_COOLDOWN_TICKS;
        }
    }

    void releaseTransport() {
        if (this.worker.isPassenger()) {
            this.worker.stopRiding();
        }
    }

    Component inspectionMessage() {
        if (!(this.worker.level() instanceof ServerLevel level)) {
            return Component.translatable("chat.bannermod.workers.transport.unavailable");
        }

        BannerModCourierTask task = this.worker.getActiveCourierTask();
        if (task == null) {
            return Component.translatable("chat.bannermod.workers.transport.inactive");
        }

        String unsupportedReason = unsupportedCourierRouteReason(level, task);
        if (unsupportedReason != null) {
            return Component.translatable(unsupportedReason);
        }

        Entity vehicle = this.worker.getVehicle();
        if (this.worker.isPassenger() && isApprovedTransport(vehicle) && isOwnerCompatible(vehicle)) {
            return Component.translatable("chat.bannermod.workers.transport.mounted", vehicle.getDisplayName());
        }
        if (this.worker.isPassenger()) {
            return Component.translatable("chat.bannermod.workers.transport.fallback_lost", vehicle == null ? Component.translatable("chat.bannermod.workers.transport.unknown") : vehicle.getDisplayName());
        }
        if (this.remountCooldown > 0) {
            return Component.translatable("chat.bannermod.workers.transport.fallback_remounting");
        }
        return Component.translatable("chat.bannermod.workers.transport.fallback_no_mount");
    }

    private boolean isSupportedCourierRoute(ServerLevel level, @Nullable BannerModCourierTask task) {
        return unsupportedCourierRouteReason(level, task) == null;
    }

    @Nullable
    private String unsupportedCourierRouteReason(ServerLevel level, @Nullable BannerModCourierTask task) {
        if (task == null) {
            return "chat.bannermod.workers.transport.inactive";
        }

        StorageArea source = resolveStorage(level, task.route().source().storageAreaId());
        StorageArea destination = resolveStorage(level, task.route().destination().storageAreaId());
        if (source == null || destination == null || source.isRemoved() || destination.isRemoved()) {
            return "chat.bannermod.workers.transport.fallback_storage_missing";
        }

        Vec3 sourcePos = source.position();
        Vec3 destinationPos = destination.position();
        if (sourcePos.subtract(destinationPos).horizontalDistanceSqr() < MIN_SUPPORTED_ROUTE_DISTANCE_SQR) {
            return "chat.bannermod.workers.transport.fallback_short_route";
        }
        return null;
    }

    @Nullable
    private StorageArea resolveStorage(ServerLevel level, UUID storageAreaId) {
        Entity entity = level.getEntity(storageAreaId);
        return entity instanceof StorageArea storageArea ? storageArea : null;
    }

    @Nullable
    private Entity findAvailableTransport(ServerLevel level) {
        return level.getEntitiesOfClass(Entity.class,
                        this.worker.getBoundingBox().inflate(MOUNT_SEARCH_RADIUS),
                        entity -> entity != this.worker
                                && entity.isAlive()
                                && !entity.isVehicle()
                                && entity instanceof Mob
                                && isApprovedTransport(entity)
                                && isOwnerCompatible(entity))
                .stream()
                .min(Comparator.comparingDouble(this.worker::distanceToSqr))
                .orElse(null);
    }

    private boolean isApprovedTransport(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        return entity instanceof AbstractHorse || RecruitsServerConfig.MountWhiteList.get().contains(entity.getEncodeId());
    }

    private void driveMountedTransport(ServerLevel level, @Nullable Entity vehicle) {
        if (!(vehicle instanceof Mob transport)) {
            return;
        }

        UUID targetStorageId = this.worker.getActiveCourierTargetStorageAreaId();
        if (targetStorageId == null) {
            return;
        }

        StorageArea targetStorage = resolveStorage(level, targetStorageId);
        if (targetStorage == null || targetStorage.isRemoved()) {
            return;
        }

        Vec3 target = targetStorage.position();
        transport.getNavigation().moveTo(target.x(), target.y(), target.z(), MOUNT_NAVIGATION_SPEED);
    }

    private boolean isOwnerCompatible(Entity entity) {
        UUID workerOwner = this.worker.getOwnerUUID();
        if (entity instanceof AbstractHorse horse) {
            UUID horseOwner = horse.getOwnerUUID();
            return horseOwner == null || horseOwner.equals(workerOwner);
        }
        if (entity instanceof LivingEntity livingEntity && livingEntity.getLastHurtByMob() != null) {
            return false;
        }
        return true;
    }
}
