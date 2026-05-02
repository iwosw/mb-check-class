package com.talhanation.bannermod.inventory.military;

import com.talhanation.bannermod.registry.military.ModScreens;
import de.maxhenkel.corelib.inventory.ContainerBase;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;

public class AdminRecruitSpawnMenu extends ContainerBase {
    public AdminRecruitSpawnMenu(int id, Player player) {
        super(ModScreens.ADMIN_RECRUIT_SPAWN_CONTAINER_TYPE.get(), id, null, new SimpleContainer(0));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
