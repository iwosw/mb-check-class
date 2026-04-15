package com.talhanation.bannermod.logistics;

import com.talhanation.bannerlord.entity.civilian.AbstractWorkerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Deprecated(forRemoval = false)
public final class BannerModLogisticsService {
    private static final BannerModLogisticsService SHARED = new BannerModLogisticsService();

    private final com.talhanation.bannerlord.shared.logistics.BannerModLogisticsService delegate;

    private BannerModLogisticsService() {
        this.delegate = com.talhanation.bannerlord.shared.logistics.BannerModLogisticsService.shared();
    }

    public static BannerModLogisticsService shared() {
        return SHARED;
    }

    public Optional<Reservation> reserveStack(UUID endpointId,
                                              UUID workerId,
                                              ItemStack stack,
                                              int requestedCount,
                                              long gameTime) {
        return this.delegate.reserveStack(endpointId, workerId, stack, requestedCount, gameTime).map(Reservation::fromShared);
    }

    public void release(UUID reservationId) {
        this.delegate.release(reservationId);
    }

    public void markInTransit(UUID reservationId, long gameTime) {
        this.delegate.markInTransit(reservationId, gameTime);
    }

    public Optional<BannerModCourierTask> selectBestTask(ServerLevel level, AbstractWorkerEntity worker) {
        return this.delegate.selectBestTask(level, worker).map(BannerModCourierTask::fromShared);
    }

    public Optional<String> selectBlockedReason(ServerLevel level, AbstractWorkerEntity worker) {
        return this.delegate.selectBlockedReason(level, worker);
    }

    public record ItemStock(String itemId, int count) {
        static ItemStock fromShared(com.talhanation.bannerlord.shared.logistics.BannerModLogisticsService.ItemStock stock) {
            return new ItemStock(stock.itemId(), stock.count());
        }
    }

    public record Reservation(UUID reservationId,
                              UUID endpointId,
                              UUID workerId,
                              String itemId,
                              int count,
                              long gameTime,
                              boolean inTransit) {
        static Reservation fromShared(com.talhanation.bannerlord.shared.logistics.BannerModLogisticsService.Reservation reservation) {
            return new Reservation(reservation.reservationId(), reservation.endpointId(), reservation.workerId(), reservation.itemId(), reservation.count(), reservation.gameTime(), reservation.inTransit());
        }
    }
}
