package com.talhanation.bannermod.settlement.workorder;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * In-memory registry of {@link SettlementWorkOrder} instances.
 *
 * <p>One runtime exists per server level. Orders are keyed by {@code orderUuid} and indexed
 * secondarily by building UUID and resident UUID. The runtime is not thread-safe and is
 * expected to be driven by the server tick loop.</p>
 *
 * <p>Completed and cancelled orders are removed immediately. Claims that expire (because
 * the claimant never reported completion) are returned to {@code PENDING} by
 * {@link #reclaimAbandoned(long)}.</p>
 */
public final class SettlementWorkOrderRuntime {

    /** Default claim expiry window (ticks) when callers pass 0. Roughly 40 seconds. */
    public static final long DEFAULT_CLAIM_EXPIRY_TICKS = 20L * 40L;

    private final Map<UUID, SettlementWorkOrder> ordersByUuid = new LinkedHashMap<>();
    private final Map<UUID, List<UUID>> byBuilding = new HashMap<>();
    private final Map<UUID, List<UUID>> byClaim = new HashMap<>();
    private final Map<UUID, UUID> orderByResident = new HashMap<>();

    /** Publish a new pending order. Rejects duplicates matching (building, type, targetPos). */
    public Optional<SettlementWorkOrder> publish(SettlementWorkOrder order) {
        Objects.requireNonNull(order, "order");
        if (order.status() != SettlementWorkOrderStatus.PENDING) {
            throw new IllegalArgumentException("publish requires PENDING order, got " + order.status());
        }
        if (findDuplicate(order).isPresent()) {
            return Optional.empty();
        }
        insertInternal(order);
        return Optional.of(order);
    }

    private Optional<SettlementWorkOrder> findDuplicate(SettlementWorkOrder order) {
        for (UUID existingUuid : byBuilding.getOrDefault(order.buildingUuid(), List.of())) {
            SettlementWorkOrder existing = ordersByUuid.get(existingUuid);
            if (existing == null) {
                continue;
            }
            if (existing.type() != order.type()) {
                continue;
            }
            if (Objects.equals(existing.targetPos(), order.targetPos())) {
                return Optional.of(existing);
            }
        }
        return Optional.empty();
    }

    /**
     * Claim the highest-priority pending order matching {@code filter} for {@code residentUuid}.
     * The resident may only hold one active claim — an existing claim is returned unchanged
     * if it still matches.
     */
    public Optional<SettlementWorkOrder> claim(UUID claimUuid,
                                               UUID residentUuid,
                                               @Nullable Predicate<SettlementWorkOrder> filter,
                                               long gameTime,
                                               long expiryTicks) {
        Objects.requireNonNull(claimUuid, "claimUuid");
        Objects.requireNonNull(residentUuid, "residentUuid");
        UUID existingOrderUuid = orderByResident.get(residentUuid);
        if (existingOrderUuid != null) {
            SettlementWorkOrder existing = ordersByUuid.get(existingOrderUuid);
            if (existing != null && existing.status() == SettlementWorkOrderStatus.CLAIMED) {
                return Optional.of(existing);
            }
        }

        long expiry = gameTime + (expiryTicks > 0 ? expiryTicks : DEFAULT_CLAIM_EXPIRY_TICKS);
        List<UUID> claimOrders = byClaim.getOrDefault(claimUuid, List.of());
        List<SettlementWorkOrder> candidates = new ArrayList<>();
        for (UUID orderUuid : claimOrders) {
            SettlementWorkOrder order = ordersByUuid.get(orderUuid);
            if (order == null || order.status() != SettlementWorkOrderStatus.PENDING) {
                continue;
            }
            if (filter != null && !filter.test(order)) {
                continue;
            }
            candidates.add(order);
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        candidates.sort(Comparator
                .comparingInt(SettlementWorkOrder::priority).reversed()
                .thenComparingLong(SettlementWorkOrder::createdGameTime));

        SettlementWorkOrder picked = candidates.get(0);
        SettlementWorkOrder claimed = picked.withStatus(SettlementWorkOrderStatus.CLAIMED, residentUuid, expiry);
        replaceInternal(claimed);
        orderByResident.put(residentUuid, claimed.orderUuid());
        return Optional.of(claimed);
    }

    /**
     * Claim the highest-priority pending order tied to a specific building. Behaves like
     * {@link #claim(UUID, UUID, Predicate, long, long)} but scopes the search to one building.
     */
    public Optional<SettlementWorkOrder> claimForBuilding(UUID buildingUuid,
                                                          UUID residentUuid,
                                                          @Nullable Predicate<SettlementWorkOrder> filter,
                                                          long gameTime,
                                                          long expiryTicks) {
        Objects.requireNonNull(buildingUuid, "buildingUuid");
        Objects.requireNonNull(residentUuid, "residentUuid");
        UUID existingOrderUuid = orderByResident.get(residentUuid);
        if (existingOrderUuid != null) {
            SettlementWorkOrder existing = ordersByUuid.get(existingOrderUuid);
            if (existing != null && existing.status() == SettlementWorkOrderStatus.CLAIMED) {
                return Optional.of(existing);
            }
        }

        long expiry = gameTime + (expiryTicks > 0 ? expiryTicks : DEFAULT_CLAIM_EXPIRY_TICKS);
        List<UUID> buildingOrders = byBuilding.getOrDefault(buildingUuid, List.of());
        List<SettlementWorkOrder> candidates = new ArrayList<>();
        for (UUID orderUuid : buildingOrders) {
            SettlementWorkOrder order = ordersByUuid.get(orderUuid);
            if (order == null || order.status() != SettlementWorkOrderStatus.PENDING) {
                continue;
            }
            if (filter != null && !filter.test(order)) {
                continue;
            }
            candidates.add(order);
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        candidates.sort(Comparator
                .comparingInt(SettlementWorkOrder::priority).reversed()
                .thenComparingLong(SettlementWorkOrder::createdGameTime));

        SettlementWorkOrder picked = candidates.get(0);
        SettlementWorkOrder claimed = picked.withStatus(SettlementWorkOrderStatus.CLAIMED, residentUuid, expiry);
        replaceInternal(claimed);
        orderByResident.put(residentUuid, claimed.orderUuid());
        return Optional.of(claimed);
    }

    /** Release a claimed order back to PENDING. */
    public Optional<SettlementWorkOrder> release(UUID orderUuid) {
        Objects.requireNonNull(orderUuid, "orderUuid");
        SettlementWorkOrder existing = ordersByUuid.get(orderUuid);
        if (existing == null || existing.status() != SettlementWorkOrderStatus.CLAIMED) {
            return Optional.empty();
        }
        SettlementWorkOrder released = existing.withStatus(SettlementWorkOrderStatus.PENDING, null, 0L);
        replaceInternal(released);
        if (existing.claimedByResidentUuid() != null) {
            orderByResident.remove(existing.claimedByResidentUuid());
        }
        return Optional.of(released);
    }

    /** Mark an order as completed and remove it from the runtime. */
    public Optional<SettlementWorkOrder> complete(UUID orderUuid) {
        SettlementWorkOrder existing = ordersByUuid.get(orderUuid);
        if (existing == null) {
            return Optional.empty();
        }
        SettlementWorkOrder completed = existing.withStatus(
                SettlementWorkOrderStatus.COMPLETED, existing.claimedByResidentUuid(), 0L);
        removeInternal(completed);
        return Optional.of(completed);
    }

    /** Cancel and remove an order regardless of current state. */
    public Optional<SettlementWorkOrder> cancel(UUID orderUuid) {
        SettlementWorkOrder existing = ordersByUuid.get(orderUuid);
        if (existing == null) {
            return Optional.empty();
        }
        SettlementWorkOrder cancelled = existing.withStatus(
                SettlementWorkOrderStatus.CANCELLED, null, 0L);
        removeInternal(cancelled);
        return Optional.of(cancelled);
    }

    /** Return expired CLAIMED orders to PENDING. Returns the count reclaimed. */
    public int reclaimAbandoned(long gameTime) {
        List<UUID> toRelease = new ArrayList<>();
        for (SettlementWorkOrder order : ordersByUuid.values()) {
            if (order.status() == SettlementWorkOrderStatus.CLAIMED
                    && order.claimExpiryGameTime() > 0
                    && gameTime >= order.claimExpiryGameTime()) {
                toRelease.add(order.orderUuid());
            }
        }
        for (UUID uuid : toRelease) {
            release(uuid);
        }
        return toRelease.size();
    }

    /** Remove every order belonging to a settlement claim (e.g. claim disbanded). */
    public int purgeClaim(UUID claimUuid) {
        List<UUID> toRemove = new ArrayList<>(byClaim.getOrDefault(claimUuid, List.of()));
        for (UUID uuid : toRemove) {
            SettlementWorkOrder existing = ordersByUuid.get(uuid);
            if (existing != null) {
                removeInternal(existing);
            }
        }
        return toRemove.size();
    }

    /** Remove every order belonging to a specific building. */
    public int purgeBuilding(UUID buildingUuid) {
        List<UUID> toRemove = new ArrayList<>(byBuilding.getOrDefault(buildingUuid, List.of()));
        for (UUID uuid : toRemove) {
            SettlementWorkOrder existing = ordersByUuid.get(uuid);
            if (existing != null) {
                removeInternal(existing);
            }
        }
        return toRemove.size();
    }

    public Optional<SettlementWorkOrder> find(UUID orderUuid) {
        if (orderUuid == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ordersByUuid.get(orderUuid));
    }

    public Optional<SettlementWorkOrder> currentClaim(UUID residentUuid) {
        if (residentUuid == null) {
            return Optional.empty();
        }
        UUID orderUuid = orderByResident.get(residentUuid);
        if (orderUuid == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ordersByUuid.get(orderUuid));
    }

    public List<SettlementWorkOrder> pendingFor(UUID claimUuid) {
        return filterStatus(byClaim.getOrDefault(claimUuid, List.of()), SettlementWorkOrderStatus.PENDING);
    }

    public List<SettlementWorkOrder> ordersForBuilding(UUID buildingUuid) {
        List<UUID> ids = byBuilding.getOrDefault(buildingUuid, List.of());
        List<SettlementWorkOrder> out = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            SettlementWorkOrder existing = ordersByUuid.get(id);
            if (existing != null) {
                out.add(existing);
            }
        }
        return Collections.unmodifiableList(out);
    }

    public Collection<SettlementWorkOrder> all() {
        return Collections.unmodifiableCollection(ordersByUuid.values());
    }

    public int size() {
        return ordersByUuid.size();
    }

    public int countForClaim(UUID claimUuid, SettlementWorkOrderStatus status) {
        return filterStatus(byClaim.getOrDefault(claimUuid, List.of()), status).size();
    }

    /** Find a pending order matching (building, type, targetPos). */
    public Optional<SettlementWorkOrder> findPendingAt(UUID buildingUuid,
                                                       SettlementWorkOrderType type,
                                                       @Nullable BlockPos targetPos) {
        for (UUID uuid : byBuilding.getOrDefault(buildingUuid, List.of())) {
            SettlementWorkOrder existing = ordersByUuid.get(uuid);
            if (existing == null || existing.status() != SettlementWorkOrderStatus.PENDING) {
                continue;
            }
            if (existing.type() == type && Objects.equals(existing.targetPos(), targetPos)) {
                return Optional.of(existing);
            }
        }
        return Optional.empty();
    }

    private List<SettlementWorkOrder> filterStatus(List<UUID> uuids, SettlementWorkOrderStatus status) {
        List<SettlementWorkOrder> out = new ArrayList<>();
        for (UUID uuid : uuids) {
            SettlementWorkOrder existing = ordersByUuid.get(uuid);
            if (existing != null && existing.status() == status) {
                out.add(existing);
            }
        }
        return Collections.unmodifiableList(out);
    }

    private void insertInternal(SettlementWorkOrder order) {
        ordersByUuid.put(order.orderUuid(), order);
        byBuilding.computeIfAbsent(order.buildingUuid(), ignored -> new ArrayList<>()).add(order.orderUuid());
        byClaim.computeIfAbsent(order.claimUuid(), ignored -> new ArrayList<>()).add(order.orderUuid());
    }

    private void replaceInternal(SettlementWorkOrder order) {
        ordersByUuid.put(order.orderUuid(), order);
    }

    private void removeInternal(SettlementWorkOrder order) {
        ordersByUuid.remove(order.orderUuid());
        List<UUID> buildingList = byBuilding.get(order.buildingUuid());
        if (buildingList != null) {
            buildingList.remove(order.orderUuid());
            if (buildingList.isEmpty()) {
                byBuilding.remove(order.buildingUuid());
            }
        }
        List<UUID> claimList = byClaim.get(order.claimUuid());
        if (claimList != null) {
            claimList.remove(order.orderUuid());
            if (claimList.isEmpty()) {
                byClaim.remove(order.claimUuid());
            }
        }
        if (order.claimedByResidentUuid() != null) {
            UUID currentForResident = orderByResident.get(order.claimedByResidentUuid());
            if (order.orderUuid().equals(currentForResident)) {
                orderByResident.remove(order.claimedByResidentUuid());
            }
        }
    }
}
