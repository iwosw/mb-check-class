package com.talhanation.bannermod.logistics;

import net.minecraft.world.Container;

import java.util.List;

@Deprecated(forRemoval = false)
public class BannerModCombinedContainer extends com.talhanation.bannerlord.shared.logistics.BannerModCombinedContainer {

    public BannerModCombinedContainer(List<Container> containers) {
        super(containers);
    }

    public static Container of(List<Container> containers) {
        return com.talhanation.bannerlord.shared.logistics.BannerModCombinedContainer.of(containers);
    }
}
