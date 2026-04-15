package com.talhanation.bannermod.logistics;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @deprecated Use {@link com.talhanation.bannermod.shared.logistics.BannerModCombinedContainer} instead.
 * Forwarder retained for staged migration per Phase 21 D-05 -- legacy shared-package overlap is
 * documented in MERGE_NOTES.md and is intentionally NOT deduplicated during Phase 21.
 *
 * <p>This thin subclass inherits all behavior from the canonical class, so existing callers that
 * still name this type get identical semantics. Do not add new members here.
 */
@Deprecated
public class BannerModCombinedContainer extends com.talhanation.bannermod.shared.logistics.BannerModCombinedContainer {

    public BannerModCombinedContainer(List<Container> containers) {
        super(containers);
    }

    public static Container of(List<Container> containers) {
        return com.talhanation.bannermod.shared.logistics.BannerModCombinedContainer.of(containers);
    }

    // Inherited behavior from the canonical class covers: getContainerSize, isEmpty, getItem,
    // removeItem, removeItemNoUpdate, setItem, setChanged, stillValid, clearContent.
    // No additional overrides needed.
    @Override
    public int getContainerSize() {
        return super.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return super.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return super.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return super.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player);
    }

    @Override
    public void clearContent() {
        super.clearContent();
    }
}
