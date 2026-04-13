package com.talhanation.recruits.entities;

import com.talhanation.bannermod.citizen.CitizenCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CitizenRecruitBridgeTest {

    @Test
    void recruitOwnershipAndFollowStateHydrateThroughCitizenWrapperHelper() {
        UUID ownerId = UUID.randomUUID();
        CompoundTag legacy = new CompoundTag();
        legacy.putUUID("OwnerUUID", ownerId);
        legacy.putInt("FollowState", 2);
        legacy.putBoolean("isOwned", true);

        StubCitizenCore citizenCore = new StubCitizenCore();
        AbstractRecruitEntity.hydrateCitizenStateFromLegacy(citizenCore, legacy);

        assertEquals(ownerId, citizenCore.ownerUuid);
        assertEquals(2, citizenCore.followState);
        assertEquals(true, citizenCore.owned);
    }

    @Test
    void recruitPersistenceHelperWritesLegacyKeysWithoutChangingWrapperFacingContract() {
        UUID ownerId = UUID.randomUUID();
        StubCitizenCore citizenCore = new StubCitizenCore();
        citizenCore.ownerUuid = ownerId;
        citizenCore.followState = 1;
        citizenCore.owned = true;

        CompoundTag legacy = AbstractRecruitEntity.persistCitizenStateToLegacy(citizenCore, new CompoundTag());

        assertEquals(ownerId, legacy.getUUID("OwnerUUID"));
        assertEquals(1, legacy.getInt("FollowState"));
        assertEquals(true, legacy.getBoolean("isOwned"));
    }

    @Test
    void recruitWrapperRemainsTheLiveTypeWhileCitizenHelpersStayInternal() throws Exception {
        Method hydrate = AbstractRecruitEntity.class.getMethod("hydrateCitizenStateFromLegacy", CitizenCore.class, CompoundTag.class);
        Method persist = AbstractRecruitEntity.class.getMethod("persistCitizenStateToLegacy", CitizenCore.class, CompoundTag.class);

        assertEquals(AbstractRecruitEntity.class, hydrate.getDeclaringClass());
        assertEquals(AbstractRecruitEntity.class, persist.getDeclaringClass());
    }

    private static final class StubCitizenCore implements CitizenCore {
        private UUID ownerUuid;
        private int followState;
        private boolean owned;
        private final SimpleContainer inventory = new SimpleContainer(9);

        @Override
        public UUID getOwnerUUID() {
            return this.ownerUuid;
        }

        @Override
        public void setOwnerUUID(Optional<UUID> ownerUuid) {
            this.ownerUuid = ownerUuid.orElse(null);
        }

        @Override
        public int getFollowState() {
            return this.followState;
        }

        @Override
        public void setFollowState(int state) {
            this.followState = state;
        }

        @Override
        public SimpleContainer getInventory() {
            return this.inventory;
        }

        @Override
        public String getTeamId() {
            return null;
        }

        @Override
        public Vec3 getHoldPos() {
            return null;
        }

        @Override
        public void setHoldPos(Vec3 holdPos) {
        }

        @Override
        public void clearHoldPos() {
        }

        @Override
        public BlockPos getMovePos() {
            return null;
        }

        @Override
        public void setMovePos(BlockPos movePos) {
        }

        @Override
        public void clearMovePos() {
        }

        @Override
        public boolean isOwned() {
            return this.owned;
        }

        @Override
        public void setOwned(boolean owned) {
            this.owned = owned;
        }

        @Override
        public boolean isWorking() {
            return this.followState == 6;
        }

        @Override
        public UUID getBoundWorkAreaUUID() {
            return null;
        }

        @Override
        public void setBoundWorkAreaUUID(UUID boundWorkAreaUuid) {
        }

        @Override
        public boolean getRuntimeFlag(RuntimeFlag flag) {
            return false;
        }

        @Override
        public void setRuntimeFlag(RuntimeFlag flag, boolean value) {
        }
    }
}
