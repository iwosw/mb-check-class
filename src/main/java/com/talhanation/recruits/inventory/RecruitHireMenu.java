package com.talhanation.recruits.inventory;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.init.ModScreens;
import de.maxhenkel.corelib.inventory.ContainerBase;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class RecruitHireMenu extends ContainerBase {
    private final Player playerEntity;
    private final AbstractRecruitEntity recruit;
    private final com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity bannerlordRecruit;

    public RecruitHireMenu(int id, Player playerEntity, AbstractRecruitEntity recruit, Inventory playerInventory) {
        super(ModScreens.HIRE_CONTAINER_TYPE.get(), id, playerInventory, new SimpleContainer(0));
        this.playerEntity = playerEntity;
        this.recruit = recruit;
        this.bannerlordRecruit = null;
        this.playerInventory = playerInventory;

        addPlayerInventorySlots();
    }

    public RecruitHireMenu(int id, Player playerEntity, com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity recruit, Inventory playerInventory) {
        super(ModScreens.HIRE_CONTAINER_TYPE.get(), id, playerInventory, new SimpleContainer(0));
        this.playerEntity = playerEntity;
        this.recruit = null;
        this.bannerlordRecruit = recruit;
        this.playerInventory = playerInventory;

        addPlayerInventorySlots();
    }

    @Override
    public int getInvOffset() {
        return 56;
    }

    public AbstractRecruitEntity getRecruitEntity() {
        return recruit;
    }
}
