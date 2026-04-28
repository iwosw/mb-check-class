package com.talhanation.bannermod.client.military.gui.widgets;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public abstract class ListScreenListBase<T extends ListScreenEntryBase<T>> extends ContainerObjectSelectionList<T> {

    public ListScreenListBase(int width, int height, int x, int y, int size) {
        super(Minecraft.getInstance(), width, height, y, size);
        this.setX(x);
    }

}
