package com.talhanation.bannermod.inventory.civilian;

import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.registry.civilian.ModMenuTypes;
import de.maxhenkel.corelib.inventory.ContainerBase;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class CitizenProfileMenu extends ContainerBase {
    private final CitizenEntity citizen;
    private final Container citizenInventory;

    public CitizenProfileMenu(int id, CitizenEntity citizen, Inventory playerInventory) {
        super(ModMenuTypes.CITIZEN_PROFILE_CONTAINER_TYPE.get(), id, playerInventory, citizen.getInventory());
        this.citizen = citizen;
        this.citizenInventory = citizen.getInventory();
        addCitizenInventorySlots();
        addPlayerInventorySlots(playerInventory);
    }

    public CitizenEntity getCitizen() {
        return citizen;
    }

    @Override
    public boolean stillValid(Player player) {
        return citizen.isAlive() && player.distanceToSqr(citizen) < 64.0D;
    }

    private void addCitizenInventorySlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(citizenInventory, col + row * 9, 96 + col * 18, 116 + row * 18));
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 14 + col * 18, 174 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 14 + col * 18, 232));
        }
    }
}
