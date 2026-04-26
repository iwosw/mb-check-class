package com.talhanation.bannermod.entity.citizen;

import com.talhanation.bannermod.citizen.CitizenCore;
import com.talhanation.bannermod.citizen.CitizenRole;
import com.talhanation.bannermod.citizen.CitizenRoleController;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.entity.military.AbstractInventoryEntity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Canonical base for any controllable citizen unit.
 *
 * <p>Concrete subclasses (legacy recruit/worker wrappers and future single citizen
 * runtime variants) provide concrete {@link CitizenCore} backing storage and role
 * identity. Shared role-controller plumbing and militia conversion policy live here.
 */
public abstract class AbstractCitizenEntity extends AbstractInventoryEntity {

    private CitizenRoleController citizenRoleController = CitizenRoleController.noop(CitizenRole.CIVILIAN_RESIDENT);

    protected AbstractCitizenEntity(EntityType<? extends AbstractInventoryEntity> entityType, Level world) {
        super(entityType, world);
    }

    public abstract CitizenCore getCitizenCore();

    public abstract CitizenRole getCitizenRole();

    public CitizenRoleController getCitizenRoleController() {
        return this.citizenRoleController;
    }

    public void setCitizenRoleController(CitizenRoleController controller) {
        this.citizenRoleController = controller == null
                ? CitizenRoleController.noop(this.getCitizenRole())
                : controller;
    }

    public boolean isMilitiaRole() {
        return this.getCitizenRole() == CitizenRole.MILITIA;
    }

    public boolean shouldMobilizeAsMilitia(RandomSource random) {
        if (random == null) {
            return false;
        }
        return random.nextFloat() < WorkersServerConfig.citizenMilitiaMobilizationChance();
    }
}
