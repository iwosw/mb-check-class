package com.talhanation.bannermod.shared.logistics;

import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class BannerModLogisticsRuntime {

    private static BannerModLogisticsService service = new BannerModLogisticsService();

    private BannerModLogisticsRuntime() {
    }

    public static BannerModLogisticsService service() {
        return service;
    }

    public static List<BannerModSeaTradeEntrypoint> listSeaTradeEntrypoints(Collection<StorageArea> storageAreas) {
        Objects.requireNonNull(storageAreas, "storageAreas");

        Map<UUID, StorageArea> storageAreasById = storageAreas.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(StorageArea::getUUID, storageArea -> storageArea, (left, right) -> left));

        return service.listSeaTradeEntrypoints(
                storageAreasById.values().stream()
                        .map(StorageArea::getAuthoredLogisticsRoute)
                        .flatMap(java.util.Optional::stream)
                        .toList(),
                storageAreasById::containsKey,
                storageAreaId -> {
                    StorageArea storageArea = storageAreasById.get(storageAreaId);
                    return storageArea != null && storageArea.isPortEntrypoint();
                }
        );
    }

    public static void resetForTests() {
        service = new BannerModLogisticsService();
    }
}
