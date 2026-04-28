package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.shared.logistics.BannerModLogisticsItemFilter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stateless transport-payload helpers used by {@link SettlementOrderWorkGoal} when executing
 * {@code FETCH_INPUT} and {@code HAUL_RESOURCE} settlement work orders.
 *
 * <p>Container interactions are implemented against the vanilla {@link Container} interface so
 * the same code services chests, barrels, dispensers, hoppers, and the synthetic
 * {@code SimpleContainer} backing worker inventories. Tests exercise these methods with
 * {@code SimpleContainer} instances without spinning up a full level.</p>
 */
public final class TransportContainerExchange {

    private TransportContainerExchange() {
    }

    /**
     * Build a stack predicate from the comma-separated resource-location hint stored on a
     * transport order. Blank hints match every non-empty stack.
     */
    public static BannerModLogisticsItemFilter filterFromResourceHint(@Nullable String resourceHintId) {
        if (resourceHintId == null) {
            return BannerModLogisticsItemFilter.any();
        }
        String trimmed = resourceHintId.trim();
        if (trimmed.isEmpty()) {
            return BannerModLogisticsItemFilter.any();
        }
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (String token : trimmed.split(",")) {
            String tok = token.trim();
            if (tok.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(tok);
            if (id != null) {
                ids.add(id);
            }
        }
        if (ids.isEmpty()) {
            return BannerModLogisticsItemFilter.any();
        }
        return BannerModLogisticsItemFilter.ofItemIds(ids);
    }

    /**
     * Move up to {@code budget} matching items from {@code source} into {@code carrier}.
     * Returns the actual count moved. Items that cannot fit in the carrier are returned to
     * their original slot in the source container.
     */
    public static int withdrawInto(Container source,
                                   Container carrier,
                                   BannerModLogisticsItemFilter filter,
                                   int budget) {
        if (source == null || carrier == null || budget <= 0) {
            return 0;
        }
        int moved = 0;
        for (int slot = 0; slot < source.getContainerSize() && moved < budget; slot++) {
            ItemStack stack = source.getItem(slot);
            if (stack.isEmpty() || !filter.matches(stack)) {
                continue;
            }
            int take = Math.min(stack.getCount(), budget - moved);
            ItemStack split = stack.split(take);
            ItemStack leftover = pushIntoContainer(carrier, split);
            int returned = leftover.getCount();
            if (returned > 0) {
                stack.grow(returned);
            }
            int actualMoved = take - returned;
            if (actualMoved > 0) {
                moved += actualMoved;
                source.setChanged();
            }
            if (returned > 0) {
                // Carrier can no longer accept this stack type; further slots will not help either.
                break;
            }
        }
        return moved;
    }

    /**
     * Move every matching stack from {@code carrier} into {@code destination}. Stacks that do
     * not fit fully are returned to the carrier slot. Returns the count successfully moved.
     */
    public static int depositInto(Container destination,
                                  Container carrier,
                                  BannerModLogisticsItemFilter filter) {
        if (destination == null || carrier == null) {
            return 0;
        }
        int moved = 0;
        for (int slot = 0; slot < carrier.getContainerSize(); slot++) {
            ItemStack stack = carrier.getItem(slot);
            if (stack.isEmpty() || !filter.matches(stack)) {
                continue;
            }
            int before = stack.getCount();
            ItemStack remaining = pushIntoContainer(destination, stack);
            int delta = before - remaining.getCount();
            if (delta > 0) {
                moved += delta;
                carrier.setChanged();
            }
            carrier.setItem(slot, remaining);
            if (!remaining.isEmpty()) {
                // Destination container is at capacity for this stack type; bail out.
                break;
            }
        }
        return moved;
    }

    /**
     * Push {@code stack} into {@code container}, merging into matching stacks first and then
     * filling empty slots. Returns the unplaced remainder (empty when fully accepted).
     */
    public static ItemStack pushIntoContainer(Container container, ItemStack stack) {
        if (container == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        int slotCap = container.getMaxStackSize();
        for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack target = container.getItem(i);
            if (target.isEmpty() || !ItemStack.isSameItemSameComponents(target, stack)) {
                continue;
            }
            int max = Math.min(target.getMaxStackSize(), slotCap);
            int free = max - target.getCount();
            if (free <= 0) {
                continue;
            }
            int take = Math.min(free, stack.getCount());
            target.grow(take);
            stack.shrink(take);
            container.setChanged();
        }
        for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            if (!container.getItem(i).isEmpty()) {
                continue;
            }
            int max = Math.min(stack.getMaxStackSize(), slotCap);
            int take = Math.min(max, stack.getCount());
            container.setItem(i, stack.split(take));
            container.setChanged();
        }
        return stack;
    }
}
