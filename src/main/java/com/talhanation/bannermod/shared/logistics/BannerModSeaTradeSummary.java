package com.talhanation.bannermod.shared.logistics;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure-logic aggregation of {@link BannerModSeaTradeEntrypoint}s into per-resource export /
 * import totals plus a bottleneck-reason list. Feeds two consumers (both follow-up slices):
 *
 * <ol>
 *   <li>Settlement production / consumption loop — reads {@code totalExportableCount(itemId)}
 *       and {@code totalImportableCount(itemId)} to decide what the settlement can produce
 *       beyond local supply or obtain beyond local demand.</li>
 *   <li>Settlement UI — reads {@link Summary#bottlenecks()} to explain why a particular
 *       trade good is currently not flowing (no port at all, only one direction wired,
 *       filter is "any" so not specific enough to allocate, etc.).</li>
 * </ol>
 *
 * <p>The summariser is pure: takes a {@code Collection} of entrypoints, returns a value
 * record. No level / world references, no {@code SavedData} writes. Tests pass deterministic
 * fakes, production passes the live entrypoint list from
 * {@code BannerModLogisticsRuntime.listSeaTradeEntrypoints(...)}.
 */
public final class BannerModSeaTradeSummary {

    /** Bottleneck token: no entrypoints at all — settlement has no sea-trade hookup. */
    public static final String BOTTLENECK_NO_PORT = "NO_PORT";

    /** Bottleneck token: there are entrypoints but only one direction is represented. */
    public static final String BOTTLENECK_ONLY_EXPORTS = "ONLY_EXPORTS";
    public static final String BOTTLENECK_ONLY_IMPORTS = "ONLY_IMPORTS";

    /** Bottleneck token: at least one entrypoint uses the "any item" filter (too generic). */
    public static final String BOTTLENECK_UNFILTERED_ROUTE = "UNFILTERED_ROUTE";

    private BannerModSeaTradeSummary() {
    }

    public static Summary summarise(Collection<BannerModSeaTradeEntrypoint> entrypoints) {
        Map<ResourceLocation, Integer> exportTotals = new LinkedHashMap<>();
        Map<ResourceLocation, Integer> importTotals = new LinkedHashMap<>();
        Set<String> bottlenecks = new LinkedHashSet<>();

        if (entrypoints == null || entrypoints.isEmpty()) {
            bottlenecks.add(BOTTLENECK_NO_PORT);
            return new Summary(exportTotals, importTotals, List.copyOf(bottlenecks));
        }

        boolean anyExport = false;
        boolean anyImport = false;
        for (BannerModSeaTradeEntrypoint entry : entrypoints) {
            if (entry == null) continue;
            BannerModSeaTradeDirection direction = entry.direction();
            if (direction == BannerModSeaTradeDirection.EXPORT) {
                anyExport = true;
                addFiltered(exportTotals, entry, bottlenecks);
            } else if (direction == BannerModSeaTradeDirection.IMPORT) {
                anyImport = true;
                addFiltered(importTotals, entry, bottlenecks);
            }
        }

        if (!anyExport && !anyImport) {
            bottlenecks.add(BOTTLENECK_NO_PORT);
        } else if (!anyExport) {
            bottlenecks.add(BOTTLENECK_ONLY_IMPORTS);
        } else if (!anyImport) {
            bottlenecks.add(BOTTLENECK_ONLY_EXPORTS);
        }

        return new Summary(exportTotals, importTotals, List.copyOf(bottlenecks));
    }

    /** Convenience: total exportable count for a specific resource. */
    public static int totalExportableCount(Summary summary, ResourceLocation itemId) {
        if (summary == null || itemId == null) return 0;
        Integer value = summary.exportableByItem().get(itemId);
        return value == null ? 0 : value;
    }

    /** Convenience: total importable count for a specific resource. */
    public static int totalImportableCount(Summary summary, ResourceLocation itemId) {
        if (summary == null || itemId == null) return 0;
        Integer value = summary.importableByItem().get(itemId);
        return value == null ? 0 : value;
    }

    private static void addFiltered(Map<ResourceLocation, Integer> totals,
                                    BannerModSeaTradeEntrypoint entry,
                                    Set<String> bottlenecks) {
        BannerModLogisticsItemFilter filter = entry.filter();
        int count = Math.max(0, entry.requestedCount());
        if (filter == null || filter.isAny()) {
            // Cannot allocate per-resource totals from an unrestricted filter; record the
            // bottleneck so a UI can flag the route as needing a tighter item filter.
            bottlenecks.add(BOTTLENECK_UNFILTERED_ROUTE);
            return;
        }
        for (ResourceLocation itemId : filter.itemIds()) {
            if (itemId == null) continue;
            totals.merge(itemId, count, Integer::sum);
        }
    }

    /**
     * Aggregated summary of a settlement's sea-trade entrypoints.
     *
     * @param exportableByItem item -> total export-able count across every EXPORT route that
     *                         names that item in its filter. Insertion-ordered for stable UI.
     * @param importableByItem same shape for IMPORT routes.
     * @param bottlenecks      ordered list of reason tokens explaining why some trade flow
     *                         is missing or fuzzy. Empty when the settlement has both
     *                         directions and every route is item-filtered.
     */
    public record Summary(Map<ResourceLocation, Integer> exportableByItem,
                          Map<ResourceLocation, Integer> importableByItem,
                          List<String> bottlenecks) {
        public Summary {
            exportableByItem = exportableByItem == null
                    ? Map.of() : Map.copyOf(exportableByItem);
            importableByItem = importableByItem == null
                    ? Map.of() : Map.copyOf(importableByItem);
            bottlenecks = bottlenecks == null ? List.of() : List.copyOf(bottlenecks);
        }
    }
}
