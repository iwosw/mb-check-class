package com.talhanation.bannermod.shared.logistics;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public record BannerModLogisticsAuthoringState(@Nullable UUID destinationStorageAreaId,
                                               String filterText,
                                               int requestedCount,
                                               BannerModLogisticsPriority priority) {

    public BannerModLogisticsAuthoringState {
        Objects.requireNonNull(filterText, "filterText");
        Objects.requireNonNull(priority, "priority");
        if (requestedCount <= 0) {
            throw new IllegalArgumentException("Route requested count must be greater than 0.");
        }
    }

    public static BannerModLogisticsAuthoringState parse(String destinationText,
                                                         String filterText,
                                                         String requestedCountText,
                                                         String priorityText) {
        UUID destinationStorageAreaId = parseDestination(destinationText);
        String canonicalFilterText = canonicalizeFilterText(filterText);
        int requestedCount = parseRequestedCount(requestedCountText);
        BannerModLogisticsPriority priority = parsePriority(priorityText);
        return new BannerModLogisticsAuthoringState(destinationStorageAreaId, canonicalFilterText, requestedCount, priority);
    }

    public BannerModLogisticsRoute toRoute(UUID sourceStorageAreaId) {
        Objects.requireNonNull(sourceStorageAreaId, "sourceStorageAreaId");
        if (this.destinationStorageAreaId == null) {
            throw new IllegalStateException("Cannot create a route without a destination storage area.");
        }
        return new BannerModLogisticsRoute(
                UUID.nameUUIDFromBytes((sourceStorageAreaId.toString() + "->" + this.destinationStorageAreaId + ":" + this.filterText + ":" + this.requestedCount + ":" + this.priority.name()).getBytes()),
                new BannerModLogisticsNodeRef(sourceStorageAreaId),
                new BannerModLogisticsNodeRef(this.destinationStorageAreaId),
                this.toFilter(),
                this.requestedCount,
                this.priority
        );
    }

    public BannerModLogisticsItemFilter toFilter() {
        if (this.filterText.isBlank()) {
            return BannerModLogisticsItemFilter.any();
        }
        List<ResourceLocation> itemIds = new ArrayList<>();
        for (String token : this.filterText.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            itemIds.add(ResourceLocation.parse(trimmed));
        }
        return BannerModLogisticsItemFilter.ofItemIds(itemIds);
    }

    public String destinationText() {
        return this.destinationStorageAreaId == null ? "" : this.destinationStorageAreaId.toString();
    }

    public String requestedCountText() {
        return Integer.toString(this.requestedCount);
    }

    public String priorityText() {
        return this.priority.name();
    }

    @Nullable
    private static UUID parseDestination(String destinationText) {
        String trimmed = destinationText == null ? "" : destinationText.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(trimmed);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Route destination must be a valid storage-area UUID.");
        }
    }

    private static int parseRequestedCount(String requestedCountText) {
        String trimmed = requestedCountText == null ? "" : requestedCountText.trim();
        if (trimmed.isEmpty()) {
            return 16;
        }
        try {
            int requestedCount = Integer.parseInt(trimmed);
            if (requestedCount <= 0) {
                throw new IllegalArgumentException("Route requested count must be greater than 0.");
            }
            return requestedCount;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Route requested count must be a whole number.");
        }
    }

    private static BannerModLogisticsPriority parsePriority(String priorityText) {
        String normalized = priorityText == null ? "" : priorityText.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return BannerModLogisticsPriority.NORMAL;
        }
        try {
            return BannerModLogisticsPriority.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Route priority must be HIGH, NORMAL, or LOW.");
        }
    }

    private static String canonicalizeFilterText(String filterText) {
        String normalized = filterText == null ? "" : filterText.trim();
        if (normalized.isEmpty()) {
            return "";
        }
        return List.of(normalized.split(","))
                .stream()
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .map(token -> {
                    ResourceLocation itemId = ResourceLocation.tryParse(token);
                    if (itemId == null) {
                        throw new IllegalArgumentException("Route filter must be a comma-separated list of item ids.");
                    }
                    return itemId.toString();
                })
                .distinct()
                .collect(Collectors.joining(","));
    }
}
