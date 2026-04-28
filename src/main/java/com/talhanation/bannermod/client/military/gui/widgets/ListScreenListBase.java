package com.talhanation.bannermod.client.military.gui.widgets;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public abstract class ListScreenListBase<T extends ListScreenEntryBase<T>> extends ContainerObjectSelectionList<T> {

    public ListScreenListBase(int width, int height, int x, int y, int itemHeight) {
        super(Minecraft.getInstance(), width, height, y, itemHeight);
        this.setX(x);
    }

    public void setListBounds(int width, int height, int x, int y) {
        this.setRectangle(width, height, x, y);
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}
