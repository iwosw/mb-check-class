package com.talhanation.bannermod.shared.logistics;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class BannerModLogisticsService {
    private static final Comparator<BannerModLogisticsRoute> ROUTE_ORDER =
            Comparator.comparingInt((BannerModLogisticsRoute route) -> route.priority().sortOrder())
                    .thenComparing(BannerModLogisticsRoute::routeId);
    private static final Comparator<BannerModSeaTradeEntrypoint> SEA_ENTRYPOINT_ORDER =
            Comparator.comparingInt((BannerModSeaTradeEntrypoint entrypoint) -> entrypoint.priority().sortOrder())
                    .thenComparing(BannerModSeaTradeEntrypoint::routeId);

    private final Map<UUID, BannerModLogisticsReservation> reservationsById = new HashMap<>();
    private final Map<UUID, UUID> reservationIdsByWorker = new HashMap<>();
    private final Map<UUID, UUID> reservationIdsByRoute = new HashMap<>();

    public Optional<BannerModCourierTask> claimNextTask(UUID workerId,
                                                        Collection<BannerModLogisticsRoute> routes,
                                                        Predicate<BannerModLogisticsRoute> routeEligible,
                                                        long gameTime,
                                                        long reservationDuration) {
        Objects.requireNonNull(workerId, "workerId");
        Objects.requireNonNull(routes, "routes");
        Objects.requireNonNull(routeEligible, "routeEligible");
        if (reservationDuration <= 0L) {
            throw new IllegalArgumentException("reservationDuration must be > 0");
        }

        cleanupExpiredReservations(gameTime);

        BannerModLogisticsReservation existing = getReservationForWorker(workerId);
        if (existing != null) {
            BannerModLogisticsRoute existingRoute = findRoute(routes, existing.routeId());
            if (existingRoute != null) {
                return Optional.of(new BannerModCourierTask(existingRoute, existing));
            }
            releaseReservation(existing.reservationId());
        }

        return routes.stream()
                .filter(Objects::nonNull)
                .filter(routeEligible)
                .filter(route -> !this.reservationIdsByRoute.containsKey(route.routeId()))
                .sorted(ROUTE_ORDER)
                .findFirst()
                .map(route -> createReservation(workerId, route, gameTime + reservationDuration));
    }

    public int cleanupExpiredReservations(long gameTime) {
        int removed = 0;
        for (BannerModLogisticsReservation reservation : this.reservationsById.values().toArray(BannerModLogisticsReservation[]::new)) {
            if (!reservation.isExpired(gameTime)) {
                continue;
            }
            if (releaseReservation(reservation.reservationId()) != null) {
                removed++;
            }
        }
        return removed;
    }

    @Nullable
    public BannerModLogisticsReservation getReservation(UUID reservationId) {
        return this.reservationsById.get(reservationId);
    }

    @Nullable
    public BannerModLogisticsReservation getReservationForWorker(UUID workerId) {
        UUID reservationId = this.reservationIdsByWorker.get(workerId);
        return reservationId == null ? null : this.reservationsById.get(reservationId);
    }

    @Nullable
    public BannerModLogisticsReservation releaseReservation(UUID reservationId) {
        BannerModLogisticsReservation removed = this.reservationsById.remove(reservationId);
        if (removed == null) {
            return null;
        }
        this.reservationIdsByWorker.remove(removed.workerId());
        this.reservationIdsByRoute.remove(removed.routeId());
        return removed;
    }

    public int releaseReservationsForWorker(UUID workerId) {
        BannerModLogisticsReservation existing = getReservationForWorker(workerId);
        if (existing == null) {
            return 0;
        }
        releaseReservation(existing.reservationId());
        return 1;
    }

    public List<BannerModSeaTradeEntrypoint> listSeaTradeEntrypoints(Collection<BannerModLogisticsRoute> routes,
                                                                     Predicate<UUID> isPortStorageArea) {
        return this.listSeaTradeEntrypoints(routes, storageAreaId -> true, isPortStorageArea);
    }

    public List<BannerModSeaTradeEntrypoint> listSeaTradeEntrypoints(Collection<BannerModLogisticsRoute> routes,
                                                                     Predicate<UUID> storageAreaAvailable,
                                                                     Predicate<UUID> isPortStorageArea) {
        Objects.requireNonNull(routes, "routes");
        Objects.requireNonNull(storageAreaAvailable, "storageAreaAvailable");
        Objects.requireNonNull(isPortStorageArea, "isPortStorageArea");

        return routes.stream()
                .filter(Objects::nonNull)
                .filter(route -> storageAreaAvailable.test(route.source().storageAreaId()))
                .filter(route -> storageAreaAvailable.test(route.destination().storageAreaId()))
                .map(route -> toSeaTradeEntrypoint(route, isPortStorageArea))
                .flatMap(Optional::stream)
                .sorted(SEA_ENTRYPOINT_ORDER)
                .toList();
    }

    private BannerModCourierTask createReservation(UUID workerId,
                                                   BannerModLogisticsRoute route,
                                                   long expiresAtGameTime) {
        BannerModLogisticsReservation reservation = new BannerModLogisticsReservation(
                UUID.randomUUID(),
                route.routeId(),
                workerId,
                route.filter(),
                route.requestedCount(),
                expiresAtGameTime
        );
        this.reservationsById.put(reservation.reservationId(), reservation);
        this.reservationIdsByWorker.put(workerId, reservation.reservationId());
        this.reservationIdsByRoute.put(route.routeId(), reservation.reservationId());
        return new BannerModCourierTask(route, reservation);
    }

    @Nullable
    private static BannerModLogisticsRoute findRoute(Collection<BannerModLogisticsRoute> routes, UUID routeId) {
        for (BannerModLogisticsRoute route : routes) {
            if (route != null && route.routeId().equals(routeId)) {
                return route;
            }
        }
        return null;
    }

    private static Optional<BannerModSeaTradeEntrypoint> toSeaTradeEntrypoint(BannerModLogisticsRoute route,
                                                                               Predicate<UUID> isPortStorageArea) {
        UUID sourceId = route.source().storageAreaId();
        UUID destinationId = route.destination().storageAreaId();
        boolean sourceIsPort = isPortStorageArea.test(sourceId);
        boolean destinationIsPort = isPortStorageArea.test(destinationId);
        if (sourceIsPort == destinationIsPort) {
            return Optional.empty();
        }

        return Optional.of(new BannerModSeaTradeEntrypoint(
                route.routeId(),
                sourceIsPort ? sourceId : destinationId,
                sourceIsPort ? destinationId : sourceId,
                sourceIsPort ? BannerModSeaTradeDirection.IMPORT : BannerModSeaTradeDirection.EXPORT,
                route.filter(),
                route.requestedCount(),
                route.priority()
        ));
    }
}
