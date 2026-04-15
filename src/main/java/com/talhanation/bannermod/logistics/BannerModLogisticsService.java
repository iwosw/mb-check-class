package com.talhanation.bannermod.logistics;

import com.talhanation.bannerlord.entity.civilian.AbstractWorkerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BannerModLogisticsService {
    private static final BannerModLogisticsService SHARED = new BannerModLogisticsService();

    private final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();

    private BannerModLogisticsService() {
    }

    public static BannerModLogisticsService shared() {
        return SHARED;
    }

    public Optional<Reservation> reserveStack(UUID endpointId,
                                              UUID workerId,
                                              ItemStack stack,
                                              int requestedCount,
                                              long gameTime) {
        if (endpointId == null || workerId == null || stack == null || stack.isEmpty() || requestedCount <= 0) {
            return Optional.empty();
        }
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        Reservation reservation = new Reservation(UUID.randomUUID(), endpointId, workerId, itemId, Math.min(requestedCount, stack.getCount()), gameTime, false);
        this.reservations.put(reservation.reservationId(), reservation);
        return Optional.of(reservation);
    }

    public void release(UUID reservationId) {
        if (reservationId != null) {
            this.reservations.remove(reservationId);
        }
    }

    public void markInTransit(UUID reservationId, long gameTime) {
        Reservation reservation = this.reservations.get(reservationId);
        if (reservation != null) {
            this.reservations.put(reservationId, reservation.withInTransit(true, gameTime));
        }
    }

    public Optional<BannerModCourierTask> selectBestTask(ServerLevel level, AbstractWorkerEntity worker) {
        return Optional.empty();
    }

    public Optional<String> selectBlockedReason(ServerLevel level, AbstractWorkerEntity worker) {
        return Optional.empty();
    }

    public record ItemStock(String itemId, int count) {
    }

    public record Reservation(UUID reservationId,
                              UUID endpointId,
                              UUID workerId,
                              String itemId,
                              int count,
                              long gameTime,
                              boolean inTransit) {
        private Reservation withInTransit(boolean nextInTransit, long nextGameTime) {
            return new Reservation(this.reservationId, this.endpointId, this.workerId, this.itemId, this.count, nextGameTime, nextInTransit);
        }
    }
}
