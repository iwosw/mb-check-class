package com.talhanation.bannerlord.shared.logistics;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BannerModCombinedContainer implements Container {
    private final List<Container> containers;
    private final int size;

    public BannerModCombinedContainer(List<Container> containers) {
        this.containers = containers == null ? List.of() : List.copyOf(containers);
        int totalSize = 0;
        for (Container container : this.containers) {
            totalSize += container.getContainerSize();
        }
        this.size = totalSize;
    }

    public static Container of(List<Container> containers) {
        List<Container> filtered = new ArrayList<>();
        if (containers != null) {
            for (Container container : containers) {
                if (container != null) {
                    filtered.add(container);
                }
            }
        }
        if (filtered.isEmpty()) {
            return null;
        }
        if (filtered.size() == 1) {
            return filtered.get(0);
        }
        return new BannerModCombinedContainer(filtered);
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (Container container : this.containers) {
            if (!container.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        SlotRef ref = this.resolve(slot);
        return ref == null ? ItemStack.EMPTY : ref.container().getItem(ref.slot());
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        SlotRef ref = this.resolve(slot);
        return ref == null ? ItemStack.EMPTY : ref.container().removeItem(ref.slot(), amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        SlotRef ref = this.resolve(slot);
        return ref == null ? ItemStack.EMPTY : ref.container().removeItemNoUpdate(ref.slot());
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        SlotRef ref = this.resolve(slot);
        if (ref != null) {
            ref.container().setItem(ref.slot(), stack);
        }
    }

    @Override
    public void setChanged() {
        for (Container container : this.containers) {
            container.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        for (Container container : this.containers) {
            if (!container.stillValid(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearContent() {
        for (Container container : this.containers) {
            container.clearContent();
        }
    }

    private SlotRef resolve(int slot) {
        if (slot < 0 || slot >= this.size) {
            return null;
        }
        int remaining = slot;
        for (Container container : this.containers) {
            int containerSize = container.getContainerSize();
            if (remaining < containerSize) {
                return new SlotRef(container, remaining);
            }
            remaining -= containerSize;
        }
        return null;
    }

    private record SlotRef(Container container, int slot) {
    }
}
