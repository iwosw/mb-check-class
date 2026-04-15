package com.talhanation.bannermod.inventory.military;

import com.talhanation.bannermod.entity.military.AbstractLeaderEntity;
import com.talhanation.bannermod.registry.military.ModScreens;
import de.maxhenkel.corelib.inventory.ContainerBase;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;

public class PatrolLeaderContainer extends ContainerBase {
    private final Player playerEntity;
    private final AbstractLeaderEntity recruit;

    public PatrolLeaderContainer(int id, Player playerEntity, AbstractLeaderEntity leader) {
        super(ModScreens.PATROL_LEADER.get(), id, playerEntity.getInventory(), new SimpleContainer(0));
        this.playerEntity = playerEntity;
        this.recruit = leader;
    }

    public Player getPlayerEntity() {
        return playerEntity;
    }

    public AbstractLeaderEntity getRecruit() {
        return recruit;
    }
}
